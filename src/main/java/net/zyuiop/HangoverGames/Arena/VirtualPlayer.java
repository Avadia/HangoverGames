package net.zyuiop.HangoverGames.Arena;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class VirtualPlayer {
	private UUID playerID;
	
	public VirtualPlayer(Player p) {
		this.playerID = p.getUniqueId();
	}
	
	public VirtualPlayer(UUID p) {
		this.playerID = p;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Player) {
			return (playerID.equals(((Player) other).getUniqueId()));
		} else if (other instanceof UUID) {
			return (playerID.equals((UUID) other));
		} else if (other instanceof VirtualPlayer || other.getClass().equals(VirtualPlayer.class)) {
			VirtualPlayer pl = (VirtualPlayer) other;
			return (playerID.equals(pl.getPlayerID()));
		} else {
			return false;
		}
	}
	
	public Player getPlayer() {
		return Bukkit.getPlayer(playerID);
	}
	
	public UUID getPlayerID() {
		return playerID;
	}
}
