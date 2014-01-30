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
package org.energy_home.jemma.utils.xml.jaxb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;

public abstract class XmlConverter {	
	public static final String XML_SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
	public static final String XML_SCHEMA_INSTANCE_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
	
	protected static final String getPrintableString(String xml) {
		StringBuilder sb = new StringBuilder(xml);
		int i = 0;
		while ((i = sb.indexOf(" ", i + 1000)) != -1) {
			sb.replace(i, i + 1, "\n");
		}

		return sb.toString();
	}
	
	private class CustomNameSpacePrefixMapper extends NamespacePrefixMapper {
		private Map<String, String> namespacesPrefixMap;
		
		CustomNameSpacePrefixMapper(Map<String, String> namespacesPrefixMap) {
			this.namespacesPrefixMap = Collections.unmodifiableMap(namespacesPrefixMap);
		}
		
		Map<String, String> getNamespacePreferredPrefixMap() {
			return namespacesPrefixMap;
		}
		
		public String[] getPreDeclaredNamespaceUris() {
			String[] result = new String[namespacesPrefixMap.size()];
			namespacesPrefixMap.keySet().toArray(result);
			return result;
		}

		public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
			String result = namespacesPrefixMap.get(namespaceUri);
			if (result == null)
				return suggestion;
			return result;
		}
		
	}
	
	private class ConverterPool {
		private int poolMaxSize = 0;
		private int currentIndex = -1;
		private List<JaxbConverter> pool;
		
		ConverterPool(int poolMaxSize) {
			if (poolMaxSize <= 0)
				throw new IllegalStateException("Pool size cannot be 0 or a negative number");
			this.poolMaxSize = poolMaxSize;
			pool = new ArrayList<JaxbConverter>(poolMaxSize);
			this.currentIndex = 0;
		}
		
		private void incrementIndex() {
			currentIndex++;
			if (currentIndex == poolMaxSize)
				currentIndex = 0;
		}
		
		int getMaxSize() {
			return poolMaxSize;
		}
		
		synchronized JaxbConverter get() throws JAXBException {	
			JaxbConverter converter;
			if (pool.size() < poolMaxSize) {
				converter = createConverter();
				pool.add(converter);
			} else {
				converter = pool.get(currentIndex);
			}
				
			if (poolMaxSize > 1)
				incrementIndex();
			return converter;
		}
	}
	
	private String contextPath;
	private String defaultNamespace;
	private ConverterPool convertersPool;
	private CustomNameSpacePrefixMapper namespacePrefixMapper;
	
	private void setPoolMaxSize(int poolMaxSize) {
		if (poolMaxSize > 0) {
			convertersPool = new ConverterPool(poolMaxSize);
		} else {
			convertersPool = null;
		}
	}
	
	protected final NamespacePrefixMapper getNamespacePrefixMapper() {
		return this.namespacePrefixMapper;
	}

	protected JaxbConverter getConverter() throws JAXBException {
		JaxbConverter jaxbConverter;	
		if (convertersPool == null) {
			jaxbConverter = createConverter();
		} else {
			jaxbConverter = convertersPool.get();
		}
		return jaxbConverter;
	}
	
	protected JaxbConverter createConverter() throws JAXBException {
		return new JaxbConverter(this);
	}
	
	protected abstract JAXBContext createJaxbContext(String contextPath) throws JAXBException;

	protected XmlConverter(String contextPath, String defaultNamespace, Map<String, String> nameSpacePreferredPrefixMap, int poolMaxSize) {
		this.contextPath = contextPath;
		this.defaultNamespace = defaultNamespace;
		if (nameSpacePreferredPrefixMap != null)
			this.namespacePrefixMapper = new CustomNameSpacePrefixMapper(nameSpacePreferredPrefixMap);
		setPoolMaxSize(poolMaxSize);
	}
	
	public String getContextPath() {
		return this.contextPath;
	}
	
	public String getDefaultNamespace() {
		return this.defaultNamespace;
	}
	
	public Map<String, String> getNameSpacePreferredPrefixMap() {
		if (namespacePrefixMapper == null)
			return null;
		else
			return namespacePrefixMapper.getNamespacePreferredPrefixMap();
	}
	
	public byte[] getByteArray(Object o) {
		try {
			return getConverter().getByteArrayOutputStream(o).toByteArray();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getString(Object o) {
		try {
			return getConverter().getByteArrayOutputStream(o).toString();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getPrintableString(Object o) {
		String result;
		try {
			result = getConverter().getByteArrayOutputStream(o).toString();
			return getPrintableString(result);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getFormattedString(Object o) {
		String result;
		try {
			result = getConverter().getFormattedByteArrayOutputStream(o).toString();
			return getPrintableString(result);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return null;

	}
	
	public Object getObject(String xmlString) {
		try {
			return getConverter().readObject(
					new ByteArrayInputStream(xmlString.getBytes(JaxbConverter.UTF8_CHAR_ENCODING)));
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Object readObject(InputStream in) {
		try {
			return getConverter().readObject(in);
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void writeObject(Object object, OutputStream out) {
		try {
			getConverter().writeObject(object, out);
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public Object loadFromFile(String filePath) {
		Object object = null;
		BufferedInputStream fileIn = null;
		try {
			fileIn = new BufferedInputStream(new FileInputStream(filePath));
			object = readObject(fileIn);
			fileIn.close();
			return object;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fileIn != null)
				try {
					fileIn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return object;
	}

	public boolean saveToFile(String filePath, Object object) {
		BufferedOutputStream fileOut = null;
		try {
			fileOut = new BufferedOutputStream(new FileOutputStream(filePath));
			writeObject(object, fileOut);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (fileOut != null)
				try {
					fileOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}	
	
}
