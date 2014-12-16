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
package org.energy_home.jemma.ah.hap.client.lib;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.energy_home.jemma.internal.ah.hap.client.HapServiceManager;
import org.energy_home.jemma.m2m.ContentInstanceItems;
import org.energy_home.jemma.m2m.ContentInstancesBatchRequest;
import org.energy_home.jemma.m2m.M2MXmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlBatchRequestFileProcessor {
	
	private static final Logger LOG = LoggerFactory.getLogger(XmlBatchRequestFileProcessor.class);

	private static void execute(String inputDir, String outputDir) {
		String fileNamePrefix = HapServiceManager.BATCH_REQUESTS_FILE_NAME_PREFIX;
		if (inputDir == null)
			inputDir = "./";
		File inputFileDir = new File(inputDir);
		File outputFileDir = new File(outputDir);
		
		LOG.info("Processing files:\ninput dir: " + inputFileDir.getAbsolutePath() + "\noutput dir: "
				+ outputFileDir.getAbsolutePath());
		if (inputFileDir.isDirectory()) {
			File[] files = inputFileDir.listFiles();
			String fileName = null;
			ContentInstancesBatchRequest inputRequest = null;
			List<ContentInstanceItems> itemslist = null;
			ContentInstancesBatchRequest outputCostRequest = null;
			ContentInstancesBatchRequest outputPowerRequest = null;
			ContentInstancesBatchRequest outputEnergyRequest = null;
			for (int i = 0; i < files.length; i++) {
				fileName = files[i].getName();
				if (fileName.startsWith(fileNamePrefix)) {
					inputRequest = (ContentInstancesBatchRequest) M2MXmlObject.loadFromFile(inputDir + "/" + fileName);
					itemslist = inputRequest.getContentInstanceItems();
					outputCostRequest = new ContentInstancesBatchRequest();
					outputCostRequest.setTimestamp(inputRequest.getTimestamp());
					outputPowerRequest = new ContentInstancesBatchRequest();
					outputPowerRequest.setTimestamp(inputRequest.getTimestamp());
					outputEnergyRequest = new ContentInstancesBatchRequest();
					outputEnergyRequest.setTimestamp(inputRequest.getTimestamp());
					for (Iterator<ContentInstanceItems> iterator = itemslist.iterator(); iterator.hasNext();) {
						ContentInstanceItems contentInstanceItems = (ContentInstanceItems) iterator.next();
						if (contentInstanceItems.getAddressedId().contains("Cost"))
							outputCostRequest.getContentInstanceItems().add(contentInstanceItems);
						else if (contentInstanceItems.getAddressedId().contains("Power"))
							outputPowerRequest.getContentInstanceItems().add(contentInstanceItems);
						else
							outputEnergyRequest.getContentInstanceItems().add(contentInstanceItems);
					}
					M2MXmlObject.saveToFile(outputDir + "/cost." + fileName, outputCostRequest);
					M2MXmlObject.saveToFile(outputDir + "/energy." + fileName, outputEnergyRequest);
					M2MXmlObject.saveToFile(outputDir + "/power." + fileName, outputPowerRequest);
				}
			}
		}
	}

	public static void main(String[] args) {
		String inputDir = "";
		String outputDir = "";

		if (args.length > 0 && args[0] != null)
			inputDir = args[0];

		if (args.length > 1 && args[1] != null)
			outputDir = args[1];
		else
			outputDir = inputDir;

		execute(inputDir, outputDir);
	}

}
