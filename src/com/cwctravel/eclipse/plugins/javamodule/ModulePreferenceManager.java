package com.cwctravel.eclipse.plugins.javamodule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import com.cwctravel.eclipse.plugins.javamodule.utils.StringUtil;

public class ModulePreferenceManager {
	private static final Pattern MODULE_NAME_PROPERTY_PATTERN = Pattern.compile("module\\.([0-9]+)\\.name");
	private static final Pattern MODULE_PACKAGE_PATTERN_PROPERTY_PATTERN = Pattern.compile("module\\.([0-9]+)\\.packagePattern");

	private final IEclipsePreferences preferences;

	public ModulePreferenceManager(IEclipsePreferences preferences) {
		this.preferences = preferences;
	}

	public List<ModuleInfo> read() {
		List<ModuleInfo> result = new ArrayList<ModuleInfo>();
		Map<Integer, ModuleInfo> modulesMap = new HashMap<Integer, ModuleInfo>();
		try {
			String[] keys = preferences.keys();
			for(String key: keys) {
				Matcher modulePropertyNameMatcher = MODULE_NAME_PROPERTY_PATTERN.matcher(key);
				if(modulePropertyNameMatcher.matches()) {
					int moduleIndex = StringUtil.toInt(modulePropertyNameMatcher.group(1), 0);
					String moduleName = preferences.get(key, "");

					ModuleInfo moduleInfo = modulesMap.get(moduleIndex);
					if(moduleInfo == null) {
						moduleInfo = new ModuleInfo(moduleName);
						modulesMap.put(moduleIndex, moduleInfo);
					}

					moduleInfo.setIndex(moduleIndex);
					moduleInfo.setName(moduleName);
				}
				else {
					Matcher modulePropertyPackagePatternMatcher = MODULE_PACKAGE_PATTERN_PROPERTY_PATTERN.matcher(key);
					if(modulePropertyPackagePatternMatcher.matches()) {
						int moduleIndex = StringUtil.toInt(modulePropertyPackagePatternMatcher.group(1), 0);
						String packagePatternStr = preferences.get(key, "");

						ModuleInfo moduleInfo = modulesMap.get(moduleIndex);
						if(moduleInfo == null) {
							moduleInfo = new ModuleInfo();
							modulesMap.put(moduleIndex, moduleInfo);
						}

						moduleInfo.setIndex(moduleIndex);
						moduleInfo.setPackagePattern(packagePatternStr);
					}
				}
			}

			for(ModuleInfo moduleInfo: modulesMap.values()) {
				if(!StringUtil.isEmpty(moduleInfo.getName())) {
					result.add(moduleInfo);
				}
			}

			Collections.sort(result, new Comparator<ModuleInfo>() {
				@Override
				public int compare(ModuleInfo o1, ModuleInfo o2) {
					return o1.getIndex() - o2.getIndex();
				}
			});
		}
		catch(BackingStoreException e) {
			JavaModulePlugin.log(IStatus.ERROR, e.getMessage(), e);
		}

		return result;
	}

	public void store(List<ModuleInfo> modules) throws BackingStoreException {
		String[] keys = preferences.keys();
		Map<String, String> oldProperties = new HashMap<String, String>();
		Map<String, String> newProperties = new HashMap<String, String>();
		
		for(String key: keys) {
			Matcher modulePropertyNameMatcher = MODULE_NAME_PROPERTY_PATTERN.matcher(key);
			if(modulePropertyNameMatcher.matches()) {
				oldProperties.put(key, preferences.get(key, ""));
			}
			else {
				Matcher modulePropertyPackagePatternMatcher = MODULE_PACKAGE_PATTERN_PROPERTY_PATTERN.matcher(key);
				if(modulePropertyPackagePatternMatcher.matches()) {
					oldProperties.put(key, preferences.get(key, ""));
				}
			}
		}
		
		if(modules != null) {
			for(ModuleInfo module: modules) {
				String moduleNameProperty = "module." + module.getIndex() + ".name";
				newProperties.put(moduleNameProperty, module.getName());

				String modulePackagePatternProperty = "module." + module.getIndex() + ".packagePattern";
				newProperties.put(modulePackagePatternProperty, module.getPackagePattern());
			}
		}
		
		List<String[]> propertiesDelta = computePropertiesDelta(oldProperties, newProperties);
		for(String[] propertyDelta: propertiesDelta) {
			String key = propertyDelta[0];
			String changeType = propertyDelta[1];
			if("ADD".equals(changeType) || "CHANGE".equals(changeType)) {
				String value = propertyDelta[2];
				preferences.put(key, value);
			}else if("DELETE".equals(changeType)) {
				preferences.remove(key);
			}
		}
		
	
		preferences.flush();
		preferences.sync();
	}

	private List<String[]> computePropertiesDelta(Map<String, String> oldProperties,
			Map<String, String> newProperties) {
		List<String[]> result = new ArrayList<String[]>();
		for(Map.Entry<String, String> oldPropertiesEntry: oldProperties.entrySet()) {
			String key = oldPropertiesEntry.getKey();
			String oldValue = oldPropertiesEntry.getValue();
			String newValue = newProperties.get(key);
			if(newValue == null) {
				result.add(new String[]{key, "DELETE", null});
			}else if(StringUtil.compare(oldValue, newValue) !=0){
				result.add(new String[]{key, "CHANGE", newValue});
			}
		}
		
		for(Map.Entry<String, String> newPropertiesEntry: newProperties.entrySet()) {
			String key = newPropertiesEntry.getKey();
			String newValue = newPropertiesEntry.getValue();
			String oldValue = oldProperties.get(key);
			if(oldValue == null) {
				result.add(new String[]{key, "ADD", newValue});
			}
		}
		
		return result;
	}
}
