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

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.StructureModifier;

class InterceptorProtocolLib extends TimeInterceptor {
	// The current listener
	protected PacketListener listener;
	
	/**
	 * Construct a new time interceptor for ProtocolLib.
	 * @param plugin - the parent plugin.
	 */
	public InterceptorProtocolLib(Plugin plugin) {
		super(plugin);
		listener = constructListener(plugin);
		ProtocolLibrary.getProtocolManager().addPacketListener(listener);
	}
	
	private PacketListener constructListener(Plugin plugin) {
		return new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, Packets.Server.UPDATE_TIME) {
			@Override
			public void onPacketSending(PacketEvent event) {
				final StructureModifier<Long> longs = event.getPacket().getLongs();
				long totalTime = longs.read(0);
				long relativeTime = longs.read(1);
				
				try {
					longs.write(1, invokeListeners(event.getPlayer(), totalTime, relativeTime));
				} catch (Exception e) {
					throw new RuntimeException("Unable to process time packet.", e);
				}
			}
		};
	}
	
	@Override
	public void close() {
		if (listener != null) {
			ProtocolLibrary.getProtocolManager().removePacketListener(listener);
			listener = null;
		}
	}
}
