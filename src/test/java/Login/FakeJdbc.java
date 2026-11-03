package Login;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Base de datos de mentira para las pruebas: NO necesita SQL Server ni Mockito.
 * Se le dice qué filas devuelve cada consulta (por un trozo de su texto SQL) y anota
 * qué INSERT/UPDATE/DELETE se ejecutaron y con qué parámetros.
 */
final class FakeJdbc {

    private static final class Regla {
        final String contiene;
        final Function<List<Object>, List<Object[]>> filas;
        final int afectadas;
        Regla(String contiene, Function<List<Object>, List<Object[]>> filas, int afectadas) {
            this.contiene = contiene; this.filas = filas; this.afectadas = afectadas;
        }
    }

    private final List<Regla> consultas = new ArrayList<>();
    private final List<Regla> cambios = new ArrayList<>();

    /** SQL de cada executeUpdate, en orden. */
    final List<String> actualizaciones = new ArrayList<>();
    /** Parámetros (en orden 1, 2, 3...) de cada executeUpdate. */
    final List<List<Object>> parametros = new ArrayList<>();
    /** Si es true, todo executeUpdate lanza SQLException (simula que falla el guardado). */
    boolean fallaAlActualizar = false;

    boolean autoCommit = true;
    int commits = 0;
    int rollbacks = 0;

    /** Toda consulta cuyo SQL contenga {@code contiene} devuelve estas filas. */
    FakeJdbc consulta(String contiene, Object[]... filas) {
        final List<Object[]> lista = Arrays.asList(filas);
        consultas.add(new Regla(contiene, p -> lista, 0));
        return this;
    }

    /** Igual, pero las filas dependen de los parámetros de la consulta. */
    FakeJdbc consultaSegun(String contiene, Function<List<Object>, List<Object[]>> filas) {
        consultas.add(new Regla(contiene, filas, 0));
        return this;
    }

    /** Todo UPDATE/DELETE/INSERT cuyo SQL contenga {@code contiene} afecta esa cantidad de filas. */
    FakeJdbc cambio(String contiene, int filasAfectadas) {
        cambios.add(new Regla(contiene, null, filasAfectadas));
        return this;
    }

    Connection conexion() {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{Connection.class}, new InvocationHandler() {
            @Override public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
                if (m.getDeclaringClass() == Object.class) return objeto(proxy, m, args);
                switch (m.getName()) {
                    case "prepareStatement": return sentencia((String) args[0]);
                    case "getAutoCommit":    return autoCommit;
                    case "setAutoCommit":    autoCommit = (Boolean) args[0]; return null;
                    case "commit":           commits++; return null;
                    case "rollback":         rollbacks++; return null;
                    case "close":            return null;
                    case "isClosed":         return false;
                    default: throw new UnsupportedOperationException("Connection." + m.getName());
                }
            }
        });
    }

    private PreparedStatement sentencia(final String sql) {
        final Map<Integer, Object> params = new HashMap<>();
        return (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{PreparedStatement.class}, new InvocationHandler() {
            @Override public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
                if (m.getDeclaringClass() == Object.class) return objeto(proxy, m, args);
                String n = m.getName();
                if (n.startsWith("set") && args != null && args.length >= 2 && args[0] instanceof Integer) {
                    params.put((Integer) args[0], args[1]);
                    return null;
                }
                switch (n) {
                    case "executeQuery": {
                        List<Object[]> filas = new ArrayList<>();
                        for (Regla r : consultas) {
                            if (sql.contains(r.contiene)) { filas = r.filas.apply(ordenados(params)); break; }
                        }
                        return resultado(filas);
                    }
                    case "executeUpdate": {
                        if (fallaAlActualizar) throw new SQLException("Fallo simulado de la base de datos");
                        actualizaciones.add(sql);
                        parametros.add(ordenados(params));
                        for (Regla r : cambios) if (sql.contains(r.contiene)) return r.afectadas;
                        return 1;
                    }
                    case "close": case "clearParameters": return null;
                    default: throw new UnsupportedOperationException("PreparedStatement." + n);
                }
            }
        });
    }

    private static List<Object> ordenados(Map<Integer, Object> params) {
        List<Object> l = new ArrayList<>();
        for (int i = 1; params.containsKey(i); i++) l.add(params.get(i));
        return l;
    }

    private static ResultSet resultado(final List<Object[]> filas) {
        final int[] pos = {-1};
        final Object[] ultimo = {null};
        return (ResultSet) Proxy.newProxyInstance(FakeJdbc.class.getClassLoader(),
                new Class<?>[]{ResultSet.class}, new InvocationHandler() {
            private Object celda(Object[] args) throws SQLException {
                if (!(args[0] instanceof Integer))
                    throw new UnsupportedOperationException("Las pruebas leen las columnas por número");
                Object v = filas.get(pos[0])[(Integer) args[0] - 1];
                ultimo[0] = v;
                return v;
            }
            @Override public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
                if (m.getDeclaringClass() == Object.class) return objeto(proxy, m, args);
                switch (m.getName()) {
                    case "next":      pos[0]++; return pos[0] < filas.size();
                    case "close":     return null;
                    case "wasNull":   return ultimo[0] == null;
                    case "getInt":    { Object v = celda(args); return v == null ? 0 : ((Number) v).intValue(); }
                    case "getLong":   { Object v = celda(args); return v == null ? 0L : ((Number) v).longValue(); }
                    case "getDouble": { Object v = celda(args); return v == null ? 0.0 : ((Number) v).doubleValue(); }
                    case "getBoolean":{ Object v = celda(args); return v != null && (v instanceof Boolean ? (Boolean) v : ((Number) v).intValue() != 0); }
                    case "getString": { Object v = celda(args); return v == null ? null : String.valueOf(v); }
                    case "getObject": return celda(args);
                    default: throw new UnsupportedOperationException("ResultSet." + m.getName());
                }
            }
        });
    }

    private static Object objeto(Object proxy, Method m, Object[] args) {
        switch (m.getName()) {
            case "toString": return "FakeJdbc";
            case "hashCode": return System.identityHashCode(proxy);
            case "equals":   return proxy == args[0];
            default:         return null;
        }
    }
}
