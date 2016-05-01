package com.cwctravel.eclipse.plugins.javamodule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ModulesContentProvider implements IStructuredContentProvider {
	private List<ModuleInfo> modules;
	private final Map<String, ModuleInfo> moduleMap;

	public ModulesContentProvider(List<ModuleInfo> modules) {
		this.modules = modules;
		this.moduleMap = new HashMap<String, ModuleInfo>();

		if(modules != null) {
			for(ModuleInfo moduleInfo: modules) {
				moduleMap.put(moduleInfo.getName(), moduleInfo);
			}
		}
	}

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	@Override
	public Object[] getElements(Object inputElement) {
		if(modules != null) {
			return modules.toArray();
		}
		return new Object[] {};
	}

	public List<ModuleInfo> getModules() {
		if(modules == null) {
			modules = new ArrayList<ModuleInfo>();
		}
		return modules;
	}

	public void addModule(ModuleInfo module) {
		if(module != null) {
			if(!moduleMap.containsKey(module.getName())) {
				getModules().add(module);
				moduleMap.put(module.getName(), module);
			}
		}
	}

	public void removeModules(List<Integer> moduleIndices) {
		if(moduleIndices != null) {
			List<ModuleInfo> packageGroups = getModules();
			Collections.sort(moduleIndices);
			int packageGroupIndicesCount = moduleIndices.size();
			for(int i = packageGroupIndicesCount - 1; i >= 0; i--) {
				ModuleInfo removedPackageGroupInfo = packageGroups.remove((int)moduleIndices.get(i));
				moduleMap.remove(removedPackageGroupInfo.getName());
			}
		}
	}
}
