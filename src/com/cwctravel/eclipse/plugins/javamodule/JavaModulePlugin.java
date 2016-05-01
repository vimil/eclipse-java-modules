package com.cwctravel.eclipse.plugins.javamodule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

import com.cwctravel.eclipse.plugins.javamodule.exception.ModuleValidationException;
import com.cwctravel.eclipse.plugins.javamodule.exception.PackagePatternValidationException;
import com.cwctravel.eclipse.plugins.javamodule.validation.PackagePatternInfo;

/**
 * The activator class controls the plug-in life cycle
 */
public class JavaModulePlugin extends AbstractUIPlugin {

	private final class ModuleConfigurationValidationJob extends WorkspaceJob {
		private IProject project;

		private ModuleConfigurationValidationJob(IProject project, String name) {
			super(name);
			this.project = project;
		}

		@Override
		public IStatus runInWorkspace(IProgressMonitor monitor) {
			List<ModuleInfo> moduleInfos = readModuleInfosForProject(project);
			try {
				try {
					if (project.isOpen()) {
						project.deleteMarkers(ModuleConstants.PROBLEM_MARKER_ID, false, IResource.DEPTH_ZERO);
						validateConfiguration(moduleInfos);
					}
				} catch (ModuleValidationException mVE) {
					IMarker marker = project.createMarker(ModuleConstants.PROBLEM_MARKER_ID);
					marker.setAttribute(IMarker.MESSAGE, mVE.getMessage());
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					marker.setAttribute("moduleName", mVE.getModuleName());

				}
			} catch (CoreException e) {
				log(IStatus.ERROR, e.getMessage(), e);
			}
			return Status.OK_STATUS;
		}
	}

	public static final String MODULE_ICON_ID = "com.cwctravel.eclipse.plugins.javamodule.icons.module";

	// The shared instance
	private static JavaModulePlugin plugin;

	private Map<String, List<Module>> modulesMap;

	private IResourceChangeListener projectChangeListener;

