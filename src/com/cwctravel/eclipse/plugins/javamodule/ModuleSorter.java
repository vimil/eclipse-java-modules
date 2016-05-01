package com.cwctravel.eclipse.plugins.javamodule;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class ModuleSorter extends ViewerSorter {

	private final ModuleComparator fComparator;

	public ModuleSorter() {
		super(null);
		fComparator = new ModuleComparator();
	}

	public int category(Object element) {
		return fComparator.category(element);
	}

	public int compare(Viewer viewer, Object e1, Object e2) {
		return fComparator.compare(viewer, e1, e2);
	}
}
