package net.zyuiop.HangoverGames.Arena;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.zyuiop.HangoverGames.HangoverGames;

public class ArenasManager {
	
	private HangoverGames plugin;
	private HashMap<String, Arena> arenas = new HashMap<String, Arena>();
	
	private HashMap<UUID, String> joinWait = new HashMap<UUID, String>();
	
	public ArenasManager(HangoverGames plugin) {
		this.plugin = plugin;
	}
	
	public HashMap<String, Arena> getArenas() {
		return arenas;
	}
	
	public HashMap<UUID, String> attempts() {
		return joinWait;
	}
	
	public boolean isAttempted(VirtualPlayer player) {
		return joinWait.containsKey(player.getPlayerID());
	}
	
	public String prepareJoin(UUID plid, String arena) {
		final VirtualPlayer player = new VirtualPlayer(plid);
		System.out.println("Trying to add player "+plid+" in arena "+arena);
		if (getPlayerArena(player) != null) {
			return ChatColor.RED+"Vous êtes déjà dans une arène.";
		}
		if (joinWait.containsKey(player.getPlayerID())) {
			System.out.println("Returned good : player already in list.");
			return "good";
		}
		Arena ar = arenas.get(arena);
		if (ar == null) {
			return ChatColor.RED+"Une erreur s'est produite : l'arène demandée n'existe pas";
		}
		if (!ar.canJoin()) {
			return ChatColor.RED+"Il est impossible de rejoindre l'arène pour le moment.";
		}
		System.out.println("Added player !");
		joinWait.put(player.getPlayerID(), arena);
		Bukkit.getScheduler().runTaskLaterAsynchronously(HangoverGames.instance,
				new Runnable() { public void run() { String val = joinWait.remove(player.getPlayerID()); System.out.println("Removed "+val); } }, 5*20L);
		return "good";
	}
	
	public Arena getArena(String name) {
		return arenas.get(name);
	}
	
	public Arena getPlayerArena(VirtualPlayer player) {
		for (Arena ar : arenas.values()) 
			if (ar.isPlaying(player)) return ar;
		return null;
	}
	
	public Arena getPlayerArena(UUID player) {
		return getPlayerArena(new VirtualPlayer(player));
	}
	
	public void loadArenas() {
		Bukkit.getLogger().info("Loading arenas...");
		for (World w : Bukkit.getWorlds()) {
			File folder = new File(w.getWorldFolder(), "arenas");
			if (!folder.exists()) {
				Bukkit.getLogger().warning("Arenas load failed for world "+w.getName()+" : folder "+folder.getAbsolutePath()+" not found !");
				continue;
			}
			
			for (File arena : folder.listFiles()) {
				Bukkit.getLogger().info("[ArenaLoad]["+w.getName()+"] Found arena "+arena.getName()+", attempting to load.");
				YamlConfiguration arenaData = YamlConfiguration.loadConfiguration(arena);
				if (arenaData == null) {
					Bukkit.getLogger().warning("[ArenaLoad]["+w.getName()+"] Failed to load "+arena.getName()+" !");
					continue;
				}
				
				Arena nw = new Arena();
				nw.dataSource = arena;
				nw.arenaId = UUID.fromString(arenaData.getString("uuid", UUID.randomUUID().toString()));
				// Sécurité : 
				arenaData.set("uuid", nw.arenaId.toString());
				try {
					arenaData.save(arena);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				nw.arenaName = arenaData.getString("name");
				nw.mapName = arenaData.getString("mapname");
				nw.maxPlayers = arenaData.getInt("max-players");
				nw.minPlayers = arenaData.getInt("min-players");
				
				String spawnString = arenaData.getString("spawn-location");
				String[] parts = spawnString.split(";");
				if (parts.length < 3) {
					Bukkit.getLogger().warning("[ArenaLoad]["+w.getName()+"] Failed to load "+arena.getName()+" : spawn location is wrong !");
					continue;
				}
				Location loc = null;
				if (parts.length == 5) {
					loc = new Location(w, Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Float.parseFloat(parts[3]), Float.parseFloat(parts[4]));
				} else {
					loc = new Location(w, Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
				}
				nw.spawn = loc;
				
				nw.cauldrons = new ArrayList<Location>();
				for (Object lineObj : arenaData.getList("cauldrons")) {
					String line = (String)lineObj;
					String[] locaparts = line.split(";");
					if (locaparts.length < 3) {
						Bukkit.getLogger().warning("[ArenaLoad]["+w.getName()+"] Invalid cauldron location for "+arena.getName()+"");
					}
					Location cauld = null;
					cauld = new Location(w, Double.parseDouble(locaparts[0]), Double.parseDouble(locaparts[1]), Double.parseDouble(locaparts[2]));
					nw.cauldrons.add(cauld);
				}
				
				arenas.put(nw.arenaName, nw);
				Bukkit.getLogger().info("[ArenaLoad]["+w.getName()+"] Successfully loaded arena "+arena.getName()+" !");
			}
			Bukkit.getLogger().info("[ArenaLoad] Loaded world "+w.getName());
		}
		Bukkit.getLogger().info("[ArenaLoad] Task ended. "+arenas.size()+" arenas loaded.");
	}

	public void disable() {
		for (Arena a : arenas.values()) {
			Bukkit.getLogger().info(">> Disabling arena "+a.arenaName);
			a.endGame();
		}
	}
	
	public String finishJoin(Player p) {
		VirtualPlayer player = new VirtualPlayer(p);
		if (!isAttempted(player)) {
			return "Vous n'êtes pas en attente.";
		} 
		Arena ar = getArena(joinWait.get(player.getPlayerID()));
		joinWait.remove(player.getPlayerID());
		if (ar == null) {
			return "L'arène est introuvable";
		}
		if (!ar.canJoin()) {
			return "Arène non joignable.";
		}
		
		// Tentative d'ajout //
		return ar.addPlayer(player.getPlayer());
	}
}
