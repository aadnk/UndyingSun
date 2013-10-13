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
