package org.energy_home.jemma.javagal.gui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.energy_home.jemma.zgd.GatewayException;
import org.energy_home.jemma.zgd.GatewayInterface;



public class WebApplication extends HttpServlet {
	
	GatewayInterface interfaceofGal;
	
	public WebApplication(GatewayInterface _interface)
	{
		interfaceofGal = _interface;
		
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		/*
		if (request.getContextPath().equals(""))
		{
			
			response.setContentType("text/html");
			FileReader fstream = new FileReader("out.txt"); 
			BufferedReader br = new BufferedReader(fstream); 
			String s; 
			PrintWriter out = response.getWriter();
			while((s = br.readLine()) != null) { 
				  out.println(s); 
				}  
			
		
		}*/
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("</head>");
		out.println("<body>");
		out.println("<h1>GAL Web Gui</h1>");
		
		try {
			out.println("<h2>Network channel:"+ interfaceofGal.getChannelSync(5000)  +"</h2>");
		} catch (GatewayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		out.println("</body>");
		
		
		out.println("</html>");
		
	

		
	
	}

}
