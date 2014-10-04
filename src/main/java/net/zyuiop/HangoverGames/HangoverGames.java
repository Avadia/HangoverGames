package net.zyuiop.HangoverGames;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import net.samagames.network.Network;
import net.samagames.network.client.GamePlugin;
import net.samagames.permissionsapi.PermissionsAPI;
import net.samagames.permissionsbukkit.PermissionsBukkit;
import net.zyuiop.HangoverGames.Arena.Alcool;
import net.zyuiop.HangoverGames.Arena.ArenasManager;
import net.zyuiop.HangoverGames.Commands.CommandStart;
import net.zyuiop.HangoverGames.Listeners.PlayerListener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HangoverGames extends GamePlugin {

	public static HangoverGames instance;
    public PermissionsAPI api;

    public HangoverGames() {
        super("hangovergames");
    }

    @Override
	public void onEnable() {
        super.onEnable();

		HangoverGames.instance = this;
		
		this.saveDefaultConfig();
		int comPort = getConfig().getInt("com-port");
		String BungeeName = getConfig().getString("BungeeName");

        PermissionsBukkit plugin = (PermissionsBukkit) this.getServer().getPluginManager().getPlugin("SamaPermissionsBukkit");
        api = plugin.getApi();
		
		arenaManager = new ArenasManager(this);
        ((ArenasManager)arenaManager).loadArenas();
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		
		getCommand("start").setExecutor(new CommandStart());

        Network.registerGame(this, comPort, BungeeName);
    }

	
	public void kickPlayer(final Player player) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);

		try {
			out.writeUTF("Connect");
			out.writeUTF("lobby");
		} catch (IOException e) {
			// Rien du tout
		}
		player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                player.kickPlayer("Une erreur s'est produite lors de la tentative de kick.");
            }
        }, 3*20L);
	}
	
	public ItemStack getBottle(Alcool alc) {
		ItemStack bottle = new ItemStack(Material.POTION, 1);
		
		List<String> loreA = alc.getLore();
		ArrayList<String> lore = new ArrayList<String>();
		
		for (String s : loreA) 
			lore.add(s);
		lore.add(" ");
		if (alc.getValue() < 0) lore.add(ChatColor.RED+"Malus : "+alc.getValue()+" points");
		else lore.add(ChatColor.GREEN+"+ "+alc.getValue()+" points");
		
		String pointsStr = null;
		if (alc.getValue() < 0) pointsStr = ChatColor.RED+""+alc.getValue()+" g/L";
		else pointsStr = ChatColor.GREEN+"+"+alc.getValue()+" g/L";
		ItemMeta meta = bottle.getItemMeta();
		meta.setDisplayName(alc.getNom()+ChatColor.WHITE+" ("+pointsStr+ChatColor.WHITE+")");
		meta.setLore(lore);
		bottle.setItemMeta(meta);
		return bottle;
	}
	
	
	public ItemStack getLeaveItem() {
		ItemStack leave = new ItemStack(Material.WOOD_DOOR, 1);
		ItemMeta leavemeta = leave.getItemMeta();
		leavemeta.setDisplayName(ChatColor.GOLD + "Quitter le jeu");
		leave.setItemMeta(leavemeta);
		return leave;
	}
	
	public ItemStack emptyBottle() {
		ItemStack bouteille = new ItemStack(Material.GLASS_BOTTLE, 1);
		ItemMeta data = bouteille.getItemMeta();
		data.setDisplayName(ChatColor.GOLD+"Bouteille (encore) vide");
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GOLD+"Remplissez cette bouteille et buvez là vite !");
		data.setLore(lore);
		bouteille.setItemMeta(data);
		return bouteille;
	}
	
	public static LinkedHashMap<UUID, Integer> sortHashMapByValuesD(HashMap<UUID, Integer> scores) {
		   List<UUID> mapKeys = new ArrayList<UUID>(scores.keySet());
		   List<Integer> mapValues = new ArrayList<Integer>(scores.values());
		   Collections.sort(mapValues, Collections.reverseOrder());
		   Collections.sort(mapKeys, Collections.reverseOrder());

		   LinkedHashMap<UUID, Integer> sortedMap = new LinkedHashMap<UUID, Integer>();

		   Iterator valueIt = mapValues.iterator();
		   while (valueIt.hasNext()) {
		       Object val = valueIt.next();
		       Iterator keyIt = mapKeys.iterator();

		       while (keyIt.hasNext()) {
		           Object key = keyIt.next();
		           String comp1 = scores.get(key).toString();
		           String comp2 = val.toString();

		           if (comp1.equals(comp2)){
		               scores.remove(key);
		               mapKeys.remove(key);
		               sortedMap.put((UUID)key, (Integer)val);
		               break;
		           }

		       }

		   }
		   return sortedMap;
		}
}
