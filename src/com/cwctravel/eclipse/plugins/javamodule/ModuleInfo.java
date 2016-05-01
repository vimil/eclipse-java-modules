package com.cwctravel.eclipse.plugins.javamodule;

import java.util.ArrayList;
import java.util.List;

import com.cwctravel.eclipse.plugins.javamodule.utils.StringUtil;

public class ModuleInfo {
	private int index;
	private String name;
	private List<String> packagePatterns;

	public ModuleInfo() {}

	public ModuleInfo(String name) {
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getPackagePatterns() {
		if(packagePatterns == null) {
			packagePatterns = new ArrayList<String>();
		}
		return packagePatterns;
	}

	public void addPackagePattern(String packagePattern) {
		if(!StringUtil.isEmpty(packagePattern)) {
			getPackagePatterns().add(packagePattern);
		}
	}

	public void setPackagePattern(String packagePatternStr) {
		getPackagePatterns().clear();
		if(!StringUtil.isEmpty(packagePatternStr)) {
			String[] packagePatterns = packagePatternStr.split(",");
			for(String packagePattern: packagePatterns) {
				addPackagePattern(packagePattern);
			}
		}
	}

	public String getPackagePattern() {
		return StringUtil.joinCollection(getPackagePatterns(), ",");
	}

}
