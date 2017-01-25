package com.buromc.autenticacion.servlets.implemets;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import Negocio.Consulta1;
import java.io.*;
import java.sql.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.security.*;
import java.util.logging.*;

/**
 *
 * @author jromero
 */
public class Servlet2 extends HttpServlet 
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
        PrintWriter out = res.getWriter();
        out.println("Inicia Servlet");
        res.setContentType("text/html;charset=utf-8");
        req.setCharacterEncoding("utf-8");
        String user = req.getParameter("nombre");
        String email = req.getParameter("correo");
        String password = req.getParameter("contrasena");
        String password1 = req.getParameter("contrasena1");
        out.println(" -- ");
        Pattern pat1 = Pattern.compile("[a-zA-z0-9-_.]{3,}@[a-zA-Z0-9]{2,}[.][a-zA-Z]{2,}");
        Matcher mat1 = pat1. matcher(email);
        MessageDigest md = null;
        try 
        {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) 
        {
            Logger.getLogger(Servlet2.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(mat1.find())
        {
            if (password.equals(password1)) 
            {
                if(password.length()>7)
                {
                    Pattern pat = Pattern.compile("[a-zA-Z0-9]");
                    Matcher mat = pat.matcher(password);
                    if(mat.find())
                    {
                        md.update(password.getBytes());
                        byte byteData[] = md.digest();
                        StringBuilder hexString = new StringBuilder();
                        for (int i=0;i<byteData.length;i++)
                        {
                            String hex = Integer.toHexString(0xff & byteData[i]);
                            if(hex.length() == 1) hexString.append('0');
                            hexString.append(hex);
                        }
                        try
                        {
                            Consulta1 registro = new Consulta1("root","root");
                            registro.Registrarse(user,email,hexString.toString(),hexString.toString());
                            out.println("Usuario Registrado");
                        }catch(SQLException ex) 
                        {
                            out.println(ex);
                        }
                    }else
                    {   
                        out.println("Error al intentar registrar al usuario: La contraseña debe contener 1 mayuscula, 1 minuscula y 1 número. ");
                    }     
                }else
                {   
                    out.println("Error al intentar registrar al usuario: La contraseña debe tener al menos 8 caracteres. ");
                }
            }else 
            {
                out.println("Error al intentar registrar al usuario: Las contraseñas no coinciden.");
            }
        }else
        {
            out.println("Error al intentar registrar al usuario: Introduzca un correo válido. ");
        }
    }
}