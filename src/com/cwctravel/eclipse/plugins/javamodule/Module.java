package com.cwctravel.eclipse.plugins.javamodule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

public class Module implements IAdaptable {

	private IJavaProject javaProject;
	private String name;
	private List<String> packagePatterns;
	private int index;

	public Module(IJavaProject javaProject, String name) {
		this.name = name;
		this.javaProject = javaProject;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getPackagePatterns() {
		return packagePatterns;
	}

	public void setPackagePatterns(List<String> packagePatterns) {
		this.packagePatterns = packagePatterns;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Object[] getChildren() {
		Object[] result = null;
		try {
			if (javaProject.isOpen()) {
				IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
				if (packageFragmentRoots != null) {
					List<IJavaElement> filteredJavaElements = new ArrayList<IJavaElement>();
					for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
						if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
							IJavaElement[] javaElements = packageFragmentRoot.getChildren();
							for (IJavaElement javaElement : javaElements) {
								if (javaElement instanceof IPackageFragment) {
									IPackageFragment packageFragment = (IPackageFragment) javaElement;
									if (filter(packageFragment)) {
										filteredJavaElements.add(packageFragment);
									}
								}
							}
						}
					}
					result = filteredJavaElements.toArray(new IPackageFragment[0]);
				}
			}
		} catch (JavaModelException e) {
			JavaModulePlugin.log(IStatus.ERROR, e.getMessage(), e);
		}

		return result;
	}

	boolean filter(IPackageFragment packageFragment) {
		boolean result = false;
		if (packageFragment != null) {
			String packageName = packageFragment.getElementName();
			if (packagePatterns != null) {
				for (String packagePattern : packagePatterns) {
					boolean isWildCardMatch = packagePattern.endsWith(".*");
					String packagePrefix = isWildCardMatch ? packagePattern.substring(0, packagePattern.length() - 2)
							: packagePattern;
					if ((isWildCardMatch && packageName.startsWith(packagePrefix))
							|| packageName.equals(packagePrefix)) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

	public boolean hasChildren() {
		Object[] children = getChildren();
		return children != null && children.length > 0;
	}

	public String toString() {
		return name;
	}

	public Object getParent() {
		return javaProject;
	}

	public IProject getProject() {
		return javaProject.getProject();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		IResource result = null;

		if (IResource.class.equals(adapter)) {
			Object[] children = getChildren();
			if (children != null && children.length > 0) {
				result = ((IPackageFragment) children[0]).getAdapter(IResource.class);
			}
		}

		return (T) result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean filterChildren(Set theCurrentChildren) {
		boolean result = false;
		Iterator<Object> itr = theCurrentChildren.iterator();
		while (itr.hasNext()) {
			Object oChild = itr.next();
			if (oChild instanceof IPackageFragment) {
				IPackageFragment packageFragment = (IPackageFragment) oChild;
				if (filter(packageFragment)) {
					itr.remove();
					result = true;
				}
			}
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((javaProject == null) ? 0 : javaProject.getProject().getName().hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Module other = (Module) obj;
		if (javaProject == null) {
			if (other.javaProject != null)
				return false;
		} else if (!javaProject.equals(other.javaProject))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
