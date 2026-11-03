/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa.logica;

import capa.datos.ConexionSQL;
import java.sql.Connection;

/**
 *
 * @author JLPC
 */
public class Conectar {
    public static String ProbarConexion() throws ClassNotFoundException{
        String Mensaje="";
        try
        {
        Connection conexion=null;
         conexion=ConexionSQL.getConexionSqlServer();
         if (conexion!=null)
             Mensaje="Conexion Exitosa!!";
         else
            Mensaje="La conexion no fue exitosa";
             
         return Mensaje;
        }catch(Exception ex)
        {
           Mensaje= ex.getMessage();
        }
       
            return Mensaje;
        
    }
}
