package com.cwctravel.eclipse.plugins.javamodule.utils;

import java.util.Collection;
import java.util.Iterator;

public class StringUtil {
	public static Integer toInt(String str, Integer defaultValue) {
		return toInt(str, 10, defaultValue);
	}

	public static Integer toInt(String str, int radix, Integer defaultValue) {
		try {
			return Integer.parseInt(str, radix);
		}
		catch(NumberFormatException nFE) {
			return defaultValue;
		}
	}

	public static boolean isEmpty(String str) {
		return((str == null) || (str.length() == 0));
	}

	public static <T> String joinCollection(Collection<T> collection, String sepStr) {
		StringBuilder sB = new StringBuilder();
		if(collection != null) {
			Iterator<T> iter = collection.iterator();
			while(iter.hasNext()) {
				T t = iter.next();
				if(t != null) {
					sB.append(t.toString());
				}
				if(iter.hasNext()) {
					sB.append(sepStr);
				}
			}
		}
		return sB.toString();
	}

	public static int compare(String oldValue, String newValue) {
		if(oldValue == newValue) {
			return 0;
		}
		if(oldValue == null) {
			return -1;
		}
		if(newValue == null) {
			return 1;
		}
		return oldValue.compareTo(newValue);
	}
}