	public JavaModulePlugin() {
		modulesMap = Collections.synchronizedMap(new HashMap<String, List<Module>>());
		plugin = this;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		super.initializeImageRegistry(registry);
		Bundle bundle = Platform.getBundle("com.cwctravel.eclipse.plugins.javamodule");

		ImageDescriptor tomcatContextImage = ImageDescriptor
				.createFromURL(FileLocator.find(bundle, new Path("icons/module-icon.png"), null));
		registry.put(MODULE_ICON_ID, tomcatContextImage);
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		projectChangeListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				if (event != null && event.getDelta() != null) {
					try {
						final List<IProject> affectedProjects = new ArrayList<IProject>();
						event.getDelta().accept(new IResourceDeltaVisitor() {
							public boolean visit(final IResourceDelta delta) {
								IResource resource = delta.getResource();
								if ((resource.getType() & IResource.PROJECT) != 0) {
									IProject project = (IProject) resource;
									if (modulesMap.remove(project.getName()) != null) {
										affectedProjects.add(project);
									}
								} else if ((resource.getType() & IResource.FILE) != 0
										&& resource.getName().equals(ModuleConstants.PLUGIN_ID + ".prefs")) {
									WorkspaceJob workspaceJob = new ModuleConfigurationValidationJob(
											resource.getProject(), "Validating Module configuration");
									workspaceJob.schedule();
								}
								return true;
							}
						});

						if (!affectedProjects.isEmpty()) {
							UIJob job = new UIJob("Refreshing Projects") {
								@Override
								public IStatus runInUIThread(IProgressMonitor monitor) {
									IWorkbenchWindow[] workbenchWIndows = PlatformUI.getWorkbench()
											.getWorkbenchWindows();
									if (workbenchWIndows != null) {
										for (IWorkbenchWindow workbenchWIndow : workbenchWIndows) {
											IWorkbenchPage[] workbenchPages = workbenchWIndow.getPages();
											if (workbenchPages != null) {
												for (IWorkbenchPage workbenchPage : workbenchPages) {
													IViewReference[] viewReferences = workbenchPage.getViewReferences();
													if (viewReferences != null) {
														for (IViewReference viewReference : viewReferences) {
															IViewPart viewPart = viewReference.getView(false);
															if (viewPart instanceof ProjectExplorer) {
																ProjectExplorer projectExplorer = (ProjectExplorer) viewPart;
																for (IProject project : affectedProjects) {
																	CommonViewer commonViewer = projectExplorer
																			.getCommonViewer();
																	commonViewer.refresh(project, true);
																}
															}
														}
													}
												}
											}
										}
									}
									return Status.OK_STATUS;
								}
							};

							job.schedule();
						}
					} catch (CoreException e) {
						JavaModulePlugin.log(IStatus.ERROR, e.getMessage(), e);
					}
				}
			}
		};
		workspace.addResourceChangeListener(projectChangeListener);

	}

	public List<Module> getModulesForProject(IJavaProject javaProject) {
		List<Module> modules = Collections.emptyList();
		IProject project = javaProject.getProject();
		String projectName = project.getName();
		if (project.isOpen()) {
			modules = modulesMap.get(projectName);
			if (modules == null) {
				modules = new ArrayList<Module>();
				List<ModuleInfo> moduleInfos = readModuleInfosForProject(project);
				for (ModuleInfo moduleInfo : moduleInfos) {
					Module module = new Module(javaProject, moduleInfo.getName());
					module.setPackagePatterns(moduleInfo.getPackagePatterns());
					module.setIndex(moduleInfo.getIndex());
					modules.add(module);
				}
				modulesMap.put(projectName, modules);
			}
		}
		return modules;
	}

	private List<ModuleInfo> readModuleInfosForProject(IProject project) {
		IEclipsePreferences preferences = new ProjectScope(project).getNode(ModuleConstants.PLUGIN_ID);
		ModulePreferenceManager modulePreferenceManager = new ModulePreferenceManager(preferences);
		List<ModuleInfo> moduleInfos = modulePreferenceManager.read();
		return moduleInfos;
	}

	public List<ModuleInfo> readConfiguration(IProject project) {
		IEclipsePreferences preferences = new ProjectScope(project).getNode(ModuleConstants.PLUGIN_ID);
		ModulePreferenceManager modulePreferenceManager = new ModulePreferenceManager(preferences);
		return modulePreferenceManager.read();
	}

	public void saveConfiguration(IProject project, List<ModuleInfo> moduleInfos)
			throws BackingStoreException, ModuleValidationException {
		validateConfiguration(moduleInfos);

		for (int i = 0; i < moduleInfos.size(); i++) {
			ModuleInfo moduleInfo = moduleInfos.get(i);
			moduleInfo.setIndex(i);
		}

		IEclipsePreferences preferences = new ProjectScope(project).getNode(ModuleConstants.PLUGIN_ID);
		ModulePreferenceManager modulePreferenceManager = new ModulePreferenceManager(preferences);
		modulePreferenceManager.store(moduleInfos);
	}

	public void validateConfiguration(List<ModuleInfo> moduleInfos) throws ModuleValidationException {
		if (moduleInfos != null) {
			Map<String, Object> packagePatternMap = new HashMap<String, Object>();
			for (ModuleInfo moduleInfo : moduleInfos) {
				List<String> packagePatterns = moduleInfo.getPackagePatterns();
				for (String packagePattern : packagePatterns) {
					try {
						PackagePatternInfo packagePatternInfo = PackagePatternInfo.parse(packagePattern);
						Object[] existingModuleAndPackagePattern = addPackagePatternToMap(packagePatternMap,
								new Object[] { moduleInfo, packagePatternInfo });
						if (existingModuleAndPackagePattern != null) {
							ModuleInfo otherModuleInfo = (ModuleInfo) existingModuleAndPackagePattern[0];
							PackagePatternInfo otherPackagePatternInfo = (PackagePatternInfo) existingModuleAndPackagePattern[1];
							throw new ModuleValidationException(moduleInfo.getName(),
									String.format(
											"Module %s has package pattern '%s' that conflicts with package pattern '%s' defined in Module %s",
											otherModuleInfo.getName(), otherPackagePatternInfo.getPattern(),
											packagePattern, moduleInfo.getName()));
						}
					} catch (PackagePatternValidationException pPVE) {
						throw new ModuleValidationException(moduleInfo.getName(),
								String.format("Module %s has an invalid  package pattern '%s'", moduleInfo.getName(),
										packagePattern),
								pPVE);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Object[] addPackagePatternToMap(Map<String, Object> packagePatternMap, Object[] moduleAndPackagePattern) {
		PackagePatternInfo packagePatternInfo = (PackagePatternInfo) moduleAndPackagePattern[1];
		Map<String, Object> currentPackagePatternMap = packagePatternMap;
		String[] packagePatternSegments = packagePatternInfo.getSegments();
		for (int i = 0; i < packagePatternSegments.length; i++) {
			String segment = packagePatternSegments[i];
			Object obj = currentPackagePatternMap.get(segment);
			if (obj == null) {
				currentPackagePatternMap = null;
				break;
			} else if (obj instanceof Map) {
				currentPackagePatternMap = (Map<String, Object>) obj;
			} else if (obj instanceof Object[]) {
				return (Object[]) obj;
			}
		}

		if (currentPackagePatternMap != null) {
			Object obj = currentPackagePatternMap.values().iterator().next();
			while (obj instanceof Map) {
				currentPackagePatternMap = (Map<String, Object>) obj;
				obj = currentPackagePatternMap.values().iterator().next();
			}
			return (Object[]) obj;
		}

		currentPackagePatternMap = packagePatternMap;
		for (int i = 0; i < packagePatternSegments.length; i++) {
			String segment = packagePatternSegments[i];
			Object obj = currentPackagePatternMap.get(segment);
			if (obj == null) {
				if (i < packagePatternSegments.length - 1) {
					Map<String, Object> map = new HashMap<String, Object>();
					currentPackagePatternMap.put(segment, map);
					currentPackagePatternMap = map;
				} else {
					currentPackagePatternMap.put(segment, moduleAndPackagePattern);
				}
			} else {
				currentPackagePatternMap = (Map<String, Object>) obj;
			}
		}
		return null;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static JavaModulePlugin getDefault() {
		return plugin;
	}

	public static void log(int severity, String message, Throwable t) {
		getDefault().getLog().log(new Status(severity, ModuleConstants.PLUGIN_ID, message, t));
	}

}
