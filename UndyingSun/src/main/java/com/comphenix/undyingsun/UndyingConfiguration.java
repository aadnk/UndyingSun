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

import java.io.File;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import com.comphenix.undyingsun.temporal.Clock;
import com.comphenix.undyingsun.temporal.DaylightPreset;
import com.comphenix.undyingsun.temporal.TimeOfDay;


class UndyingConfiguration {
	private static final String CONFIG_CLIENT_CLOCK = "client";
	private static final String CONFIG_SERVER_CLOCK = "server";
	
	// Loading clock
	private static final String CONFIG_CLOCK_SPEED = "speed";
	private static final String CONFIG_CLOCK_TIME = "time";
	private static final String CONFIG_CLOCK_DAYLIGHT = "daylight";
	
	// Loading preset
	private static final String CONFIG_PRESET_DAY = "day";
	private static final String CONFIG_PRESET_EVENING = "evening";
	private static final String CONFIG_PRESET_NIGHT = "night";
	private static final String CONFIG_PRESET_DAWN = "dawn";
	
	private Plugin plugin;

	// The configurations
	private Clock serverClock;
	private Clock clientClock;
	
	public UndyingConfiguration(Plugin plugin) {
		this.plugin = plugin;
		loadConfig(false);
	}

	/**
	 * Reload configuration file.
	 */
	public void reloadConfig() {
		loadConfig(true);
	}
	
	public void loadConfig(boolean forceReload) {
		FileConfiguration config = plugin.getConfig();
		
		// Automatically copy defaults
		if (!getFile().exists()) {
			if (config != null)
				config.options().copyDefaults(true);
			plugin.saveDefaultConfig();
			// Inform the user
			plugin.getLogger().info("Created default configuration.");
		}
		// Reload the configuration
		if (forceReload) {
			plugin.reloadConfig();
			config = plugin.getConfig();
		}
		
		serverClock = loadClock(config.getConfigurationSection(CONFIG_SERVER_CLOCK));
		clientClock = loadClock(config.getConfigurationSection(CONFIG_CLIENT_CLOCK));
	}
	
	/**
	 * Save the current configuration.
	 */
	public void saveConfig() {
		FileConfiguration config = plugin.getConfig();
		saveClock(config.createSection(CONFIG_CLIENT_CLOCK), clientClock);
		saveClock(config.createSection(CONFIG_SERVER_CLOCK), serverClock);
		plugin.saveConfig();
	}
	
	/**
	 * Retrieve the fixed server time.
	 * @return The fixed server time, or NULL if not fixed.
	 */
	public TimeOfDay getServerTime() {
		return serverClock.getOrigin();
	}
		
	/**
	 * Retrieve the server time tick rate.
	 * @return The tick rate of the server.
	 */
	public double getServerSpeed() {
		return serverClock.getTickRate();
	}
	
	/**
	 * Retrieve the fixed client time.
	 * @return The fixed client time, or NULL if not fixed.
	 */
	public TimeOfDay getClientTime() {
		return clientClock.getOrigin();
	}
	
	/**
	 * Retrieve the client time tick rate.
	 * @return The tick rate of the client.
	 */
	public double getClientSpeed() {
		return clientClock.getTickRate();
	}
	
	/**
	 * Set the current fixed server time.
	 * @param serverTime - the new fixed server time.
	 */
	public void setServerTime(TimeOfDay serverTime) {
		this.serverClock = serverClock.withOrigin(serverTime);
	}
	
	/**
	 * Set the server time tick rate.
	 * <p>
	 * Use a rate of zero to lock down the time.
	 * @param serverSpeed - the new server tick rate.
	 */
	public void setServerSpeed(double serverSpeed) {
		this.serverClock = serverClock.withSpeed(serverSpeed);
	}
	
	/**
	 * Set the current fixed client time.
	 * @param clientTime - the new fixed client time.
	 */
	public void setClientTime(TimeOfDay clientTime) {
		this.clientClock = clientClock.withOrigin(clientTime);
	}
		
