/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
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
package org.energy_home.jemma.osgi.utils.equinox.console;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Dictionary;


import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.Bundle;

public class TestCommandProvider {
	public static Object getObjectValue(Class objectType, String stringValue) {
		Object objectValue = null;
		if (objectType.isPrimitive()) {
			if (objectType.equals(boolean.class))
				objectValue = Boolean.parseBoolean(stringValue);
			else if (objectType.equals(byte.class))
				objectValue = Byte.parseByte(stringValue);
			else if (objectType.equals(short.class))
				objectValue = Short.parseShort(stringValue);
			else if (objectType.equals(int.class))
				objectValue = Integer.parseInt(stringValue);
			else if (objectType.equals(long.class))
				objectValue = Long.parseLong(stringValue);
			else if (objectType.equals(float.class))
				objectValue = Float.parseFloat(stringValue);
			else if (objectType.equals(double.class))
				objectValue = Double.parseDouble(stringValue);
		} else {
			Constructor constructor;
			try {
				constructor = objectType.getConstructor(String.class);
				objectValue = constructor.newInstance(stringValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return objectValue;
	}

	private class CommandInterpreterProxy implements CommandInterpreter {
		private String[] commands;
		private CommandInterpreter ci;
		private String[] currentCommandParams;
		private int currentCommandParamsIndex;
		
		CommandInterpreterProxy(String[] commands, CommandInterpreter ci) {
			this.commands = commands;
			this.ci = ci;
		}
		
		public void execAllCommands() {
			String delims = "[ ]+";
			String[] commandLine = null;
			Class commandProviderClass= commandProvider.getClass();
			if (commands == null)
				throw new IllegalStateException("Null command list");
			for (int i = 0; i < commands.length; i++)  {
				commandLine = commands[i].split(delims);	
				if (commandLine == null || commandLine.length < 2)
					ci.println("Invalid command " + i);
				else {
					ci.println("\n\n\nExecuting command: \"" + commands[i] + "\"");
				}
				try {
					currentCommandParams = new String[commandLine.length - 2];
			        System.arraycopy(commandLine, 2, currentCommandParams, 0, commandLine.length - 2);
			        // Old code
					// currentCommandParams = Arrays.copyOfRange(commandLine, 2, commandLine.length);
			        
					currentCommandParamsIndex = 0;
					commandProviderClass.getMethod("_"+commandLine[1], CommandInterpreter.class).invoke(commandProvider, this);
				} catch (Exception e) {
					e.printStackTrace();
					ci.println("Error while invoking command " + i);
				}
			}
		} 
		
		public Object execute(String arg0) {
			return ci.execute(arg0);
		}

		public String nextArgument() {
			if (currentCommandParamsIndex >= currentCommandParams.length)
				return null;
			String argument = currentCommandParams[currentCommandParamsIndex];
			currentCommandParamsIndex++;
			return argument;
		}

		public void print(Object arg0) {
			ci.print(arg0);
		}

		public void printBundleResource(Bundle arg0, String arg1) {
			ci.printBundleResource(arg0, arg1);
		}

		public void printDictionary(Dictionary arg0, String arg1) {
			ci.printDictionary(arg0, arg1);
		}

		public void printStackTrace(Throwable arg0) {
			ci.printStackTrace(arg0);
		}

		public void println() {
			ci.println();
		}

		public void println(Object arg0) {
			ci.println(arg0);
		}

	}
	
	protected String[] testCommands;
	private CommandProvider commandProvider;
	
	public TestCommandProvider(CommandProvider commandProvider, String[] testCommands) {
		this.commandProvider = commandProvider;
		this.testCommands = testCommands;
	}
	
	public void test (CommandInterpreter ci) {
		CommandInterpreterProxy proxy = new CommandInterpreterProxy(testCommands, ci);
		proxy.execAllCommands();
	}
}
