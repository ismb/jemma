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
package org.energy_home.jemma.ah.internal.hac.lib;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.ICategory;
import org.energy_home.jemma.ah.hac.lib.ext.Category;

public class Categories {
	private Vector categories = new Vector();
	private Hashtable name2category = new Hashtable();
	private Hashtable pid2category = new Hashtable();

	public ICategory[] getCategories() {
		if (categories == null || categories.size() == 0)
			return null;
		ICategory[] locationArray = new ICategory[categories.size()];
		return (ICategory[]) categories.toArray(locationArray);
	}

	public void add(ICategory category) throws HacException {
		if (pid2category.get(category.getPid()) == null) {
			name2category.put(category.getName(), category);
			pid2category.put(category.getPid(), category);
			categories.add(category);
		} else {
			throw new HacException("Duplicate category exception");
		}
	}
	
	public void remove(String categoryPid) throws HacException {
		Category category = this.getCategoryByPid(categoryPid);
		if (category != null) {
			pid2category.remove(categoryPid);
			name2category.remove(category.getName());
			categories.remove(category);
		} else {
			throw new HacException("Unknown category pid");
		}
	}

	public void clear() {
		this.categories.clear();
		this.name2category.clear();
		this.pid2category.clear();
	}

	public Category getCategoryByPid(String categoryPid) {
		return (Category) this.pid2category.get(categoryPid);
	}

	public Iterator iterator() {
		return categories.iterator();
	}
}
