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

import java.util.Random;

import org.energy_home.jemma.ah.ebrain.CalendarUtil;
import org.energy_home.jemma.ah.ebrain.EnergyPhaseInfo;
import org.energy_home.jemma.ah.ebrain.PowerProfileInfo;
import org.energy_home.jemma.ah.ebrain.PowerProfileInfo.PowerProfileTimeConstraints;


/*
 * Single power profile candidate schedule, it's a sub-particle of an overall particle candidate schedule
 * 
 * Phases are "clustered" among those that have MaxActivationDelay lower than ACTIVATION_DELAY_THRESHOLD.
 * In other words, all phases with a (very) short MaxActivationDelay will be treated as all clustered
 * together. This should definitely reduce the problem's dimension.
 */

final class ProfileScheduleSubparticle {
	public static final int PHASE_DELAY_THRESHOLD = CalendarUtil.slotsFromMinutes(4);
	public static final int EXCEEDING_TIME_ALLOCATION_PENALTY = 1000 * 1000;

	
	public static final double HALF_PI = 0.5 * Math.PI;
	public static final double LEVY_ALFA = 1.5;

	public static double CAUCHY_CONSTRICTION = 0.35;
	public static double LEVY_CONSTRICTION = 0.75;
	public static double EXPONENTIAL_CONSTRICTION = 1.15;
	
	private static final Random rand = new Random();

	
	// the general Levy distribution is rather computing demanding
	public static final double nextLevy() {
		double v = Math.PI * (rand.nextDouble() - 0.5);
		double k = LEVY_ALFA * v;
		double w = -Math.log(rand.nextDouble()) / Math.cos(v - k);
		double h = w * Math.cos(v);
		return LEVY_CONSTRICTION * Math.abs(w * Math.sin(k) * Math.exp( -Math.log(h) / LEVY_ALFA));
	}

	public static final double nextCauchy() {
		return CAUCHY_CONSTRICTION * Math.tan(Math.PI * (rand.nextDouble() - 0.5));
		//return CAUCHY_CONSTRICTION * Math.tan(HALF_PI * rand.nextDouble());
	}
	
	public static final double nextExponential() {
		return EXPONENTIAL_CONSTRICTION * -Math.log(rand.nextDouble());
	}

	
	/*
	public static final double nextRandom(int randomDistribution) {
		double r;
		switch (randomDistribution) {
		case GAUSSIAN_STEP:
			return GAUSSIAN_CONSTRICTION * rand.nextGaussian();
		
		case ABS_GAUSSIAN_STEP:
			return GAUSSIAN_CONSTRICTION * Math.abs(rand.nextGaussian());
			
		case CAUCHY_STEP:
			return CAUCHY_CONSTRICTION * Math.tan(Math.PI * (rand.nextDouble() - 0.5));
		
		case ABS_CAUCHY_STEP:
			return CAUCHY_CONSTRICTION * Math.tan(HALF_PI * rand.nextDouble());
		
		case EXPONENTIAL_STEP:
			r = Math.log(rand.nextDouble());
			return EXPONENTIAL_CONSTRICTION * (rand.nextDouble() < 0.5 ? -r : r);
		
		case ABS_EXPONENTIAL_STEP:
			return EXPONENTIAL_CONSTRICTION * -Math.log(rand.nextDouble());
			
		case LEVY_STEP:
			r = nextLevy();
			return LEVY_CONSTRICTION * (rand.nextDouble() < 0.5 ? -r : r);
			
		case ABS_LEVY_STEP:
			return LEVY_CONSTRICTION * nextLevy();
		
		default:
			return rand.nextDouble();
		}
	}
	*/

	private EnergyPhaseInfo[] phases = new EnergyPhaseInfo[0];
	private int profileEarliestStart, profileSlackInterval;
	private float tardiness;
	private float[] phasesBestPositions, phasesCurrentPositions;

	ProfileScheduleSubparticle(PowerProfileInfo ppi) {
		phases = ppi.getEnergyPhases();
		
		PowerProfileTimeConstraints pptc = ppi.getTimeConstraints();
		long startTime = Math.max(pptc.getStartAfterConstraint(), System.currentTimeMillis());
		profileEarliestStart = CalendarUtil.getSlotOf(startTime);
		
		profileSlackInterval = CalendarUtil.slotsFromMillis(pptc.getStopBeforeConstraint() - startTime);
		
		for (int i = phases.length; --i >= 0; profileSlackInterval -= phases[i].getSlotDuration());
		
		if (profileSlackInterval <= 0)
			throw new IllegalArgumentException("Inconsistent time constraints with profile duration.");
				
		phasesBestPositions = new float[phases.length];
		phasesCurrentPositions = new float[phases.length];
		
		randomizePositions();
	}
	
	ProfileScheduleSubparticle(ProfileScheduleSubparticle sampleSchedule) {
		phases = sampleSchedule.phases;
		profileEarliestStart = sampleSchedule.profileEarliestStart;
		profileSlackInterval = sampleSchedule.profileSlackInterval;
		
		phasesBestPositions = new float[phases.length];
		phasesCurrentPositions = new float[phases.length];
		
		randomizePositions();
	}
		
