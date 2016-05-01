package com.cwctravel.eclipse.plugins.javamodule;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jdt.ui.ProblemsLabelDecorator;

public class ModuleProblemsLabelDecorator extends ProblemsLabelDecorator {

	protected int computeAdornmentFlags(Object obj) {
		int result = 0;
		if (obj instanceof Module) {
			Module module = (Module) obj;
			int max = 0;
			Object[] children = module.getChildren();
			if (children != null) {
				for (Object child : children) {
					int val = super.computeAdornmentFlags(child);
					max = Math.max(max, val);
				}
			}
			result = max;
			if (result != JavaElementImageDescriptor.ERROR) {
				try {
					IProject project = module.getProject();
					IMarker[] markers = project.findMarkers(ModuleConstants.PROBLEM_MARKER_ID, false,
							IResource.DEPTH_ZERO);
					if (markers != null) {
						for (IMarker marker : markers) {
							String moduleName = marker.getAttribute("moduleName", null);
							if (module.getName().equals(moduleName)) {
								result = JavaElementImageDescriptor.ERROR;
							}
						}
					}
				} catch (CoreException e) {
					JavaModulePlugin.log(IStatus.ERROR, e.getMessage(), e);
				}
			}

		} else {
			result = super.computeAdornmentFlags(obj);
		}

		return result;
	}
}
