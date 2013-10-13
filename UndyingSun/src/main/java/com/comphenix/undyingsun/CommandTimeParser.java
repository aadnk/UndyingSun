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

package com.comphenix.undyingsun;


import com.comphenix.undyingsun.temporal.TimeOfDay;
import com.google.common.collect.ImmutableSet;

/**
 * Represents a parser for reading the input time in ticks.
 * @author Kristian
 */
class CommandTimeParser {
	/**
	 * Represents non-fixed time of days.
	 */
	public static ImmutableSet<String> DISABLED = ImmutableSet.of("none", "disabled");
	
	/**
	 * Attempt to parse the given command time.
	 * @param commandTime - the command time.
	 * @return The time in ticks, or NULL if the time should be disabled.
	 * @throws NumberFormatException If the command is neither a time of day nor a valid number.
	 */
	public static TimeOfDay parse(String commandTime) throws NumberFormatException {
		TimeOfDay namedTime = TimeOfDay.fromAlias(commandTime);
		
		if (namedTime != null) {
			return namedTime;
		} else if (DISABLED.contains(commandTime.toLowerCase())) {
			// Disable the time
			return null;
		}
		return new TimeOfDay(Integer.parseInt(commandTime.trim()), commandTime);
	}
}
