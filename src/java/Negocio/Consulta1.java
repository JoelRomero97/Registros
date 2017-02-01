package Negocio;


import com.mysql.jdbc.PreparedStatement;
import java.sql.SQLException;
import java.text.*;
import java.util.*;

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
    
    
    
    public int Registrarse (String usuario, String correo, String contrasena, String contrasena1) throws SQLException        /*REGISTRA AL USUARIO*/
    {	
        Conectar();
        int resp = 0;
        String str = "INSERT INTO usuario (username,email, pass) VALUES (?,?,?);";
        System.out.println(usuario+" -- "+correo+" -- "+contrasena+" -- "+contrasena1);
        try
        {
            if(!ExisteUsuario(usuario))
            {
                resp = 1;
                sqlP = (PreparedStatement) con.prepareStatement(str);
                sqlP.setString(1, usuario);
                sqlP.setString(2, correo);
                sqlP.setString(3, contrasena);
                sqlP.executeUpdate();
                System.out.println("Usuario "+usuario+" registrado.");
            }else
            {
                resp = 2;
                System.out.println("El usuario "+usuario+" ya existe.");
            }
        }catch(SQLException ex)
        {
            Desconectar();
            System.out.println("Error al intentar registrar al usuario "+ex.getMessage());
        }
        return resp;
    }
    
    
    
    public int Login (String correo, String contrasena) throws SQLException, ParseException             /*LOGUEA O NO AL USUARIO*/
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
                            System.out.println("---------------------------------------------");
                            System.out.println("Inicio de sesión correcto");
                            resp = 4;
                        }else //EL USUARIO ESTA BLOQUEADO
                        {
                            if(YaPasoElTiempo(correo))
                            {
                                DesbloquearUsuario(correo);
                                BorrarIntentos(correo);
                                resp = 4;
                                Login(correo,contrasena);
                            }else
                            {
                                System.out.println("El usuario está bloqueado.");
                                resp = 3;
                            }
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
            System.out.println("El usuario "+user+" tiene "+resp+" intentos falidos.");
        }catch(SQLException ex)
        {            
            Desconectar();
            System.out.println("Error al intentar obtener los intentos de logueo fallidos "+ex.getMessage());
        }
        return resp;
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
    
    
    
    public boolean UsuarioBloqueado (String user) throws SQLException       /*NOS DICE SI EL USUARIO ESTÁ O NO BLOQUEADO*/
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
                System.out.println("El usuario "+user+" está bloqueado.");
            }else
            {
                System.out.println("El usuario "+user+" no está bloqueado.");
            }
        }catch(SQLException ex)
        {
            Desconectar();
            System.out.println("Error al intentar verificar si el usuario está o no bloqueado "+ex.getMessage());
        }
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
            System.out.println("El usuario "+user+" ha sido bloqueado por pendejo.");
        }catch(SQLException ex)
        {
            Desconectar();
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
        System.out.println("El usuario "+user+" ha sido desbloqueado.");
        }catch(SQLException ex)
        {
            Desconectar();
            System.out.println("Error al intentar bloquear al usuario "+ex.getMessage());
        }
        return "Usuario Desbloqueado: OK";
    }
    
    
    
    public boolean ContraseñaCorrecta (String user, String password) throws SQLException        /*INDICA SI ES CORRECTA O NO LA CONTRASEÑA*/
    {
        boolean resp = true;
        String str = "SELECT pass FROM usuario WHERE id_user=?";
        String pwd;
        int usuario = ObtenerId(user);
        Conectar();
        try
        {
            sqlP = (PreparedStatement) con.prepareStatement(str);
            sqlP.setInt(1, usuario);
            rs = sqlP.executeQuery();
            while(rs.next())
            {
                pwd = rs.getString("pass");
                if(password.equals(pwd))
                {
                    System.out.println("La contraseña del usuario "+user+" es correcta.");
                }else
                {
                    resp = false;
                    System.out.println("La contraseña del usuario "+user+" es incorrecta.");
                }
            }
        }catch(SQLException ex)
        {
            Desconectar();
            System.out.println("Error al intentar verificar si la contraseña del usuario "+user+" es correcta. "+ex.getMessage());
        } 
        return resp;
    }
            
    
    
    public boolean ExisteUsuario (String user) throws SQLException          /*NOS DICE SI EL USUARIO ESTA REGISTRADO*/
    {
        Conectar();
        boolean resp = true;
        int usuario = ObtenerId(user);
        String str = "SELECT * FROM usuario WHERE id_user=?";
        try
        {
            sqlP = (PreparedStatement) con.prepareStatement(str);
            sqlP.setInt(1,usuario);
            rs = sqlP.executeQuery();
            if(rs.next())
            {
                System.out.println("El usuario "+user+" existe en la base de datos.");
            }else
            {
                resp = false;
                System.out.println("El usuario "+user+" no existe en la base de datos.");
            }
        }catch(SQLException ex)
        {
            Desconectar();
            System.out.println("Error al intentar verificar si existe el usuario "+user+": "+ex.getMessage());
        }
        return resp;
    }
    
    
    
    public boolean ExisteUsuarioEnRegistro (String user) throws SQLException        /*NOS DICE SI EL USUARIO YA FALLO EN EL LOGUEO*/
    {
        boolean resp = true;
        Conectar();
        int usuario = ObtenerId(user);
        String str = "SELECT * FROM registro WHERE id_user=?";
        try
        {
            sqlP = (PreparedStatement) con.prepareStatement(str);
            sqlP.setInt(1, usuario);
            rs = sqlP.executeQuery();
            if(rs.next())
            {
                System.out.println("El usuario "+user+" ya tuvo al menos 1 intento fallido de Login.");
            }else
            {
                resp = false;
                System.out.println("El usuario "+user+" no ha tenido ningún intento de Login.");
            }
        }catch(SQLException ex)
        {
            Desconectar();
            System.out.println("Error al intentar verificar si el usuario "+user+" ya falló en algún intento de Login." +ex.getMessage());
        }
        return resp;
    }
    
    
    
    public boolean YaPasoElTiempo (String user) throws SQLException, ParseException             /*NOS DICE SI YA PASARON O NO 20 MINUTOS PARA DESBLOQUEAR*/
    {
        boolean resp = true;
        Calendar c1 = Calendar.getInstance();
        int añoActual, mesActual, diaActual, horaActual,minutoActual;
        String horaActual2 = null;
        añoActual = c1.get(Calendar.YEAR);
        mesActual = c1.get(Calendar.MONTH);
        diaActual = c1.get(Calendar.DAY_OF_MONTH);
        horaActual =c1.get(Calendar.HOUR_OF_DAY);
        minutoActual = c1.get(Calendar.MINUTE);
        if(minutoActual>=1)
        {
            horaActual+=1;
        }
            if(mesActual<9&&diaActual<10)
            {
                horaActual2 = añoActual+"-0"+(mesActual+1)+"-0"+diaActual+" "+horaActual;
            }else if(mesActual>=9&&diaActual<10)
            {
                horaActual2 = añoActual+"-"+(mesActual+1)+"-0"+diaActual+" "+horaActual;
            }else if(mesActual<9&&diaActual>10)
            {
                horaActual2 = añoActual+"-0"+(mesActual+1)+"-"+diaActual+" "+horaActual;
            }else
            {
                horaActual2 = añoActual+"-"+(mesActual+1)+"-"+diaActual+" "+horaActual;
            }
        Date horaIntento = new Date();
        int usuario = ObtenerId(user);
        System.out.println("Hora actual: "+horaActual2);
        Conectar();
        String str = "SELECT hora FROM registro WHERE id_user=?";
        try
        {
            sqlP = (PreparedStatement) con.prepareStatement(str);
            sqlP.setInt(1, usuario);
            rs = sqlP.executeQuery();
            while(rs.next())
            {
                horaIntento = rs.getTimestamp("hora");
                System.out.println("Hora del intento fallido: "+horaIntento);
                String horaFallida = horaIntento.toString();
                int tiempoTranscurrido = horaActual2.compareTo(horaFallida);    //horaHoy - horaIntento 
                if(tiempoTranscurrido>0)
                {
                    System.out.println("Ya pasó 1 hora o más.");
                }else
                {
                    System.out.println("No ha pasado 1 hora aun.");
                    resp = false;
                }
            }  
        }catch(SQLException ex)
        {
            Desconectar();
            System.out.println("Error al intentar contar los minutos "+ex.getMessage());
        }
        return resp;
    }
    
    
    
    public static void main (String[] args) throws SQLException, ParseException
    {   
        Consulta1 test = new Consulta1("root","root");
        test.YaPasoElTiempo("joelrg1288@gmail.com");
        //test.Tiempo("abel.mejia.hdz@gmail.com");
    }
}