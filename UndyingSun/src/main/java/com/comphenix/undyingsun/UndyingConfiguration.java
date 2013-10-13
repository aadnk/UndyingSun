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
import com.comphenix.undyingsun.temporal.TimeOfDay;


class UndyingConfiguration {
	private static final String CONFIG_CLIENT_TIME = "client.time";
	private static final String CONFIG_SERVER_TIME = "server.time";
	private static final String CONFIG_CLIENT_SPEED = "client.speed";
	private static final String CONFIG_SERVER_SPEED = "server.speed";
	
	private Plugin plugin;

	// The configurations
	private TimeOfDay serverTime;
	private TimeOfDay clientTime;
	private double serverSpeed;
	private double clientSpeed;
	
	private Clock serverClock;
	private Clock clientClock;
	
	public UndyingConfiguration(Plugin plugin) {
		this.plugin = plugin;
		reloadConfig();
	}

	/**
	 * Reload configuration file.
	 */
	public void reloadConfig() {
		FileConfiguration config = plugin.getConfig();
		
		// Automatically copy defaults
		if (!getFile().exists()) {
			if (config != null)
				config.options().copyDefaults(true);
			plugin.saveDefaultConfig();
			plugin.reloadConfig();
			config = plugin.getConfig();
			
			// Inform the user
			plugin.getLogger().info("Created default configuration.");
		}
		
		setServerTime(loadTime(config, CONFIG_SERVER_TIME));
		setClientTime(loadTime(config, CONFIG_CLIENT_TIME));
		setServerSpeed(config.getDouble(CONFIG_SERVER_SPEED, 0));
		setClientSpeed(config.getDouble(CONFIG_CLIENT_SPEED, 0));
	}
	
	/**
	 * Save the current configuration.
	 */
	public void saveConfig() {
		FileConfiguration config = plugin.getConfig();
		config.set(CONFIG_SERVER_TIME, serverTime != null ? serverTime.getAlias() : "none");
		config.set(CONFIG_CLIENT_TIME, clientTime != null ? clientTime.getAlias() : "none");
		config.set(CONFIG_SERVER_SPEED, serverSpeed);
		config.set(CONFIG_CLIENT_SPEED, clientSpeed);
		plugin.saveConfig();
	}
	
	/**
	 * Retrieve the fixed server time.
	 * @return The fixed server time, or NULL if not fixed.
	 */
	public TimeOfDay getServerTime() {
		return serverTime;
	}
		
	/**
	 * Retrieve the server time tick rate.
	 * @return The tick rate of the server.
	 */
	public double getServerSpeed() {
		return serverSpeed;
	}
	
	/**
	 * Retrieve the fixed client time.
	 * @return The fixed client time, or NULL if not fixed.
	 */
	public TimeOfDay getClientTime() {
		return clientTime;
	}
	
	/**
	 * Retrieve the client time tick rate.
	 * @return The tick rate of the client.
	 */
	public double getClientSpeed() {
		return clientSpeed;
	}
	
	/**
	 * Set the current fixed server time.
	 * @param serverTime - the new fixed server time.
	 */
	public void setServerTime(TimeOfDay serverTime) {
		this.serverTime = serverTime;
		updateServerClock();
	}
	
	/**
	 * Set the server time tick rate.
	 * <p>
	 * Use a rate of zero to lock down the time.
	 * @param serverSpeed - the new server tick rate.
	 */
	public void setServerSpeed(double serverSpeed) {
		this.serverSpeed = serverSpeed;
		updateServerClock();
	}
	
	/**
	 * Set the current fixed client time.
	 * @param clientTime - the new fixed client time.
	 */
	public void setClientTime(TimeOfDay clientTime) {
		this.clientTime = clientTime;
		updateClientClock();
	}
		
	/**
	 * Set the client time tick rate.
	 * <p>
	 * Use a rate of zero to lock down the time.
	 * @param clientSpeed - the new client tick rate.
	 */
	public void setClientSpeed(double clientSpeed) {
		this.clientSpeed = clientSpeed;
		updateClientClock();
	}
	
	/**
	 * Recreate the client clock.
	 */
	private void updateClientClock() {
		clientClock = getClientTime() != null ? 
			new Clock(getClientTime(), getClientSpeed()) : 
			Clock.defaultClock();
	}
	
	/**
	 * Recreate the server clock.
	 */
	private void updateServerClock() {
		serverClock = getServerTime() != null ? 
			new Clock(getServerTime(), getServerSpeed()) :
			Clock.defaultClock();
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
	 * Load the time from a given configuration section.
	 * @param parent - the root node.
	 * @param key - the node name.
	 * @return The time of day, or NULL if not present.
	 */
	private TimeOfDay loadTime(ConfigurationSection parent, String key) {
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
