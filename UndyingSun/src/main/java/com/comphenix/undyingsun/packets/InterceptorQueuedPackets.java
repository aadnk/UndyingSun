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

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Primitives;

// You can do most of this already with setPlayerTime(), but you end up with a slightly annoying
// "wobbling" of the sun/moon as you cannot stop the passage of time on the client without 
// changing the gameRule doDayNightCycle. This setting is global, so we can't use it.
class InterceptorQueuedPackets extends TimeInterceptor implements Listener {
	/**
	 * Represents a field set operation.
	 * @author Kristian
	 */
	private static class FieldSetter {
		private final Field field;
		private final Object target;
		private final Object newValue;
		
		// The old value
		private final WeakReference<Object> oldValue;
		
		public FieldSetter(Field field, Object target, Object value) throws IllegalAccessException {
			this.field = field;
			this.target = target;
			this.newValue = value;
			this.oldValue = new WeakReference<Object>(field.get(target));
		}
		
		/**
		 * Determine if the current field value has changed since the FieldSetter was created.
		 * @return TRUE if it has, FALSE otherwise.
		 */
		public boolean hasChanged() throws IllegalAccessException {
			return Objects.equal(oldValue.get(), getCurrentValue());
		}
		
		/**
		 * Retrieve the current value of the field.
		 * @return The current value.
		 */
		public Object getCurrentValue() throws IllegalAccessException {
			return field.get(target);
		}
		
		/**
		 * Apply the field operation.
		 * @throws IllegalAccessException 
		 * @throws IllegalArgumentException 
		 */
		public void apply() throws IllegalArgumentException {
			try {
				field.set(target, newValue);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Unable to access field.", e);
			}
		}
	}
	
	// Method for injecting players
	private Method getHandleMethod;
	private Field connectionField;
	private Field networkField;
	private Field highPriorityQueueField;
	private Field lowPriorityQueueField;

	// Packet fields
	private Field fullTimeField;
	private Field relativeTimeField;
	
	// The packet class
	private Class<?> timePacket;
	private Multimap<Player, FieldSetter> revertOperations = ArrayListMultimap.create();
	
	// Whether or not we have detected interfering plugins
	private boolean detectedInterference;
	
	public InterceptorQueuedPackets(Plugin plugin) {
		// Register this as a listener
		super(plugin);
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}

	/**
	 * Invoked when we have intercepted a packet.
	 * @param packet - the packet to intercept.
	 */
	private Object interceptPacket(Player player, Object packet) {
		Class<?> clazz = packet.getClass();
		
		if (timePacket == null && clazz.getSimpleName().equals("Packet4UpdateTime")) {
			timePacket = clazz;
		}
		if (timePacket != null && timePacket.isAssignableFrom(clazz)) {
			// Setup fields
			for (Field field : timePacket.getDeclaredFields()) {
				if (Primitives.unwrap(field.getType()).equals(long.class)) {
					if (fullTimeField == null)
						fullTimeField = field;
					else
						relativeTimeField = field;
				}
			}
			
			try {
				relativeTimeField.set(packet, invokeListeners(
					player, fullTimeField.getLong(packet), relativeTimeField.getLong(packet)
				));
			} catch (Exception e) {
				// Clean up
				e.printStackTrace();
				uninjectPlayer(player);
			}
		}
		return packet;
	}
	
	@SuppressWarnings("unchecked")
	private void injectPlayer(Player player) throws Exception {
		// Cannot inject twice
		if (revertOperations.containsKey(player))
			throw new IllegalArgumentException("Cannot inject "+ player + "twice");
		
		Object nmsPlayer = getNmsPlayer(player);
		
		if (connectionField == null)
			connectionField = Reflection.getField(nmsPlayer, nmsPlayer.getClass(), "playerConnection");
		Object connection = connectionField.get(nmsPlayer);
		
		if (networkField == null)
			networkField = Reflection.getField(connection, connection.getClass(), "networkManager");
		Object networkManager = networkField.get(connection);
		
		if (highPriorityQueueField == null)
			highPriorityQueueField = Reflection.getField(networkManager, networkManager.getClass(), "highPriorityQueue");
		if (lowPriorityQueueField == null)
			lowPriorityQueueField = Reflection.getField(networkManager, networkManager.getClass(), "lowPriorityQueue");
		List<Object> highPriorityQueue = (List<Object>) highPriorityQueueField.get(networkManager);
		List<Object> lowPriorityQueue = (List<Object>) lowPriorityQueueField.get(networkManager);
		
		// Save old values
		revertOperations.put(player, new FieldSetter(highPriorityQueueField, networkManager, highPriorityQueue));
		revertOperations.put(player, new FieldSetter(lowPriorityQueueField, networkManager, lowPriorityQueue));
		
		// Proxy the lists
		highPriorityQueueField.set(networkManager, new ProxyList(player, highPriorityQueue));
		lowPriorityQueueField.set(networkManager, new ProxyList(player, lowPriorityQueue));
	}
	
	private void uninjectPlayer(Player player) {
		for (FieldSetter setter : revertOperations.removeAll(player)) {
			setter.apply();
		}
	}
	
	private Object getNmsPlayer(Player player) throws Exception {
		if (getHandleMethod == null) {
			getHandleMethod = Reflection.getMethod(0, Modifier.STATIC, player.getClass(), "getHandle");
		}
		return getHandleMethod.invoke(player);
	}
	
	@Override
	public void close() {
		// Clear as a listener
		HandlerList.unregisterAll(this);
		
		// Revert all proxy lists
		for (FieldSetter setter : revertOperations.values()) {
			setter.apply();
		}
		revertOperations.clear();
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e) {
		final Player player = e.getPlayer();
		
		// Wait until the playerConnection has been assigned
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					injectPlayer(player);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 1L);
		
		// Wait a second and see if the lists remain the same
		if (!detectedInterference) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					// Check every field
					for (FieldSetter setter : revertOperations.get(player)) {
						try {
							if (setter.hasChanged()) {
								detectInterference(setter);
								return;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}, 20L);
		}
	}
	
	/**
	 * Invoked when we have detected interference.
	 */
	protected void detectInterference(FieldSetter setter) throws IllegalAccessException {
		plugin.getLogger().warning("Detected interfering plugin(s). Field value: " + setter.getCurrentValue());
		plugin.getLogger().warning("Please install ProtocolLib.");
		detectedInterference = true;
	}
	
	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent e) {
		uninjectPlayer(e.getPlayer());
	}
	
	/**
	 * Represents a list that intercepts all insertions.
	 * @author Kristian
	 */
	private class ProxyList extends ForwardingList<Object> {
		private Player player;
		private List<Object> original;

		public ProxyList(Player player, List<Object> original) {
			this.player = player;
			this.original = original;
		}

		@Override
		protected List<Object> delegate() {
			return original;
		}
		
		@Override
		public boolean add(Object element) {
			return super.add(interceptPacket(player, element));
		}
		
		@Override
		public boolean addAll(Collection<? extends Object> collection) {
			return super.addAll(process(collection));
		}
		
		@Override
		public void add(int index, Object element) {
			super.add(index, interceptPacket(player, element));
		}
		
		@Override
		public Object set(int index, Object element) {
			return super.set(index, interceptPacket(player, element));
		}
		
		@Override
		public boolean addAll(int index, Collection<? extends Object> elements) {
			return super.addAll(index, process(elements));
		}
		
		private Collection<Object> process(Collection<? extends Object> iterable) {
			return Collections2.transform(iterable, new Function<Object, Object>() {
				@Override
				public Object apply(@Nullable Object packet) {
					return interceptPacket(player, packet);
				}
			});
		}
	}
}
