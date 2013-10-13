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

import com.google.common.base.Objects;

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
	private static final Clock DEFAULT_CLOCK = new Clock(DaylightPreset.defaultPreset(), TimeOfDay.MORNING, 1);
	
	private final DaylightPreset preset;
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
	public Clock(DaylightPreset preset, TimeOfDay origin, double tickRate) {
		this.preset = preset;
		this.origin = origin;
		this.tickRate = tickRate;
	}
	
	/**
	 * Retrieve the starting time of the clock.
	 * @return The origin.
	 */
	public TimeOfDay getOrigin() {
		return origin;
	}
	
	/**
	 * Retrieve the number of clock ticks in a single game tick.
	 * @return The tick rate.
	 */
	public double getTickRate() {
		return tickRate;
	}
	
	/**
	 * Retrieve the current daylight preset.
	 * @return The daylight preset.
	 */
	public DaylightPreset getPreset() {
		return preset;
	}
	
	/**
	 * Determine whether or not the clock is ticking.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public boolean isRunning() {
		return tickRate != 0;
	}
	
	/**
	 * Determine if this is the default clock. 
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public boolean isDefault() {
		return this.equals(DEFAULT_CLOCK);
	}
	
	/**
	 * Retrieve the current Minecraft time after the given number of elapsed ticks.
	 * <p>
	 * Use {@link World#getFullTime()} to get a world's total elapsed ticks.
	 * @param elapsedTicks - number of elapsed ticks.
	 * @return The current time.
	 */
	public int get(long elapsedTicks) {
		int time = 0;
		
		if (isRunning()) {
			int phase = (int) ((elapsedTicks % TICKS_PER_DAY) * tickRate);
			time = (origin.getGameTick() + phase) % TICKS_PER_DAY;
		} else {
			time = origin.getGameTick();
		}
		return preset.toNormalTime(time);
	}
	
	/**
	 * Retrieve a new clock based on the current with the given preset.
	 * @param preset - the new daylight preset.
	 * @return The new clock.
	 */
	public Clock withPreset(DaylightPreset preset) {
		return new Clock(preset, origin, tickRate);
	}
	
	/**
	 * Retrieve a new clock based on the current with the given time of day as a starting point.
	 * @param origin - the new starting time of day.
 	 * @return The new clock.
	 */
	public Clock withOrigin(TimeOfDay origin) {
		return new Clock(preset, origin, tickRate);
	}
	
	/**
	 * Retrieve a new clock based on the current with the given tick rate.
	 * @param tickRate - the new tick rat.e
	 * @return The new clock.
	 */
	public Clock withSpeed(double tickRate) {
		return new Clock(preset, origin, tickRate);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Clock) {
			Clock other = (Clock) obj;
			return Objects.equal(getPreset(), other.getPreset()) &&
				   Objects.equal(getOrigin(), other.getOrigin()) &&
				   getTickRate() == other.getTickRate();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(preset, origin, tickRate);
	}

	@Override
	public String toString() {
		return "Clock [preset=" + preset + ", origin=" + origin + ", tickRate=" + tickRate + "]";
	}
}
