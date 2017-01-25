package Negocio;


import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jromero
 */
public class Conexion {
    public static String strUsuario = "";
	public static String strPassword = "";
	public static Statement sql=null;
	public static PreparedStatement sqlP=null;
        public static PreparedStatement sqlP2=null;
	public static ResultSet rs=null;
	public static Connection con = null;   
	public static String HOST = "localhost";
	public static String PORT = "3306";
	public static String DATABASE = "usuarios";
	public static String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE;

	public Conexion(String usr, String pwd) throws SQLException
	{
		strUsuario = usr;
		strPassword = pwd;
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException e)
		{
			System.out.println("ERROR: Error al cargar la clase del Driver");
		}
	}

	public void Conectar() throws SQLException
	{
		con = (Connection) DriverManager.getConnection(URL, strUsuario, strPassword);
		if(con!=null)
		{
			sql = (Statement) con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		}
	}
	public void Desconectar() throws SQLException
	{
		try
		{
			sql.close();
			rs.close();
			con.close();
		}catch(SQLException ex){
                    System.out.println("error al desconectar"+ex);
                }
	}
}