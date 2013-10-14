package com.comphenix.undyingsun;

import java.util.Map;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Maps;

/**
 * Represents a class that keeps track of the total elapsed time per world.
 * @author Kristian
 */
class WorldTimer {
	/**
	 * The default number of milliseconds per tick.
	 */
	private static final int MILLISECONDS_PER_TICK = 50;
	
	private Map<World, Long> initialTime = Maps.newConcurrentMap();
	
	public WorldTimer(Plugin plugin) {
		final Server server = plugin.getServer();	

		// Register world tracking
		server.getPluginManager().registerEvents(new Listener() {
			@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
			public void onWorldLoaded(WorldLoadEvent e) {
				handleLoaded(e.getWorld());
			}
			
			@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
			public void onWorldUnloaded(WorldUnloadEvent e) {
				handleUnloaded(e.getWorld());
			}
		}, plugin);
		
		// Handle existing worlds
		for (World world : server.getWorlds()) {
			handleLoaded(world);
		}
	}
	
	/**
	 * Retrieve the current time of a world in ticks.
	 * @param world - the world to check.
	 * @return The current time.
	 */
	public long getWorldTime(World world) {
		if (!initialTime.containsKey(world))
			handleLoaded(world);
		return currentTimeTicks() - initialTime.get(world);
	}
	
	/**
	 * Handle a loaded world.
	 * @param world - the world that have loaded.
	 */
	private void handleLoaded(World world) {
		// Save the time (measured since 1970) the world started
		initialTime.put(world, currentTimeTicks() - world.getFullTime());
	}
	
	/**
	 * Handle an unloaded world. 
	 * @param world - the unloaded world.
	 */
	private void handleUnloaded(World world) {
		Long time = initialTime.remove(world);
		
		if (time != null) {
			// Save the correct elapsed time
			world.setFullTime(currentTimeTicks() - time);
		}
	}

	/**
	 * Clear all resources.
	 */
	public void close() {
		initialTime.clear();
	}
	
	/**
	 * Retrieve the current time in game ticks since 1. January 1970.
	 * @return Number of ticks since that date.
	 */
	private long currentTimeTicks() {
		return System.currentTimeMillis() / MILLISECONDS_PER_TICK;
	}
}
