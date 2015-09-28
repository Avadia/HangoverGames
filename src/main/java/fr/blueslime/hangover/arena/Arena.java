package fr.blueslime.hangover.arena;

import fr.blueslime.hangover.HangoverGames;
import fr.blueslime.hangover.Messages;
import fr.blueslime.hangover.tasks.DrinkTimer;
import fr.blueslime.hangover.tasks.LolNoise;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.Game;
import net.samagames.api.games.GamePlayer;
import net.samagames.api.games.themachine.messages.templates.PlayerLeaderboardWinTemplate;
import net.samagames.tools.ColorUtils;
import net.samagames.tools.GameUtils;
import net.samagames.tools.chat.ActionBarAPI;
import net.samagames.tools.scoreboards.ObjectiveSign;
import net.samagames.tools.scoreboards.VObjective;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Arena extends Game<GamePlayer>
{
    private ArrayList<Location> cauldrons;
    private HashMap<UUID, Integer> scores;
    private HashMap<UUID, Integer> effectLevel;
    private HashMap<UUID, Date> damagedCooldown;
    private HashMap<UUID, Date> doubleLock;
    private HashMap<UUID, BukkitTask> bottleTasks;
    private Location spawn;
    private LolNoise noise;
    private Integer nocive;
    private VObjective objective;
	private BukkitTask gameTime;

    public Arena(Location spawn, ArrayList<Location> cauldrons)
    {
        super("arcade", "HangoverGames", "Boissons illimités pour tous !", GamePlayer.class);

        this.scores = new HashMap<>();
        this.effectLevel = new HashMap<>();
        this.damagedCooldown = new HashMap<>();
        this.doubleLock = new HashMap<>();
        this.bottleTasks = new HashMap<>();
        this.noise = new LolNoise(this);
        this.cauldrons = cauldrons;
        this.spawn = spawn;
        this.nocive = 0;

        this.objective = new ObjectiveSign("hangoverbar", ChatColor.GREEN + "" + ChatColor.BOLD + "HangoverGames" + ChatColor.WHITE + " | " + ChatColor.AQUA + "00:00");
        this.objective.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "HangoverGames" + ChatColor.WHITE + " | " + ChatColor.AQUA + "00:00");
    }

    @Override
    public void startGame()
    {
        super.startGame();

        this.objective.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "HangoverGames" + ChatColor.WHITE + " | " + ChatColor.AQUA + "00:00");
        this.objective.getScore(ChatColor.GREEN + "> Objectif : ").setScore(15);

        for (GamePlayer gamePlayer : this.getInGamePlayers().values())
        {
            Player player = gamePlayer.getPlayerIfOnline();

            this.setupPlayer(player);
            player.getInventory().addItem(this.getEmptyBottle());
            player.teleport(this.spawn);

            this.objective.addReceiver(player);
            this.objective.getScore(player.getName()).setScore(1);
            this.objective.getScore(player.getName()).setScore(0);
            this.scores.put(player.getUniqueId(), 0);

            this.increaseStat(player.getUniqueId(), "played_games", 1);
        }

        this.gameTime = Bukkit.getScheduler().runTaskTimerAsynchronously(HangoverGames.getInstance(), new Runnable()
        {
            private int time = 0;

            @Override
            public void run()
            {
                this.time++;
                objective.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "HangoverGames" + ChatColor.WHITE + " | " + ChatColor.AQUA + this.formatTime(this.time));
                objective.update();
            }

            public String formatTime(int time)
            {
                int mins = time / 60;
                int remainder = time - mins * 60;
                int secs = remainder;

                String secsSTR = (secs < 10) ? "0"+secs : secs+"";

                if(secs == 15 || secs == 30 || secs == 45 || secs == 59)
                    for(Player player : Bukkit.getOnlinePlayers())
                        ActionBarAPI.sendPermanentMessage(player, Messages.actionBarWarning.toString());

                return mins + ":" + secsSTR;
            }
        }, 0L, 20L);

        Bukkit.broadcastMessage(Messages.alcoolWarning.toString());

        this.noise.start();

        this.resetCauldrons();
        this.fillRandom();
        this.fillRandom();
        this.fillRandom();
    }

    @Override
    public void handleLogin(Player player)
    {
        super.handleLogin(player);

        player.teleport(this.spawn);
        this.setupPlayer(player);
        player.getInventory().setItem(8, this.coherenceMachine.getLeaveItem());

        this.gameManager.refreshArena();
    }

    @Override
    public void handleLogout(Player player)
    {
        super.handleLogout(player);

        this.objective.removeReceiver(player);
    }

	public void forceDrink(Player player)
    {
		if (!this.isGameStarted())
			return;
		
		ItemStack bottle = null;

		for (ItemStack stack : player.getInventory().getContents())
        {
			if (stack.getType().equals(Material.GLASS_BOTTLE)) return;

			if (stack.getType().equals(Material.POTION))
            {
                bottle = stack;
				break;
			}
		}

		Bukkit.broadcastMessage(Messages.tooLateAlcool.toString().replace("${PLAYER}", player.getName()));
		Bukkit.getServer().getPluginManager().callEvent(new PlayerItemConsumeEvent(player, bottle));
	}
	
	public void noMoreBottle(Player player)
    {
		BukkitTask task = this.bottleTasks.get(player.getUniqueId());

		if (task != null)
			task.cancel();

		this.bottleTasks.remove(player.getUniqueId());
        player.setLevel(0);
	}
	
	public void newBottle(Player player)
    {
		this.noMoreBottle(player);
		this.bottleTasks.put(player.getUniqueId(), new DrinkTimer(this, player).runTaskTimer(HangoverGames.getInstance(), 0L, 20L));
	}
	
	public void setupPlayer(Player p)
    {
		p.setGameMode(GameMode.ADVENTURE);
		p.setMaxHealth(20.0);
		p.setHealthScale(20);
		p.setHealth(20.0);
		p.setSaturation(20);
		p.getInventory().clear();

		for (PotionEffect ef : p.getActivePotionEffects())
			p.removePotionEffect(ef.getType());
	}

	public void win(Player player)
    {
        this.gameTime.cancel();
        this.increaseStat(player.getUniqueId(), "wins", 1);

        this.bottleTasks.values().forEach(org.bukkit.scheduler.BukkitTask::cancel);

        LinkedHashMap<UUID, Integer> top = this.sortHashMapByValues(new HashMap<>(this.scores));
        Player first = null;
        Player second = null;
        Player third = null;
        Player last = null;
        int i = 0;

        for (UUID uuid : top.keySet())
        {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            i++;

            if (i == 1)
            {
                this.addCoins(p, 50, "1er");
                this.addStars(p, 1, "1er");
                first = p;
            }
            else if (i == 2)
            {
                this.addCoins(p, 30, "2eme");
                second = p;
            }
            else if (i == 3)
            {
                this.addCoins(p, 10, "3eme");
                third = p;
            }
            else if (i == top.size())
            {
                last = p;
            }
        }

        PlayerLeaderboardWinTemplate template = SamaGamesAPI.get().getGameManager().getCoherenceMachine().getTemplateManager().getPlayerLeaderboardWinTemplate();
        template.execute(first, second, third, null, this.scores.get(first.getUniqueId()), this.scores.get(second.getUniqueId()), this.scores.get(third.getUniqueId()));

        if (last != null)
            Bukkit.broadcastMessage(ChatColor.GOLD + "Heureusement que " + ChatColor.AQUA + last.getName() + ChatColor.GOLD + " n'a pas trop bû et les ramènera en voiture !");

        Bukkit.getScheduler().scheduleSyncRepeatingTask(HangoverGames.getInstance(), new Runnable()
        {
            int number = (int) (10 * 1.5);
            int count = 0;

            public void run()
            {
                if (this.count >= this.number || player.getPlayer() == null)
                    return;

                Firework fw = (Firework) player.getPlayer().getWorld().spawnEntity(player.getPlayer().getLocation(), EntityType.FIREWORK);
                FireworkMeta fwm = fw.getFireworkMeta();

                Random r = new Random();

                int rt = r.nextInt(4) + 1;
                Type type = Type.BALL;
                if (rt == 1) type = Type.BALL;
                if (rt == 2) type = Type.BALL_LARGE;
                if (rt == 3) type = Type.BURST;
                if (rt == 4) type = Type.CREEPER;
                if (rt == 5) type = Type.STAR;

                int r1i = r.nextInt(17) + 1;
                int r2i = r.nextInt(17) + 1;
                Color c1 = ColorUtils.getColor(r1i);
                Color c2 = ColorUtils.getColor(r2i);

                FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();

                fwm.addEffect(effect);

                int rp = r.nextInt(2) + 1;
                fwm.setPower(rp);

                fw.setFireworkMeta(fwm);

                this.count++;
            }
        }, 5L, 5L);

        this.handleGameEnd();
    }

	public void resetCauldrons()
    {
		for (Location location : this.cauldrons)
            location.getBlock().setType(Material.AIR);
	}
	
	public void fillRandom()
    {
		Random random = new Random();
		Location location = cauldrons.get(random.nextInt(cauldrons.size()));

		if (location.getBlock().getType().equals(Material.CAULDRON))
        {
			this.fillRandom();
			return;
		}

        location.getBlock().setType(Material.CAULDRON);
        location.getBlock().setData((byte) 1);
        GameUtils.broadcastSound(Sound.ANVIL_LAND, location);
    }

    public static LinkedHashMap<UUID, Integer> sortHashMapByValues(HashMap<UUID, Integer> scores)
    {
        List<UUID> mapKeys = new ArrayList<>(scores.keySet());
        List<Integer> mapValues = new ArrayList<>(scores.values());
        Collections.sort(mapValues, Collections.reverseOrder());
        Collections.sort(mapKeys, Collections.reverseOrder());

        LinkedHashMap<UUID, Integer> sortedMap = new LinkedHashMap<>();

        Iterator valueIt = mapValues.iterator();

        while (valueIt.hasNext())
        {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();

            while (keyIt.hasNext())
            {
                Object key = keyIt.next();
                String comp1 = scores.get(key).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2))
                {
                    scores.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((UUID) key, (Integer) val);
                    break;
                }
            }
        }

        return sortedMap;
    }

    public void setNocive(int nocive)
    {
        this.nocive = nocive;
    }

    public HashMap<UUID, Date> getDamagedCooldown()
    {
        return this.damagedCooldown;
    }

    public HashMap<UUID, Date> getDoubleLock()
    {
        return this.doubleLock;
    }

    public HashMap<UUID, Integer> getEffectLevel()
    {
        return this.effectLevel;
    }

    public HashMap<UUID, Integer> getScores()
    {
        return this.scores;
    }

    public ItemStack getEmptyBottle()
    {
        ItemStack bottle = new ItemStack(Material.GLASS_BOTTLE, 1);
        ItemMeta data = bottle.getItemMeta();

        data.setDisplayName(ChatColor.GOLD + "Bouteille (encore) vide");

        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD+"Remplissez cette bouteille et buvez là vite !");

        data.setLore(lore);
        bottle.setItemMeta(data);

        return bottle;
    }

    public Location getSpawn()
    {
        return this.spawn;
    }

    public VObjective getObjective()
    {
        return this.objective;
    }

    public int getNocive()
    {
        return this.nocive;
    }
}
