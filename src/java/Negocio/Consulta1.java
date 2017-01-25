package Negocio;


import com.mysql.jdbc.PreparedStatement;
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
public class Consulta1 extends Conexion
{
    public Consulta1(String usr, String pwd) throws SQLException 
    {
        super(usr, pwd);
    }
    
    public String Registrarse (String usuario, String correo, String contrasena, String contrasena1) throws SQLException
    {	
        Conectar();
        String str = "INSERT INTO usuario (username,email, pass) VALUES (?,?,?);";
        System.out.println(usuario+" -- "+correo+" -- "+contrasena+" -- "+contrasena1);
        sqlP = (PreparedStatement) con.prepareStatement(str);
        sqlP.setString(1, usuario);
        sqlP.setString(2, correo);
        sqlP.setString(3, contrasena);
        sqlP.executeUpdate();
        return "User register: OK";
    }
    
    public boolean Login(String correo, String contrasena) throws SQLException
    {
        Conectar();
        Boolean resp = true;
        String str1 = "SELECT pass,status FROM usuario WHERE email=? OR username=?";
        int uno = 1;
        String status = "";
        status = String.valueOf(uno);
        status= Integer.toString(uno);
        System.out.println("-----------------------");
        System.out.println(correo);
        System.out.println(contrasena);
        try 
        {
            sqlP2 = (PreparedStatement) con.prepareStatement(str1);
            sqlP2.setString(1, correo);
            sqlP2.setString(2, correo);
            rs = sqlP2.executeQuery();
            if(rs.next())
            {
                rs = sqlP2.executeQuery();
                while(rs.next())
                {
                    if((rs.getString("pass")).equals(contrasena))
                    {
                        if((rs.getString("status").equals(status)))
                        {
                            System.out.println("-----------------------");
                            System.out.println("Inicio de sesión correcto");
                        }else
                        {
                            System.out.println("El usuario no tiene permisos para acceder. ");
                            resp = false;
                        }
                    }else
                    {
                        System.out.println("El usuario y/o contraseña no coinciden. ");
                        resp = false;
                    }
                }//cierra while
            }else
            {
                System.out.println("El usuario no existe, registrarse primero. ");
                resp = false;
            }
            Desconectar();
        }catch (SQLException ex) 
        {
            resp=false;
            Desconectar();
            System.out.println("Error al intentar iniciar sesión: "+ ex.getMessage());
        }//cierra catch
        return resp;
    }//cierra login
    
    public static void main(String[] args) throws SQLException
    {
       // Consulta1 registro = new Consulta1("root","root");
        Consulta1 log = new Consulta1("root","root");
        // System.out.println(registro.Registrarse("Joel Romero","joelrg1288@gmail.com","12345","12345")); /*Usuario,correo,contraseña,contraseña1*/	
       // System.out.println(log.Login("y_tam_hhw@hotmail.com","Contraseña17")); /*Correo, contraseña*/
    }
}
