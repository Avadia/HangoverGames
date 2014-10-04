package net.zyuiop.HangoverGames.listeners;

import net.samagames.network.Network;
import net.samagames.network.client.GameArena;
import net.samagames.network.client.GamePlayer;
import net.zyuiop.HangoverGames.arena.*;
import net.zyuiop.HangoverGames.HangoverGames;
import net.zyuiop.HangoverGames.Messages;
import net.zyuiop.statsapi.StatsApi;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Date;

public class PlayerListener implements Listener {
	
	@EventHandler
	public void onInventory(InventoryInteractEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if (event.getItem() != null && event.getItem().getType().equals(Material.WOOD_DOOR)) 
				HangoverGames.instance.kickPlayer(event.getPlayer());
		}
		
		Arena ar = (Arena) HangoverGames.instance.getArenaManager().getPlayerArena(event.getPlayer().getUniqueId());
		
		
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
					int maxNocive = (int) Math.floor(ar.getPlayers().size()*0.5);
					while (true) {
						got = AlcoolRandom.getRandom();
						if (got.getValue() < 0) {
							Bukkit.getLogger().info("Checking quantity of nocive : "+ar.nocive+" - allowed : "+maxNocive);
							if (ar.nocive+1 <= maxNocive) {
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
					
					Date cooldown = new Date();
					cooldown.setTime(cooldown.getTime() + 1000);
					ar.damagedcooldown.put(event.getPlayer().getUniqueId(), cooldown);
					
					
					ar.newBottle(event.getPlayer());
					
					
					event.setCancelled(true);
					
					if (got.equals(Alcool.Whisky)) {
						ar.broadcastMessage(ChatColor.AQUA+event.getPlayer().getName()+ChatColor.GOLD+" a retrouvé une bouteille de "+ChatColor.GREEN+"Whisky"+ChatColor.GOLD+" dans sa cave !");
						event.getPlayer().getWorld().strikeLightningEffect(event.getPlayer().getLocation());
					}
				}
			} else if (event.getPlayer().getItemInHand().getType() == Material.GLASS_BOTTLE) {
				event.setCancelled(true);
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
		final GameArena arena = HangoverGames.instance.getArenaManager().getPlayerArena(player.getUniqueId());
		
		if(arena == null) {
			return;
		}
		event.getRecipients().clear();
		for (GamePlayer p : arena.getPlayers())
			event.getRecipients().add(p.getPlayer());	   
		return;   
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Block b = event.getTo().getBlock();
		if (event.getTo().getBlockY() < 0 || (b.isLiquid() && b.getData() < ((byte)4))) {
			Arena ar = (Arena) HangoverGames.instance.getArenaManager().getPlayerArena(event.getPlayer().getUniqueId());
			if (ar == null) return;
			event.getPlayer().teleport(ar.spawn);
			event.getPlayer().sendMessage(Messages.MAP_END);
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
	public void onPlayerDrink(PlayerItemConsumeEvent ev) {
		ItemStack item = ev.getItem();
		System.out.println(item.getType().toString());
		if (item.getType().equals(Material.POTION) || item.getType().equals(Material.GLASS_BOTTLE)) {
			StatsApi.increaseStat(ev.getPlayer(), "trollcade", "hangovergames.drinks", 1);
			Alcool alc = AlcoolRandom.getByItemName(item.getItemMeta().getDisplayName());
			if (alc == null) {
				System.out.println("Alcool not found : "+item.getItemMeta().getDisplayName());
				return;
			}
			
			Arena ar = (Arena) HangoverGames.instance.getArenaManager().getPlayerArena(ev.getPlayer().getUniqueId());
			if (ar == null || !ar.isStarted()) {
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
			} else {
				ar.broadcastMessage(Messages.POINTS_GAINED.replace("{PSEUDO}", ev.getPlayer().getName()).replace("{NB}", ""+alc.getValue()).replace("{BOISSON}", alc.getNom()));
				ar.broadcastSound(Sound.BURP, ev.getPlayer().getLocation());
			}
			
			if (alc.equals(Alcool.Whisky)) {
				ar.broadcastSound(Sound.WITHER_DEATH, ev.getPlayer().getLocation());
			}
			
			ar.noMoreBottle(ev.getPlayer());
			
			
			if (score >= 15) {
				ar.win(new GamePlayer(ev.getPlayer()));
			}
		}
	}
	
	@EventHandler
	public void onSaturationChange(FoodLevelChangeEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onPaint(HangingBreakEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onPaint(HangingPlaceEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onPaint(HangingBreakByEntityEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerFight(EntityDamageEvent e) {
		if (e.getEntityType() != EntityType.PLAYER) {
			if (e.getEntityType() == EntityType.ITEM_FRAME || e.getEntityType() == EntityType.PAINTING)
				e.setCancelled(true);
			return;
		}
		final Player damaged = (Player) e.getEntity();
		final Arena ar = (Arena) HangoverGames.instance.getArenaManager().getPlayerArena(damaged.getUniqueId());
		if (ar == null || !ar.isGameStarted()) {
			e.setCancelled(true);
			return;
		}
		e.setCancelled(true);
		
		if (e instanceof EntityDamageByEntityEvent) {
			final EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) e;
			if (ev.getDamager() != null) {
				Entity damagerE = ev.getDamager();
				if (damagerE instanceof Player) {
					final Player damager = (Player) damagerE;
					Date now = new Date();
					
					Date damaged_cool = ar.damagedcooldown.get(damaged.getUniqueId());
					Date antiDouble = ar.antiDouble.get(damaged.getUniqueId());
					Date antiDoubleDamager = ar.antiDouble.get(damager.getUniqueId());
					
					if (damaged_cool != null && damaged_cool.after(now)) {
						damager.sendMessage(ChatColor.GOLD+""+ChatColor.ITALIC+"Attends un peu, "+damaged.getName()+" se fait tabasser !");
						return;
					}
					
					if (antiDouble != null && antiDouble.after(now) || antiDoubleDamager != null && antiDoubleDamager.after(now)) {
						damager.sendMessage(ChatColor.GOLD+""+ChatColor.ITALIC+"Attends un peu quand même !");
						return;
					}
					
					ItemStack damagerbot = null;
					ItemStack damagedbot = null;
					for (ItemStack i : damager.getInventory().getContents()) {
						if (i == null) continue;
						if (i.getType().equals(Material.POTION) || i.getType().equals(Material.GLASS_BOTTLE)) {
							damagerbot = i.clone();
							break;
						}
					}
					
					for (ItemStack i : damaged.getInventory().getContents()) {
						if (i == null) continue;
						if (i.getType().equals(Material.POTION) || i.getType().equals(Material.GLASS_BOTTLE)) {
							damagedbot = i.clone();
							break;
						}
					}
					
					if (damagerbot == null) {
						damagerbot = HangoverGames.instance.emptyBottle();
					} else if (damagedbot == null) {
						damagedbot = HangoverGames.instance.emptyBottle();
					}
					
					damager.getInventory().clear();
					damaged.getInventory().clear();
					
					damager.sendMessage(ChatColor.GREEN+"Vous avez échangé de bouteille avec "+ChatColor.RED+damaged.getName());
					damaged.sendMessage(ChatColor.RED+"Vous avez échangé de bouteille avec "+ChatColor.GREEN+damager.getName());
							
					final ItemStack BottleDamager = damagerbot;
					final ItemStack BottleDamaged = damagedbot;
					
					Bukkit.getScheduler().runTaskLater(HangoverGames.instance, new Runnable() {
						public void run() {
							damager.getInventory().addItem(BottleDamaged);
							damaged.getInventory().addItem(BottleDamager);
							
							if (BottleDamager.getType().equals(Material.POTION) && AlcoolRandom.getByItemName(BottleDamager.getItemMeta().getDisplayName()).equals(Alcool.Whisky)) {
								ar.broadcastMessage(ChatColor.AQUA+damaged.getName()+ChatColor.GOLD+" a volé la bouteille de "+ChatColor.GREEN+"Whisky"+ChatColor.GOLD+" à "+ChatColor.RED+damager.getName()+" !");
								damaged.getWorld().strikeLightningEffect(damaged.getLocation());
							}
							
							if (BottleDamaged.getType().equals(Material.POTION) && AlcoolRandom.getByItemName(BottleDamaged.getItemMeta().getDisplayName()).equals(Alcool.Whisky)) {
								ar.broadcastMessage(ChatColor.AQUA+damager.getName()+ChatColor.GOLD+" a volé la bouteille de "+ChatColor.GREEN+"Whisky"+ChatColor.GOLD+" à "+ChatColor.RED+damaged.getName()+" !");
								damager.getWorld().strikeLightningEffect(damager.getLocation());
							}
						}
					}, 5L);
					
					if (damagerbot.equals(HangoverGames.instance.emptyBottle())) {
						ar.noMoreBottle(damaged);
					} else {
						ar.newBottle(damaged);
					}
					
					if (damagedbot.equals(HangoverGames.instance.emptyBottle())) {
						ar.noMoreBottle(damager);
					} else {
						ar.newBottle(damager);
					}
					
					
					Date antidouble = new Date();
					antidouble.setTime(antidouble.getTime() + 350);
					ar.antiDouble.put(damager.getUniqueId(), antidouble);
					ar.antiDouble.put(damaged.getUniqueId(), antidouble);
					
					Date cooldown = new Date();
					cooldown.setTime(cooldown.getTime() + 2200);
					ar.damagedcooldown.put(damager.getUniqueId(), cooldown);
					ar.damagedcooldown.put(damaged.getUniqueId(), cooldown);
					
					damaged.playEffect(EntityEffect.HURT);
					damager.playEffect(EntityEffect.HURT);
				}
			}
		}
		
	}
	
}
