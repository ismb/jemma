/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2013 Telecom Italia (http://www.telecomitalia.it)
 *
 * JEMMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) version 3
 * or later as published by the Free Software Foundation, which accompanies
 * this distribution and is available at http://www.gnu.org/licenses/lgpl.html
 *
 * JEMMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License (LGPL) for more details.
 *
 */
package org.energy_home.jemma.hac.adapter.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.energy_home.jemma.ah.hac.IAppliance;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceNoParam extends HttpServlet {

	private final static Logger LOG=LoggerFactory.getLogger(ServiceNoParam.class.getName());
	private static final long serialVersionUID = 1L;

	private HttpServletBinder httpAdapter = null;
	int offset;
	private boolean log = false;
	
	public Properties props;

	public ServiceNoParam(HttpServletBinder httpAdapter, String prefix, Properties props) {
		this.httpAdapter = httpAdapter;
		offset = prefix.length();
		
		this.props = props;
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String content = streamToString(req.getInputStream());
		
		try {
			JSONObject response = new JSONObject();

			if (props != null){
				response.put("EnergiaProdottaGiornalieroSimul", props.getProperty("EnergiaProdottaGiornalieroSimul"));
			} else {
				response.put("none", 0);
			}
			resp.getOutputStream().print(response.toString());
		} catch (JSONException e) {
			LOG.error("Error creating JSON object",e);
		} catch (Exception e) {
			LOG.error("Error creating JSON object",e);
		}
		
		/*
		try {
			JSONObject rpcObject = new JSONObject(content);
			String methodName = rpcObject.getString("method");
			String id = rpcObject.getString("id");

			JSONArray paramsArray = rpcObject.getJSONArray("params");
			ArrayList paramValues = new ArrayList();

			JSONObject response = new JSONObject();

			response.put("id", id);

			Object targetObject = null;

			try {
				String targetPath = req.getPathInfo().substring(1);
				targetObject = httpAdapter.getObjectByPid(targetPath);

				if (targetObject == null) {
					response.put("result", JSONObject.NULL);
					response.put("error", "ERROR: unable to find target object " + targetPath);
					resp.getOutputStream().print(response.toString());
					return;
				}

				if ((targetObject instanceof IAppliance) && (targetObject != httpAdapter.getImplementor())) {
					IAppliance ac = (IAppliance) targetObject;
					paramValues.add(ac.getPid());
					targetObject = httpAdapter.getImplementor();
				}

				for (int i = 0; i < paramsArray.length(); i++) {
					paramValues.add(paramsArray.get(i));
				}

			} catch (Exception e) {
				fillResponse(response, e);
				resp.getOutputStream().print(response.toString());
			}

			try {
				Object result = httpAdapter.invokeMethod(targetObject, methodName, paramValues);
				response.put("result", result);
				response.put("error", JSONObject.NULL);
				resp.getOutputStream().print(response.toString());
				return;
			} catch (Exception e) {
				if (log) {
					System.out.println("[JSonRpc] EXCEPTION: su methodName = " + methodName + ' ' + e.getMessage());
				}
				
				fillResponse(response, e);
				response.put("result", JSONObject.NULL);
				resp.getOutputStream().print(response.toString());
			}
		} catch (JSONException jsonException) {
			throw new RuntimeException(jsonException);
		}*/
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			JSONObject response = new JSONObject();

			try {
				String targetPath = req.getPathInfo().substring(1);
				System.out.println("target path " + targetPath);
				Object target = httpAdapter.getObjectByPid(targetPath);
				if (target == null) {
					throw new RuntimeException("Could not find object " + targetPath);
				}

				response.put("envelope", "JSON-RPC-1.0");

				JSONObject jsonMethods = new JSONObject();

				Method[] methods = target.getClass().getMethods();

				for (int i = 0; i < methods.length; i++) {
					Method method = methods[i];
					JSONObject jsonSignature = new JSONObject();
					jsonMethods.put(method.getName(), jsonSignature);
				}

				response.put("methods", jsonMethods);
				resp.getOutputStream().print(response.toString());
			} catch (Throwable e) {
				fillResponse(response, e);

				resp.getOutputStream().print(response.toString());
			}
		} catch (JSONException jsonException) {
			throw new RuntimeException(jsonException);
		}
	}

	private void fillResponse(JSONObject response, Throwable e) throws JSONException {
		response.put("result", JSONObject.NULL);
		response.put("error", (e.getMessage() != null) ? e.getMessage() : JSONObject.NULL);
	}

	static String streamToString(InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}
}
