package com.cwctravel.eclipse.plugins.javamodule;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class ModuleEditingSupport extends EditingSupport {
	private boolean isName;
	private CellEditor cellEditor;

	ModuleEditingSupport(ColumnViewer viewer, boolean isName) {
		super(viewer);
		this.isName = isName;

		cellEditor = new CellEditor((Composite)viewer.getControl()) {
			private Text editor;

			@Override
			protected Control createControl(Composite parent) {
				editor = new Text(parent, SWT.SINGLE);
				return editor;
			}

			@Override
			protected Object doGetValue() {
				return editor.getText();
			}

			@Override
			protected void doSetFocus() {
				editor.setFocus();
			}

			@Override
			protected void doSetValue(Object value) {
				editor.setText((String)value);
			}

			@Override
			public LayoutData getLayoutData() {
				LayoutData data = new LayoutData();
				data.minimumWidth = 0;
				return data;
			}
		};
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return cellEditor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		String result = "";
		ModuleInfo moduleInfo = (ModuleInfo)element;
		if(moduleInfo != null) {
			if(isName) {
				result = moduleInfo.getName();
			}
			else {
				result = moduleInfo.getPackagePattern();
			}
		}
		return result;
	}

	@Override
	protected void setValue(Object element, Object value) {
		ModuleInfo moduleInfo = (ModuleInfo)element;
		if(moduleInfo != null) {
			String strValue = (String)value;
			if(isName) {
				moduleInfo.setName(strValue);
			}
			else {
				moduleInfo.setPackagePattern(strValue);
			}
			getViewer().refresh(element);
		}
	}
}