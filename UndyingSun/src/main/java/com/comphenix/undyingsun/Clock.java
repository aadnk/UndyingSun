package com.comphenix.undyingsun;

import org.bukkit.World;

import com.comphenix.undyingsun.CommandTimeParser.TimeOfDay;

/**
 * Represents a Minecraft clock.
 * @author Kristian
 */
class Clock {
	/**
	 * The number of ticks in a single Minecraft day.
	 */
	public static final int TICKS_PER_DAY = 24000;
	
	private TimeOfDay origin;
	private double tickRate;
	
	/**
	 * Construct a new Minecraft clock.
	 * @param origin - the starting time.
	 * @param tickRate - the tick rate.
	 */
	public Clock(TimeOfDay origin, double tickRate) {
		this.origin = origin;
		this.tickRate = tickRate;
	}
	
	/**
	 * Retrieve the current time given the number of elapsed tick.
	 * <p>
	 * Use {@link World#getFullTime()} to get a world's total elapsed ticks.
	 * @param elapsedTicks - number of elapsed ticks.
	 * @return The current time.
	 */
	public int get(long elapsedTicks) {
		if (tickRate != 0)
			return (int) (origin.getGameTick() + (elapsedTicks % TICKS_PER_DAY) * tickRate) % TICKS_PER_DAY;
		else
			return origin.getGameTick();
	}
}
