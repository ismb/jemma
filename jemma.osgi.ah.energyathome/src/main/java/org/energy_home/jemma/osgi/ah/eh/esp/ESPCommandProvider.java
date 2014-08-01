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
package org.energy_home.jemma.osgi.ah.eh.esp;


import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.energy_home.jemma.ah.eh.esp.ESPConfigParameters;
import org.energy_home.jemma.ah.eh.esp.ESPException;
import org.energy_home.jemma.ah.eh.esp.ESPService;

public class ESPCommandProvider implements CommandProvider {

	private ESPService espService = null;

	private boolean checkESPService(CommandInterpreter ci) {
		if (espService == null) {
			ci.println("ESP service not available");
			return false;
		}
		return true;
	}

	private Float parseFloat(CommandInterpreter ci) {
		String strValue = ci.nextArgument();
		Float value = null;
		if (strValue != null) {
			try {
				value = Float.parseFloat(strValue);
			} catch (Exception e) {
			}
		}
		return value;
	}

	private Long getTimestamp(CommandInterpreter ci) {
		String strTimestamp = ci.nextArgument();
		Long timestamp = null;
		if (strTimestamp != null) {
			try {
				timestamp = Long.parseLong(strTimestamp);
				if (timestamp < 0)
					timestamp = null;
			} catch (Exception e) {
				timestamp = null;
			}
		}
		return timestamp;
	}

	private Integer getResolution(CommandInterpreter ci) {
		String strResolution = ci.nextArgument();
		Integer resolution = null;
		if (strResolution != null) {
			try {
				resolution = Integer.parseInt(strResolution);
				if (resolution != ESPService.HOUR_RESOLUTION && resolution != ESPService.DAY_RESOLUTION
						&& resolution != ESPService.MONTH_RESOLUTION)
					resolution = null;
			} catch (Exception e) {
				resolution = null;
			}
		}
		return resolution;
	}

	private Integer getWeekDay(CommandInterpreter ci) {
		String strWeekDay = ci.nextArgument();
		Integer weekDay = null;
		if (strWeekDay != null) {
			try {
				weekDay = new Integer(Integer.parseInt(strWeekDay));
				if (weekDay != null && weekDay.intValue() < 1 || weekDay.intValue() > 7)
					weekDay = null;
			} catch (Exception e) {
				weekDay = null;
			}
		}
		return weekDay;
	}

	public void setESPService(ESPService espService) {
		this.espService = espService;
	}

	public void unsetESPService(ESPService espService) {
		this.espService = null;
	}

