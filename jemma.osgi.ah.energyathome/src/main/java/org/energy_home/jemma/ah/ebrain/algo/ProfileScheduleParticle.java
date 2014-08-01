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

import org.energy_home.jemma.ah.ebrain.PowerProfileInfo;


final class ProfileScheduleParticle {
	private ProfileScheduleSubparticle[] profileSchedule;
	private float currentOverload = Float.POSITIVE_INFINITY;
	private float currentCost = Float.POSITIVE_INFINITY;
	private double currentPenalty = Double.POSITIVE_INFINITY;
	private double leastPenalty = Double.POSITIVE_INFINITY;
	
	ProfileScheduleSubparticle[] getProfileScheduleSubparticles() {
		return profileSchedule;
	}
	
	ProfileScheduleParticle(PowerProfileInfo[] ppiset) {
		profileSchedule = new ProfileScheduleSubparticle[ppiset.length];
		for (int i = 0; i < ppiset.length; ++i) {
			profileSchedule[i] = new ProfileScheduleSubparticle(ppiset[i]);
		}
	}
	
	ProfileScheduleParticle(ProfileScheduleParticle sampleParticle) {
		ProfileScheduleSubparticle[] sampleSubparticles = sampleParticle.getProfileScheduleSubparticles();
		profileSchedule = new ProfileScheduleSubparticle[sampleSubparticles.length];
		for (int i = 0; i < sampleSubparticles.length; ++i) {
			profileSchedule[i] = new ProfileScheduleSubparticle(sampleSubparticles[i]);
		}
	}
	/*
	void randomizePositions() {
		for (int i = 0; i < profileSchedule.length; ++i) {
			profileSchedule[i].randomizePositions();
		}
	}
	*/
	void allocateBiasedPeakEnergy(float[] energyAllocation) {
		for (int i = 0; i < profileSchedule.length; ++i) {
			profileSchedule[i].allocateBiasedPeakEnergy(energyAllocation);
			//profileSchedule[i].allocateEnergy(energyAllocation, true);
		}
	}
	
	void allocateMeanEnergy(float[] energyAllocation) {
		for (int i = 0; i < profileSchedule.length; ++i) {
			profileSchedule[i].allocateMeanEnergy(energyAllocation);
			//profileSchedule[i].allocateEnergy(energyAllocation, false);
		}
	}

	float getCurrentOverload() {
		return currentOverload;
	}
	
	void setCurrentOverload(float overload) {
		currentOverload = overload;
	}
	
	float getCurrentCost() {
		return currentCost;
	}
	
	void setCurrentCost(float energyCost) {
		currentCost = energyCost;
	}
	
	double getCurrentPenalty() {
		return currentPenalty;
	}
	
	double getLeastPenaly() {
		return leastPenalty;
	}

	boolean updatePenalty(double penalty) {
		currentPenalty = penalty;
		if (penalty > leastPenalty) {
			return false;
		} else {
		
			leastPenalty = penalty;
			// For each i iterating over all Phases of the Profile
			for (int i = 0; i < profileSchedule.length; ++i) {
				//Set Phase(i)'s bestPosition to Phase(i)'s currentPosition
				profileSchedule[i].setCurrentAsBest();
			}
			return true;
		}
	}

	float getCurrentTardiness() {
		float tardiness = 0;
		for (int i = 0; i < profileSchedule.length; ++i) {
			tardiness += profileSchedule[i].getTardiness();
		}
		return tardiness;
	}
	

	void nextRandomStep(ProfileScheduleParticle leader) {
		ProfileScheduleSubparticle[] profileLeaders = leader.getProfileScheduleSubparticles();
		for (int i = 0; i < profileSchedule.length; ++i) {
			profileSchedule[i].nextRandomFlight(profileLeaders[i]);
		}
	}
	
	
	void setEnergyPhasesBestSchedule() {
		for (int i = 0; i < profileSchedule.length; ++i) {
			profileSchedule[i].setEnergyPhasesBestSchedule();
		}
	}
}
