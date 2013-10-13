package com.comphenix.undyingsun;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.comphenix.undyingsun.CommandTimeParser.TimeOfDay;
import com.comphenix.undyingsun.packets.TimeInterceptor;
import com.comphenix.undyingsun.packets.TimeInterceptor.TimeListener;

public class UndyingSunPlugin extends JavaPlugin implements TimeListener {
	public static final String PERMISSION_EXCEMPT = "undyingsun.excempt";
	
	/**
	 * The number of ticks per second.
	 */
	public static final int TICKS_PER_SECOND = 20;
		
	/**
	 * The number of seconds to wait until we update the time.
	 */
	public static final int UPDATE_DELAY = 8;
	
	// The configuration
	private UndyingConfiguration config;
	private BukkitTask timeLock;
	
	// Packet interception
	private TimeInterceptor interceptor;
	
	@Override
	public void onEnable() {
		// Prepare configuration
		config = new UndyingConfiguration(this);
		
		// Setup command(s)
		registerTabExecutor(CommandUndying.NAME, new CommandUndying(config));
		
		// Tell the console
		getLogger().info( "Server time: " + TimeOfDay.toTimeString(config.getServerTime()) );
		getLogger().info( "Client time: " + TimeOfDay.toTimeString(config.getClientTime()) );
		
		// Set up interception
		registerPacketHandler();
		registerTimeLock(UPDATE_DELAY);
	}

	private void registerPacketHandler() {
		// Choose the correct method
		if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
			interceptor = TimeInterceptor.fromProtocolLib(this);
			getLogger().info("ProtocolLib detected!");
		} else {
			interceptor = TimeInterceptor.fromQueuedPackets(this);
			getLogger().info("Intercepting packets manually.");
		}
		interceptor.addTimeListener(this);
	}
	
	private void registerTimeLock(int updateDelay) {
		// Run every second
		timeLock = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() {
				if (config.getServerTime() != null) {
					// Update all loaded worlds
					for (World world : getServer().getWorlds()) {
						int time = config.getServerClock().get(world.getFullTime());
						world.setTime(time);
					}
				}
			}
		}, 0, updateDelay * TICKS_PER_SECOND);
	}
	
	@Override
	public long onTimeSending(Player reciever, long totalTime, long relativeTime) {
		// Only if ProtocolLib is present
		if (!reciever.hasPermission(PERMISSION_EXCEMPT)) {
			// Change the perceived time
			if (config.getClientTime() != null) {
				return config.getClientClock().get(totalTime);
			}
		}
		return relativeTime;
	}
	
	private void registerTabExecutor(String name, TabExecutor executor) {
		PluginCommand command = getCommand(name);
		command.setExecutor(executor);
		command.setTabCompleter(executor);
	}
	
	@Override
	public void onDisable() {
		// Clean up
		if (interceptor != null) {
			interceptor.close();
			interceptor = null;
		}
		if (timeLock != null) {
			timeLock.cancel();
			timeLock = null;
		}
	}
}