	private void randomizePositions() {
		// initialize profile position using uniform distribution
		phasesCurrentPositions[0] = phasesBestPositions[0] = rand.nextFloat() * profileSlackInterval;
		tardiness = phasesCurrentPositions[0];

		for (int i = 1; i < phases.length; ++i) {
			float maxDelay = Math.min(profileSlackInterval - tardiness, phases[i].getSlotMaxDelay());
			phasesCurrentPositions[i] = phasesBestPositions[i] = maxDelay * rand.nextFloat();
			tardiness += phasesCurrentPositions[i];
		}
	}
	
	
	// tardiness is the difference between the current profile end and the minimum theoretical termination
	float getTardiness() {
		return tardiness;
	}	

	
	/*
	 * Implements a Quantum inspired PSO with Cauchy (Levy) flights
	 */
	void nextRandomFlight(ProfileScheduleSubparticle bestParticle) {
		// Set globalBestPositions Array to the best Particle's best Positions Array
		float[] globalBestPositions = bestParticle.phasesBestPositions;

		float currentMaxSlack = profileSlackInterval;
		float phaseMaxDelay = profileSlackInterval;
		
		// For each i iterating over all Phases of the Profile
		for (int i = 0; i < phases.length; i++) {
			// If i is greater than 0 (i.e. for all Pahses other than the firts one)
			if (i > 0) {
				// Set phaseMaxDelay to the minimum between currentMaxSlack, and Phase(i)'s MaxAlowedDelay
				phaseMaxDelay = Math.min(currentMaxSlack, phases[i].getSlotMaxDelay());
				if (phaseMaxDelay < PHASE_DELAY_THRESHOLD) {
					phasesCurrentPositions[i] = 0;
					continue;
				}
			}
			
			// Initialise r to a random real number uniform in [0,1]
			double r = rand.nextDouble();
			// Initialise attractor to r multiplied by Phase(i)'s currentBestPositions(i) plus ( 1 minus r ) multiplied by Phases(i)'s globalBestPositions(i)
			double attractor = r * phasesBestPositions[i] + (1 - r) * globalBestPositions[i];
			// Initialise c to a random real number with Cauchy distribution
			double c = nextCauchy();
			// Initialise step to c multiplied by (attractor minus Phase(i)'s currentPositions(i))
			double step = c * (attractor - phasesCurrentPositions[i]);
			// Set Phase(i)'s currentPosition to attractor plus step
			phasesCurrentPositions[i] = (float)(attractor + step);
			
			// If Phase(i)'s currentPosition is less than 0 or greater than phaseMaxDelay
			if (phasesCurrentPositions[i] < 0 || phasesCurrentPositions[i] > phaseMaxDelay)
				// Set Phase(i)'s currentPosition to 0
				phasesCurrentPositions[i] = 0;
			
			// Subtract to currentMaxSlack the new updated Phase(i)'s currentPosition
			currentMaxSlack -= phasesCurrentPositions[i];
		}

		tardiness = profileSlackInterval - currentMaxSlack;
	}
	
	
	void allocateBiasedPeakEnergy(float[] energyAllocation) {
		int start = profileEarliestStart;
		for (int i = 0; i < phases.length; ++i) {
			start += (int)Math.round(phasesCurrentPositions[i]);
			int duration = phases[i].getSlotDuration();
			allocateEnergyPhase(start, duration, phases[i].getOneSlotBiasedPeakEnergy(), energyAllocation);
			start += duration;
		}
	}
	
	void allocateMeanEnergy(float[] energyAllocation) {
		int start = profileEarliestStart;
		for (int i = 0; i < phases.length; ++i) {
			start += (int)Math.round(phasesCurrentPositions[i]);
			int duration = phases[i].getSlotDuration();
			allocateEnergyPhase(start, phases[i].getSlotDuration(), phases[i].getOneSlotMeanEnergy(), energyAllocation);
			start += duration;
		}
	}
	
	private void allocateEnergyPhase(int start, int duration, float oneSlotEnergy, float[] energyAllocation){
		while (start + duration > energyAllocation.length) {
			start -= CalendarUtil.SLOTS_IN_ONE_DAY;
			oneSlotEnergy += EXCEEDING_TIME_ALLOCATION_PENALTY;
		}
		for (int i = duration; --i >= 0; energyAllocation[start + i] += oneSlotEnergy);
	}
	
	void allocateEnergy(float[] energyAllocation, boolean isOverloadCalculation) {
		int start = profileEarliestStart;
		for (int i = 0; i < phases.length; ++i) {
			start += (int)Math.round(phasesCurrentPositions[i]);
			int duration = phases[i].getSlotDuration();
			float oneSlotEnergy = isOverloadCalculation ? phases[i].getOneSlotBiasedPeakEnergy() : phases[i].getOneSlotMeanEnergy();

			int offset = start;
			while (offset + duration > energyAllocation.length) {
				offset -= CalendarUtil.SLOTS_IN_ONE_DAY;
				oneSlotEnergy += EXCEEDING_TIME_ALLOCATION_PENALTY;
			}
			for (int j = duration; --j >= 0; energyAllocation[offset + j] += oneSlotEnergy);
			start += duration;
		}
	}
		
	// copy current position and velocity as local best
	void setCurrentAsBest() {
		System.arraycopy(phasesCurrentPositions, 0, phasesBestPositions, 0, phasesCurrentPositions.length);
	}
	
	void setEnergyPhasesBestSchedule() {
		// 1st phase schedule is set as absolute, while all other phases are relative delays
		phases[0].setScheduledSlot(profileEarliestStart + (int)Math.round(phasesBestPositions[0]));
		for (int i = 1; i < phases.length; ++i) {
			phases[i].setScheduledSlot((int)Math.round(phasesBestPositions[i]));
		}
	}
}
