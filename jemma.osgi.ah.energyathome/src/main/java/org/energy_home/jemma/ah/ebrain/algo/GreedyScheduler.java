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

import org.energy_home.jemma.ah.ebrain.old.PowerProfilePhaseExtended;

/*
 * When scheduling phases, the MaxActivationDelay is a hard window and if it cannot be achieved
 * needs to reschedule (mark infeasible) the preceding phases
 * 	
 * Sequential Generation Scheme with greedy heuristics (priority order)
 * 
 * daily tariffs are represented as an ordered list (best to worst) of a tariff value and
 * its relative time windows. To find the min cost check the feasible allocation within the
 * window intervals, from left to right (move forward) and from right to left (move backward)
 * considered times are the intersection of the phase and the windows
 *  
 *  public void findMinimumCost:
 *  forech tariff (from best to worst) do {
 *  	foreach time-window do {
 *  		forward-search from startTime to 1st feasible slot
 *  		if (slot found) {
 *  			record the correspondent cost.
 *  			backward-search from startTime -1 to startTime - phase-duration to 1st feasible slot
 *  			if (slot found) {
 *  				 record the correspondent cost.
 *  
 *  ...
 *  
 *  return the failing slot (one without enough power), or -1 if it's feasible
 *  such info is useful to look for next feasible startTime
 *  
 *  public int isFeaisibleSlot(PowerPhase phase, int time) {
 *  foreach slot in time + phase-duration --slot (move backward)
 *  	if (available-energy[slot] < phase-demand) return slot;
 *  
 *  no overload found: return -1;
 *  
 *  Tabu-search
 *  move 1:
 *  shift a given activity with findMinimumCost
 *  
 *  move 2:
 *  swap scheduling (priority order) of 2 conflicting activities
 *  (LNS strategy: destroy & repair)
 *  
 *  Bulldozing for chained phases with max-time-lags. When phase[i] is allocated, a check is made
 *  to see it the max-time-lag with phase[i-1] is OK. If not attempt to recursively delay (bulldoze)
 *  the preceding phases through a recursive call.
 *  
 */

public class GreedyScheduler {
	// should contain today and tomorrow. Better yet: it's always a window from now to XX hours in the future
	int[] currentAvailablePower;
	
	
	public int isFeaisibleTime(PowerProfilePhaseExtended phase, int time) {
		for (int t = time + phase.getTransferredPhase().ExpectedDuration; t >= time; --t) {
			if (currentAvailablePower[t] < phase.getRequiredPower()) return t;
		}
		return -1;
	}
	
	public int findFeasibleTimeForward(PowerProfilePhaseExtended phase, int erliestStart, int latestStart) {
		for (int start = erliestStart; start < latestStart;) {
			/*int duration = phase.ExpectedDuration;
			while (duration-- >= 0) {
				if (currentAvailablePower[start + duration] < phase.getRequiredPower()) {
					start += duration +1; // move forward to the next failing slot
					break;
				}
			}
			if (duration == 0) return start; // here means all power requirements are met
			*/
			int end = start + phase.getTransferredPhase().ExpectedDuration;
			while (end-- >= start) {
				if (currentAvailablePower[end] < phase.getRequiredPower()) {
					start = end+1; // move forward to the next failing slot
					break;
				}
			}
			if (end == start) return start; // here means all power requirements are met
		}
		// here means no feasible start time was found in the time window
		return -1;
	}
	


	public int[] findFeasibleTimeForward(PowerProfilePhaseExtended[] phases, int erliestStart, int latestStart) {
		//PowerProfileTransferredPhase[] _phases = phases.powerProfileTransferredPhases;
		int [] scheduledStartTimes = new int[phases.length];
		int nextStart = erliestStart;
		
		while (nextStart < latestStart) {
			int nextEnd = latestStart;
    		
			loop: { 
			for (int i = 0; i < phases.length; ++i) {
    			int next = findFeasibleTimeForward(phases[i], nextStart, nextEnd);
    			if (next == -1) {
    				// infeasible, so try try all over again.
    				nextStart = scheduledStartTimes[0] +1;
    				break loop;
    			}
    			scheduledStartTimes[i] = next;
    			
    			// check if selected start time respects max-delay of all preceding phases
    			for (int j = i; j > 0; --j) {
        			int constrainedMinStart = scheduledStartTimes[j] - phases[j-1].getTransferredPhase().ExpectedDuration - phases[j].getTransferredPhase().MaxActivationDelay;
        			if (constrainedMinStart <= scheduledStartTimes[j-1]) break; // we're OK!
    				// try shift forward phase i-1 in the new time window
        			next = findFeasibleTimeForward(phases[j-1], constrainedMinStart, scheduledStartTimes[j] - phases[j-1].getTransferredPhase().ExpectedDuration);
        			if (next == -1) {
        				nextStart = scheduledStartTimes[0] +1;
        				break loop;
        			}
    				scheduledStartTimes[j-1] = next;
    			}
    			nextStart = scheduledStartTimes[i] + phases[i].getTransferredPhase().ExpectedDuration +1;
    			nextEnd = nextStart + phases[i+1].getTransferredPhase().MaxActivationDelay;// + REASONABLE_TIME_LAG;
    		}
			// success!
			return scheduledStartTimes;
		}
	}
		return scheduledStartTimes;
	}
}
