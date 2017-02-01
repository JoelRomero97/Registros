package com.buromc.autenticacion.servlets.implemets;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import Negocio.Consulta1;
import java.io.*;
import java.security.*;
import java.sql.*;
import java.text.ParseException;
import java.util.logging.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author jromero
 */
public class Servlet extends HttpServlet 
{

    /**
     *
     * @param req
     * @param res
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException 
    {
        
        String msgBlock = "El usuario está bloqueado.";
        PrintWriter out = res.getWriter();
        res.setContentType("text/html;charset=utf-8");
        req.setCharacterEncoding("utf-8");
        String email = req.getParameter("correo");
        String password = req.getParameter("contrasena");
        Pattern pat = Pattern.compile("[a-zA-Z0-9]{8,}");
        Matcher mat = pat.matcher(password);
        MessageDigest md = null;
        String contrasena = null;
        try 
        {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) 
        {
            Logger.getLogger(Servlet.class.getName()).log(Level.SEVERE, null, ex);
        }
            if(mat.find())   /*LA CONTRASEÑA ES VÁLIDA*/
            {
                md.update(password.getBytes());
                byte byteData[] = md.digest();
                StringBuilder hexString = new StringBuilder();
                for (int i=0;i<byteData.length;i++)
                {
                    String hex = Integer.toHexString(0xff & byteData[i]);
                    if(hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                    contrasena = hexString.toString();
                }
                try
                {
                    Consulta1 log = new Consulta1("root","root");
                    switch (log.Login(email,contrasena))            /************ENVIAMOS LA CONTRASEÑA EN HASH************/
                    {
                        case 4:
                            out.println("Inicio de Sesión Correcto.");
                            req.getRequestDispatcher("index.html").forward(req, res);
                            break;
                        case 3:
                           out.println(msgBlock);
                            break;
                        case 2:
                            out.println("La contraseña es incorrecta.");
                            break;
                        case 1:
                            out.println("El usuario ingresado no existe, regístrese primero.");
                            break;
                        default:
                            out.println("Error dentro de la consulta.");
                            break;
                    }
                }catch(SQLException ex)
                {
                    out.println("Error al intentar iniciar sesión: "+ex.getMessage());
                } catch (ParseException ex) 
                {
                    Logger.getLogger(Servlet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else
            {   
                out.println("Error al intentar iniciar sesión: Introduzca una contraseña válida. ");
            }
    }
}