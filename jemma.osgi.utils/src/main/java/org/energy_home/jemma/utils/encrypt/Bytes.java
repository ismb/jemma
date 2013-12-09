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
package org.energy_home.jemma.utils.encrypt;

public class Bytes {
	  private final static char[] HEX = {
  	    '0', '1', '2', '3', '4', '5', '6', '7',
  	    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
  	  };

  	  public static String toHex(byte[] bytes) {
  	    StringBuilder buffer;

  	    buffer = new StringBuilder();
  	    for (byte b : bytes) {
  	      buffer.append(HEX[(b >> 4) & 0xf]);
  	      buffer.append(HEX[b & 0xf]);
  	    }
  	    return buffer.toString();
  	  }

  	  public static byte[] fromHex(String hex) throws NumberFormatException {
  	    char[] chars;
  	    char c;
  	    int i;
  	    int j;
  	    byte[] bytes;
  	    byte b;

  	    chars = hex.toUpperCase().toCharArray();

  	    if (chars.length % 2 != 0) {
  	      throw new NumberFormatException("Incomplete hex value");
  	    }

  	    bytes = new byte[chars.length / 2];
  	    b = 0;
  	    j = 0;
  	    for (i = 0; i < chars.length; i++) {
  	      c = chars[i];
  	      if (c >= '0' && c <= '9') {
  	        b = (byte) ((b << 4) | (0xff & (c - '0')));
  	      } else if (c >= 'A' && c <= 'F') {
  	        b = (byte) ((b << 4) | (0xff & (c - 'A' + 10)));
  	      } else {
  	        throw new NumberFormatException("Invalid hex character: " + c);
  	      }
  	      if ((i + 1) % 2 == 0) {
  	        bytes[j++] = b;
  	        b = 0;
  	      }
  	    }

  	    return bytes;
  	  }
}
