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

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.undyingsun.packets.TimeInterceptor;
import com.comphenix.undyingsun.packets.TimeInterceptor.TimeListener;
import com.comphenix.undyingsun.packets.TimeSetter;
import com.comphenix.undyingsun.temporal.Clock;
import com.comphenix.undyingsun.temporal.TimeOfDay;

public class UndyingSunPlugin extends JavaPlugin implements TimeListener {
	public static final String PERMISSION_EXEMPT = "undyingsun.exempt";
	
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
	
	// Track the elapsed time per world
	private WorldTimer worldTimer;
	
	// Non-positive delay permanently disables the server clock
	private int serverClockDelay = TICKS_PER_SECOND;
	
	// Packet interception
	private TimeInterceptor interceptor;
	
	@Override
	public void onEnable() {
		// Prepare configuration
		config = new UndyingConfiguration(this);
		worldTimer = new WorldTimer(this);
		
		// Setup command(s)
		registerTabExecutor(CommandUndying.NAME, new CommandUndying(config));
		
		// Tell the console
		getLogger().info( "Server time: " + TimeOfDay.toTimeString(config.getServerTime()) );
		getLogger().info( "Client time: " + TimeOfDay.toTimeString(config.getClientTime()) );
				
		// Setup client-side clock
		registerPacketHandler();
		
		// Setup server-side clock
		onUpdateServerTime();
	}

	private void registerPacketHandler() {
		try {
			// Choose the correct method
			if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
				interceptor = TimeInterceptor.fromProtocolLib(this);
				getLogger().info("ProtocolLib detected!");
			} else {
				interceptor = TimeInterceptor.fromQueuedPackets(this);
				getLogger().info("Intercepting packets manually.");
			}
			
		} catch (Exception e) {
			// Fail gracefully
			getLogger().warning("Cannot register packet handler. Reverting to native Bukkit.");
			e.printStackTrace();
			
			// This should work - hopefully
			interceptor = new TimeSetter(this);
		}
		// Add this class as a listener
		interceptor.addTimeListener(this);
	}
	
	/**
	 * Invoked when we need to update the server time.
	 */
	private void onUpdateServerTime() {
		if (serverClockDelay <= 0)
			return;
		
		// Update the time if needed
		if (!config.getServerClock().isDefault()) {
			// Update all loaded worlds
			for (World world : getServer().getWorlds()) {
				long fullTime = worldTimer.getWorldTime(world);
				long time = config.getServerClock().get(fullTime);
				world.setTime(time);
			}
		}
		
		// Update setter
		if (interceptor instanceof TimeSetter) {
			((TimeSetter) interceptor).update();
		}
		
		// Speed or slow down delay
		checkClockDelay();
		
		// Reschedule check
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				onUpdateServerTime();
			}
		}, serverClockDelay);
	}
	
	private void checkClockDelay() {
		if (serverClockDelay > 0) {
			// See if we really need frequent updates
			if (hasCustomRunning(config.getClientClock()) || hasCustomRunning(config.getServerClock()) ) 
				serverClockDelay = 1;
			else
				serverClockDelay = TICKS_PER_SECOND;
		}
	}
	
	private boolean hasCustomRunning(Clock clock) {
		return !clock.isDefault() && clock.isRunning();
	}
	
	@Override
	public long onTimeSending(Player reciever, long totalTime, long relativeTime) {
		// Only if ProtocolLib is present
		if (!reciever.hasPermission(PERMISSION_EXEMPT)) {
			// Change the perceived time
			if (!config.getClientClock().isDefault()) {
				Clock clock = config.getClientClock();
				long fullTime = worldTimer.getWorldTime(reciever.getWorld());
				
				// The gamerule doDaylightCycle is encoded in the sign bit
				return clock.get(fullTime) * (clock.isRunning() ? 1 : -1);
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
		if (worldTimer != null) {
			worldTimer.close();
			worldTimer = null;
		}
		
		// Cancel server update
		serverClockDelay = 0;
	}
}