	/**
	 * Set the client time tick rate.
	 * <p>
	 * Use a rate of zero to lock down the time.
	 * @param clientSpeed - the new client tick rate.
	 */
	public void setClientSpeed(double clientSpeed) {
		this.clientClock = clientClock.withSpeed(clientSpeed);
	}
	
	/**
	 * Retrieve the client clock used to calculate its current time.
	 * @return Client clock.
	 */
	public Clock getClientClock() {
		return clientClock;
	}
	
	/**
	 * Retrieve the server clock used to calculate its current time.
	 * @return Server clock.
	 */
	public Clock getServerClock() {
		return serverClock;
	}
		
	/**
	 * Retrieve a reference to the configuration file.
	 * @return Configuration file on disk.
	 */
	public File getFile() {
		return new File(plugin.getDataFolder(), "config.yml");
	}
	
	/**
	 * Save a clock at a given destination section,
	 * @param destination - the destination section.
	 * @param clock - the clock to save.
	 */
	private void saveClock(ConfigurationSection destination, Clock clock) {
		savePreset(
			destination.createSection(CONFIG_CLOCK_DAYLIGHT), clock.getPreset());
		destination.set(CONFIG_CLOCK_TIME, clock.getOrigin());
		destination.set(CONFIG_CLOCK_SPEED, clock.getTickRate());
	}
	
	/**
	 * Load a clock from a given section.
	 * @param section - the source section.
	 * @return The loaded clock.
	 */
	private Clock loadClock(ConfigurationSection section) {
		if (section == null)
			return Clock.defaultClock();
		
		// Load the clock attributes
		DaylightPreset preset = loadPreset(section.getConfigurationSection(CONFIG_CLOCK_DAYLIGHT));
		TimeOfDay time = loadTime(section, CONFIG_CLOCK_TIME, TimeOfDay.MORNING);
		double speed = section.getDouble(CONFIG_CLOCK_SPEED, 1.0);
		
		return new Clock(preset, time, speed);
	}
	
	/**
	 * Load a daylight preset from a section.
	 * <p>
	 * Returns a default preset if the section is empty.
	 * @param section - the section to load from.
	 * @return The daylight preset.
	 */
	private DaylightPreset loadPreset(ConfigurationSection section) {
		if (section != null) {
			return DaylightPreset.newPreset(
				section.getDouble(CONFIG_PRESET_DAY, 0),
				section.getDouble(CONFIG_PRESET_EVENING, 0),
				section.getDouble(CONFIG_PRESET_NIGHT, 0),
				section.getDouble(CONFIG_PRESET_DAWN, 0)
			);
		} else {
			return DaylightPreset.defaultPreset();
		}
	}
	
	/**
	 * Save the daylight preset in the given section.
	 * @param section - the destination section.
	 * @param preset - the preset to save.
	 */
	private void savePreset(ConfigurationSection section, DaylightPreset preset) {
		section.set(CONFIG_PRESET_DAY, preset.getDay());
		section.set(CONFIG_PRESET_EVENING, preset.getEvening());
		section.set(CONFIG_PRESET_NIGHT, preset.getNight());
		section.set(CONFIG_PRESET_DAWN, preset.getDawn());
	}
	
	/**
	 * Load the time from a given configuration section.
	 * @param parent - the root node.
	 * @param key - the node name.
	 * @return The time of day, or NULL if not present.
	 */
	private TimeOfDay loadTime(ConfigurationSection parent, String key, TimeOfDay defaultValue) {
		Object value = parent.get(key);
		
		try {
			// Don't parse missing values
			if (value != null) {
				return CommandTimeParser.parse(String.valueOf(value));
			}
		} catch (NumberFormatException e) {
			plugin.getLogger().warning("Cannot load time: " + value);
		}
		return null;
	}
}
