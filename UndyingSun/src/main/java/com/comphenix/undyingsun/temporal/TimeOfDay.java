package com.comphenix.undyingsun.temporal;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Different time of days in Minecraft.
 * @author Kristian
 */
public class TimeOfDay {
	/**
	 * Every default time of day in Minecraft.
	 */
	public static final ImmutableList<TimeOfDay> VALUES = createDefault();
	
	private final int gameTick;
	private final String alias;
	
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
	
	@Override
	public String toString() {
		return alias;
	}
	
	/**
	 * Construct every default time of day.
	 * @return Every default.
	 */
	private static ImmutableList<TimeOfDay> createDefault() {
		List<TimeOfDay> times = Lists.newArrayList();
		defineTime(times, 22000, "dawn", "twilight");
		defineTime(times, 22580, "sunrise");
		defineTime(times, 500, "morning");
		defineTime(times, 3000, "forenoon");
		defineTime(times, 6000, "noon", "midday", "day");
		defineTime(times, 9000, "afternoon");
		defineTime(times, 13400, "sunset");
		defineTime(times, 14000, "dusk");
		defineTime(times, 18000, "midnight", "night");
		return ImmutableList.copyOf(times);
	}
	
	/**
	 * Add a new time of day of the given game tick for each alias.
	 * @param destination - the destination list.
	 * @param gameTick - the game tick.
	 * @param aliases - the aliases.
	 */
	private static void defineTime(List<TimeOfDay> destination, int gameTick, String... aliases) {
		for (String alias : aliases) {
			destination.add(new TimeOfDay(gameTick, alias));
		}
	}
	
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
}