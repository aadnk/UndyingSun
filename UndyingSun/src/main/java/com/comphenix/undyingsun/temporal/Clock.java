/*
 *  UndyingSum - Bukkit server plugin that allows for decoupling the server and client clock.
 *  Copyright (C) 2013 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.undyingsun.temporal;

import org.bukkit.World;


/**
 * Represents a Minecraft clock.
 * @author Kristian
 */
public class Clock {
	/**
	 * The number of ticks in a single Minecraft day.
	 */
	public static final int TICKS_PER_DAY = 24000;
	
	/**
	 * Represents the default clock.
	 */
	private static final Clock DEFAULT_CLOCK = new Clock(new TimeOfDay(0, "default"), 1);
	
	private final TimeOfDay origin;
	private final double tickRate;
	
	/**
	 * Retrieve the default clock in Minecraft.
	 * @return The default clock.
	 */
	public static Clock defaultClock() {
		return DEFAULT_CLOCK;
	}
	
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
	 * Determine whether or not the clock is ticking.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public boolean isRunning() {
		return tickRate != 0;
	}
	
	/**
	 * Retrieve the current time after the given number of elapsed ticks.
	 * <p>
	 * Use {@link World#getFullTime()} to get a world's total elapsed ticks.
	 * @param elapsedTicks - number of elapsed ticks.
	 * @return The current time.
	 */
	public int get(long elapsedTicks) {
		if (isRunning()) {
			int phase = (int) ((elapsedTicks % TICKS_PER_DAY) * tickRate);
			return (origin.getGameTick() + phase) % TICKS_PER_DAY;
		} else {
			return origin.getGameTick();
		}
	}
}
