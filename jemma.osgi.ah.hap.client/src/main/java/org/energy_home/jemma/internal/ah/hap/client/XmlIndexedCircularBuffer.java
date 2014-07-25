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
package org.energy_home.jemma.internal.ah.hap.client;

//import org.energy_home.jemma.ah.FloatCDV;
//import org.energy_home.jemma.m2m.Content;
//import org.energy_home.jemma.m2m.ContentInstance;
//import org.energy_home.jemma.m2m.ContentInstanceItems;
//import org.energy_home.jemma.m2m.ContentInstanceItemsList;
//import org.energy_home.jemma.m2m.ContentInstancesBatchRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.energy_home.jemma.m2m.M2MXmlObject;

// TODO check for a solution based on buffered reader/writer and for a finally clause to catch all exception and close all 
// open i/o resources
public class XmlIndexedCircularBuffer {
	private static final String FILE_EXTENSION = ".xml";
	private static final int INDEX_AND_EXTENSION_LENGTH = 14;

	private static Long getFileIndex(String fileNameOrPath) {
		int fileNameLength = fileNameOrPath.length();
		if (fileNameLength < INDEX_AND_EXTENSION_LENGTH + 1)
			return null;
		String strIndex = fileNameOrPath.substring(fileNameLength - INDEX_AND_EXTENSION_LENGTH,
				fileNameLength - FILE_EXTENSION.length());
		return new Long(Long.parseLong(strIndex));
	}

	private static String getFilePath(String filePathPrefix, Long fileIndex) {
		return filePathPrefix + String.format("%010d", fileIndex) + FILE_EXTENSION;
	}

	private static M2MXmlObject loadFromFile(String filePathPrefix, Long fileIndex) {
		String filePath = getFilePath(filePathPrefix, fileIndex);
		return M2MXmlObject.loadFromFile(filePath);
	}

	private static boolean saveToFile(String filePathPrefix, Long fileIndex, M2MXmlObject object) {
		String filePath = getFilePath(filePathPrefix, fileIndex);
		return M2MXmlObject.saveToFile(filePath, object);
	}

