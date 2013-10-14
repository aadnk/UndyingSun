package com.comphenix.undyingsun.packets;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Represents a TimeSetter that uses the Bukkit API to modify each player's time.
 * @author Kristian
 */
public class TimeSetter extends TimeInterceptor {
	public TimeSetter(Plugin plugin) {
		super(plugin);
	}

	/**
	 * Update the client-side time of the given player.
	 * @param player - the player to update.
	 */
	public void update(Player player) {
		try {
			World world = player.getWorld();
			long totalTime = world.getFullTime();
			long relativeTime = world.getTime();
			long changedTime = invokeListeners(player, totalTime, relativeTime);
			
			System.out.printf("Before: %s After: %s\n", totalTime, changedTime);
			
			if (relativeTime != changedTime) {
				player.setPlayerTime(changedTime, false);
			}
			
		} catch (Exception e) {
			throw new RuntimeException("Cannot update player time.", e);
		}
	}
	
	/**
	 * Update the client-side time of every player on the server.
	 */
	public void update() {
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			update(player);
		}
	}
	
	@Override
	public void close() {
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			player.resetPlayerTime();
		}
	}
}
