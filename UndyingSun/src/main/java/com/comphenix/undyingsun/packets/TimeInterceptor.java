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
