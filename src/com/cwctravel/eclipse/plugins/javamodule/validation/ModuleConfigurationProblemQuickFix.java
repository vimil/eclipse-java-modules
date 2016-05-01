package com.cwctravel.eclipse.plugins.javamodule.validation;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.cwctravel.eclipse.plugins.javamodule.ModuleConstants;

public class ModuleConfigurationProblemQuickFix implements IMarkerResolution {

	@Override
	public String getLabel() {
		return "Fix Module Configuration";
	}

	@Override
	public void run(IMarker marker) {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		PreferencesUtil.createPropertyDialogOn(shell, marker.getResource(),
				ModuleConstants.MODULE_CONFIGURATION_PROPERTY_PAGE_ID, null, null).open();

	}

}
