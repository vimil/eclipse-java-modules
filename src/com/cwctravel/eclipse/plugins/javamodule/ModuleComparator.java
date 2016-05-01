package com.cwctravel.eclipse.plugins.javamodule;

import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jface.viewers.Viewer;

public class ModuleComparator extends JavaElementComparator{

	public int compare(Viewer viewer, Object e1, Object e2) {
		int result = 0;
		if(e1 instanceof Module && e2 instanceof Module) {
			Module m1 = (Module)e1;
			Module m2 = (Module)e2;
			result = m1.getIndex() - m2.getIndex();
		}else {
			result = super.compare(viewer, e1, e2);
		}
		return result;
	}
}
