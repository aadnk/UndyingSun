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

package com.comphenix.undyingsun.packets;

import java.util.List;
import java.util.concurrent.Callable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Lists;

/**
 * Intercept the time before it is transmitted to the client.
 * @author Kristian
 */
public abstract class TimeInterceptor {
	public interface TimeListener {
		/**
		 * Invoked when a player is recieving a time update packet.
		 * @param reciever - the receiving player.
		 * @param totalTime - the total time.
		 * @param relativeTime - the relative time.
		 * @return The new relative time to send.
		 */
		public long onTimeSending(Player reciever, long totalTime, long relativeTime);
	}
	
	protected List<TimeListener> timeListeners = Lists.newArrayList();
	protected Plugin plugin;
	
	public TimeInterceptor( Plugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Add a new time listener.
	 * @param listener - the listener to add.
	 */
	public void addTimeListener(TimeListener listener) {
		timeListeners.add(listener);
	}
	
	/**
	 * Remove an existing time listener.
	 * @param listener - the listener to remove.
	 */
	public void removeTimeListener(TimeListener listener) {
		timeListeners.remove(listener);
	}
	
	/**
	 * Invoke every listener with the given parameters.
	 * @param reciever - the player reciever.
	 * @param totalTime - the total time.
	 * @param relativeTime - the relative time.
	 * @return The modified relative time.
	 */
	protected long invokeListeners(final Player reciever, final long totalTime, final long relativeTime) throws Exception {
		// Handle method calls from other threads
		if (Bukkit.isPrimaryThread()) {
			return processListeners(reciever, totalTime, relativeTime);
		} else {
			return Bukkit.getScheduler().callSyncMethod(plugin, new Callable<Long>() {
				@Override
				public Long call() throws Exception {
					return processListeners(reciever, totalTime, relativeTime);
				}
			}).get();
		}
	}
	
	private long processListeners(Player reciever, final long totalTime, long relativeTime) {
		// Process on each listener
		for (TimeListener listener : timeListeners) {
			relativeTime = listener.onTimeSending(reciever, totalTime, relativeTime);
		}
		return relativeTime;
	}
	
	/**
	 * Close the current interceptor.
	 */
	public abstract void close();
	
	/**
	 * Construxt a new time interceptor that uses ProtocolLib to do its dirty work.
	 * @param plugin - the current plugin.
	 * @return The new interceptor.
	 */
	public static TimeInterceptor fromProtocolLib(Plugin plugin) {
		return new InterceptorProtocolLib(plugin);
	}
	
	/**
	 * Construct a new time interceptor that injects itself into the queued packet lists.
	 * @param plugin - the current plugin.
	 * @return The new time interceptor.
	 */
	public static TimeInterceptor fromQueuedPackets(Plugin plugin) {
		return new InterceptorQueuedPackets(plugin);
	}
}
