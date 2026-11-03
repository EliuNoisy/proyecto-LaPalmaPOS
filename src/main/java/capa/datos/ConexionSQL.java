package capa.datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionSQL {
    public static Connection getConexionSqlServer() {
        String conexionURL = "jdbc:sqlserver://JORGEZAZUETA\\SQLEXPRESS:1433;"
                           + "databaseName=RefresqueríaLaPalma;"
                           + "user=sa;"
                           + "password=TU_PASSWORD;"
                           + "encrypt=false;"  
                           + "trustServerCertificate=true;"
                           + "loginTimeout=30;";

        try {
            // Cargar el driver solo si es necesario
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            Connection conexionSQL = DriverManager.getConnection(conexionURL);
            System.out.println("Conexion exitosa.");
            return conexionSQL;
        } catch (ClassNotFoundException e) {
            System.out.println("Error: No se encontró el driver JDBC. Asegúrate de agregar el .jar al classpath.");
        } catch (SQLException ex) {
            System.out.println("Error de conexión: " + ex.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        Connection con = getConexionSqlServer();
        if (con != null) {
            System.out.println("Conexion establecida correctamente.");
        } else {
            System.out.println("Fallo en la conexión.");
        }
    }

    public static Connection conectar() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
