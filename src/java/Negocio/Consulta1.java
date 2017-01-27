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
    
    
    
    public int Login (String correo, String contrasena) throws SQLException             /*LOGUEA O NO AL USUARIO*/
    {
        Conectar();
        int resp = 5;
        String str = "SELECT pass,status FROM usuario WHERE email=? OR username=?";
        System.out.println(correo);
        System.out.println(contrasena);
        try 
        {
            if(ExisteUsuario(correo)&&ObtenerId(correo)!=0)
            {
                sqlP = (PreparedStatement) con.prepareStatement(str);
                sqlP.setString(1, correo);
                sqlP.setString(2, correo);
                rs = sqlP.executeQuery();
                while(rs.next())
                {
                    if(ContraseñaCorrecta(correo,contrasena))
                    {
                        if(!UsuarioBloqueado(correo))
                        {
                            BorrarIntentos(correo);
                            System.out.println("-----------------------");
                            System.out.println("Inicio de sesión correcto");
                            resp = 4;
                        }else //EL USUARIO ESTA BLOQUEADO
                        {
                            System.out.println("El usuario no tiene permisos para acceder o está bloqueado. ");
                            resp = 3;
                        }
                    }else   //LA CONTRASEÑA ES INCORRECTA
                    {
                        if(NumeroIntentos(correo)>=3&&ExisteUsuarioEnRegistro(correo))
                        {
                            BloquearUsuario(correo);
                            resp = 3;
                        }else if(NumeroIntentos(correo)>=0&&ExisteUsuarioEnRegistro(correo))
                        {
                            SumarIntento(correo);
                            resp = 2;
                        }else if(!ExisteUsuarioEnRegistro(correo))
                        {
                            PrimerIntento(correo);
                            resp = 2;
                        }
                        System.out.println("La contraseña no coincide con el usuario: "+correo); 
                    }
                }//cierra while
            }else if(!ExisteUsuario(correo)||ObtenerId(correo)==0)
            {
                System.out.println("El usuario no existe, registrarse primero. ");
                resp = 1;
            }
            Desconectar();
        }catch (SQLException ex) 
        {
            resp=5;
            Desconectar();
            System.out.println("Error al intentar iniciar sesión: "+ ex.getMessage());
        }
        return resp;
    }



    public int ObtenerId (String user) throws SQLException      /*OBTENEMOS EL ID PARA INGRESARLO A REGISTRO*/
    {
        Conectar();
        int resp = 0;
        String str = "SELECT id_user FROM usuario WHERE username=? OR email=?";
        try
        {
            sqlP = (PreparedStatement) con.prepareStatement(str);
            sqlP.setString(1,user);
            sqlP.setString(2,user);
            rs = sqlP.executeQuery();
            while(rs.next())
            {
                resp = rs.getInt("id_user");
            }
            System.out.println("El ID del usuario es: "+resp);
        }catch(SQLException e)
        {
            Desconectar();
            System.out.println("Error al intentar obtener ID "+e.getMessage());
        }
        if (resp==0)
        {
            System.out.println("El usuario "+user+" no existe.");
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
        System.out.println("Usuario "+user+" insertado en registro. ");
        }catch(SQLException ex)
        {
            System.out.println("Error al intentar ingresar a registro "+ex.getMessage());
        }
        return "Insertar en Registro: OK";
    }



    public String SumarIntento (String user) throws SQLException    /*SE SUMA UN INTENTO FALLIDO DE LOGUEO*/
    {
        int intentos = NumeroIntentos(user);
        int usuario = ObtenerId(user);
        Conectar();
        String str = "UPDATE registro SET intentos=? WHERE id_user=?";
        try
        {
            sqlP = (PreparedStatement) con.prepareStatement(str);
            sqlP.setInt(1,intentos+1);
            sqlP.setInt(2,usuario);
            sqlP.executeUpdate();
            System.out.println("Intento Sumado al usuario "+user);
        }catch(SQLException ex)
        {
            Desconectar();
            System.out.println("Error al intentar sumar el intento fallido de Logueo "+ex.getMessage());
        }
        return "Intento sumado: OK";
    }
    
    
    
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



    public boolean UsuarioBloqueado (String user) throws SQLException
	{
	    boolean resp = false;
	    int usuario = ObtenerId(user);
	    int status = 0;
	    Conectar();
	    String str = "SELECT status FROM usuario WHERE id_user=?";
	    try
	    {
	        sqlP = (PreparedStatement) con.prepareStatement(str);
	        sqlP.setInt(1, usuario);
	        rs = sqlP.executeQuery();
	        while(rs.next())
	        {
	            status = rs.getInt("status");
	        }
	        if(status==0)
	        {
	            resp=true;
	        }
	        Desconectar();
	    }catch(SQLException ex)
	    {
	        Desconectar();
	        System.out.println("Error al intentar verificar si el usuario está o no bloqueado "+ex.getMessage());
	    }
	    System.out.println(resp);
	    System.out.println(status);
	    return resp;
	}



	 public String BloquearUsuario (String user) throws SQLException     /*BLOQUEA AL USUARIO*/
    {
        int usuario = ObtenerId(user);
        Conectar();
        int status  = 0;
        String str = "UPDATE usuario SET status=? WHERE id_user=?";
        try
        {
        sqlP = (PreparedStatement) con.prepareStatement(str);
        sqlP.setInt(1,status);
        sqlP.setInt(2,usuario);
        sqlP.executeUpdate();
        }catch(SQLException ex)
        {
            System.out.println("Error al intentar bloquear al usuario "+ex.getMessage());
        }
        return "Usuario Bloqueado: OK";
    }
    
    
    
    public String DesbloquearUsuario (String user) throws SQLException      /*DESBLOQUEA AL USUARIO*/
    {
        int status = 1;
        int usuario = ObtenerId(user);
        Conectar();
        String str = "UPDATE usuario SET status=? WHERE id_user=?";
        try
        {
        sqlP = (PreparedStatement) con.prepareStatement(str);
        sqlP.setInt(1,status);
        sqlP.setInt(2,usuario);
        sqlP.executeUpdate();
        }catch(SQLException ex)
        {
            System.out.println("Error al intentar bloquear al usuario "+ex.getMessage());
        }
        return "Usuario Desbloqueado: OK";
    }



    public String BorrarIntentos (String user) throws SQLException            /*SE PONE A 0 EL NUMERO DE INTENTOS FALLIDOS*/
    {
        int num = 0;
        int usuario = ObtenerId(user);
        Conectar();
        String str = "UPDATE registro SET intentos=? WHERE  id_user=?";
        try
        {
        sqlP = (PreparedStatement) con.prepareStatement(str);
        sqlP.setInt(1,num);
        sqlP.setInt(2, usuario);
        sqlP.executeUpdate();
        System.out.println("El usuario "+user+" ya no tiene intentos fallidos.");
        }catch(SQLException ex)
        {
            System.out.println("Error al intentar bloquear al usuario "+ex.getMessage());
        }
        return "Se pusieron en cero los intentos fallidos: OK";
    }



    public boolean Tiempo (String user) throws SQLException
	{
	    long minutosDia = 1440;         /*MINUTOS EN UN DIA*/
	    long minutosMes = 43200;        /*MINUTOS EN UN MES*/
	    long minutosAnio = 518400;       /*MINUTOS EN UN AÑO*/
	    boolean resp = true;
	    Date base = new Date();
	    int usuario = ObtenerId(user);
	    Conectar();
	    String str = "SELECT hora FROM registro WHERE id_user=?";
	    try
	    {
	        sqlP = (PreparedStatement) con.prepareStatement(str);
	        sqlP.setInt(1, usuario);
	        rs = sqlP.executeQuery();
	        while(rs.next())
	        {
	            base = rs.getTime("hora");
	            System.out.println(base); 
	        }
	        Desconectar();
	    }catch(SQLException ex)
	    {
	        Desconectar();
	        System.out.println("Error al intentar contar los minutos "+ex.getMessage());
	    }
	    Calendar cal = Calendar.getInstance();
	    
	    /*Date fecha = new Date();
	    long dia = fecha.getTime();
	    System.out.println(fecha);*/
	    
	    
	    return resp;
	}



    public static void main(String[] args) throws SQLException
    {
        Consulta1 test = new Consulta1("root","root");
        test.Tiempo("abel.mejia.hdz@gmail.com");
    }
}
