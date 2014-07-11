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

/* The dimension of the problem, i.e number of variables, is the sum of all phases of all power profiles,
 * i.e. sum_i(sum_j(profile[i].phase[j]))
 */
public final class ParticleSwarmScheduler {
	
	public static final double OVERLOAD_WEIGHT = 1000 * 1000;
	public static final double ENERGY_COST_WEIGHT = 1;
	//TODO: check merge
//	public static final double TARDINESS_WEIGHT = 1.0 / (100 * 1000);
	public static final double TARDINESS_WEIGHT = 1.0 / (1000 * 1000);

	public static final double REINIT_WORST_PARTICLE_PROBABILITY = 0.0; 

	private EnergyAllocator energyAllocator;
	private ProfileScheduleParticle[] swarm;
	private ProfileScheduleParticle bestParticle;
	private double leastPenalty = Double.POSITIVE_INFINITY;
	private float leastOverload = Float.POSITIVE_INFINITY;
	private float leastCost = Float.POSITIVE_INFINITY;
	private float leastTardiness = Float.POSITIVE_INFINITY;
	private int swarmSize;
	
	public ParticleSwarmScheduler(PowerProfileInfo ppi, EnergyAllocator ea, int size) {
		PowerProfileInfo[] ppiset = new PowerProfileInfo[1];
		ppiset[0] = ppi;
		init(ppiset, ea, size);
	}
	
	public ParticleSwarmScheduler(PowerProfileInfo[] ppiset, EnergyAllocator ea, int size) {
		init(ppiset, ea, size);
	}
	
	private void init(PowerProfileInfo[] ppiset, EnergyAllocator ea, int size) {
		energyAllocator = ea;
		swarmSize = size;
		// create the swarm
		swarm = new ProfileScheduleParticle[swarmSize];
		// create a prototype particle and then clone the others
		swarm[0] = new ProfileScheduleParticle(ppiset);
		for (int i = 1; i < swarmSize; swarm[i++] = new ProfileScheduleParticle(swarm[0]));
	}
	
	public float getLeastOverload() {return leastOverload;}
	public float getLeastCost() {return leastCost;}
	public float getLeastTardiness() {return leastTardiness;}
	public double getLeastPenalty() {return leastPenalty;}
	
		
	public boolean evolve() {
		// 2-step process: 1st evaluate fitness of all particles
		boolean isSwarmImproving = false;
		// keep track of the worst particle in the swarm
		//ProfileScheduleParticle worstParticle = swarm[0];

		// For each i iterating over all Particles in the Swarm
		for (int i = 0; i < swarmSize; ++i) {
			// Compute current Constraints violation as overload amount for Particle(i)
			float overload = energyAllocator.computeOverload(swarm[i]);
			// Store such overload for Particle(i)
			swarm[i].setCurrentOverload(overload);
			double penalty = overload * OVERLOAD_WEIGHT;
			
			// Compute cost and tardiness only if it's a feasible schedule (no overload)
			float energyCost = Float.POSITIVE_INFINITY;
			float tardiness = Float.POSITIVE_INFINITY;
			if (penalty == 0) {
				// Set energyCost to current Particle(i)'s Energy Cost
				energyCost = energyAllocator.computeEnergyCost(swarm[i]);
				// Store such energyCost for Particle(i)
				swarm[i].setCurrentCost(energyCost);
				// Set tardiness to current Particle(i)'s Tardiness
				tardiness = swarm[i].getCurrentTardiness();
				
				// Add to penalty (energyCost multiplied by ENERGY_COST_WEIGHT) plus (tardiness multiplied by TARDINESS_WEIGHT)
				penalty += energyCost * ENERGY_COST_WEIGHT + tardiness * TARDINESS_WEIGHT;
			}
			
			// Update Particle(i)'s penalty: If the new penalty is less than Particle(i)'s previous penalty Then Set Particle(i)'s bestPostion to its currentPosition
			boolean isParticleImproving = swarm[i].updatePenalty(penalty);
			// If penaly is less than leastPenalty
			if (penalty < leastPenalty) {
				leastPenalty = penalty;
				leastOverload = overload;
				leastCost = energyCost;
				leastTardiness = tardiness;
				bestParticle = swarm[i];
				isSwarmImproving = true;
			}
			
			//if (penalty > worstParticle.getCurrentPenalty()) worstParticle = swarm[i];
		}
		
		// with a certain probability reinitialize the worst particle to a random position
		//if (Math.random() < REINIT_WORST_PARTICLE_PROBABILITY) worstParticle.randomizePositions();
		
		// 2nd step: do a random flight
		// For each i iterating over all Particles in the Swarm
		for (int i = 0; i < swarmSize; ++i) {
			// Perform Particle(i) nextRandomFlight
			swarm[i].nextRandomStep(bestParticle);
		}
		return isSwarmImproving;
	}
	
	
	public int run(long timeLimit) {
    	return run(timeLimit, null);
	}
		
	public int run(long timeLimit, SwarmStatistics stats) {
		double elapsed = 0;
		int totalIters = 0;
    	for (long start = System.currentTimeMillis(); elapsed < timeLimit; elapsed = System.currentTimeMillis() - start) {
    		evolve();
    		++totalIters;
    		if (stats != null) {
        		stats.addOverloads(getParticlesOverloads());
        		stats.addCosts(getParticlesCosts());
        		stats.addTardiness(getParticlesTardiness());
        		stats.addPenalties(getParticlesPenalties());
    		}
    	}
    	
    	bestParticle.setEnergyPhasesBestSchedule();
		return totalIters;
	}
	
	
	public float[] getParticlesOverloads() {
		float[] currentOverloads = new float[swarmSize];
		for (int i = 0; i < swarmSize; ++i) {
			currentOverloads[i] = swarm[i].getCurrentOverload();
		}
		return currentOverloads;
	}
	
	public float[] getParticlesCosts() {
		float[] currentCosts = new float[swarmSize];
		for (int i = 0; i < swarmSize; ++i) {
			currentCosts[i] = swarm[i].getCurrentCost();
		}
		return currentCosts;
	}
	
	public float[] getParticlesTardiness() {
		float[] currentTardiness = new float[swarmSize];
		for (int i = 0; i < swarmSize; ++i) {
			currentTardiness[i] = swarm[i].getCurrentTardiness();
		}
		return currentTardiness;
	}
	
	public double[] getParticlesPenalties() {
		double[] currentPenalties = new double[swarmSize];
		for (int i = 0; i < swarmSize; ++i) {
			currentPenalties[i] = swarm[i].getCurrentPenalty();
		}
		return currentPenalties;
	}
}