	public void _esp(CommandInterpreter ci) {
		String command = ci.nextArgument();
		Method method = null;

		try {
			method = this.getClass().getMethod("_" + command, new Class[] { CommandInterpreter.class });
		} catch (SecurityException e) {
			ci.println("Invalid hap command");
			return;
		} catch (NoSuchMethodException e) {
			ci.println("Invalid hap command");
			return;
		}

		try {
			method.invoke(this, new Object[] { ci });
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void _log(CommandInterpreter ci) {
		if (!checkESPService(ci))
			return;
		String msg = ci.nextArgument();
		if (msg == null) {
			ci.println("A message must be specified");
			return;
		}
		try {
			espService.sendGuiLog(msg);
			ci.print("Result: ok");
		} catch (ESPException e) {
			e.printStackTrace();
		}
	}

	public void _cgp(CommandInterpreter ci) {
		if (!checkESPService(ci))
			return;
		try {
			ci.println("Initial configuration time: " + espService.getInitialConfigurationTime());
			ci.println("Configuration parameters: " + espService.getCurrentConfiguration());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void _csp(CommandInterpreter ci) {
		if (!checkESPService(ci))
			return;
		Float instantaneousPowerLimit = parseFloat(ci);
		Float peakProducedPower = parseFloat(ci);
		
		ESPConfigParameters configParameters = instantaneousPowerLimit != null ? new ESPConfigParameters(
				instantaneousPowerLimit.floatValue(), peakProducedPower == null ? 
						ESPConfigParameters.DEFAULT_PEAK_PRODUCED_POWER : peakProducedPower.floatValue()) : null;
		try {
			espService.setConfiguration(configParameters);
			ci.print("Configuration updated: " + configParameters);
		} catch (ESPException e) {
			e.printStackTrace();
		}
	}

	public void _gtp(CommandInterpreter ci) {
		if (!checkESPService(ci))
			return;
		float result;
		try {
			result = espService.getTotalInstantaneousPowerFloatValue();
			ci.print("Result: " + result);
		} catch (ESPException e) {
			e.printStackTrace();
		}
	}

	public void _gip(CommandInterpreter ci) {
		if (!checkESPService(ci))
			return;
		String appliancePid = ci.nextArgument();
		float result;
		try {
			result = espService.getInstantaneousPowerFloatValue(appliancePid);
			ci.print("Result: " + result);
		} catch (ESPException e) {
			e.printStackTrace();
		}
	}

	public void _gge(CommandInterpreter ci) {
		if (!checkESPService(ci))
			return;
		Long startTime = getTimestamp(ci);
		if (startTime == null) {
			ci.println("Invalid start time");
			return;
		}
		Long endTime = getTimestamp(ci);
		if (endTime == null) {
			ci.println("Invalid end time");
			return;
		}
		Integer resolution = getResolution(ci);
		if (resolution == null) {
			ci.println("Invalid resolution (1=hour, 2=day, 3=month)");
			return;
		}
		try {
			List<Float> result = espService.getProducedEnergy(startTime.longValue(), endTime.longValue(), resolution.intValue());
			if (result != null)
				ci.println("Result: " + result);
			else
				ci.println("No result");			
		} catch (ESPException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void _gse(CommandInterpreter ci) {
		if (!checkESPService(ci))
			return;
		Long startTime = getTimestamp(ci);
		if (startTime == null) {
			ci.println("Invalid start time");
			return;
		}
		Long endTime = getTimestamp(ci);
		if (endTime == null) {
			ci.println("Invalid end time");
			return;
		}
		Integer resolution = getResolution(ci);
		if (resolution == null) {
			ci.println("Invalid resolution (1=hour, 2=day, 3=month)");
			return;
		}
		try {
			List<Float> result = espService.getSoldEnergy(startTime.longValue(), endTime.longValue(), resolution.intValue());
			if (result != null)
				ci.println("Result: " + result);
			else
				ci.println("No result");			
		} catch (ESPException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void _gev(CommandInterpreter ci) {
		if (!checkESPService(ci))
			return;
		Long startTime = getTimestamp(ci);
		if (startTime == null) {
			ci.println("Invalid start time");
			return;
		}
		Long endTime = getTimestamp(ci);
		if (endTime == null) {
			ci.println("Invalid end time");
			return;
		}
		Integer resolution = getResolution(ci);
		if (resolution == null) {
			ci.println("Invalid resolution (1=hour, 2=day, 3=month)");
			return;
		}
		String appliancePid = ci.nextArgument();
		try {
			if (appliancePid != null) {
				List<Float> result = espService.getEnergyConsumption(appliancePid, startTime.longValue(), endTime.longValue(),
						resolution.intValue());
				if (result != null)
					ci.println("Result: " + result);
				else
					ci.println("No result");
			} else {
				Map<String, List<Float>> result = espService.getEnergyConsumption(startTime.longValue(), endTime.longValue(),
						resolution.intValue());
				if (result != null)
					ci.println("Result: " + result);
				else
					ci.println("No result");
			}
		} catch (ESPException e) {
			e.printStackTrace();
			return;
		}

	}

	public void _gcv(CommandInterpreter ci) {
		if (!checkESPService(ci))
			return;
		Long startTime = getTimestamp(ci);
		if (startTime == null) {
			ci.println("Invalid start time");
			return;
		}
		Long endTime = getTimestamp(ci);
		if (endTime == null) {
			ci.println("Invalid start time");
			return;
		}
		Integer resolution = getResolution(ci);
		if (resolution == null) {
			ci.println("Invalid resolution (1=hour, 2=day, 3=month)");
			return;
		}
		String appliancePid = ci.nextArgument();
		try {
			if (appliancePid != null) {
				List<Float> result = espService.getEnergyCost(appliancePid, startTime.longValue(), endTime.longValue(),
						resolution.intValue());
				if (result != null)
					ci.println("Result: " + result);
				else
					ci.println("No result");
			} else {
				Map<String, List<Float>> result = espService.getEnergyCost(startTime.longValue(), endTime.longValue(),
						resolution.intValue());
				if (result != null)
					ci.println("Result: " + result);
				else
					ci.println("No result");
			}
		} catch (ESPException e) {
			e.printStackTrace();
			return;
		}
	}

	public void _gef(CommandInterpreter ci) {
		if (!checkESPService(ci))
			return;
		Integer resolution = getResolution(ci);
		if (resolution == null) {
			ci.println("Invalid resolution (1=hour, 2=day, 3=month)");
			return;
		}
		String appliancePid = ci.nextArgument();
		try {
			Float result = espService.getEnergyConsumptionForecast(appliancePid, resolution);
			if (result != null)
				ci.println("Result: " + result);
			else
				ci.println("No result");
		} catch (ESPException e) {
			e.printStackTrace();
			return;
		}
	}

	public void _gcf(CommandInterpreter ci) {
		if (!checkESPService(ci))
			return;
		Integer resolution = getResolution(ci);
		if (resolution == null) {
			ci.println("Invalid resolution (1=hour, 2=day, 3=month)");
			return;
		}
		String appliancePid = ci.nextArgument();
		try {
			Float result = espService.getEnergyCostForecast(appliancePid, resolution);
			if (result != null)
				ci.println("Result: " + result);
			else
				ci.println("No result");
		} catch (ESPException e) {
			e.printStackTrace();
			return;
		}
	}

	public void _gea(CommandInterpreter ci) {
		if (!checkESPService(ci))
			return;
		Integer weekDay = getWeekDay(ci);
		if (weekDay == null) {
			ci.println("Invalid resolution (1=sunday, 7=saturday)");
			return;
		}
		String appliancePid = ci.nextArgument();
		try {
			List<Float> result = espService.getWeekDayEnergyConsumpionAverage(appliancePid, weekDay);
			if (result != null)
				ci.println("Result: " + result);
			else
				ci.println("No result");
		} catch (ESPException e) {
			e.printStackTrace();
			return;
		}
	}

	public void _gca(CommandInterpreter ci) {
		if (!checkESPService(ci))
			return;
		Integer weekDay = getWeekDay(ci);
		if (weekDay == null) {
			ci.println("Invalid resolution (1=sunday, 7=saturday)");
			return;
		}
		String appliancePid = ci.nextArgument();
		try {
			List<Float> result = espService.getWeekDayEnergyCostAverage(appliancePid, weekDay);
			if (result != null)
				ci.println("Result: " + result);
			else
				ci.println("No result");
		} catch (ESPException e) {
			e.printStackTrace();
			return;
		}
	}

	public String getHelp() {
		String help = "--- Automation@Home - ESP Service ---\n";
		help += "\tesp log <message> - store a gui log message on hap server\n";
		help += "\tesp cgp - print configuration pramaters\n";
		help += "\tesp csp <power limit> <peak produced power> - update current configuration parameters (if no data is specified all configuration parameters are deleted)\n";
		help += "\tesp gtp - retrieve latest total instantaneous power\n";
		help += "\tesp gip <appliance pid> - retrieve latest instantaneous power read from an appliance (from smart info in case no appliance pid is specified)\n";
		help += "\tesp gev <start time> <end time> <resolution> <appliance pid> - retrieve the energy consumption values for a specific appliance pid (all values if no appliance pid is specified)\n";
		help += "\tesp gge <start time> <end time> <resolution> <appliance pid> - retrieve the generated energy values\n";
		help += "\tesp gse <start time> <end time> <resolution> <appliance pid> - retrieve the sold energy values\n";
		help += "\tesp gcv <start time> <end time> <resolution> <appliance pid> - retrieve the energy cost values for a specific appliance pid (all values if no appliance pid is specified)\n";
		help += "\tesp gef <resolution> <appliance pid> - retrieve the energy consumption forecast for a specific appliance pid (smart info forecast if no appliance pid is specified)\n";
		help += "\tesp gcf <resolution> <appliance pid> - retrieve the energy cost forecast for a specific appliance pid (smart info forecast if no appliance pid is specified)\n";
		help += "\tesp gea <week day> <appliance pid> - retrieve the energy consumption week day average value for a specific appliance pid (smart info forecast if no appliance pid is specified)\n";
		help += "\tesp gca <week day> <appliance pid> - retrieve the energy cost week day average value  for a specific appliance pid (smart info forecast if no appliance pid is specified)\n";
		return help;
	}
}
