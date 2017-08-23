package fr.blueslime.hangover.listeners;

import fr.blueslime.hangover.HangoverGames;
import fr.blueslime.hangover.Messages;
import fr.blueslime.hangover.arena.Alcool;
import fr.blueslime.hangover.arena.AlcoolRandom;
import fr.blueslime.hangover.arena.Arena;
import net.samagames.api.SamaGamesAPI;
import net.samagames.tools.GameUtils;
import net.samagames.tools.PlayerUtils;
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

/*
 * This file is part of HangoverGames.
 *
 * HangoverGames is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HangoverGames is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HangoverGames.  If not, see <http://www.gnu.org/licenses/>.
 */
public class PlayerListener implements Listener
{
    private final HangoverGames plugin;
    private final Arena arena;

    public PlayerListener(HangoverGames plugin, Arena arena)
    {
        this.plugin = plugin;
        this.arena = arena;
    }

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

        if (!this.arena.isGameStarted())
        {
            event.setCancelled(true);
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)
        {
            if (event.getClickedBlock().getType() == Material.CAULDRON && event.getPlayer().getItemInHand().getType() == Material.GLASS_BOTTLE)
            {
                if ((int)event.getClickedBlock().getData() != 0)
                {
                    this.arena.fillRandom();
                    event.getClickedBlock().setType(Material.AIR);

                    Alcool got;
                    int maxNocive = (int) Math.floor(this.arena.getInGamePlayers().size() * 0.5);

                    while (true)
                    {
                        got = AlcoolRandom.getRandom();

                        if (got.getValue() < 0 && this.arena.getNocive() + 1 <= maxNocive)
                        {
                            this.arena.setNocive((this.arena.getNocive() + 1));
                            break;
                        }
                        else
                        {
                            break;
                        }
                    }

                    ItemStack bottle = got.getBottle();

                    int diff = 0;

                    if (event.getPlayer().getItemInHand().getAmount() > 1)
                        diff = event.getPlayer().getInventory().getItem(0).getAmount() - 1;

                    event.getPlayer().getInventory().setItem(0, bottle);

                    if (diff > 0)
                    {
                        ItemStack add = this.arena.getEmptyBottle();
                        add.setAmount(diff);
                        event.getPlayer().getInventory().addItem(add);
                    }

                    Date cooldown = new Date();
                    cooldown.setTime(cooldown.getTime() + 1000);
                    this.arena.getDamagedCooldown().put(event.getPlayer().getUniqueId(), cooldown);
                    this.arena.newBottle(event.getPlayer());

                    event.setCancelled(true);

                    if (got.equals(Alcool.WHISKY))
                    {
                        Bukkit.broadcastMessage(ChatColor.AQUA + event.getPlayer().getName() + ChatColor.GOLD + " a trouvé une bouteille de " + ChatColor.GREEN + "Whisky" + ChatColor.GOLD + " dans sa cave !");
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
            event.getPlayer().teleport(this.arena.getSpawn());
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

        if (item.getType().equals(Material.POTION) || item.getType().equals(Material.GLASS_BOTTLE))
        {
            //TODO: SamaGamesAPI.get().getStatsManager().getPlayerStats(event.getPlayer().getUniqueId()).getHangoverStatistics().incrByDrinks(1);
            Alcool alcool = AlcoolRandom.getAlcoolByName(item.getItemMeta().getDisplayName());

            if (alcool == null)
                return;

            if (!this.arena.isGameStarted())
            {
                event.setCancelled(true);
                return;
            }

            int score = 0;

            if (this.arena.getScores().containsKey(event.getPlayer().getUniqueId()))
                score = this.arena.getScores().get(event.getPlayer().getUniqueId());

            int value = alcool.getValue();
            score += value;

            if (score < 0)
                score = 0;

            this.arena.getScores().put(event.getPlayer().getUniqueId(), score);
            this.arena.getObjective().getScore(PlayerUtils.getColoredFormattedPlayerName(event.getPlayer())).setScore(score);

            if (alcool.getValue() < 0)
                this.arena.setNocive((this.arena.getNocive() - 1));

            Integer eff = arena.getEffectLevel().get(event.getPlayer().getUniqueId());

            if (eff == null)
                eff = 0;

            eff += value * value;
            this.arena.getEffectLevel().put(event.getPlayer().getUniqueId(), eff);
            alcool.applyEffects(event.getPlayer());
            event.getPlayer().getInventory().setItem(0, this.arena.getEmptyBottle());
            event.setCancelled(true);

            if (alcool.getValue() < 0)
            {
                Bukkit.broadcastMessage(Messages.pointsLost.toString().replace("${PLAYER}", event.getPlayer().getName()).replace("${NUMBER}", "" + alcool.getValue() * -1).replace("${ALCOOL}", alcool.getName()));
                GameUtils.broadcastSound(Sound.ENTITY_ZOMBIE_HORSE_DEATH, event.getPlayer().getLocation());
            }
            else
            {
                Bukkit.broadcastMessage(Messages.pointsGained.toString().replace("${PLAYER}", event.getPlayer().getName()).replace("${NUMBER}", "" + alcool.getValue()).replace("${ALCOOL}", alcool.getName()));
                GameUtils.broadcastSound(Sound.ENTITY_PLAYER_BURP, event.getPlayer().getLocation());
            }

            if (alcool.equals(Alcool.WHISKY))
            {
                GameUtils.broadcastSound(Sound.ENTITY_WITHER_DEATH, event.getPlayer().getLocation());
            }

            this.arena.noMoreBottle(event.getPlayer());

            if (score >= 15)
                this.arena.win(event.getPlayer());
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
        if (!this.arena.isGameStarted())
        {
            event.setCancelled(true);
            return;
        }

        if (event.getDamager() != null)
        {
            Entity damagedEntity = event.getEntity();
            Entity damagerEntity = event.getDamager();

            if (damagerEntity instanceof Player && damagedEntity instanceof Player)
            {
                Player damaged = (Player) damagedEntity;
                Player damager = (Player) damagerEntity;
                Date now = new Date();

                Date damaged_cool = this.arena.getDamagedCooldown().get(damaged.getUniqueId());
                Date antiDouble = this.arena.getDoubleLock().get(damaged.getUniqueId());
                Date antiDoubleDamager = this.arena.getDoubleLock().get(damager.getUniqueId());

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
                    damagerBottle = this.arena.getEmptyBottle();
                else if (damagedBottle == null)
                    damagedBottle = this.arena.getEmptyBottle();

                damager.getInventory().clear();
                damaged.getInventory().clear();

                damager.sendMessage(ChatColor.GREEN + "Vous avez échangé de bouteille avec " + ChatColor.RED + damaged.getName());
                damaged.sendMessage(ChatColor.RED + "Vous avez échangé de bouteille avec " + ChatColor.GREEN + damager.getName());

                final ItemStack damagerBottleFinal = damagerBottle;
                final ItemStack damagedBottleFinal = damagedBottle;

                this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
                {
                    damager.getInventory().setItem(0, damagedBottleFinal);
                    damaged.getInventory().setItem(0, damagerBottleFinal);

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

                if (damagerBottle.equals(this.arena.getEmptyBottle()))
                    this.arena.noMoreBottle(damaged);
                else
                    this.arena.newBottle(damaged);

                if (damagedBottle.equals(this.arena.getEmptyBottle()))
                    this.arena.noMoreBottle(damager);
                else
                    this.arena.newBottle(damager);

                Date antidouble = new Date();
                antidouble.setTime(antidouble.getTime() + 350);
                this.arena.getDoubleLock().put(damager.getUniqueId(), antidouble);
                this.arena.getDoubleLock().put(damaged.getUniqueId(), antidouble);

                Date cooldown = new Date();
                cooldown.setTime(cooldown.getTime() + 2200);
                this.arena.getDamagedCooldown().put(damager.getUniqueId(), cooldown);
                this.arena.getDamagedCooldown().put(damaged.getUniqueId(), cooldown);

                damaged.playEffect(EntityEffect.HURT);
                damager.playEffect(EntityEffect.HURT);
            }
        }
    }
}
