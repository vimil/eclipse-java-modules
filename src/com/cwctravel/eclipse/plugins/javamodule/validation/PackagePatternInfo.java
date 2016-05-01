package com.cwctravel.eclipse.plugins.javamodule.validation;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cwctravel.eclipse.plugins.javamodule.exception.PackagePatternValidationException;

public class PackagePatternInfo {
	private static final Pattern PACKAGE_PATTERN_FORMAT = Pattern
			.compile("%default|([a-zA-Z0-9_]+)(\\.([a-zA-Z0-9_]+))*(\\.\\*)?");

	private boolean isWildCardMatch;
	private String[] segments;
	private String pattern;

	private PackagePatternInfo() {
	}

	public boolean isWildCardMatch() {
		return isWildCardMatch;
	}

	public void setWildCardMatch(boolean isWildCardMatch) {
		this.isWildCardMatch = isWildCardMatch;
	}

	public String[] getSegments() {
		return segments;
	}

	public void setSegments(String[] segments) {
		this.segments = segments;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isWildCardMatch ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(segments);
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
		PackagePatternInfo other = (PackagePatternInfo) obj;
		if (isWildCardMatch != other.isWildCardMatch)
			return false;
		if (!Arrays.equals(segments, other.segments))
			return false;
		return true;
	}

	public static PackagePatternInfo parse(String packagePatternStr) throws PackagePatternValidationException {
		Matcher packagePatternMatcher = PACKAGE_PATTERN_FORMAT.matcher(packagePatternStr);
		if (packagePatternMatcher.matches()) {
			PackagePatternInfo result = new PackagePatternInfo();
			result.setPattern(packagePatternStr);
			if (packagePatternStr.endsWith(".*")) {
				result.setWildCardMatch(true);
				packagePatternStr = packagePatternStr.substring(0, packagePatternStr.length() - 2);
			}
			result.setSegments(packagePatternStr.split("\\."));
			return result;
		}

		throw new PackagePatternValidationException(
				String.format("'%s' is not a valid packagePattern", packagePatternStr));
	}
}
