package fr.blueslime.hangover.listeners;

import fr.blueslime.hangover.HangoverGames;
import fr.blueslime.hangover.Messages;
import fr.blueslime.hangover.arena.Alcool;
import fr.blueslime.hangover.arena.AlcoolRandom;
import fr.blueslime.hangover.arena.Arena;
import net.samagames.api.SamaGamesAPI;
import net.samagames.tools.GameUtils;
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

public class PlayerListener implements Listener
{
	@EventHandler
	public void onInventoryInteract(InventoryInteractEvent event)
    {
        event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
    {
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
			if (event.getItem() != null && event.getItem().getType().equals(Material.WOOD_DOOR)) 
				SamaGamesAPI.get().getGameManager().kickPlayer(event.getPlayer(), null);
            if (event.getItem() != null && event.getItem().getType().equals(Material.WRITTEN_BOOK))
                return;
		}
		
		Arena arena = HangoverGames.getInstance().getArena();
		
		if (!arena.isGameStarted())
        {
			event.setCancelled(true);
			return;
		}
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
			if (event.getClickedBlock().getType() == Material.CAULDRON && event.getPlayer().getItemInHand().getType() == Material.GLASS_BOTTLE)
            {
				if ((int)event.getClickedBlock().getData() != 0)
                {
                    arena.fillRandom();
					event.getClickedBlock().setType(Material.AIR);

					Alcool got;
					int maxNocive = (int) Math.floor(arena.getInGamePlayers().size() * 0.5);

					while (true)
                    {
						got = AlcoolRandom.getRandom();

						if (got.getValue() < 0)
                        {
							if (arena.getNocive() + 1 <= maxNocive)
                            {
                                arena.setNocive((arena.getNocive() + 1));
								break;
							}
                            else
                            {
								continue;
							}
						}
                        else
                        {
                            break;
                        }
					}
					
					ItemStack bottle = got.getBottle();
					
					int diff = 0;

					if (event.getPlayer().getItemInHand().getAmount() > 1)
						diff = event.getPlayer().getItemInHand().getAmount() - 1;

					event.getPlayer().getInventory().setItemInHand(bottle);

                    if (diff > 0)
                    {
						ItemStack add = arena.getEmptyBottle();
						add.setAmount(diff);
						event.getPlayer().getInventory().addItem(add);
					}
					
					Date cooldown = new Date();
					cooldown.setTime(cooldown.getTime() + 1000);
                    arena.getDamagedCooldown().put(event.getPlayer().getUniqueId(), cooldown);
					arena.newBottle(event.getPlayer());

					event.setCancelled(true);
					
					if (got.equals(Alcool.WHISKY))
                    {
						Bukkit.broadcastMessage(ChatColor.AQUA + event.getPlayer().getName() + ChatColor.GOLD + " a retrouvé une bouteille de " + ChatColor.GREEN + "Whisky" + ChatColor.GOLD + " dans sa cave !");
						event.getPlayer().getWorld().strikeLightningEffect(event.getPlayer().getLocation());
					}
				}
			}
            else if (event.getPlayer().getItemInHand().getType() == Material.GLASS_BOTTLE)
            {
				event.setCancelled(true);
			}
		}

		ItemStack i = event.getItem();

		if (i == null || (!i.getType().equals(Material.GLASS_BOTTLE) && !i.getType().equals(Material.POTION)))
        {
			event.setCancelled(true);
			return;
		}

		if (event.getClickedBlock() == null)
            return;

		Material[] blacklist = new Material[] { Material.NOTE_BLOCK, Material.JUKEBOX, Material.LADDER, Material.TRAP_DOOR, Material.TRAPPED_CHEST, Material.FENCE_GATE, Material.WOOD_DOOR, Material.WOOD_BUTTON, Material.WOODEN_DOOR, Material.LEVER, Material.WORKBENCH, Material.STONE_BUTTON, Material.ITEM_FRAME};

