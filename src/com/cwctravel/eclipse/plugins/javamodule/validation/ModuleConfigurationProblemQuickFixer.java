package com.cwctravel.eclipse.plugins.javamodule.validation;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class ModuleConfigurationProblemQuickFixer implements IMarkerResolutionGenerator {

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		return new IMarkerResolution[] { new ModuleConfigurationProblemQuickFix() };
	}

}
