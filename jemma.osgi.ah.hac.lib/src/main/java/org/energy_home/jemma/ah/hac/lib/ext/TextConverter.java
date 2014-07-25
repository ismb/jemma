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
package org.energy_home.jemma.ah.hac.lib.ext;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.StringTokenizer;

import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ISubscriptionParameters;
import org.energy_home.jemma.ah.hac.lib.AttributeValue;
import org.energy_home.jemma.ah.hac.lib.SubscriptionParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextConverter {
	
	private static final Logger LOG = LoggerFactory.getLogger(TextConverter.class);
	
	private static final String ERROR_PREFIX = "ERROR: ";
	private static final String OBJECT_SEPARATOR = "|";
	private static final String EXADECIMAL_VALUE_PREFIX = "0X";
	private static final String NULL_STRING_REPRESENTATION = "/";
	
	private static String ISUBSCRIPTION_PARAMETERS_MIN_REPORTING_INTERVAL = "MinReportingInterval";
	private static String ISUBSCRIPTION_PARAMETERS_MAX_REPORTING_INTERVAL = "MaxReportingInterval";
	private static String ISUBSCRIPTION_PARAMETERS_REPORTABLE_CHANGE = "ReportableChange";
	
	private static String IATTRIBUTE_VALUE_VALUE = "Value";
	private static String IATTRIBUTE_VALUE_TIMESTAMP = "Timestamp";
	
	private static String lowerCaseInitialChar(String s) {
		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}

	private static String upperCaseInitialChar(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}
	
	private static void recursiveAppendObject(Object obj, StringBuilder sb) throws IllegalArgumentException, IllegalAccessException {
		if (obj == null) {
			sb.append(OBJECT_SEPARATOR);
			sb.append(NULL_STRING_REPRESENTATION);
			return;
		}
		Class type = obj.getClass();
		
		if (type.isPrimitive() || type.equals(Boolean.class) || type.equals(Short.class) || type.equals(Integer.class) ||
				type.equals(Long.class) || type.equals(Float.class) || type.equals(Double.class) || type.equals(String.class) ||
				type.equals(Byte.class) || type.equals(byte.class)) {
			sb.append(OBJECT_SEPARATOR);
			sb.append(obj.toString());
		} else if (type.isArray()) {
			int arrayLength = Array.getLength(obj);	
			if (arrayLength > 0 && (type.getComponentType().equals(Byte.class) || type.getComponentType().equals(byte.class))) {
			    BigInteger bi = new BigInteger(1, (byte[])obj);
			    sb.append(OBJECT_SEPARATOR);
			    sb.append(EXADECIMAL_VALUE_PREFIX);
			    sb.append(String.format("%0" + (((byte[])obj).length << 1) + "X", bi));
			} else {
				sb.append(OBJECT_SEPARATOR);		
				sb.append(arrayLength);
				if (arrayLength > 0) {
					for (int i = 0; i < arrayLength; i++) {
						recursiveAppendObject(Array.get(obj, i), sb);
					}				
				}
			}
		} else {
			// Cluster classes can be extended 
			Class parentType = type.getSuperclass().getSuperclass();
			while (parentType != null) {
				type=type.getSuperclass();
				parentType = parentType.getSuperclass();
			}
			boolean useDeclaredFields = false;
			if (ISubscriptionParameters.class.isAssignableFrom(type)) {
				type = SubscriptionParameters.class;
				useDeclaredFields = true;
			}
			if (IAttributeValue.class.isAssignableFrom(type)) {
				type = AttributeValue.class;
				useDeclaredFields = true;
			}
			Field[] fields = useDeclaredFields ? type.getDeclaredFields() : type.getFields();
			String name = null;
			for (int i = 0; i < fields.length; i++) {
				sb.append(OBJECT_SEPARATOR);
				name = useDeclaredFields ? upperCaseInitialChar(fields[i].getName()) : fields[i].getName();
				sb.append(name);
				recursiveAppendObject(fields[i].get(obj), sb);
			}
		}		
	}

	private static void appendObject(Object obj, StringBuilder sb) throws IllegalArgumentException, IllegalAccessException {
		if (sb != null && obj instanceof Exception) {
			sb.append(ERROR_PREFIX);
			sb.append(((Exception) obj).getMessage());
		} else if (sb != null && obj instanceof ISubscriptionParameters) {
			ISubscriptionParameters sp = (ISubscriptionParameters) obj;
			sb.append(OBJECT_SEPARATOR);
			sb.append(ISUBSCRIPTION_PARAMETERS_MIN_REPORTING_INTERVAL);
			sb.append(OBJECT_SEPARATOR);
			sb.append(sp.getMinReportingInterval());
			sb.append(OBJECT_SEPARATOR);
			sb.append(ISUBSCRIPTION_PARAMETERS_MAX_REPORTING_INTERVAL);
			sb.append(OBJECT_SEPARATOR);
			sb.append(sp.getMaxReportingInterval());
			sb.append(OBJECT_SEPARATOR);
			sb.append(ISUBSCRIPTION_PARAMETERS_REPORTABLE_CHANGE);
			sb.append(OBJECT_SEPARATOR);
			sb.append(sp.getReportableChange());
			sb.append(OBJECT_SEPARATOR);
		} else	if (sb != null && obj instanceof IAttributeValue) {
			IAttributeValue av = (IAttributeValue) obj;
			sb.append(OBJECT_SEPARATOR);
			sb.append(IATTRIBUTE_VALUE_TIMESTAMP);
			sb.append(OBJECT_SEPARATOR);
			sb.append(av.getTimestamp());
			sb.append(OBJECT_SEPARATOR);
			sb.append(IATTRIBUTE_VALUE_VALUE);
			recursiveAppendObject(av.getValue(), sb);
			sb.append(OBJECT_SEPARATOR);
		} else {		
			recursiveAppendObject(obj, sb);
			sb.append(OBJECT_SEPARATOR);
		}
	}
	
	private static Object parseObject(Class type, StringTokenizer st) throws InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException {
		boolean useDeclaredFields = false;
		String str = st.nextToken();
		if (str.equals(NULL_STRING_REPRESENTATION)) {
			return null;
		}
		Object result = null;
		int base = 10;
		boolean isExadecimal = false;
		if (str.toUpperCase().startsWith(EXADECIMAL_VALUE_PREFIX)) {
			str = str.substring(2);
			base = 16;
			isExadecimal = true;
		}
		if (type.equals(Object.class)) {
			// Just a hack to manage WriteAttributeRecord serialization (it works only for integer attributes)
			if (Character.isDigit(str.charAt(0))) {
				// Correct cast is made during serialization (e.g. ZclDataTypeUI8, ZclDataTypeUI16)
				result = Long.valueOf(str, base);
			} else {
				result = Boolean.valueOf(str);
			}
		} else if (type.equals(Boolean.class) || type.equals(boolean.class))
			result = Boolean.valueOf(str);
		else if (type.equals(Short.class) || type.equals(short.class)) {
			result = Short.valueOf(str, base);
		} else if (type.equals(Integer.class) || type.equals(int.class))
			result =  Integer.valueOf(str, base);
		else if (type.equals(Long.class) || type.equals(long.class))
			result = Long.valueOf(str, base);
		else if (type.equals(Float.class) || type.equals(float.class))
			result = Float.valueOf(str);
		else if (type.equals(Double.class) || type.equals(double.class))
			result = Double.valueOf(str);
		else if (type.equals(Byte.class) || type.equals(byte.class))
			result = new Byte(Byte.parseByte(str, base));
		else if (type.equals(String.class))
			result = str;
		else if (type.isArray()) {
			if ((type.getComponentType().equals(Byte.class) || type.getComponentType().equals(byte.class)) && isExadecimal) {
				BigInteger bi = new BigInteger(str, base);
				byte[] baResult = bi.toByteArray();
				int baResultSize = str.length()/2;
				result = new byte[baResultSize];
				System.arraycopy(baResult, 0, result, baResultSize - baResult.length, baResult.length);
			} else {
				int arrayLength = Integer.parseInt(str);
				Class arrayClass = type.getComponentType();
				result = Array.newInstance(arrayClass, arrayLength);		
				for (int i = 0; i < arrayLength; i++) {
					Array.set(result, i, parseObject(arrayClass, st));
				}
			}
		} else {
			if (type.equals(ISubscriptionParameters.class)) {
				type = SubscriptionParameters.class;
				useDeclaredFields = true;
			}
			if (type.equals(IAttributeValue.class)) {
				type = AttributeValue.class;
				useDeclaredFields = true;
			}
			result = type.newInstance();
			Field[] fields = useDeclaredFields ? type.getDeclaredFields() : type.getFields();
			Field field = useDeclaredFields ? type.getDeclaredField(lowerCaseInitialChar(str)) : type.getField(str);
			for (int i = 0; i < fields.length; i++) {
				field.set(result, parseObject(field.getType(), st));
				if (i < fields.length-1)
					field = useDeclaredFields ? type.getDeclaredField(lowerCaseInitialChar(st.nextToken())) : type.getField(st.nextToken());
			}
		}
		return result;
	}
	
	public static Object[] getObjectParameters(Class clusterClass, String methodName, String[] params, IEndPointRequestContext context) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException {
		Object[] objParams = null;
		if (params == null ){
			objParams = new Object[1];
		} else {
			objParams = new Object[params.length+1];
			Method[] clusterMethods = clusterClass.getMethods();
			Class[] clusterParamsTypes = null;
			for (int i = 0; i < clusterMethods.length; i++) {
				clusterParamsTypes = clusterMethods[i].getParameterTypes();
				if (methodName.equals(clusterMethods[i].getName()) && clusterParamsTypes.length - 1 == params.length) {
					for (int j = 0; j < params.length; j++) {
						StringTokenizer st = new StringTokenizer(params[j], "|");
						objParams[j] = parseObject(clusterParamsTypes[j], st);
					}
					break;
				}
			}
		}
		objParams[objParams.length-1] = context;
		return objParams;
	}
	
	public static Object getObject(String str, Class type) throws SecurityException, InstantiationException, NoSuchFieldException, IllegalAccessException {
		StringTokenizer st = new StringTokenizer(OBJECT_SEPARATOR);
		return parseObject(type, st);
	}
	
	public static String getTextRepresentation(Object obj) {
		StringBuilder sb = new StringBuilder();
		try {
			appendObject(obj, sb);
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
			return null;
		}
		return sb.toString();
	}
	
}
