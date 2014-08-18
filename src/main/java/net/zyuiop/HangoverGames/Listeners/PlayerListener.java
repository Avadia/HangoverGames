package net.zyuiop.HangoverGames.Listeners;

import java.util.Arrays;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.server.v1_7_R3.NBTTagCompound;
import net.minecraft.server.v1_7_R3.NBTTagList;
import net.zyuiop.HangoverGames.HangoverGames;
import net.zyuiop.HangoverGames.Messages;
import net.zyuiop.HangoverGames.Arena.Alcool;
import net.zyuiop.HangoverGames.Arena.AlcoolRandom;
import net.zyuiop.HangoverGames.Arena.Arena;
import net.zyuiop.HangoverGames.Arena.ArenasManager;
import net.zyuiop.HangoverGames.Arena.VirtualPlayer;
import net.zyuiop.HangoverGames.Utils.ParticleLibrary;
import net.zyuiop.HangoverGames.Utils.ParticleLibrary.ParticleType;
import net.zyuiop.coinsManager.CoinsManager;
import net.zyuiop.statsapi.StatsApi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

public class PlayerListener implements Listener {
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if (event.getItem() != null && event.getItem().getType().equals(Material.WOOD_DOOR)) 
				HangoverGames.instance.kickPlayer(event.getPlayer());
		}
		
		Arena ar = HangoverGames.instance.arenasManager.getPlayerArena(event.getPlayer().getUniqueId());
		
		
		if (ar == null || !ar.isGameStarted()) {
			event.setCancelled(true);
			return;
		}
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getClickedBlock().getType() == Material.CAULDRON && event.getPlayer().getItemInHand().getType() == Material.GLASS_BOTTLE) {
				if ((int)event.getClickedBlock().getData() != 0) {
					
					
					
					// Reset du bloc //
					ar.fillRandom();
					event.getClickedBlock().setType(Material.AIR);
					
				
					
					Alcool got = null;
					int maxNocive = (int) Math.floor(ar.minPlayers*0.5);
					while (true) {
						got = AlcoolRandom.getRandom();
						if (got.getValue() < 0) {
							Bukkit.getLogger().info("Checking quantity of nocive : "+ar.nocive+" - allowed : "+maxNocive);
							if (ar.nocive+1 < maxNocive) {
								ar.nocive += 1;
								Bukkit.getLogger().info("-> Allowed");
								break;
							} else {
								Bukkit.getLogger().info("-> Denied");
								continue;
							}
						} else 
							break;
						
					}
					
					ItemStack bottle = HangoverGames.instance.getBottle(got);
					
					int diff = 0;
					if (event.getPlayer().getItemInHand().getAmount() > 1) {
						diff = event.getPlayer().getItemInHand().getAmount() - 1;
					}
					event.getPlayer().getInventory().setItemInHand(bottle);
					if (diff > 0) {
						ItemStack add = HangoverGames.instance.emptyBottle();
						add.setAmount(diff);
						event.getPlayer().getInventory().addItem(add);
					}
					
					event.setCancelled(true);
					
					if (got.equals(Alcool.Whisky)) {
						ar.broadcastMessage(ChatColor.AQUA+event.getPlayer().getName()+ChatColor.GOLD+" a retrouvé une bouteille de "+ChatColor.GREEN+"Whisky"+ChatColor.GOLD+" dans sa cave !");
						event.getPlayer().getWorld().strikeLightningEffect(event.getPlayer().getLocation());
					}
				}
			}
		}
		ItemStack i = event.getItem();
		if (i == null || (!i.getType().equals(Material.GLASS_BOTTLE) && !i.getType().equals(Material.POTION))) {
			event.setCancelled(true);
			return;
		}
		if (event.getClickedBlock() == null) return;
		Material[] blacklist = new Material[] { Material.NOTE_BLOCK, Material.JUKEBOX, Material.LADDER, Material.TRAP_DOOR, Material.TRAPPED_CHEST, Material.FENCE_GATE, Material.WOOD_DOOR, Material.WOOD_BUTTON, Material.WOODEN_DOOR, Material.LEVER, Material.WORKBENCH, Material.STONE_BUTTON, Material.ITEM_FRAME};
		if (Arrays.asList(blacklist).contains(event.getClickedBlock().getType())) event.setCancelled(true);
	}
	
	@EventHandler
	public void onInteractEntity(PlayerInteractEntityEvent ev) {
		ev.setCancelled(true);
	}
	
	@EventHandler
	public void onChestOpen(InventoryOpenEvent e) {
		if (!e.getInventory().getType().equals(InventoryType.PLAYER)) e.setCancelled(true);
	}
		
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		final Arena arena = HangoverGames.instance.arenasManager.getPlayerArena(new VirtualPlayer(player));
		
		if(arena == null)
		{
			return;
		}
		event.getRecipients().clear();
		for (VirtualPlayer p : arena.players)
			event.getRecipients().add(p.getPlayer());	   
		return;   
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.getTo().getBlockY() < 0) {
			Arena ar = HangoverGames.instance.arenasManager.getPlayerArena(event.getPlayer().getUniqueId());
			if (ar == null || !ar.isGameStarted()) return;
			event.getPlayer().teleport(ar.spawn);
		}
	}
	
	@EventHandler
	public void onBlocBreak(BlockBreakEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onBlocPlace(BlockPlaceEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onLogin(PlayerLoginEvent e) {
		Player p = e.getPlayer();
		ArenasManager m = HangoverGames.instance.arenasManager;
		if (!m.isAttempted(new VirtualPlayer(p.getUniqueId())) && !p.isOp()) {
			e.setKickMessage(ChatColor.RED+"Une erreur s'est produite, vous ne pouvez pas joindre l'arène.");
			e.setResult(Result.KICK_FULL);
			return;
		}
	}
	
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		final ArenasManager m = HangoverGames.instance.arenasManager;
		final VirtualPlayer vp = new VirtualPlayer(p.getUniqueId());
		if (!m.isAttempted(vp) && !p.isOp()) {
			e.getPlayer().sendMessage(ChatColor.RED+"Une erreur s'est produite, vous ne pouvez pas joindre l'arène.");
			e.getPlayer().kickPlayer("Impossible de vous connecter.");
			System.out.println("Joueur non attendu.");
			return;
		} else if (!m.isAttempted(vp)) {
			e.getPlayer().sendMessage(ChatColor.GOLD+"Welcome in SANDBOX MODE ! Yay !");
			return;
		}
		
		e.setJoinMessage(null);
		
		String res = m.finishJoin(p);
		if (!res.equals("good")) {
			e.getPlayer().sendMessage(ChatColor.RED+"Une erreur s'est produite, vous ne pouvez pas joindre l'arène.");
			e.getPlayer().sendMessage(ChatColor.RED+"Code erreur : "+res);
			e.getPlayer().kickPlayer("Impossible de vous connecter.");
			System.out.println("Erreur de connexion du joueur : "+res);
			return;
		}
			
	}
	
	@EventHandler
	public void onPlayerDrink(PlayerItemConsumeEvent ev) {
		ItemStack item = ev.getItem();
		System.out.println(item.getType().toString());
		if (item.getType().equals(Material.POTION) || item.getType().equals(Material.GLASS_BOTTLE)) {
			Alcool alc = AlcoolRandom.getByItemName(item.getItemMeta().getDisplayName());
			if (alc == null) {
				System.out.println("Alcool not found : "+item.getItemMeta().getDisplayName());
				return;
			}
			
			Arena ar = HangoverGames.instance.arenasManager.getPlayerArena(ev.getPlayer().getUniqueId());
			if (ar == null || !ar.isGameStarted()) {
				ev.setCancelled(true);
				return;
			}
			Integer score = ar.scores.get(ev.getPlayer().getUniqueId());
			if (score == null) score = 0;
			Integer value = alc.getValue();
			Bukkit.getLogger().info("Value : "+value);
			score += value;
			if (score < 0) score = 0;
			ar.scores.put(ev.getPlayer().getUniqueId(), score);
			
			ar.objective.getScore(ev.getPlayer().getName()).setScore(score);
			
			if (alc.getValue() < 0)
				ar.nocive--;
			
			Integer eff = ar.effectLevel.get(ev.getPlayer().getUniqueId());
			if (eff == null) eff = 0;
			eff += value * value;
			ar.effectLevel.put(ev.getPlayer().getUniqueId(), eff);
			//ar.playerEffects(ev.getPlayer());
			alc.applyEffects(ev.getPlayer());
			
			ev.getPlayer().getInventory().setItemInHand(HangoverGames.instance.emptyBottle());
			ev.setCancelled(true);
			
			if (alc.getValue() < 0) {
				ar.broadcastMessage(Messages.POINTS_LOST.replace("{PSEUDO}", ev.getPlayer().getName()).replace("{NB}", ""+alc.getValue()*-1).replace("{BOISSON}", alc.getNom()));
				ar.broadcastSound(Sound.HORSE_ZOMBIE_DEATH, ev.getPlayer().getLocation());
				ParticleLibrary.sendParticleToLocation(ev.getPlayer().getLocation(), ParticleType.DRIP_LAVA, 1, 1, 1, 1, 50);
			} else {
				ar.broadcastMessage(Messages.POINTS_GAINED.replace("{PSEUDO}", ev.getPlayer().getName()).replace("{NB}", ""+alc.getValue()).replace("{BOISSON}", alc.getNom()));
				ar.broadcastSound(Sound.BURP, ev.getPlayer().getLocation());
			}
			
			if (score >= 15) {
				ar.win(new VirtualPlayer(ev.getPlayer()));
			}
		}
	}
	
	@EventHandler
	public void onSaturationChange(FoodLevelChangeEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onLogout(PlayerQuitEvent event) {
		Arena ar = HangoverGames.instance.arenasManager.getPlayerArena(event.getPlayer().getUniqueId());
		if (ar == null) return;
		ar.stumpPlayer(new VirtualPlayer(event.getPlayer()));
		HangoverGames.instance.network.sendArenasInfos(false);
	}
	
	@EventHandler
	public void onPlayerFight(EntityDamageEvent e) {
		if (e.getEntityType() != EntityType.PLAYER) {
			if (e.getEntityType() == EntityType.ITEM_FRAME || e.getEntityType() == EntityType.PAINTING)
				e.setCancelled(true);
			return;
		}
		final Player p = (Player) e.getEntity();
		final Arena ar = HangoverGames.instance.arenasManager.getPlayerArena(p.getUniqueId());
		if (ar == null || !ar.isGameStarted()) {
			e.setCancelled(true);
			return;
		}
		e.setCancelled(true);
		
		if (e instanceof EntityDamageByEntityEvent) {
			final EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) e;
			if (ev.getDamager() != null) {
				Entity damager = ev.getDamager();
				if (damager instanceof Player) {
					final Player t = (Player) damager;
					Date now = new Date();
					
					Date t_cool = ar.cooldown.get(t.getUniqueId());
					Date p_cool = ar.cooldown.get(p.getUniqueId());
					
					if (t_cool != null && t_cool.after(now)) {
						t.sendMessage(ChatColor.RED+"He, ho, attends un peu, tu vas finir par te blesser !");
						return;
					} else if (p_cool != null && p_cool.after(now)) {
						t.sendMessage(ChatColor.RED+"He, ho, attends un peu, "+p.getName()+" se fait tabasser !");
						return;
					}
					
					ItemStack tbot = null;
					ItemStack pbot = null;
					for (ItemStack i : t.getInventory().getContents()) {
						if (i == null) continue;
						if (i.getType().equals(Material.POTION) || i.getType().equals(Material.GLASS_BOTTLE)) {
							tbot = i.clone();
							break;
						}
					}
					
					for (ItemStack i : p.getInventory().getContents()) {
						if (i == null) continue;
						if (i.getType().equals(Material.POTION) || i.getType().equals(Material.GLASS_BOTTLE)) {
							pbot = i.clone();
							break;
						}
					}
					
					t.getInventory().clear();
					p.getInventory().clear();
					
					t.sendMessage(ChatColor.GREEN+"Vous avez échangé de bouteille avec "+ChatColor.RED+p.getName());
					p.sendMessage(ChatColor.RED+"Vous avez échangé de bouteille avec "+ChatColor.GREEN+t.getName());
							
					final ItemStack BottleT = tbot;
					final ItemStack BottleP = pbot;
					
					Bukkit.getScheduler().runTaskLater(HangoverGames.instance, new Runnable() {
						public void run() {
							t.getInventory().addItem(BottleP);
							p.getInventory().addItem(BottleT);
							
							if (AlcoolRandom.getByItemName(BottleP.getItemMeta().getDisplayName()).equals(Alcool.Whisky)) {
								ar.broadcastMessage(ChatColor.AQUA+t.getName()+ChatColor.GOLD+" a volé la bouteille de "+ChatColor.GREEN+"Whisky"+ChatColor.GOLD+" à "+ChatColor.RED+p.getName()+" !");
								t.getWorld().strikeLightningEffect(t.getLocation());
							}
							
							if (AlcoolRandom.getByItemName(BottleT.getItemMeta().getDisplayName()).equals(Alcool.Whisky)) {
								ar.broadcastMessage(ChatColor.AQUA+p.getName()+ChatColor.GOLD+" a volé la bouteille de "+ChatColor.GREEN+"Whisky"+ChatColor.GOLD+" à "+ChatColor.RED+t.getName()+" !");
								p.getWorld().strikeLightningEffect(p.getLocation());
							}
						}
					}, 5L);
					
					
					
					
					Date cooldown = new Date();
					cooldown.setTime(cooldown.getTime() + 2300);
					ar.cooldown.put(t.getUniqueId(), cooldown);
					ar.cooldown.put(p.getUniqueId(), cooldown);
					
					t.playEffect(EntityEffect.HURT);
					p.playEffect(EntityEffect.HURT);
				}
			}
		}
		
	}
	
}
