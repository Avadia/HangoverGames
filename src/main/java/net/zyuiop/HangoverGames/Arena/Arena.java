package net.zyuiop.HangoverGames.Arena;

import net.samagames.network.Network;
import net.samagames.network.client.GameArena;
import net.samagames.network.client.GamePlayer;
import net.samagames.network.json.Status;
import net.zyuiop.HangoverGames.HangoverGames;
import net.zyuiop.HangoverGames.Messages;
import net.zyuiop.HangoverGames.Tasks.BeginTimer;
import net.zyuiop.HangoverGames.Tasks.DrinkTimer;
import net.zyuiop.HangoverGames.Tasks.LolNoise;
import net.zyuiop.coinsManager.CoinsManager;
import net.zyuiop.statsapi.StatsApi;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.util.*;
public class Arena extends GameArena {
	
	/* Arena data */
	public int minPlayers;
	public ArrayList<Location> cauldrons;
	public Location spawn;
	
	/* Run data */
	public HashMap<UUID, Integer> scores = new HashMap<UUID, Integer>();
	public HashMap<UUID, Integer> effectLevel = new HashMap<UUID, Integer>();
	public BeginTimer timer =  null;
	
	public HashMap<UUID, Date> damagedcooldown = new HashMap<UUID, Date>();
	public HashMap<UUID, Date> antiDouble = new HashMap<UUID, Date>();
	
	public HashMap<UUID, BukkitTask> bottleTasks = new HashMap<UUID, BukkitTask>();
	
	public Integer nocive = 0;
	public LolNoise noise = null;
	
	/* Misc data */
	public File dataSource;
	
	public Scoreboard scoreboard;
	public Objective objective;
	
	private BukkitTask gameTime;

    protected Arena(int maxPlayers, int maxVIP, String mapName, UUID arenaID) {
        super(maxPlayers, maxVIP, mapName, arenaID, false);
    }

    @Override
    public String finishJoinPlayer(UUID playerID) {
        Bukkit.getLogger().info("Joining arena : "+this);
        String ret = super.finishJoinPlayer(playerID);
        Bukkit.getLogger().info(ret);
        if (!ret.equals("OK"))
            return ret;
		Player player = Bukkit.getPlayer(playerID);
		// Ajoute le joueur
		player.sendMessage(Messages.REJOINT_ARENE);
		player.teleport(this.spawn);
		
		broadcastMessage(Messages.REJOINT_ARENE_BROADCAST.replace("{PSEUDO}", player.getName()).replace("{JOUEURS}", ""+players.size()).replace("{JOUEURS_MAX}", ""+this.maxPlayers));
		
		refreshPlayers(true);
		setupPlayer(player);
		
		for (GameArena ar : HangoverGames.instance.getArenaManager().getArenas().values()) {
			if (!ar.getArenaID().equals(this.arenaID)) {
				for (GamePlayer joueur : ar.getPlayers()) {
					Player j = joueur.getPlayer();
					if (j != null) {
						j.hidePlayer(player);
						player.hidePlayer(j);
					}
				}
			}
		}
		
		ArrayList<GamePlayer> removal = new ArrayList<>();
		for (GamePlayer joueur : players) {
			Player j = joueur.getPlayer();
			if (j == null) {
				removal.add(joueur);
				continue;
			}
			j.showPlayer(player);
			player.showPlayer(j);
		}
		
		for (GamePlayer joueur : removal) {
			players.remove(joueur);
		}

		int slot = 10;
		for (Alcool a : Alcool.values()) {
			ItemStack bottle = HangoverGames.instance.getBottle(a);
			player.getInventory().setItem(slot, bottle);
			slot++;
		}
		
		player.getInventory().setItem(8, HangoverGames.instance.getLeaveItem());
	    ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
	    BookMeta bm = (BookMeta)book.getItemMeta();
	    bm.setAuthor("SamaGames - zyuiop");
	    bm.setTitle("Règles du jeu");
	    ArrayList<String> pages = new ArrayList<String>();
	    // Typo?
	    pages.add(ChatColor.GOLD+"Bienvenue dans les "+ChatColor.DARK_AQUA+"Hangover Games"+ChatColor.DARK_GREEN+" ! \n\n > Sommaire : "+ChatColor.BLACK+"\n\n P.2: Principe du jeu \n P.3: Fonctionnement\n\n"+ChatColor.RED+"L'abus d'alcool est dangereux pour la santé.\n\n"+ChatColor.BLACK+"Jeu : zyuiop\nMaps : Amalgar");
	    pages.add(ChatColor.DARK_GREEN+"Principe du jeu :"+ChatColor.BLACK+"\n\nLe but du jeu est de boire le plus possible pour être le premier à avoir 15 grammes d'alcool dans le sang. Certains alcools font vomir et perdre de l'alcool.");
	    pages.add(ChatColor.DARK_GREEN+"Fonctionnement :"+ChatColor.BLACK+"\n\n- Il y a 3 marmites réparties sur la map. Remplis ta bouteille dans une marmite et obtiens un alcool au hasard. La marmite disparait et réaparait à un autre endroit de la map.");
	    pages.add(ChatColor.DARK_GREEN+"Fonctionnement :"+ChatColor.BLACK+"\n\n- Bois la bouteille et gagne / perd des points\n- N'hésite pas à taper tes amis pour échanger ta bouteille si l'alcool ne te plait pas !\n- Le premier à "+ChatColor.DARK_AQUA+"15 points"+ChatColor.BLACK+" gagne.");
	    bm.setPages(pages);
	    // Lets fix the typo
	    book.setItemMeta(bm);
	    player.getInventory().setItem(0, book);
	    player.sendMessage(ChatColor.GOLD+"\nBienvenue en "+ChatColor.AQUA+"Hangover Games"+ChatColor.GOLD+" !");
	    player.sendMessage("Avant de commencer la soirée, n'hésite pas à lire les règles et à voir les effets des alcools disposés dans ton inventaire.\n");
        Network.getManager().refreshArena(this);
		return "OK";
	}

