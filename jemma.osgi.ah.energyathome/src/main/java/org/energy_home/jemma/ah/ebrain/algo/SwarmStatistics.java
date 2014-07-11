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
package org.energy_home.jemma.ah.ebrain.algo;

import java.util.ArrayList;
import java.util.List;

import org.energy_home.jemma.ah.ebrain.ApplianceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwarmStatistics {
	private static final Logger LOG = LoggerFactory.getLogger( SwarmStatistics.class );
	
	List<float[]> overloads = new ArrayList<float[]>();
	List<float[]> costs = new ArrayList<float[]>();
	List<float[]> tardiness = new ArrayList<float[]>();
	List<double[]> penalties = new ArrayList<double[]>();
	float leastEnergyCost = Float.POSITIVE_INFINITY;
	float leastOverload = Float.POSITIVE_INFINITY;
	float leastTardiness = Float.POSITIVE_INFINITY;
	double leastPenalty = Double.POSITIVE_INFINITY;
	int lastImprovingCost, lastImprovingOverload, lastImprovedTardiness, lastImprovedPenalty;
	
	public float getLeastEnergyCost() {
		return leastEnergyCost;
	}

	public float getLeastOverload() {
		return leastOverload;
	}

	public float getLeastTardiness() {
		return leastTardiness;
	}

	public double getLeastPenalty() {
		return leastPenalty;
	}

	public int getLastImprovingCost() {
		return lastImprovingCost;
	}

	public int getLastImprovingOverload() {
		return lastImprovingOverload;
	}

	public int getLastImprovedTardiness() {
		return lastImprovedTardiness;
	}

	public int getLastImprovedPenalty() {
		return lastImprovedPenalty;
	}
		
	public List<float[]> getOverloads() {
		return overloads;
	}

	public List<float[]> getEnergyCosts() {
		return costs;
	}

	public List<float[]> getTardiness() {
		return tardiness;
	}

	public List<double[]> getPenalties() {
		return penalties;
	}

	public void addOverloads(float[] overs) {
		overloads.add(overs);
		for (int i = 0; i < overs.length; ++i) {
			if (overs[i] < leastOverload) {
				leastOverload = overs[i];
				lastImprovingOverload = overloads.size();
				LOG.debug(String.format("(%d) overload: %12.9f", lastImprovingOverload, leastOverload));
			}
		}
	}
	
	public void addCosts(float[] cos) {
		costs.add(cos);
		for (int i = 0; i < cos.length; ++i) {
			if (cos[i] < leastEnergyCost) {
				leastEnergyCost = cos[i];
				lastImprovingCost = costs.size();
				LOG.debug(String.format("(%d) cost: %12.9f", lastImprovingCost, leastEnergyCost));
			}
		}
	}
	
	public void addTardiness(float[] tars) {
		tardiness.add(tars);
		for (int i = 0; i < tars.length; ++i) {
			if (tars[i] < leastTardiness) {
				leastTardiness = tars[i];
				lastImprovedTardiness = tardiness.size();
				LOG.debug(String.format("(%d) tardiness: %12.9f", lastImprovedTardiness, leastTardiness));
			}
		}
	}
	
	public void addPenalties(double[] pens) {
		penalties.add(pens);
		for (int i = 0; i < pens.length; ++i) {
			if (pens[i] < leastPenalty) {
				leastPenalty = pens[i];
				lastImprovedPenalty = penalties.size();
				LOG.debug(String.format("(%d) penalty: %16.12f", lastImprovedPenalty, leastPenalty));
			}
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("Swarm statistics:\n");
		sb.append(String.format("least overload = %12.9f - at iteration %d\n", leastOverload, lastImprovingOverload));
		sb.append(String.format("least energyCost = %12.9f - at iteration %d\n", leastEnergyCost, lastImprovingCost));
		sb.append(String.format("least tardiness = %12.9f - at iteration %d\n", leastTardiness, lastImprovedTardiness));
		sb.append(String.format("least penalty = %16.12f - at iteration %d\n", leastPenalty, lastImprovedPenalty));
		return sb.toString();
	}
}
