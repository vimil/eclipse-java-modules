package com.cwctravel.eclipse.plugins.javamodule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.INavigatorContentService;

public class EmptySourceFolderFilter extends ViewerFilter {
	private final class FileCheckVisitor implements IResourceProxyVisitor {
		private boolean filesPresent = false;

		@Override
		public boolean visit(IResourceProxy proxy) {
			if (proxy.getType() == IResource.FILE) {
				filesPresent = true;
			}
			return true;
		}
	}

	private boolean isStateModelInitialized = false;
	private INavigatorContentService fContentService;
	private IExtensionStateModel fStateModel = null;

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!isStateModelInitialized) {
			initStateModel(viewer);
		}
		if ((fContentService == null) || (fStateModel == null)
				|| !fContentService.isActive("com.cwctravel.eclipse.plugins.javamodule.content")) {
			return true;
		}
		if (element instanceof IPackageFragmentRoot) {
			IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) element;
			try {
				IJavaProject javaProject = packageFragmentRoot.getJavaProject();
				List<Module> modules = JavaModulePlugin.getDefault().getModulesForProject(javaProject);
				Set<Object> theChildrenSet = new HashSet<Object>();
				Object[] children = packageFragmentRoot.getChildren();
				if (children != null) {
					for (Object child : children) {
						theChildrenSet.add(child);
					}
				}
				for (Module module : modules) {
					module.filterChildren(theChildrenSet);
				}

				boolean childrenFound = false;
				for (Object obj : theChildrenSet) {
					if (obj instanceof IPackageFragment) {
						IPackageFragment packageFragment = (IPackageFragment) obj;
						if (packageFragment.hasChildren()) {
							childrenFound = true;
							break;
						}
						IResource resource = packageFragment.getAdapter(IResource.class);
						if (resource instanceof IFolder) {
							IFolder folder = (IFolder) resource;
							FileCheckVisitor fileCheckVisitor = new FileCheckVisitor();
							folder.accept(fileCheckVisitor, IResource.DEPTH_ONE, IResource.NONE);
							if (fileCheckVisitor.filesPresent) {
								childrenFound = true;
								break;
							}
						}
					}
				}
				return childrenFound;
			} catch (CoreException e) {
				return false;
			}
		}
		return true;
	}

	private synchronized void initStateModel(Viewer viewer) {
		if ((!isStateModelInitialized) && ((viewer instanceof CommonViewer))) {
			CommonViewer commonViewer = (CommonViewer) viewer;
			fContentService = commonViewer.getNavigatorContentService();
			fStateModel = fContentService.findStateModel("com.cwctravel.eclipse.plugins.javamodule.content");
			isStateModelInitialized = true;
		}
	}
}
