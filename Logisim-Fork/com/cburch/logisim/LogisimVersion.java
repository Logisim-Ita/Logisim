package com.cburch.logisim;

import java.net.URISyntaxException;

import com.cburch.logisim.gui.start.Startup;

public class LogisimVersion {
	public static final int FINAL_REVISION = Integer.MAX_VALUE / 4;

	/**
	 * Create a new version object for the current Logisim instance (the constructor
	 * is private) where the revision number is set to its default value and no
	 * variant is used
	 */
	public static LogisimVersion get(int major, int minor, int release) {
		return (get(major, minor, release, FINAL_REVISION, ""));
	}

	/**
	 * Create a new version object for the current Logisim instance (the constructor
	 * is private) where no variant is used
	 */
	public static LogisimVersion get(int major, int minor, int release, int revision) {
		return (get(major, minor, release, revision, ""));
	}

	/**
	 * Create a new version object for the current Logisim instance (the constructor
	 * is private)
	 */
	public static LogisimVersion get(int major, int minor, int release, int revision, String variant) {
		return (new LogisimVersion(major, minor, release, revision, variant));
	}

	/**
	 * Create a new version object for the current Logisim instance (the constructor
	 * is private) where the revision field is set to its default value
	 */
	public static LogisimVersion get(int major, int minor, int release, String variant) {
		return (get(major, minor, release, FINAL_REVISION, variant));
	}

	public static String getVariantFromFile() {
		try {
			String s = Startup.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			if (s.endsWith(".exe"))
				return "exe";
			return "jar";
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return "jar";
		}
	}

	/**
	 * Parse a string containing a version number and returns the corresponding
	 * LogisimVersion object. No exception is thrown if the version string contains
	 * non-integers, because literal values are allowed.
	 *
	 * @return LogisimVersion built from the string passed as parameter
	 */
	public static LogisimVersion parse(String versionString) {
		String[] parts = versionString.split("\\.");
		int major = 0;
		int minor = 0;
		int release = 0;
		int revision = FINAL_REVISION;
		String variant = "";

		if (versionString.isEmpty()) {
			// Return the default values for an empty version string
			return (new LogisimVersion(major, minor, release, revision, variant));
		}

		try {
			if (parts.length >= 1)
				major = Integer.parseInt(parts[0]);
			if (parts.length >= 2)
				minor = Integer.parseInt(parts[1]);
			if (parts.length >= 3)
				release = Integer.parseInt(parts[2]);
			if (parts.length >= 4)
				revision = Integer.parseInt(parts[3]);
			if (parts.length >= 5)
				variant = parts[4];
		} catch (NumberFormatException e) {
		}
		return (new LogisimVersion(major, minor, release, revision, variant));
	}

	private int major;

	private int minor;

	private int release;

	private int revision;

	private String variant;

	private String repr;

	private LogisimVersion(int major, int minor, int release, int revision, String variant) {
		this.major = major;
		this.minor = minor;
		this.release = release;
		this.revision = revision;
		this.variant = variant;
		this.repr = null;
	}

	/**
	 * Compare two Logisim version, returning whether the one passed as parameter is
	 * newer than the current one or not
	 *
	 * @return Negative value if the current version is older than the one passed as
	 *         parameter
	 */
	public int compareTo(LogisimVersion other) {
		int ret = this.major - other.major;

		if (ret != 0) {
			return ret;
		} else {
			ret = this.minor - other.minor;
			if (ret != 0) {
				return (ret);
			} else {
				ret = this.release - other.release;
				if (ret != 0) {
					return (ret);
				} else {
					ret = this.revision - other.revision;
					if (ret != 0) {
						return (ret);
					} else {
						return (this.variant.compareTo(other.variant));
					}
				}
			}
		}
	}

	/**
	 * Compares two Logisim version numbers.
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof LogisimVersion) {
			LogisimVersion o = (LogisimVersion) other;
			return (this.major == o.major && this.minor == o.minor && this.release == o.release
					&& this.revision == o.revision && this.variant == o.variant);
		} else {
			return (false);
		}
	}

	/**
	 * Build the hash code starting from the version number
	 */
	@Override
	public int hashCode() {
		int ret = major * 31 + minor;
		ret = ret * 31 + release;
		ret = ret * 31 + revision;
		return (ret);
	}

	/**
	 * If the considered Logisim version is a jar, returns true. Assumption: It's
	 * identified by a variant equals to "jar"
	 */
	public boolean isJar() {
		if (this.variant == "jar")
			return true;
		return false;
	}

	public String mainVersion() {
		return (major + "." + minor + "." + release);
	}

	public String rev() {
		if (revision != FINAL_REVISION) {
			return ("rev. " + revision);
		} else {
			return ("");
		}
	}

	@Override
	public String toString() {
		String ret = repr;

		if (ret == null) {
			ret = major + "." + minor + "." + release;
			if (revision != FINAL_REVISION)
				ret += "." + revision;
			if (variant != "")
				ret += "." + variant;
			repr = ret;
		}
		return (ret);
	}

}