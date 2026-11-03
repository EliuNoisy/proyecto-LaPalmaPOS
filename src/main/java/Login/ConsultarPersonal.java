package Login;

import capa.datos.ConexionSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Validación de acceso. La persona puede escribir su USUARIO o su CORREO (cualquiera de los dos)
 * junto con la contraseña. El correo no distingue mayúsculas de minúsculas.
 */
public class ConsultarPersonal {

    public static boolean validarUsuarioAdmin(String usuarioOCorreo, String contraseña)
            throws ClassNotFoundException, SQLException {
        return validar(usuarioOCorreo, contraseña, "Administrador");
    }

    public static boolean validarUsuarioCajero(String usuarioOCorreo, String contraseña)
            throws ClassNotFoundException, SQLException {
        return validar(usuarioOCorreo, contraseña, "Cajero");
    }

    private static boolean validar(String usuarioOCorreo, String contraseña, String puesto)
            throws ClassNotFoundException, SQLException {
        String consulta = "SELECT 1 FROM Empleados "
                + "WHERE (Usuario = ? OR LOWER(Correo) = LOWER(?)) AND Contraseña = ? AND Puesto = ?";
        // try-with-resources: la conexión se cierra siempre (antes quedaba abierta en cada intento de login)
        try (Connection conexion = ConexionSQL.getConexionSqlServer()) {
            if (conexion == null) throw new SQLException("No hay conexión con la base de datos.");
            try (PreparedStatement sentencia = conexion.prepareStatement(consulta)) {
                sentencia.setString(1, usuarioOCorreo);
                sentencia.setString(2, usuarioOCorreo);
                sentencia.setString(3, contraseña);
                sentencia.setString(4, puesto);
                try (ResultSet resultado = sentencia.executeQuery()) {
                    return resultado.next();
                }
            }
        }
    }
}
