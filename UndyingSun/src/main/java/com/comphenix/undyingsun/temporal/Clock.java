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
	
	private final TimeOfDay origin;
	private final double tickRate;
	
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
	 * Retrieve the current time after the given number of elapsed ticks.
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