	private static boolean deleteFile(String filePathPrefix, Long fileIndex) {
		try {
			String filePath = getFilePath(filePathPrefix, fileIndex);
			File file = new File(filePath);
			file.delete();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private int maxSize = Integer.MAX_VALUE;

	private String dirName = null;
	private String fileNamePrefix = null;
	private String filePathPrefix = null;

	private ArrayList<Long> buffer;
	private M2MXmlObject firstItem = null;
	private M2MXmlObject lastItem = null;
	private ArrayList<M2MXmlObject> memoryCache = null;

	private void initBufferItems(int maxSize) {
		buffer = new ArrayList<Long>(maxSize);
		if (dirName != null) {
			File dir = new File(dirName);
			if (!dir.exists())
				dir.mkdir();
			if (dir.isDirectory()) {
				File[] files = dir.listFiles();
				String fileName = null;
				Long fileIndex = null;
				for (int i = 0; i < files.length; i++) {
					fileName = files[i].getName();
					if (fileName.startsWith(fileNamePrefix)) {
						fileIndex = getFileIndex(fileName);
						if (fileIndex != null) {
							buffer.add(fileIndex);

						}
					}
				}
				Collections.sort(buffer);
				while (buffer.size() > maxSize) {
					deleteFile(filePathPrefix, buffer.get(0));
					buffer.remove(0);
				}
				
				firstItem = null;
				lastItem = null;
				if (buffer.size() > 0) {
					while (firstItem == null && buffer.size() > 0) {
						firstItem = loadFromFile(filePathPrefix, buffer.get(0));
						if (firstItem == null) {
							deleteFile(filePathPrefix, buffer.get(0));
							buffer.remove(0);
						}
					}
					if (buffer.size() == 1)
						lastItem = firstItem;
					else {
						while (lastItem == null && buffer.size() > 1) {
							lastItem = loadFromFile(filePathPrefix, buffer.get(buffer.size() - 1));
							if (lastItem == null) {
								deleteFile(filePathPrefix, buffer.get(buffer.size() - 1));
								buffer.remove(buffer.size() - 1);
							}
						}
						if (buffer.size() == 1)
							lastItem = firstItem;
					}
				}
			}
		}
	}

	private boolean createBufferItem(M2MXmlObject item) {
		if (buffer.size() >= maxSize)
			deleteBufferItem();
		Long fileIndex = null;
		if (buffer.size() > 0)
			fileIndex = new Long(buffer.get(buffer.size() - 1).longValue() + 1);
		else
			fileIndex = new Long(0);
		boolean result = true;
		if (memoryCache != null)
			memoryCache.add(item);
		if (filePathPrefix != null)
			result = saveToFile(filePathPrefix, fileIndex, item);
		if (result) {
			buffer.add(fileIndex);
			lastItem = item;
			if (buffer.size() == 1)
				firstItem = item;
			return true;
		} else {
			// Error while saving file (e.g. the directory has been removed),
			// try to reinit the buffer
			initBufferItems(maxSize);
		}
		return false;
	}

	private boolean deleteBufferItem() {
		if (buffer.size() == 0)
			return false;
		if (memoryCache != null)
			memoryCache.remove(0);
		if (filePathPrefix != null)
			deleteFile(filePathPrefix, buffer.get(0));
		buffer.remove(0);
		if (buffer.size() == 0) {
			firstItem = null;
			lastItem = null;
		} else if (buffer.size() == 1) {
			firstItem = lastItem;
		} else {
			if (memoryCache != null)
				firstItem = memoryCache.get(0);
			if (filePathPrefix != null) {
				firstItem = loadFromFile(filePathPrefix, buffer.get(0));
				if (firstItem == null)
					// It is an error condition (some of the files have been
					// deleted), try to reinit the buffer
					initBufferItems(maxSize);
			}
		}
		return true;
	}

	public XmlIndexedCircularBuffer(int maxSize) {
		if (maxSize <= 0)
			new IllegalStateException("Size must be a positive integer value");
		this.maxSize = maxSize;
		buffer = new ArrayList<Long>(maxSize);
		this.memoryCache = new ArrayList<M2MXmlObject>(maxSize);
	}

	public XmlIndexedCircularBuffer(int maxSize, String dirName, String fileNamePrefix) {
		if (maxSize <= 0)
			new IllegalStateException("Size must be a positive integer value");
		if (Utils.isNullOrEmpty(dirName))
			new IllegalStateException("Directory name cannot be null or an empty string");
		this.maxSize = maxSize;
		this.dirName = dirName;
		this.fileNamePrefix = fileNamePrefix == null ? "" : fileNamePrefix;
		this.filePathPrefix = dirName + "/" + this.fileNamePrefix;
		initBufferItems(maxSize);
	}

	synchronized int getMaxSize() {
		return maxSize;
	}

	synchronized int getSize() {
		return buffer.size();
	}

	synchronized M2MXmlObject getFirstItem() {
		return firstItem;
	}

	synchronized M2MXmlObject getLastItem() {
		return lastItem;
	}

	synchronized Long getFirstItemIndex() {
		if (buffer.size() > 0)
			return buffer.get(0);
		return null;
	}

	synchronized Long getLastItemIndex() {
		if (buffer.size() > 0)
			return buffer.get(buffer.size() - 1);
		return null;
	}

	synchronized void addItem(M2MXmlObject item) {
		createBufferItem(item);
	}

	synchronized void removeFirstItem() {
		deleteBufferItem();
	}

	synchronized void purge() {
		for (int i = 0; i < buffer.size(); i++) {
			deleteBufferItem();
		}
	}

	// private static void printFirstAndLastItems(XmlIndexedCircularBuffer b) {
	// System.out.println("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	// System.out.println("First item index: " + b.getFirstItemIndex());
	// System.out.println("First item value: " +
	// (b.getFirstItem() != null ? b.getFirstItem().toXmlFormattedString() :
	// "null"));
	// System.out.println("Last item index: " + b.getLastItemIndex());
	// System.out.println("Last item value: " +
	// (b.getLastItem() != null ? b.getLastItem().toXmlFormattedString() :
	// "null"));
	// }
	//
	// public static void main(String[] args) {
	// XmlIndexedCircularBuffer b1 = new XmlIndexedCircularBuffer(12,
	// "org.energy_home.jemma.hap.client1", "batch.request.");
	// XmlIndexedCircularBuffer b2 = new XmlIndexedCircularBuffer(12,
	// "org.energy_home.jemma.hap.client1", "cache.latest.");
	// printFirstAndLastItems(b1);
	// printFirstAndLastItems(b2);
	//
	//
	// ContentInstanceItemsList itemsList = new ContentInstanceItemsList();
	// ContentInstancesBatchRequest batchRequest = new
	// ContentInstancesBatchRequest();
	// ContentInstanceItems ciis = new ContentInstanceItems();
	// ContentInstance ci = new ContentInstance();
	// Content c = new Content();
	// FloatCDV cost = new FloatCDV();
	// cost.setTimestamp(System.currentTimeMillis());
	// cost.setValue(0.01f);
	// cost.setDuration(120000);
	// c.setData(cost);
	// ci.setContent(c);
	// ciis.setAddressedId("testAddressedId");
	// ciis.getContentInstances().add(ci);
	//
	// itemsList.getContentInstanceItems().add(ciis);
	// batchRequest.getContentInstanceItems().add(ciis);
	//
	// for (int i = 0; i < 10; i++) {
	// b1.addItem(itemsList);
	// b2.addItem(batchRequest);
	// }
	// printFirstAndLastItems(b1);
	// printFirstAndLastItems(b2);
	//
	// for (int i = 0; i < 5; i++) {
	// b1.removeFirstItem();
	// b2.removeFirstItem();
	// }
	// printFirstAndLastItems(b1);
	// printFirstAndLastItems(b2);
	//
	// }
}
