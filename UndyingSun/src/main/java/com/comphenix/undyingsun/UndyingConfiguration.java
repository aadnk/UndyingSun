package com.comphenix.undyingsun;

import java.io.File;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import com.comphenix.undyingsun.CommandTimeParser.TimeOfDay;

public class UndyingConfiguration {
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
		
		this.serverTime = loadTime(config, CONFIG_SERVER_TIME);
		this.clientTime = loadTime(config, CONFIG_CLIENT_TIME);
		this.serverSpeed = config.getDouble(CONFIG_SERVER_SPEED, 0);
		this.clientSpeed = config.getDouble(CONFIG_CLIENT_SPEED, 0);
	}
	
	/**
	 * Save the current configuration.
	 */
	public void saveConfig() {
		FileConfiguration config = plugin.getConfig();
		config.set(CONFIG_SERVER_TIME, serverTime != null ? serverTime.getAlias() : null);
		config.set(CONFIG_CLIENT_TIME, clientTime != null ? clientTime.getAlias() : null);
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
	 * Retrieve the fixed client time.
	 * @return The fixed client time, or NULL if not fixed.
	 */
	public TimeOfDay getClientTime() {
		return clientTime;
	}
	
	/**
	 * Retrieve the server time tick rate.
	 * @return The tick rate of the server.
	 */
	public double getServerSpeed() {
		return serverSpeed;
	}
	
	/**
	 * Retrieve the client time tick rate.
	 * @return The tick rate of the client.
	 */
	public double getClientSpeed() {
		return clientSpeed;
	}
	
	/**
	 * Set the current fixed client time.
	 * @param clientTime - the new fixed client time.
	 */
	public void setClientTime(TimeOfDay clientTime) {
		this.clientTime = clientTime;
	}
	
	/**
	 * Set the current fixed server time.
	 * @param serverTime - the new fixed server time.
	 */
	public void setServerTime(TimeOfDay serverTime) {
		this.serverTime = serverTime;
	}
	
	/**
	 * Set the client time tick rate.
	 * <p>
	 * Use a rate of zero to lock down the time.
	 * @param clientSpeed - the new client tick rate.
	 */
	public void setClientSpeed(double clientSpeed) {
		this.clientSpeed = clientSpeed;
	}
	
	/**
	 * Retrieve the client clock used to calculate its current time.
	 * @return Client clock.
	 */
	public Clock getClientClock() {
		return new Clock(getClientTime(), getClientSpeed());
	}
	
	/**
	 * Retrieve the server clock used to calculate its current time.
	 * @return Server clock.
	 */
	public Clock getServerClock() {
		return new Clock(getServerTime(), getServerSpeed());
	}
	
	/**
	 * Set the server time tick rate.
	 * <p>
	 * Use a rate of zero to lock down the time.
	 * @param serverSpeed - the new server tick rate.
	 */
	public void setServerSpeed(double serverSpeed) {
		this.serverSpeed = serverSpeed;
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