    @Override
    public void logout(UUID playerID) {
        super.logout(playerID);
        stumpPlayer(new GamePlayer(playerID));
    }
	
	/*
	 * Affiche le message [message] à tous les joueurs de l'arène
	 */
	public void broadcastMessage(String message) {
		for (GamePlayer p : players) {
            Player player = Bukkit.getPlayer(p.getPlayerID());
            if (player != null)
                player.sendMessage(message);
        }
	}
	
	/*
	 * Envoie le son à tous les joueurs
	 */
	public void broadcastSound (Sound s) {
		for (GamePlayer p : players)
			p.getPlayer().playSound(p.getPlayer().getLocation(), s, 1, 1);
	}

    public void broadcastSound (Sound s, Location l) {
		for (GamePlayer p : players)
			p.getPlayer().playSound(l, s, 2, 1);
	}
	
	public boolean canJoin() {
		return ((status.equals(Status.Available) || status.equals(Status.Starting)) && players.size() < maxPlayers);
	}
	
	public boolean canJoinVIP() {
		return ((status.equals(Status.Available) || status.equals(Status.Starting)) && players.size() < maxPlayers+2);
	}
	
	public boolean canJoinStaff() {
		return (status.equals(Status.Available) || status.equals(Status.Starting));
	}
	
	public void forceDrink(Player p) {
		if (!this.isGameStarted())
			return;
		
		ItemStack bot = null;
		for (ItemStack b : p.getInventory().getContents()) {
			if (b.getType().equals(Material.GLASS_BOTTLE)) return;
			if (b.getType().equals(Material.POTION)) { bot = b;
				break;
			}
		}
		broadcastMessage(Messages.RETIENT.replace("{PSEUDO}", p.getName()));
		Bukkit.getServer().getPluginManager().callEvent(new PlayerItemConsumeEvent(p, bot));
	}
	
	public void noMoreBottle(Player p) {
		BukkitTask task = bottleTasks.get(p.getUniqueId());
		if (task != null)
			task.cancel();
		bottleTasks.remove(p.getUniqueId());
		p.setLevel(0);
	}
	
	public void newBottle(Player p) {
		noMoreBottle(p);
		bottleTasks.put(p.getUniqueId(), new DrinkTimer(this, p).runTaskTimer(HangoverGames.instance, 0L, 20L));
	}
	
	public boolean isGameStarted() {
		return (status == Status.InGame);
	}

	public boolean isPlaying(GamePlayer player) {
		return players.contains(player);
	}
	
	public boolean isPlaying(UUID player) {
		return isPlaying(new GamePlayer(player));
	}
	
	public void refreshPlayers(boolean addPlayers) {
		if (isGameStarted())  {
            Network.getManager().refreshArena(this);
			return;
		}
		
		// Compte a rebours démarré mais plus assez de joueurs
		if (timer != null && players.size() < minPlayers)  {
			broadcastMessage(Messages.MANQUE_JOUEURS);
			timer.setTimeout(0);
			timer.end();
			timer = null;
			status = Status.Available;
            Network.getManager().refreshArena(this);
			return;
		}
		
		// Compte à rebours non démarré mais assez de joueurs
		if (timer == null && players.size() >= minPlayers) {
			timer = new BeginTimer(this);
			timer.start();
			status = Status.Starting;
		}

        Network.getManager().refreshArena(this);
	}
	
