package Negocio;


import com.mysql.jdbc.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
    
    public Consulta1 (String usr, String pwd) throws SQLException       /*SE SUPERPONE EL MÉTODO DE CONEXION*/
    {
        super(usr, pwd);
    }
    
    
    
    public String Registrarse (String usuario, String correo, String contrasena, String contrasena1) throws SQLException        /*REGISTRA AL USUARIO*/
    {	
        Conectar();
        String str = "INSERT INTO usuario (username,email, pass) VALUES (?,?,?);";
        System.out.println(usuario+" -- "+correo+" -- "+contrasena+" -- "+contrasena1);
        try
        {
        sqlP = (PreparedStatement) con.prepareStatement(str);
        sqlP.setString(1, usuario);
        sqlP.setString(2, correo);
        sqlP.setString(3, contrasena);
        sqlP.executeUpdate();
        }catch(SQLException ex)
        {
            System.out.println("Error al intentar registrar al usuario "+ex.getMessage());
        }
        return "User register: OK";
    }
    
    
    
    public boolean Login (String correo, String contrasena) throws SQLException     /*LOGUEA O NO AL USUARIO*/
    {
        Conectar();
        Boolean resp = true;
        String str1 = "SELECT pass,status FROM usuario WHERE email=? OR username=?";
        String status = "1";
        System.out.println("-----------------------");
        System.out.println(correo);
        System.out.println(contrasena);
        try 
        {
            sqlP = (PreparedStatement) con.prepareStatement(str1);
            sqlP.setString(1, correo);
            sqlP.setString(2, correo);
            rs = sqlP.executeQuery();
            if(rs.next())
            {
                rs = sqlP.executeQuery();
                while(rs.next())
                {
                    if((rs.getString("pass")).equals(contrasena))
                    {
                        if((rs.getString("status").equals(status)))
                        {
                            /*********AQUI SE LE PONE EN 0 LOS INTENTOS DE INICIO DE SESIÓN*******/
                            
                            
                            
                            
                            System.out.println("-----------------------");
                            System.out.println("Inicio de sesión correcto");
                        }else
                        {
                            System.out.println("El usuario no tiene permisos para acceder. ");
                            resp = false;
                        }
                    }else
                    {
                        /*********AQUI VA LA PARTE DE SUMARLE UN INTENTO*******/
                        
                        
                        
                        
                        
                        /********AQUI SE VERIFICARÁ SI YA TIENE 3 O MÁS******/
                        if((NumeroIntentos(correo))>3)
                        {
                            
                        }
                        
                        
                        
                        
                        
                        
                        
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
    
    
    
    public int NumeroIntentos (String user) throws SQLException     /*OBTIENE EL NÚMERO DE INTENTOS FALLIDOS*/
    {
        int resp = 0;
        int usuario = ObtenerId(user);
        Conectar();
        String str = "SELECT intentos FROM registro WHERE id_user=?";
        try
        {
            sqlP = (PreparedStatement) con.prepareStatement(str);
            sqlP.setInt(1,usuario);
            rs = sqlP.executeQuery();
            while(rs.next())
            {
                resp = rs.getInt("intentos");
            }
            Desconectar();
        }catch(SQLException ex)
        {            
            Desconectar();
            System.out.println("Error al intentar obtener los intentos de logueo fallidos "+ex.getMessage());
        }
        return resp;
    }



    public String PrimerIntento (String user) throws SQLException       /*REGISTRA AL USUARIO SI ES SU PRIMER INTENTO FALLIDO*/
    {
        int num = 1;
        int usuario = ObtenerId(user);
        Conectar();
        String str = "INSERT INTO registro (intentos,id_user) VALUES (?,?);";
        try
        {
        sqlP = (PreparedStatement) con.prepareStatement(str);
        sqlP.setInt(1,num);
        sqlP.setInt(2,usuario);
        sqlP.executeUpdate();
            System.out.println("Usuario insertado en registro. ");
        }catch(SQLException ex)
        {
            System.out.println("Error al intentar ingresar a registro "+ex.getMessage());
        }
        return "Insertar en Registro: OK";
    }



	    



    public static void main(String[] args) throws SQLException
    {
       // Consulta1 registro = new Consulta1("root","root");
        Consulta1 log = new Consulta1("root","root");
        // System.out.println(registro.Registrarse("Joel Romero","joelrg1288@gmail.com","12345","12345")); /*Usuario,correo,contraseña,contraseña1*/	
       // System.out.println(log.Login("y_tam_hhw@hotmail.com","Contraseña17")); /*Correo, contraseña*/
    }
}
