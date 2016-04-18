package fr.blueslime.hangover.arena;

import fr.blueslime.hangover.HangoverGames;
import fr.blueslime.hangover.Messages;
import fr.blueslime.hangover.tasks.DrinkTimer;
import fr.blueslime.hangover.tasks.LolNoise;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.Game;
import net.samagames.api.games.GamePlayer;
import net.samagames.api.games.themachine.messages.templates.PlayerLeaderboardWinTemplate;
import net.samagames.tools.GameUtils;
import net.samagames.tools.InventoryUtils;
import net.samagames.tools.PlayerUtils;
import net.samagames.tools.chat.ActionBarAPI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class Arena extends Game<GamePlayer>
{
    private final HangoverGames plugin;
    private final List<Location> cauldrons;
    private final Map<UUID, Integer> scores;
    private final Map<UUID, Integer> effectLevel;
    private final Map<UUID, Date> damagedCooldown;
    private final Map<UUID, Date> doubleLock;
    private final Map<UUID, BukkitTask> bottleTasks;
    private final Location spawn;
    private final LolNoise noise;
    private final Scoreboard scoreboard;
    private final Objective objective;
    private BukkitTask gameTime;
    private int nocive;

    public Arena(HangoverGames plugin, Location spawn, ArrayList<Location> cauldrons)
    {
        super("arcade", "HangoverGames", "Boissons illimitées pour tous !", GamePlayer.class);

        this.plugin = plugin;
        this.scores = new HashMap<>();
        this.effectLevel = new HashMap<>();
        this.damagedCooldown = new HashMap<>();
        this.doubleLock = new HashMap<>();
        this.bottleTasks = new HashMap<>();
        this.noise = new LolNoise(this);
        this.cauldrons = cauldrons;
        this.spawn = spawn;
        this.nocive = 0;

        this.scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();

        this.objective = this.scoreboard.registerNewObjective("hangoverbar", ChatColor.GREEN + "" + ChatColor.BOLD + "HangoverGames" + ChatColor.WHITE + " | " + ChatColor.AQUA + "00:00");
        this.objective.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "HangoverGames" + ChatColor.WHITE + " | " + ChatColor.AQUA + "00:00");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    @Override
    public void startGame()
    {
        super.startGame();

        this.objective.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "HangoverGames" + ChatColor.WHITE + " | " + ChatColor.AQUA + "00:00");
        this.objective.getScore(ChatColor.GOLD + "▪ Objectif : ").setScore(15);

        for (GamePlayer gamePlayer : this.getInGamePlayers().values())
        {
            Player player = gamePlayer.getPlayerIfOnline();

            ActionBarAPI.sendPermanentMessage(player, Messages.actionBarWarning.toString());

            InventoryUtils.cleanPlayer(player);

            player.getInventory().addItem(this.getEmptyBottle());
            player.teleport(this.spawn);
            player.setScoreboard(this.scoreboard);

            this.objective.getScore(PlayerUtils.getColoredFormattedPlayerName(player)).setScore(1);
            this.objective.getScore(PlayerUtils.getColoredFormattedPlayerName(player)).setScore(0);
            this.scores.put(player.getUniqueId(), 0);

            SamaGamesAPI.get().getStatsManager().getPlayerStats(player.getUniqueId()).getHangoverStatistics().incrByPlayedGames(1);
        }

        this.gameTime = this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, new Runnable()
        {
            private int time = 0;

            @Override
            public void run()
            {
                this.time++;
                objective.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "HangoverGames" + ChatColor.WHITE + " | " + ChatColor.AQUA + this.formatTime(this.time));
            }

            public String formatTime(int time)
            {
                int mins = time / 60;
                int remainder = time - mins * 60;
                int secs = remainder;

                String secsSTR = (secs < 10) ? "0" + secs : secs + "";

                return mins + ":" + secsSTR;
            }
        }, 0L, 20L);

        this.plugin.getServer().broadcastMessage(Messages.alcoolWarning.toString());

        this.noise.start();

        this.resetCauldrons();
        this.fillRandom();
        this.fillRandom();
        this.fillRandom();
    }

    @Override
    public void handlePostRegistration()
    {
        super.handlePostRegistration();

        this.coherenceMachine.setStartCountdownCatchPhrase("Préparez-vous à boire !");
        this.coherenceMachine.setNameShortcut("HG");
    }

    @Override
    public void handleLogin(Player player)
    {
        super.handleLogin(player);

        InventoryUtils.cleanPlayer(player);

        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(this.spawn);
        player.getInventory().setItem(8, this.coherenceMachine.getLeaveItem());

        this.gameManager.refreshArena();
    }

    public void forceDrink(Player player)
    {
        if (!this.isGameStarted())
            return;

        ItemStack bottle = null;

        for (ItemStack stack : player.getInventory().getContents())
        {
            if (stack.getType().equals(Material.GLASS_BOTTLE))
                return;

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
        this.bottleTasks.put(player.getUniqueId(), new DrinkTimer(this, player).runTaskTimer(this.plugin, 0L, 20L));
    }

    public void win(Player player)
    {
        this.gameTime.cancel();
        SamaGamesAPI.get().getStatsManager().getPlayerStats(player.getUniqueId()).getHangoverStatistics().incrByWins(1);

        this.bottleTasks.values().forEach(BukkitTask::cancel);

        LinkedHashMap<UUID, Integer> top = sortHashMapByValues(new HashMap<>(this.scores));
        Player first = null;
        Player second = null;
        Player third = null;
        Player last = null;
        int i = 0;

        int scoreFirst = 0;
        int scoreSecond = 0;
        int scoreThird = 0;

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
                scoreFirst = this.scores.get(uuid);
            }
            else if (i == 2)
            {
                this.addCoins(p, 30, "2eme");

                second = p;
                scoreSecond = this.scores.get(uuid);
            }
            else if (i == 3)
            {
                this.addCoins(p, 10, "3eme");

                third = p;
                scoreThird = this.scores.get(uuid);
            }
            else if (i == top.size())
            {
                last = p;
            }
        }

        PlayerLeaderboardWinTemplate template = SamaGamesAPI.get().getGameManager().getCoherenceMachine().getTemplateManager().getPlayerLeaderboardWinTemplate();
        template.execute(first, second, third, null, scoreFirst, scoreSecond, scoreThird);

        if (last != null)
            Bukkit.broadcastMessage(ChatColor.GOLD + "Heureusement que " + ChatColor.AQUA + last.getName() + ChatColor.GOLD + " n'a pas trop bû et les ramènera en voiture !");

        this.effectsOnWinner(player);

        this.handleGameEnd();
    }

    public void resetCauldrons()
    {
        this.cauldrons.forEach(location -> location.getBlock().setType(Material.AIR));
    }

    public void fillRandom()
    {
        Random random = new Random();
        Location location = this.cauldrons.get(random.nextInt(this.cauldrons.size()));

        if (location.getBlock().getType().equals(Material.CAULDRON))
        {
            this.fillRandom();
            return;
        }

        location.getBlock().setType(Material.CAULDRON);
        location.getBlock().setData((byte) 1);
        GameUtils.broadcastSound(Sound.BLOCK_ANVIL_LAND, location);
    }

    public static LinkedHashMap<UUID, Integer> sortHashMapByValues(Map<UUID, Integer> scores)
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

    public Map<UUID, Date> getDamagedCooldown()
    {
        return this.damagedCooldown;
    }

    public Map<UUID, Date> getDoubleLock()
    {
        return this.doubleLock;
    }

    public Map<UUID, Integer> getEffectLevel()
    {
        return this.effectLevel;
    }

    public Map<UUID, Integer> getScores()
    {
        return this.scores;
    }

    public ItemStack getEmptyBottle()
    {
        ItemStack bottle = new ItemStack(Material.GLASS_BOTTLE, 1);
        ItemMeta data = bottle.getItemMeta();

        data.setDisplayName(ChatColor.GOLD + "Bouteille (encore) vide");

        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Remplissez cette bouteille et buvez là vite !");

        data.setLore(lore);
        bottle.setItemMeta(data);

        return bottle;
    }

    public Location getSpawn()
    {
        return this.spawn;
    }

    public Objective getObjective()
    {
        return this.objective;
    }

    public int getNocive()
    {
        return this.nocive;
    }
}
