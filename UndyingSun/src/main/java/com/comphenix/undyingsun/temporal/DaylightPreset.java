package com.comphenix.undyingsun.temporal;

import java.util.Arrays;

/**
 * Represents the distribution of daylight in 24-hours.
 * @author Kristian
 */
public class DaylightPreset {
	/**
	 * The number of ticks per day.
	 */
	private static final double TICKS_PER_DAY = 24000.0;
	
	/**
	 * The default daylight distribution.
	 */
	private static final DaylightPreset DEFAULT = new DaylightPreset(6, 1, 4, 1) {
		public int toNormalTime(int ticks) {
			// Optimize
			return ticks;
		}
	};
	
	// Fractions
	private final double day;
	private final double evening;
	private final double night;
	private final double dawn;
	
	// Normalized version of the current preset
	private transient DaylightPreset normalized;
	
	/**
	 * Retrieve the default preset used by Minecraft itself.
	 * @return The default preset.
	 */
	public static DaylightPreset defaultPreset() {
		return DEFAULT;
	}
	
	/**
	 * Construct a new daylight preset.
	 * <p>
	 * The sum of all the fractions will be normalized to one.
	 * @param day - fraction that occurs during the day.
	 * @param evening - fraction that occurs during evening to night.
	 * @param night - fraction that occurs during the night.
	 * @param dawn - fraction that occurs during dawn to morning.
	 */
	public static DaylightPreset newPreset(double day, double evening, double night, double dawn) {
		return new DaylightPreset(day, evening, night, dawn);
	}
	
	// Construct a new preset
	protected DaylightPreset(double day, double evening, double night, double dawn) {
		this.day = notNegative(day, "day");
		this.evening = notNegative(evening, "evening");
		this.night = notNegative(night, "night");
		this.dawn = notNegative(dawn, "dawn");
	}

	/**
	 * Ensure the given incoming parameter is not negative.
	 * @param value - the parameter value.
	 * @param param - the parameter name.
	 * @return The parameter value.
	 */
	private double notNegative(double value, String param) {
		if (value < 0)
			throw new IllegalArgumentException(param + " cannot be less than zero.");
		return value;
	}
	
	/**
	 * Normalize the distribution to fractions of 1/N, and the sum of every fraction is one.
	 * @return The normalized preset.
	 */
	protected DaylightPreset normalize() {
		// Memoize the preset
		if (normalized == null) {
			double sum = day + evening + night + dawn;
			
			// Calculate the normalized preset
			normalized = new DaylightPreset(
				day / sum, evening / sum, 
				night / sum, dawn / sum
			);
		}
		return normalized;
	}
	
	/**
	 * Express the given time in a day with altered daylight distribution as a day in normal Minecraft.
	 * @param ticks - time in the altered day.
	 * @return Relative time in a normal Minecraft world.
	 */
	public int toNormalTime(int ticks) {
		// Validate incoming parameter
		if (ticks < 0)
			throw new IllegalArgumentException("Ticks cannot be negative.");
		if (ticks > TICKS_PER_DAY)
			throw new IllegalArgumentException("Ticks cannot exceed the standard Minecraft day.");
		
		double value = ticks / TICKS_PER_DAY;
		double aggregate = 0;
		double[] modified = normalize().toArray();
		double[] standard = defaultPreset().normalize().toArray();
		
		// Calculate the equivalent time in a normal Minecraft world
		for (int i = 0; i < modified.length; i++) {
			if (value > modified[i]) {
				aggregate += standard[i];
				value -= modified[i];
			} else {
				aggregate += standard[i] * (value / modified[i]);
				return (int) (aggregate * TICKS_PER_DAY);
			}
		}
		throw new IllegalStateException("Cannot convert to normal time: " + ticks);
	}
	
	/**
	 * Retrieve each fraction in sequence, in the exact same order as the static constructor.
	 * @return Four element array.
	 */
	public double[] toArray() {
		return new double[] { day, evening, night, dawn };
	}
	
	/**
	 * Retrieve the fraction of the 24-hours that occurs during dawn to morning.
	 * @return The sunrise fraction.
	 */
	public double getDawn() {
		return dawn;
	}
	
	/**
	 * Retrieve the fraction of the 24-hours that occurs during the day.
	 * @return The day fraction.
	 */
	public double getDay() {
		return day;
	}
	
	/**
	 * Retrieve the fraction of the 24-hours that occurs during evening to night..
	 * @return The sunset fraction.
	 */
	public double getEvening() {
		return evening;
	}
	
	/**
	 * Retrieve the fraction of the 24-hours that occurs during the night.
	 * @return The night fraction.
	 */
	public double getNight() {
		return night;
	}
	
	/**
	 * Determine if this is a default preset.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public boolean isDefault() {
		return DEFAULT.equals(this);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof DaylightPreset) {
			DaylightPreset other = (DaylightPreset) obj;
			return Arrays.equals(normalize().toArray(), other.normalize().toArray());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(normalize().toArray());
	}

	@Override
	public String toString() {
		return "DaylightPreset [day=" + day + ", evening=" + evening + ", night=" + night + ", dawn=" + dawn + "]";
	}
}