	public void setupPlayer(Player p) {
		p.setGameMode(GameMode.ADVENTURE);
		p.setMaxHealth(20.0);
		p.setHealthScale(20);
		p.setHealth(20.0);
		p.setSaturation(20);
		p.getInventory().clear();
		for (PotionEffect ef : p.getActivePotionEffects())
			p.removePotionEffect(ef.getType());
	}
	
	public void start() {
		status = Status.InGame;
		
		ArrayList<GamePlayer> remove = new ArrayList<>();
		
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		objective = scoreboard.registerNewObjective("points", "dummy");
		objective.setDisplayName(ChatColor.GREEN+""+ChatColor.BOLD+"HangoverGames"+ChatColor.WHITE+" | "+ChatColor.AQUA+"00:00");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.getScore(ChatColor.GREEN+"> Objectif : ").setScore(15);
		
		for (GamePlayer pl : players) {
			Player p = pl.getPlayer();
			if (p == null) {
				remove.add(pl);
				continue;
			}
			
			p.setFireTicks(0);
			setupPlayer(p);
			p.getInventory().addItem(HangoverGames.instance.emptyBottle());
			p.sendMessage(Messages.DEBUT_PARTIE);
			p.teleport(this.spawn);
			p.setScoreboard(scoreboard);
			
			StatsApi.increaseStat(p, "trollcade", "hangovergames.played_games", 1);
		}
		
	
        gameTime = Bukkit.getScheduler().runTaskTimer(HangoverGames.instance, new Runnable() {
			private int time = 0;
        	@Override
        	public void run() {
        		time++;
				objective = scoreboard.getObjective("points");
				objective.setDisplayName(ChatColor.GREEN+""+ChatColor.BOLD+"HangoverGames"+ChatColor.WHITE+" | "+ChatColor.AQUA+formatTime(time));
			}
        	
        	public String formatTime(int time) {
        		int mins = (int) time / 60;
        		int remainder = time - mins * 60;
        		int secs = remainder;
        		String secsSTR = (secs < 10) ? "0"+secs : secs+"";
        		return mins+":"+secsSTR;
        	}
 		}, 0L, 20L);
		
		
		
		// Détruit le timer
		if (timer != null) timer.end();
		timer = null;
        Network.getManager().refreshArena(this);
		
		this.noise = new LolNoise(this);
		noise.start();
		
		// Init cauldrons //
		resetCauldrons();
		fillRandom();
		fillRandom();
		fillRandom();
	}
	
	public void endGame() {
		status = Status.Stopping;
		gameTime.cancel();
        Network.getManager().refreshArena(this);
		for (GamePlayer pl : this.players) {
			HangoverGames.instance.kickPlayer(pl.getPlayer());
		}
		
		if (this.noise != null) {
			noise.end();
			noise = null;
		}
		
		status = Status.Stopping;
		this.players.clear();
		this.scores.clear();
		try {
			for (BukkitTask t : bottleTasks.values()) 
				t.cancel();
		} catch (Exception e) {
			// on s'en bat lec'
		}
			
		this.antiDouble.clear();
		this.bottleTasks.clear();
		this.effectLevel.clear();
		this.nocive = 0;
		this.scoreboard = null;
		this.objective = null;
        Network.getManager().refreshArena(this);
		resetCauldrons();
	}
	
	public Color getColor(int i) {
        Color c = null;
        if (i == 1) {
            c = Color.AQUA;
        }
        if (i == 2) {
            c = Color.BLACK;
        }
        if (i == 3) {
            c = Color.BLUE;
        }
        if (i == 4) {
            c = Color.FUCHSIA;
        }
        if (i == 5) {
            c = Color.GRAY;
        }
        if (i == 6) {
            c = Color.GREEN;
        }
        if (i == 7) {
            c = Color.LIME;
        }
        if (i == 8) {
            c = Color.MAROON;
        }
        if (i == 9) {
            c = Color.NAVY;
        }
        if (i == 10) {
            c = Color.OLIVE;
        }
        if (i == 11) {
            c = Color.ORANGE;
        }
        if (i == 12) {
            c = Color.PURPLE;
        }
        if (i == 13) {
            c = Color.RED;
        }
        if (i == 14) {
            c = Color.SILVER;
        }
        if (i == 15) {
            c = Color.TEAL;
        }
        if (i == 16) {
            c = Color.WHITE;
        }
        if (i == 17) {
            c = Color.YELLOW;
        }

        return c;
    }
	
