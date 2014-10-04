package net.zyuiop.HangoverGames.Arena;

import net.samagames.network.client.GameArena;
import net.samagames.network.client.GameArenaManager;
import net.samagames.network.json.Status;
import net.zyuiop.HangoverGames.HangoverGames;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class ArenasManager extends GameArenaManager {
	
	private HangoverGames plugin;
	public ArenasManager(HangoverGames plugin) {
        this.plugin = plugin;
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

                UUID arenaId = UUID.fromString(arenaData.getString("uuid", UUID.randomUUID().toString()));
                String mapName = arenaData.getString("mapname");
                int maxPlayers = arenaData.getInt("max-players");
                Arena nw = new Arena(maxPlayers, 2, mapName, arenaId);
                nw.dataSource = arena;
                nw.setStatus(Status.Available);

                // Sécurité :
                arenaData.set("uuid", nw.getArenaID().toString());
                try {
                    arenaData.save(arena);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
				
				arenas.put(nw.getArenaID(), nw);
				Bukkit.getLogger().info("[ArenaLoad]["+w.getName()+"] Successfully loaded arena "+arena.getName()+" !");
			}
			Bukkit.getLogger().info("[ArenaLoad] Loaded world "+w.getName());
		}
		Bukkit.getLogger().info("[ArenaLoad] Task ended. "+arenas.size()+" arenas loaded.");
	}

	public void disable() {
		for (GameArena a : arenas.values()) {
			Bukkit.getLogger().info(">> Disabling arena "+a.getMapName());
            ((Arena)a).endGame();
		}
	}
}
