package com.cwctravel.eclipse.plugins.javamodule;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;

/**
 * Provides a label and icon for objects of type {@link Module}.
 * 
 * @since 3.2
 */
public class ModuleLabelProvider extends LabelProvider implements ILabelProvider, IDescriptionProvider {
	private ILabelDecorator fLabelDecorator = null;

	public ModuleLabelProvider() {
		fLabelDecorator = new ModuleProblemsLabelDecorator();
	}

	public Image getImage(Object element) {
		Image result = null;
		if(element instanceof Module) {
			Image image = JavaModulePlugin.getDefault().getImageRegistry().get(JavaModulePlugin.MODULE_ICON_ID);
			Image decorated = fLabelDecorator.decorateImage(image, element);

			if(decorated != null) {
				result = decorated;
			}
			else {
				result = image;
			}
		}
		return result;
	}

	public String getText(Object element) {
		if(element instanceof Module) {
			Module data = (Module)element;
			return data.getName();
		}
		return null;
	}

	public String getDescription(Object anElement) {
		if(anElement instanceof Module) {
			Module data = (Module)anElement;
			return data.getName();
		}
		return null;
	}

}