        if (Arrays.asList(blacklist).contains(event.getClickedBlock().getType()))
            event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        event.setCancelled(true);
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event)
    {
		if (!event.getInventory().getType().equals(InventoryType.PLAYER))
            event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
    {
		Block block = event.getTo().getBlock();

		if (event.getTo().getBlockY() < 0 || (block.isLiquid() && block.getData() < ((byte) 4)))
        {
			Arena arena = HangoverGames.getInstance().getArena();
			event.getPlayer().teleport(arena.getSpawn());
			event.getPlayer().sendMessage(Messages.mapEnd.toString());
		} 
	}
	
	@EventHandler
	public void onBlocBreak(BlockBreakEvent event)
    {
        event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlocPlace(BlockPlaceEvent event)
    {
        event.setCancelled(true);
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent event)
    {
        event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerDrink(PlayerItemConsumeEvent event)
    {
		ItemStack item = event.getItem();
        Arena arena = HangoverGames.getInstance().getArena();

		if (item.getType().equals(Material.POTION) || item.getType().equals(Material.GLASS_BOTTLE))
        {
			arena.increaseStat(event.getPlayer().getUniqueId(), "drinks", 1);
			Alcool alcool = AlcoolRandom.getAlcoolByName(item.getItemMeta().getDisplayName());

			if (alcool == null)
				return;
			
			if (!arena.isGameStarted())
            {
				event.setCancelled(true);
				return;
			}

			int score = 0;

			if (arena.getScores().containsKey(event.getPlayer().getUniqueId()))
                score = arena.getScores().get(event.getPlayer().getUniqueId());

			int value = alcool.getValue();
			score += value;

			if (score < 0)
                score = 0;

            arena.getScores().put(event.getPlayer().getUniqueId(), score);
            arena.getObjective().getScore(event.getPlayer().getName()).setScore(score);
			
			if (alcool.getValue() < 0)
                arena.setNocive((arena.getNocive() - 1));
			
			Integer eff = arena.getEffectLevel().get(event.getPlayer().getUniqueId());

			if (eff == null)
                eff = 0;

			eff += value * value;
            arena.getEffectLevel().put(event.getPlayer().getUniqueId(), eff);
            alcool.applyEffects(event.getPlayer());
            event.getPlayer().getInventory().setItemInHand(arena.getEmptyBottle());
            event.setCancelled(true);
			
			if (alcool.getValue() < 0)
            {
                Bukkit.broadcastMessage(Messages.pointsLost.toString().replace("${PLAYER}", event.getPlayer().getName()).replace("${NUMBER}", "" + alcool.getValue() * -1).replace("${ALCOOL}", alcool.getName()));
                GameUtils.broadcastSound(Sound.HORSE_ZOMBIE_DEATH, event.getPlayer().getLocation());
			}
            else
            {
                Bukkit.broadcastMessage(Messages.pointsGained.toString().replace("${PLAYER}", event.getPlayer().getName()).replace("${NUMBER}", "" + alcool.getValue()).replace("${ALCOOL}", alcool.getName()));
                GameUtils.broadcastSound(Sound.BURP, event.getPlayer().getLocation());
			}
			
			if (alcool.equals(Alcool.WHISKY))
            {
                GameUtils.broadcastSound(Sound.WITHER_DEATH, event.getPlayer().getLocation());
			}
			
			arena.noMoreBottle(event.getPlayer());
			
			if (score >= 15)
            {
                arena.win(event.getPlayer());
			}
		}
	}
	
	@EventHandler
	public void onFoodLevelChance(FoodLevelChangeEvent event)
    {
        event.setCancelled(true);
	}
	
	@EventHandler
	public void onHangingBreak(HangingBreakEvent event)
    {
        event.setCancelled(true);
	}
	
	@EventHandler
	public void onHangingPlace(HangingPlaceEvent event)
    {
        event.setCancelled(true);
	}
	
	@EventHandler
	public void onHangingBreakByEntity(HangingBreakByEntityEvent event)
    {
        event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event)
    {
        if (event.getEntityType() != EntityType.PLAYER)
        {
            if (event.getEntityType() == EntityType.ITEM_FRAME || event.getEntityType() == EntityType.PAINTING)
                event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
		if (event.getDamager() != null)
        {
            Entity damagedEntity = event.getEntity();
            Entity damagerEntity = event.getDamager();

            if (damagerEntity instanceof Player && damagedEntity instanceof Player)
            {
                Arena arena = HangoverGames.getInstance().getArena();
                Player damaged = (Player) damagedEntity;
                Player damager = (Player) damagerEntity;
                Date now = new Date();

                Date damaged_cool = arena.getDamagedCooldown().get(damaged.getUniqueId());
                Date antiDouble = arena.getDoubleLock().get(damaged.getUniqueId());
                Date antiDoubleDamager = arena.getDoubleLock().get(damager.getUniqueId());

                if (damaged_cool != null && damaged_cool.after(now))
                {
                    damager.sendMessage(ChatColor.GOLD + "" + ChatColor.ITALIC + "Attends un peu, " + damaged.getName() + " se fait tabasser !");
                    return;
                }

                if (antiDouble != null && antiDouble.after(now) || antiDoubleDamager != null && antiDoubleDamager.after(now))
                {
                    damager.sendMessage(ChatColor.GOLD + "" + ChatColor.ITALIC + "Attends un peu quand même !");
                    return;
                }

                ItemStack damagerBottle = null;
                ItemStack damagedBottle = null;

                for (ItemStack stack : damager.getInventory().getContents())
                {
                    if (stack == null)
                        continue;

                    if (stack.getType().equals(Material.POTION) || stack.getType().equals(Material.GLASS_BOTTLE))
                    {
                        damagerBottle = stack.clone();
                        break;
                    }
                }

                for (ItemStack stack : damaged.getInventory().getContents())
                {
                    if (stack == null)
                        continue;

                    if (stack.getType().equals(Material.POTION) || stack.getType().equals(Material.GLASS_BOTTLE))
                    {
                        damagedBottle = stack.clone();
                        break;
                    }
                }

                if (damagerBottle == null)
                    damagerBottle = arena.getEmptyBottle();
                else if (damagedBottle == null)
                    damagedBottle = arena.getEmptyBottle();

                damager.getInventory().clear();
                damaged.getInventory().clear();

                damager.sendMessage(ChatColor.GREEN + "Vous avez échangé de bouteille avec " + ChatColor.RED + damaged.getName());
                damaged.sendMessage(ChatColor.RED + "Vous avez échangé de bouteille avec " + ChatColor.GREEN + damager.getName());

                final ItemStack damagerBottleFinal = damagerBottle;
                final ItemStack damagedBottleFinal = damagedBottle;

                Bukkit.getScheduler().runTaskLater(HangoverGames.getInstance(), () ->
                {
                    damager.getInventory().addItem(damagedBottleFinal);
                    damaged.getInventory().addItem(damagerBottleFinal);

                    if (damagerBottleFinal.getType().equals(Material.POTION) && AlcoolRandom.getAlcoolByName(damagerBottleFinal.getItemMeta().getDisplayName()).equals(Alcool.WHISKY))
                    {
                        Bukkit.broadcastMessage(ChatColor.AQUA + damaged.getName() + ChatColor.GOLD + " a volé la bouteille de " + ChatColor.GREEN + "Whisky" + ChatColor.GOLD + " à " + ChatColor.RED+damager.getName() + " !");
                        damaged.getWorld().strikeLightningEffect(damaged.getLocation());
                    }

                    if (damagedBottleFinal.getType().equals(Material.POTION) && AlcoolRandom.getAlcoolByName(damagedBottleFinal.getItemMeta().getDisplayName()).equals(Alcool.WHISKY))
                    {
                        Bukkit.broadcastMessage(ChatColor.AQUA + damager.getName() + ChatColor.GOLD + " a volé la bouteille de " + ChatColor.GREEN + "Whisky" + ChatColor.GOLD + " à " + ChatColor.RED + damaged.getName() + " !");
                        damager.getWorld().strikeLightningEffect(damager.getLocation());
                    }
                }, 5L);

                if (damagerBottle.equals(arena.getEmptyBottle()))
                    arena.noMoreBottle(damaged);
                else
                    arena.newBottle(damaged);

                if (damagedBottle.equals(arena.getEmptyBottle()))
                    arena.noMoreBottle(damager);
                else
                    arena.newBottle(damager);

                Date antidouble = new Date();
                antidouble.setTime(antidouble.getTime() + 350);
                arena.getDoubleLock().put(damager.getUniqueId(), antidouble);
                arena.getDoubleLock().put(damaged.getUniqueId(), antidouble);

                Date cooldown = new Date();
                cooldown.setTime(cooldown.getTime() + 2200);
                arena.getDamagedCooldown().put(damager.getUniqueId(), cooldown);
                arena.getDamagedCooldown().put(damaged.getUniqueId(), cooldown);

                damaged.playEffect(EntityEffect.HURT);
                damager.playEffect(EntityEffect.HURT);
			}
		}
	}
}
