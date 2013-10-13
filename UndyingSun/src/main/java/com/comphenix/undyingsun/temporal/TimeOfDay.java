package com.comphenix.undyingsun.temporal;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Different time of days in Minecraft.
 * @author Kristian
 */
public class TimeOfDay {		
	public static final TimeOfDay DAWN = 		new TimeOfDay(22000, "dawn");
	public static final TimeOfDay SUNRISE = 	new TimeOfDay(22580, "sunrise");
	public static final TimeOfDay MORNING = 	new TimeOfDay(0, "morning");
	public static final TimeOfDay FORENOON = 	new TimeOfDay(3000, "forenoon");
	public static final TimeOfDay NOON = 		new TimeOfDay(6000, "noon");
	public static final TimeOfDay AFTERNOON =	new TimeOfDay(9000, "afternoon");
	public static final TimeOfDay EVENING = 	new TimeOfDay(12000, "evening");
	public static final TimeOfDay SUNSET = 		new TimeOfDay(13400, "sunset");
	public static final TimeOfDay DUSK = 		new TimeOfDay(14000, "dusk");
	public static final TimeOfDay MIDNIGHT = 	new TimeOfDay(18000, "midnight");
	
	/**
	 * Every default time of day in Minecraft.
	 */
	public static final ImmutableList<TimeOfDay> VALUES = createDefault();
	
	private final int gameTick;
	private final String alias;
	
	/**
	 * Retrieve the time of day from a given alias.
	 * @param alias - the alias.
	 * @return The time of day, or NULL if not found.
	 */
	public static TimeOfDay fromAlias(String alias) {
		// Linear search is fast enough for the number of values we're dealing with
		for (TimeOfDay time : VALUES) {
			if (time.getAlias().equalsIgnoreCase(alias)) {
				return time;
			}
		}
		return null;
	}
	
	/**
	 * Convert a given time of day to a human-readable string.
	 * @param time - the time of day.
	 * @return The human readable string.
	 */
	public static String toTimeString(TimeOfDay time) {
		return time != null ? time.getAlias() : "NONE";
	}
	
	/**
	 * Construct a new time of day.
	 * @param gameTick - the game tick.
	 * @param alias - the alias.
	 */
	public TimeOfDay(int gameTick, String alias) {
		this.gameTick = gameTick;
		this.alias = alias;
	}
	
	/**
	 * Retrieve the alias of this time of day.
	 * @return The alias.
	 */
	public String getAlias() {
		return alias;
	}
	
	/**
	 * Retrieve the exact game tick (of maximum 24000) of this time of day.
	 * @return The game tick.
	 */
	public int getGameTick() {
		return gameTick;
	}
	
	/**
	 * Construct every default time of day.
	 * @return Every default.
	 */
	private static ImmutableList<TimeOfDay> createDefault() {
		List<TimeOfDay> times = Lists.newArrayList();
		defineTime(times, DAWN, "twilight");
		defineTime(times, SUNRISE);
		defineTime(times, MORNING);
		defineTime(times, FORENOON);
		defineTime(times, NOON, "midday", "day");
		defineTime(times, AFTERNOON);
		defineTime(times, EVENING);
		defineTime(times, SUNSET);
		defineTime(times, DUSK);
		defineTime(times, MIDNIGHT, "night");
		return ImmutableList.copyOf(times);
	}
	
	/**
	 * Add a new time of day and its aliases.
	 * @param destination - the destination list.
	 * @param primary - the primary time of day tick.
	 * @param aliases - the aliases.
	 */
	private static void defineTime(List<TimeOfDay> destination, TimeOfDay primary, String... aliases) {
		destination.add(primary);
		
		// Add any aliases
		for (String alias : aliases) {
			destination.add(new TimeOfDay(primary.getGameTick(), alias));
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof TimeOfDay) {
			TimeOfDay other = (TimeOfDay) obj;
			return gameTick == other.gameTick;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return gameTick;
	}
	
	
	@Override
	public String toString() {
		return alias;
	}
}