	public void win(final GamePlayer player) {
		// On fera des trucs ici
		gameTime.cancel();
		status = Status.Stopping;
        Network.getManager().refreshArena(this);
		StatsApi.increaseStat(player.getPlayerID(), "trollcade", "hangovergames.wins", 1);
		
		for (BukkitTask t : this.bottleTasks.values()) {
			t.cancel();
		}
		
		int i = 0;
		String message = Messages.WIN.replace("{PSEUDO}", player.getPlayer().getName());
		
		
		broadcastMessage(ChatColor.GOLD+"===========================================");
		broadcastMessage(" ");
		broadcastMessage(message);
		broadcastMessage(" ");
		broadcastMessage(ChatColor.GOLD+"===========================================");
		
		broadcastMessage(" ");
		broadcastMessage(ChatColor.GOLD+">> Top 3 des alcooliques : <<");
		
		LinkedHashMap<UUID, Integer> top = HangoverGames.sortHashMapByValuesD(scores);
		
		Player last = null;
		for (UUID pl : top.keySet()) {
			Player p = Bukkit.getPlayer(pl);
			int score = top.get(pl);
			if (p == null) continue;
			i++;
			
			if (i <= 3)
				broadcastMessage(i+" : "+ChatColor.AQUA+p.getName()+ChatColor.GOLD+" avec "+score+" g/L de sang");
			if (i == 1)
				CoinsManager.creditJoueur(pl, 50, true);
			else if (i == 2)
				CoinsManager.creditJoueur(pl, 30, true);
			else if (i == 3)
				CoinsManager.creditJoueur(pl, 10, true);
			else
				last = p;
		}
		
		if (last != null)
			broadcastMessage(ChatColor.GOLD+"Heureusement que "+ChatColor.AQUA+last.getName()+ChatColor.GOLD+" n'a pas trop bû et les ramènera en voiture !");
		
		final int nb = (int) (10 * 1.5);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(HangoverGames.instance, new Runnable() {
            int compteur = 0;

            public void run() {

                if (compteur >= nb || player.getPlayer() == null) {
                    return;
                }

                //Spawn the Firework, get the FireworkMeta.
                Firework fw = (Firework) player.getPlayer().getWorld().spawnEntity(player.getPlayer().getLocation(), EntityType.FIREWORK);
                FireworkMeta fwm = fw.getFireworkMeta();

                //Our random generator
                Random r = new Random();

                //Get the type
                int rt = r.nextInt(4) + 1;
                Type type = Type.BALL;
                if (rt == 1) type = Type.BALL;
                if (rt == 2) type = Type.BALL_LARGE;
                if (rt == 3) type = Type.BURST;
                if (rt == 4) type = Type.CREEPER;
                if (rt == 5) type = Type.STAR;

                //Get our random colours
                int r1i = r.nextInt(17) + 1;
                int r2i = r.nextInt(17) + 1;
                Color c1 = getColor(r1i);
                Color c2 = getColor(r2i);

                //Create our effect with this
                FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();

                //Then apply the effect to the meta
                fwm.addEffect(effect);

                //Generate some random power and set it
                int rp = r.nextInt(2) + 1;
                fwm.setPower(rp);

                //Then apply this to our rocket
                fw.setFireworkMeta(fwm);

                compteur++;


            }

        }, 5L, 5L);
		
		Bukkit.getScheduler().runTaskLater(HangoverGames.instance, new Runnable() {
			public void run() {
				endGame();
			}
		}, 10*20L);
		Bukkit.getScheduler().runTaskLater(HangoverGames.instance, new Runnable() {
			public void run() {
				setupGame();
			}
		}, 12*20L);
		
	}
	
	public void stumpPlayer(GamePlayer player) {
		this.players.remove(player);
		if (!this.isGameStarted()){
			this.refreshPlayers(false);
			return;
		}
		
		this.broadcastMessage(ChatColor.AQUA+player.getPlayer().getName()+ChatColor.GOLD+" est parti(e) :'(");
		scoreboard.resetScores(player.getPlayer().getName());
		
		if (players.size() == 1)
			win(players.get(0));
		else if (players.size() < 1) {
			endGame();
			setupGame();
		}
        Network.getManager().refreshArena(this);
	}
	
	public void setupGame() {
		status = Status.Available;
        Network.getManager().refreshArena(this);
	}
	
	public long getCount() {
		if (timer == null) return 0;
		else return timer.time;
	}
	
	public void resetCauldrons() {
		for (Location l : cauldrons) {
			//l.getBlock().setData((byte)0);
			l.getBlock().setType(Material.AIR);
		}
	}
	
	public void fillRandom() {
		Random randomize = new Random();
		Location l = cauldrons.get(randomize.nextInt(cauldrons.size()));
		if (l.getBlock().getType().equals(Material.CAULDRON)) {
			fillRandom();
			return;
		}
		Bukkit.getLogger().info("Location is "+l.toString());
		l.getBlock().setType(Material.CAULDRON);
		l.getBlock().setData((byte)1);
		broadcastSound(Sound.ANVIL_LAND, l);
	}

    @Override
    public int countPlayersIngame() {
        return players.size();
    }
}
