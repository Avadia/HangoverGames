package net.zyuiop.HangoverGames;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_7_R3.EntityPlayer;
import net.zyuiop.HangoverGames.Arena.Alcool;
import net.zyuiop.HangoverGames.Arena.ArenasManager;
import net.zyuiop.HangoverGames.Commands.CommandStart;
import net.zyuiop.HangoverGames.Listeners.PlayerListener;
import net.zyuiop.HangoverGames.Network.NetworkManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class HangoverGames extends JavaPlugin {
	
	public ArenasManager arenasManager;
	public NetworkManager network;
	public static HangoverGames instance;
	
	/* Comunication settings */
	public Integer comPort;
	public String BungeeName;
	
	public void onEnable() {		
		HangoverGames.instance = this;
		
		this.saveDefaultConfig();
		comPort = getConfig().getInt("com-port");
		BungeeName = getConfig().getString("BungeeName");
		
		arenasManager = new ArenasManager(this);
		network = new NetworkManager(this);
		arenasManager.loadArenas();
		network.initListener();
		network.initInfosSender();
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		
		getCommand("start").setExecutor(new CommandStart());
	}
	
	public void onDisable() {
		network.disable();
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
}
