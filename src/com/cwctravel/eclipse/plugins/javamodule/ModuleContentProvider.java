package com.cwctravel.eclipse.plugins.javamodule;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;

/**
 * Provides the properties contained in a *.properties file as children of that
 * file in a Common Navigator.
 * 
 * @since 3.2
 */
public class ModuleContentProvider implements IPipelinedTreeContentProvider {

	/**
	 * Create the PropertiesContentProvider instance. Adds the content provider
	 * as a resource change listener to track changes on disk.
	 */
	public ModuleContentProvider() {

	}

	/**
	 * Return the model elements for a *.properties IFile or NO_CHILDREN for
	 * otherwise.
	 */
	public Object[] getChildren(Object parentElement) {
		Object[] result = null;
		if (parentElement instanceof IProject) {

			IProject project = (IProject) parentElement;
			IJavaProject javaProject = JavaCore.create(project);
			List<Module> modules = JavaModulePlugin.getDefault().getModulesForProject(javaProject);

			result = modules.toArray(new Module[0]);

		} else if (parentElement instanceof Module) {
			Module data = (Module) parentElement;
			result = data.getChildren();
		}
		return result;
	}

	public Object getParent(Object element) {
		Object result = null;
		if (element instanceof Module) {
			Module data = (Module) element;
			result = data.getProject();
		} else if (element instanceof IPackageFragment) {
			IPackageFragment packageFragment = (IPackageFragment) element;
			List<Module> modules = JavaModulePlugin.getDefault().getModulesForProject(packageFragment.getJavaProject());
			for (Module module : modules) {
				if (module.filter(packageFragment)) {
					result = module;
					break;
				}
			}
		}
		return result;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof Module) {
			Module data = (Module) element;
			return data.hasChildren();
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public void init(ICommonContentExtensionSite aConfig) {

	}

	@Override
	public void restoreState(IMemento aMemento) {

	}

	@Override
	public void saveState(IMemento aMemento) {

	}

	@SuppressWarnings("unchecked")
	@Override
	public void getPipelinedChildren(Object aParent, @SuppressWarnings("rawtypes") Set theCurrentChildren) {
		Object[] children = getChildren(aParent);
		if (children != null) {
			for (Object child : children) {
				theCurrentChildren.add(child);
			}
		}
		if (aParent instanceof IPackageFragmentRoot) {
			IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) aParent;
			IJavaProject javaProject = packageFragmentRoot.getJavaProject();
			List<Module> modules = JavaModulePlugin.getDefault().getModulesForProject(javaProject);
			for (Module module : modules) {
				module.filterChildren(theCurrentChildren);
			}
		}
	}

	@Override
	public void getPipelinedElements(Object anInput, @SuppressWarnings("rawtypes") Set theCurrentElements) {
		getPipelinedChildren(anInput, theCurrentElements);

	}

	@Override
	public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
		return getParent(anObject);
	}

	@Override
	public PipelinedShapeModification interceptAdd(PipelinedShapeModification anAddModification) {
		return anAddModification;
	}

	@Override
	public PipelinedShapeModification interceptRemove(PipelinedShapeModification aRemoveModification) {
		return null;
	}

	@Override
	public boolean interceptRefresh(PipelinedViewerUpdate aRefreshSynchronization) {
		return false;
	}

	@Override
	public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
		return false;
	}

}
