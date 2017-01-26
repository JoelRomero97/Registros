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
        PrintWriter out = res.getWriter();
        out.println("Inicia Servlet");
        res.setContentType("text/html;charset=utf-8");
        req.setCharacterEncoding("utf-8");
        String email = req.getParameter("correo");
        String password = req.getParameter("contrasena");
        out.println(" ----------------------- ");
        Pattern pat = Pattern.compile("[a-zA-Z0-9]{8,}");
        Matcher mat = pat.matcher(password);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
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
                }
                try
                {
                    Consulta1 log = new Consulta1("root","root");
                    if(log.Login(email,hexString.toString()))    /************ENVIAMOS LA CONTRASEÑA EN HASH************/ 
                    {   
                        out.println("Inicio de Sesión Correcto. ");
                    }else 
                    {
                        out.println("El usuario no existe, la contraseña es incorrecta o no tiene permiso para ingresar. ");
                    }
                }catch(SQLException ex)
                {
                    out.println("Contraseña Incorrecta. ");
                    out.println(ex);
                }
            }else
            {   
                out.println("Error al intentar iniciar sesión: Introduzca una contraseña válida. ");
            }
    }
}