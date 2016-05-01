package com.cwctravel.eclipse.plugins.javamodule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationListener;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

import com.cwctravel.eclipse.plugins.javamodule.exception.ModuleValidationException;
import com.cwctravel.eclipse.plugins.javamodule.utils.StringUtil;

public class ModulePropertyPage extends PropertyPage {

	private Label moduleConfigSeparator;

	private Text moduleNameText;

	private Text packagePatternText;

	private Button addModuleButton;

	private Label moduleSelectionSeparator;

	private TableViewer modulesViewer;

	private List<ModuleInfo> modules;

	private IProject project;

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		FormLayout layout = new FormLayout();
		composite.setLayout(layout);

		return composite;
	}

	@Override
	protected Control createContents(Composite parent) {
		IAdaptable adaptable = getElement();
		project = adaptable.getAdapter(IProject.class);
		if (project == null) {
			IMarker marker = adaptable.getAdapter(IMarker.class);
			if (marker != null) {
				project = marker.getResource().getProject();
			}
		}

		loadPreferences(project);
		initializeDialogUnits(parent);

		Composite composite = createDefaultComposite(parent);
		addMainSection(composite);
		updateView();
		return composite;
	}

	private void loadPreferences(IProject project) {
		modules = JavaModulePlugin.getDefault().readConfiguration(project);

	}

	private void addMainSection(Composite parent) {
		addModuleConfigSection(parent);
	}

	private void addModuleConfigSection(Composite parent) {
		addModulesSection(parent);
		createModulesViewer(parent);
		createModulesTable(parent);
	}

	private void addModulesSection(final Composite parent) {
		Label groupNameLabel = new Label(parent, SWT.NONE);
		groupNameLabel.setText("Module Name: ");
		FormData fdGroupNameLabel = new FormData(convertWidthInCharsToPixels(20), convertHeightInCharsToPixels(1));
		fdGroupNameLabel.top = new FormAttachment(moduleConfigSeparator, 3, SWT.BOTTOM);
		fdGroupNameLabel.left = new FormAttachment(0, 10);
		groupNameLabel.setLayoutData(fdGroupNameLabel);

		moduleNameText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		FormData fdGroupNameText = new FormData(150, convertHeightInCharsToPixels(1));
		fdGroupNameText.top = new FormAttachment(groupNameLabel, 0, SWT.CENTER);
		fdGroupNameText.left = new FormAttachment(groupNameLabel, 3);
		fdGroupNameText.right = new FormAttachment(100, -5);
		moduleNameText.setLayoutData(fdGroupNameText);

		Label packagePatternLabel = new Label(parent, SWT.NONE);
		packagePatternLabel.setText("Package Pattern: ");
		FormData fdPackagePatternLabel = new FormData(convertWidthInCharsToPixels(20), convertHeightInCharsToPixels(1));
		fdPackagePatternLabel.top = new FormAttachment(moduleNameText, 3, SWT.BOTTOM);
		fdPackagePatternLabel.left = new FormAttachment(0, 10);
		packagePatternLabel.setLayoutData(fdPackagePatternLabel);

		packagePatternText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		FormData fdPackagePatternText = new FormData(150, convertHeightInCharsToPixels(1));
		fdPackagePatternText.top = new FormAttachment(packagePatternLabel, 0, SWT.CENTER);
		fdPackagePatternText.left = new FormAttachment(packagePatternLabel, 3);
		fdPackagePatternText.right = new FormAttachment(100, -5);
		packagePatternText.setLayoutData(fdPackagePatternText);

		addModuleButton = new Button(parent, SWT.PUSH);
		addModuleButton.setText("Add");
		FormData fdModuleButton = new FormData(convertWidthInCharsToPixels(15), 20);
		fdModuleButton.right = new FormAttachment(100, -5);
		fdModuleButton.top = new FormAttachment(packagePatternText, 3, SWT.BOTTOM);
		addModuleButton.setLayoutData(fdModuleButton);
		addModuleButton.setEnabled(false);

		Listener addModuleButtonOnClickEventListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				ModulesContentProvider moduleContentProvider = (ModulesContentProvider) modulesViewer
						.getContentProvider();
				ModuleInfo moduleInfo = new ModuleInfo(moduleNameText.getText());
				moduleInfo.setPackagePattern(packagePatternText.getText());
				moduleContentProvider.addModule(moduleInfo);
				modulesViewer.refresh();

			}
		};

		addModuleButton.addListener(SWT.Selection, addModuleButtonOnClickEventListener);

		moduleSelectionSeparator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		FormData fdModuleSelectionSeparator = new FormData(10, 10);
		fdModuleSelectionSeparator.left = new FormAttachment(0, 5);
		fdModuleSelectionSeparator.right = new FormAttachment(100, -5);
		fdModuleSelectionSeparator.top = new FormAttachment(addModuleButton, 10, SWT.BOTTOM);
		moduleSelectionSeparator.setLayoutData(fdModuleSelectionSeparator);
	}

	private TableViewer createModulesViewer(Composite parent) {
		modulesViewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.CHECK);
		ModulesContentProvider moduleContentProvider = new ModulesContentProvider(new ArrayList<ModuleInfo>());
		modulesViewer.setContentProvider(moduleContentProvider);

		final Table moduleTable = modulesViewer.getTable();
		moduleTable.setHeaderVisible(true);
		moduleTable.setLinesVisible(true);

		TableViewerColumn moduleViewerColumn1 = new TableViewerColumn(modulesViewer, SWT.NONE);
		moduleViewerColumn1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element != null) {
					ModuleInfo moduleInfo = (ModuleInfo) element;
					return moduleInfo.getName();
				}
				return null;
			}
		});

		moduleViewerColumn1.setEditingSupport(new ModuleEditingSupport(modulesViewer, true));

		TableColumn moduleTableColumn1 = moduleViewerColumn1.getColumn();
		moduleTableColumn1.setText("Module Name");
		moduleTableColumn1.setWidth(150);
		moduleTableColumn1.setResizable(true);

		TableViewerColumn moduleViewerColumn2 = new TableViewerColumn(modulesViewer, SWT.NONE);
		moduleViewerColumn2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element != null) {
					ModuleInfo moduleInfo = (ModuleInfo) element;
					return moduleInfo.getPackagePattern();
				}
				return null;
			}
		});

		moduleViewerColumn2.setEditingSupport(new ModuleEditingSupport(modulesViewer, false));
		TableColumn moduleTableColumn2 = moduleViewerColumn2.getColumn();
		moduleTableColumn2.setText("Package Pattern");
		moduleTableColumn2.setWidth(490);
		moduleTableColumn2.setResizable(true);

		modulesViewer.getColumnViewerEditor().addEditorActivationListener(new ColumnViewerEditorActivationListener() {

			@Override
			public void beforeEditorDeactivated(ColumnViewerEditorDeactivationEvent event) {
			}

			@Override
			public void beforeEditorActivated(ColumnViewerEditorActivationEvent event) {
			}

			@Override
			public void afterEditorDeactivated(ColumnViewerEditorDeactivationEvent event) {
				validate();
			}

			@Override
			public void afterEditorActivated(ColumnViewerEditorActivationEvent event) {
			}
		});
		modulesViewer.setInput(moduleContentProvider.getModules());

		return modulesViewer;
	}

	private void createModulesTable(final Composite parent) {
		final Table modulesTable = modulesViewer.getTable();
		FormData fdModulesTable = new FormData(750, 100);
		fdModulesTable.top = new FormAttachment(moduleSelectionSeparator, 3, SWT.BOTTOM);
		fdModulesTable.left = new FormAttachment(0, 5);
		fdModulesTable.right = new FormAttachment(100, -5);
		fdModulesTable.bottom = new FormAttachment(100, -35);
		modulesTable.setLayoutData(fdModulesTable);

		final Button moveModulesTopButton = new Button(parent, SWT.PUSH);
		moveModulesTopButton.setText("Move Top");
		moveModulesTopButton.setEnabled(false);
		FormData fdMoveResourcesTopButtonButton = new FormData(convertWidthInCharsToPixels(15), 20);
		fdMoveResourcesTopButtonButton.left = new FormAttachment(0, 5);
		fdMoveResourcesTopButtonButton.top = new FormAttachment(modulesTable, 10, SWT.BOTTOM);
		moveModulesTopButton.setLayoutData(fdMoveResourcesTopButtonButton);

		final Button moveModulesUpButton = new Button(parent, SWT.PUSH);
		moveModulesUpButton.setText("Move Up");
		moveModulesUpButton.setEnabled(false);
		FormData fdMoveResourcesUpButton = new FormData(convertWidthInCharsToPixels(15), 20);
		fdMoveResourcesUpButton.left = new FormAttachment(moveModulesTopButton, 5);
		fdMoveResourcesUpButton.top = new FormAttachment(modulesTable, 10, SWT.BOTTOM);
		moveModulesUpButton.setLayoutData(fdMoveResourcesUpButton);

		final Button moveModulesDownButton = new Button(parent, SWT.PUSH);
		moveModulesDownButton.setText("Move Down");
		moveModulesDownButton.setEnabled(false);
		FormData fdMoveResourcesDownButton = new FormData(convertWidthInCharsToPixels(15), 20);
		fdMoveResourcesDownButton.left = new FormAttachment(moveModulesUpButton, 5);
		fdMoveResourcesDownButton.top = new FormAttachment(modulesTable, 10, SWT.BOTTOM);
		moveModulesDownButton.setLayoutData(fdMoveResourcesDownButton);

		final Button moveModulesBottomButton = new Button(parent, SWT.PUSH);
		moveModulesBottomButton.setText("Move Bottom");
		moveModulesBottomButton.setEnabled(false);
		FormData fdMoveResourcesBottomButton = new FormData(convertWidthInCharsToPixels(15), 20);
		fdMoveResourcesBottomButton.left = new FormAttachment(moveModulesDownButton, 5);
		fdMoveResourcesBottomButton.top = new FormAttachment(modulesTable, 10, SWT.BOTTOM);
		moveModulesBottomButton.setLayoutData(fdMoveResourcesBottomButton);

		final Button removeModulesButton = new Button(parent, SWT.PUSH);
		removeModulesButton.setText("Remove");
		FormData fdRemoveModulesButton = new FormData(convertWidthInCharsToPixels(15), 20);
		fdRemoveModulesButton.left = new FormAttachment(moveModulesBottomButton, 5);
		fdRemoveModulesButton.top = new FormAttachment(modulesTable, 10, SWT.BOTTOM);
		removeModulesButton.setLayoutData(fdRemoveModulesButton);
		removeModulesButton.setEnabled(false);

		final Button[] moveModulesButtons = new Button[] { moveModulesTopButton, moveModulesUpButton,
				moveModulesDownButton, moveModulesBottomButton };
		Listener moveModulesTopButtonOnClickEventListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				int[] selectedIndices = modulesTable.getSelectionIndices();
				if (selectedIndices != null) {
					ModulesContentProvider modulesContentProvider = (ModulesContentProvider) modulesViewer
							.getContentProvider();
					List<ModuleInfo> modules = modulesContentProvider.getModules();

					Set<ModuleInfo> checkedModules = getCheckedModules(modulesTable, modules);

					Arrays.sort(selectedIndices);

					List<ModuleInfo> removedModules = new ArrayList<ModuleInfo>();
					for (int i = selectedIndices.length - 1; i >= 0; i--) {
						removedModules.add(0, modules.remove(selectedIndices[i]));
					}
					modules.addAll(0, removedModules);

					modulesViewer.refresh();

					checkItems(modulesTable, modules, checkedModules);

					setMoveModulesButtonsEnablement(modulesTable.getSelectionIndices(), modulesTable.getItemCount(),
							moveModulesButtons);
				}
			}

		};

		moveModulesTopButton.addListener(SWT.Selection, moveModulesTopButtonOnClickEventListener);

		Listener moveModulesUpButtonOnClickEventListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				int[] selectedIndices = modulesTable.getSelectionIndices();
				if (selectedIndices != null) {
					ModulesContentProvider modulesContentProvider = (ModulesContentProvider) modulesViewer
							.getContentProvider();
					List<ModuleInfo> modules = modulesContentProvider.getModules();

					Set<ModuleInfo> checkedModules = getCheckedModules(modulesTable, modules);

					Arrays.sort(selectedIndices);

					for (int i = 0; i < selectedIndices.length; i++) {
						int currentIndex = selectedIndices[i];
						int previousIndex = (currentIndex - 1);
						if (previousIndex >= 0) {
							ModuleInfo previousResource = modules.get(previousIndex);
							ModuleInfo currentResource = modules.get(currentIndex);
							modules.set(previousIndex, currentResource);
							modules.set(currentIndex, previousResource);
						}
					}

					modulesViewer.refresh();

					checkItems(modulesTable, modules, checkedModules);

					setMoveModulesButtonsEnablement(modulesTable.getSelectionIndices(), modulesTable.getItemCount(),
							moveModulesButtons);
				}
			}

		};

		moveModulesUpButton.addListener(SWT.Selection, moveModulesUpButtonOnClickEventListener);

		Listener moveResourcesDownButtonOnClickEventListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				int[] selectedIndices = modulesTable.getSelectionIndices();
				if (selectedIndices != null) {
					ModulesContentProvider modulesContentProvider = (ModulesContentProvider) modulesViewer
							.getContentProvider();
					List<ModuleInfo> modules = modulesContentProvider.getModules();

					Set<ModuleInfo> checkedModules = getCheckedModules(modulesTable, modules);

					Arrays.sort(selectedIndices);

					int itemCount = modulesTable.getItemCount();

					for (int i = selectedIndices.length - 1; i >= 0; i--) {
						int currentIndex = selectedIndices[i];
						int nextIndex = (currentIndex + 1);
						if (nextIndex < itemCount) {
							ModuleInfo previousResource = modules.get(nextIndex);
							ModuleInfo currentResource = modules.get(currentIndex);
							modules.set(nextIndex, currentResource);
							modules.set(currentIndex, previousResource);
						}
					}

					modulesViewer.refresh();

					checkItems(modulesTable, modules, checkedModules);

					setMoveModulesButtonsEnablement(modulesTable.getSelectionIndices(), itemCount, moveModulesButtons);
				}
			}

		};

		moveModulesDownButton.addListener(SWT.Selection, moveResourcesDownButtonOnClickEventListener);

		Listener moveModulesBottomButtonOnClickEventListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				int[] selectedIndices = modulesTable.getSelectionIndices();
				if (selectedIndices != null) {
					ModulesContentProvider modulesContentProvider = (ModulesContentProvider) modulesViewer
							.getContentProvider();
					List<ModuleInfo> modules = modulesContentProvider.getModules();

					Set<ModuleInfo> checkedResources = getCheckedModules(modulesTable, modules);

					Arrays.sort(selectedIndices);

					List<ModuleInfo> removedModules = new ArrayList<ModuleInfo>();
					for (int i = selectedIndices.length - 1; i >= 0; i--) {
						removedModules.add(0, modules.remove(selectedIndices[i]));
					}
					modules.addAll(removedModules);

					modulesViewer.refresh();

					checkItems(modulesTable, modules, checkedResources);

					setMoveModulesButtonsEnablement(modulesTable.getSelectionIndices(), modulesTable.getItemCount(),
							moveModulesButtons);
				}
			}

		};

		moveModulesBottomButton.addListener(SWT.Selection, moveModulesBottomButtonOnClickEventListener);

		packagePatternText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				addModuleButton.setEnabled(false);

				String packagePatternStr = packagePatternText.getText();
				if (!StringUtil.isEmpty(packagePatternStr)) {
					packagePatternText.setData(packagePatternStr);
					addModuleButton.setEnabled(true);
				}
			}
		});

		Listener removeModulesButtonButtonOnClickEventListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				ModulesContentProvider moduleContentProvider = (ModulesContentProvider) modulesViewer
						.getContentProvider();
				List<Integer> checkedItemIndices = new ArrayList<Integer>();

				TableItem[] items = modulesTable.getItems();
				if (items != null) {
					for (int i = 0; i < items.length; i++) {
						if (items[i].getChecked()) {
							checkedItemIndices.add(i);
						}
					}
				}

				if (!checkedItemIndices.isEmpty()) {
					moduleContentProvider.removeModules(checkedItemIndices);
					modulesViewer.refresh();

					removeModulesButton.setEnabled(false);
				}
			}
		};

		removeModulesButton.addListener(SWT.Selection, removeModulesButtonButtonOnClickEventListener);

		Listener modulesTableOnSelectEventListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				setMoveModulesButtonsEnablement(modulesTable.getSelectionIndices(), modulesTable.getItemCount(),
						moveModulesButtons);
				removeModulesButton.setEnabled(isAnyItemChecked(modulesTable));
			}

		};

		modulesTable.addListener(SWT.Selection, modulesTableOnSelectEventListener);
	}

	private List<Integer> getCheckedItemIndices(final Table modulesTable) {
		List<Integer> result = new ArrayList<Integer>();
		TableItem[] items = modulesTable.getItems();
		if (items != null) {
			for (int i = 0; i < items.length; i++) {
				if (items[i].getChecked()) {
					result.add(i);
				}
			}
		}
		return result;
	}

	private Set<ModuleInfo> getCheckedModules(final Table modulesTable, List<ModuleInfo> modules) {
		List<Integer> checkedItemIndices = getCheckedItemIndices(modulesTable);
		Set<ModuleInfo> result = new HashSet<ModuleInfo>();
		for (int checkedItemIndex : checkedItemIndices) {
			result.add(modules.get(checkedItemIndex));
		}

		return result;
	}

	private void checkItems(final Table modulesTable, List<ModuleInfo> modules, Set<ModuleInfo> checkedModules) {
		TableItem[] items = modulesTable.getItems();
		if (items != null) {
			for (int i = 0; i < items.length; i++) {
				if (checkedModules.contains(modules.get(i))) {
					items[i].setChecked(true);
				} else {
					items[i].setChecked(false);
				}
			}
		}
	}

	private boolean isAnyItemChecked(final Table table) {
		boolean isAnyItemChecked = false;
		TableItem[] items = table.getItems();
		if (items != null) {
			for (int i = 0; i < items.length; i++) {
				if (items[i].getChecked()) {
					isAnyItemChecked = true;
					break;
				}
			}
		}
		return isAnyItemChecked;
	}

	private void setMoveModulesButtonsEnablement(int[] selectedIndices, int itemCount,
			final Button[] moveModulesButtons) {
		if (allowSelectionToMoveUp(selectedIndices)) {
			moveModulesButtons[0].setEnabled(true);
			moveModulesButtons[1].setEnabled(true);
		} else {
			moveModulesButtons[0].setEnabled(false);
			moveModulesButtons[1].setEnabled(false);
		}

		if (allowSelectionToMoveDown(selectedIndices, itemCount)) {
			moveModulesButtons[2].setEnabled(true);
			moveModulesButtons[3].setEnabled(true);
		} else {
			moveModulesButtons[2].setEnabled(false);
			moveModulesButtons[3].setEnabled(false);
		}
	}

	private boolean allowSelectionToMoveUp(int[] selectedIndices) {
		boolean result = false;
		Arrays.sort(selectedIndices);
		for (int i = 0; i < selectedIndices.length; i++) {
			if (selectedIndices[i] != i) {
				result = true;
			}
		}
		return result;
	}

	private boolean allowSelectionToMoveDown(int[] selectedIndices, int itemCount) {
		boolean result = false;
		Arrays.sort(selectedIndices);
		for (int i = selectedIndices.length - 1, j = 1; i >= 0; i--, j++) {
			if (selectedIndices[i] != itemCount - j) {
				result = true;
			}
		}
		return result;
	}

	private void updateView() {
		ModulesContentProvider modulesContentProvider = new ModulesContentProvider(modules);
		modulesViewer.setContentProvider(modulesContentProvider);
		modulesViewer.setInput(modules);
		validate();
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
	}

	@Override
	protected void performApply() {
		validate();
	}

	private void validate() {
		ModulesContentProvider modulesContentProvider = (ModulesContentProvider) modulesViewer.getContentProvider();
		modules = modulesContentProvider.getModules();
		try {
			JavaModulePlugin.getDefault().validateConfiguration(modules);
			setErrorMessage(null);
		} catch (ModuleValidationException e) {
			setErrorMessage(e.getMessage());
		}
	}

	@Override
	public boolean performOk() {
		try {
			JavaModulePlugin.getDefault().saveConfiguration(project, modules);
		} catch (BackingStoreException e) {
			JavaModulePlugin.log(IStatus.ERROR, e.getMessage(), e);
			return false;
		} catch (ModuleValidationException e) {
			setErrorMessage(e.getMessage());
			return false;
		}
		return true;
	}
}
