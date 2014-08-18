package net.zyuiop.HangoverGames.Arena;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;

import net.minecraft.server.v1_7_R3.NBTTagCompound;
import net.minecraft.server.v1_7_R3.NBTTagList;
import net.zyuiop.HangoverGames.HangoverGames;
import net.zyuiop.HangoverGames.Messages;
import net.zyuiop.HangoverGames.Network.Status;
import net.zyuiop.HangoverGames.Tasks.BeginTimer;
import net.zyuiop.HangoverGames.Tasks.LolNoise;
import net.zyuiop.coinsManager.CoinsManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
public class Arena {
	
	/* Arena data */
	public String arenaName;
	public String mapName;
	public Integer maxPlayers;
	public int minPlayers;
	public UUID arenaId;
	public ArrayList<Location> cauldrons;
	public Location spawn;
	
	/* Run data */
	public ArrayList<VirtualPlayer> players = new ArrayList<VirtualPlayer>();
	public HashMap<UUID, Integer> scores = new HashMap<UUID, Integer>();
	public HashMap<UUID, Integer> effectLevel = new HashMap<UUID, Integer>();
	public Status status = Status.Available;
	public BeginTimer timer =  null;
	
	public HashMap<UUID, Date> cooldown = new HashMap<UUID, Date>();
	
	public Integer nocive = 0;
	public LolNoise noise = null;
	
	/* Misc data */
	public File dataSource;
	
	public Scoreboard scoreboard;
	public Objective objective;
	/*
	public void playerEffects(Player player) {
		Integer score = scores.get(player.getUniqueId());
		if (score == null) score = 1;
		if (score == 0) score = 1;
		Integer effect = effectLevel.get(player.getUniqueId());
		if (effect == null) return;
		
		double indice = effect/score;
		System.out.println("DATA : "+score+" - eff "+effect);
		for (PotionEffect ef : player.getActivePotionEffects())
			player.removePotionEffect(ef.getType());
		
		if (indice <= 1) {
			// Confusion only
			player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 12*20*effect, 0));
		} else if (indice <= 3.5) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (int)Math.ceil(30*20*indice), 1));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int)Math.ceil(25*20*indice), 0));
		} else {
			player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (int)Math.ceil(30*20*indice), 2));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int)Math.ceil(20*20*indice), 1));
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int)Math.ceil(10*indice), 0));
		} 
	}
	*/
	public String addPlayer(Player player) {
		if (isPlaying(player.getUniqueId())) {
			return Messages.DEJA_DANS_ARENE;
		}
		
		if (!canJoin()) {
			return Messages.ARENE_PLEINE;
		}
		
		if (HangoverGames.instance.arenasManager.getPlayerArena(player.getUniqueId()) != null) {
			return Messages.DEJA_EN_JEU;
		}
				
		// Ajoute le joueur
		player.sendMessage(Messages.REJOINT_ARENE);
		player.teleport(this.spawn);
		players.add(new VirtualPlayer(player));
		
		broadcastMessage(Messages.REJOINT_ARENE_BROADCAST.replace("{PSEUDO}", player.getName()).replace("{JOUEURS}", ""+players.size()).replace("{JOUEURS_MAX}", ""+this.maxPlayers));
		
		refreshPlayers(true);
		setupPlayer(player);
		
		for (Arena ar : HangoverGames.instance.arenasManager.getArenas().values()) {
			if (!ar.arenaName.equals(this.arenaName)) {
				for (VirtualPlayer joueur : ar.players) {
					Player j = joueur.getPlayer();
					if (j != null) {
						j.hidePlayer(player);
						player.hidePlayer(j);
					}
				}
			}
			
		}
		
		ArrayList<VirtualPlayer> removal = new ArrayList<VirtualPlayer>();
		for (VirtualPlayer joueur : players) {
			Player j = joueur.getPlayer();
			if (j == null) {
				removal.add(joueur);
				continue;
			}
			j.showPlayer(player);
			player.showPlayer(j);
		}
		
		for (VirtualPlayer joueur : removal) {
			players.remove(joueur);
		}
		
		// Item a give
		ItemStack change = new ItemStack(Material.ITEM_FRAME, 1);
		ItemMeta changemeta = change.getItemMeta();
		
		changemeta.setDisplayName(ChatColor.RED + "Changer d'arène");
		change.setItemMeta(changemeta);
		

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
	    pages.add(ChatColor.GOLD+"Bienvenue dans les "+ChatColor.DARK_AQUA+"Hangover Games"+ChatColor.DARK_GREEN+" ! \n\n > Sommaire : "+ChatColor.BLACK+"\n\n P.2: Principe du jeu \n P.3: Fonctionnement");
	    pages.add(ChatColor.DARK_GREEN+"Principe du jeu :"+ChatColor.BLACK+"\n\nLe but du jeu est de boire le plus possible pour être le premier a avoir 12 grammes d'alcool dans le sang. Certains alcools font vomir et perdre de l'alcool.");
	    pages.add(ChatColor.DARK_GREEN+"Fonctionnement :"+ChatColor.BLACK+"\n\n- Il y a 3 marmites réparties sur la map. Remplis ta bouteille dans une marmite et obtiens un alcool au hasard. La marmite disparait et réaparait a un autre endroit de la map.");
	    pages.add(ChatColor.DARK_GREEN+"Fonctionnement :"+ChatColor.BLACK+"\n\n- Bois la bouteille et gagne / perd des points\n- N'hésite pas à taper tes amis pour échanger ta bouteille si l'alcool ne te plait pas !\n- Le premier à "+ChatColor.DARK_AQUA+"12 points"+ChatColor.BLACK+" gagne.");
	    bm.setPages(pages);
	    // Lets fix the typo
	    book.setItemMeta(bm);
	    player.getInventory().setItem(0, book);
	    player.sendMessage(ChatColor.GOLD+"\nBienvenue en "+ChatColor.AQUA+"Hangover Games"+ChatColor.GOLD+" !");
	    player.sendMessage("Avant de commencer la soirée, n'hésite pas à lire les règles et à voir les effets des alcools disposés dans ton inventaire.\n");
		HangoverGames.instance.network.sendArenasInfos(false);
		return "good";
	
	}
	
	/*
	 * Affiche le message [message] à tous les joueurs de l'arène
	 */
	public void broadcastMessage(String message) {
		for (VirtualPlayer p : players) 
			p.getPlayer().sendMessage(message);
	}
	
	/*
	 * Envoie le son à tous les joueurs
	 */
	public void broadcastSound (Sound s) {
		for (VirtualPlayer p : players) 
			p.getPlayer().playSound(p.getPlayer().getLocation(), s, 1, 1);
	}	public void broadcastSound (Sound s, Location l) {
		for (VirtualPlayer p : players) 
			p.getPlayer().playSound(l, s, 2, 1);
	}
	
	public boolean canJoin() {
		return ((status.equals(Status.Available) || status.equals(Status.Starting)) && players.size() < maxPlayers);
	}
	
	public boolean isGameStarted() {
		return (status == Status.InGame);
	}

	public boolean isPlaying(VirtualPlayer player) {
		return players.contains(player);
	}
	
	public boolean isPlaying(UUID player) {
		return isPlaying(new VirtualPlayer(player));
	}
	
	public void refreshPlayers(boolean addPlayers) {
		if (isGameStarted())  {
			HangoverGames.instance.network.sendArenasInfos(false);
			return;
		}
		
		// Compte a rebours démarré mais plus assez de joueurs
		if (timer != null && players.size() < minPlayers)  {
			broadcastMessage(Messages.MANQUE_JOUEURS);
			timer.setTimeout(0);
			timer.end();
			timer = null;
			status = Status.Available;
			HangoverGames.instance.network.sendArenasInfos(false);
			return;
		}
		
		// Compte à rebours non démarré mais assez de joueurs
		if (timer == null && players.size() >= minPlayers) {
			timer = new BeginTimer(this);
			timer.start();
			status = Status.Starting;
		}
		
		HangoverGames.instance.network.sendArenasInfos(false);
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
		
		ArrayList<VirtualPlayer> remove = new ArrayList<VirtualPlayer>();
		
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		objective = scoreboard.registerNewObjective("points", "dummy");
		objective.setDisplayName(ChatColor.DARK_RED+"Taux d'alcool (g/L)");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		for (VirtualPlayer pl : players) {
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
		}
		
		// Détruit le timer
		if (timer != null) timer.end();
		timer = null;
		HangoverGames.instance.network.sendArenasInfos(false);
		
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
		HangoverGames.instance.network.sendArenasInfos(false);
		for (VirtualPlayer pl : this.players) {
			HangoverGames.instance.kickPlayer(pl.getPlayer());
		}
		
		if (this.noise != null) {
			noise.end();
			noise = null;
		}
		
		status = Status.Stopping;
		this.players.clear();
		this.scores.clear();
		this.effectLevel.clear();
		this.nocive = 0;
		this.scoreboard = null;
		this.objective = null;
		HangoverGames.instance.network.sendArenasInfos(false);
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
	
	public void win(final VirtualPlayer player) {
		// On fera des trucs ici
		status = Status.Stopping;
		HangoverGames.instance.network.sendArenasInfos(false);
		
		TreeMap<Integer, Player> top = new TreeMap(Collections.reverseOrder());
		
		for (UUID p : scores.keySet()) {
			top.put(scores.get(p), Bukkit.getPlayer(p));
		}
		
		int i = 0;
		String message = Messages.WIN.replace("{PSEUDO}", player.getPlayer().getName());
		
		
		broadcastMessage(ChatColor.GOLD+"===========================================");
		broadcastMessage(" ");
		broadcastMessage(message);
		broadcastMessage(" ");
		broadcastMessage(ChatColor.GOLD+"===========================================");
		
		broadcastMessage(" ");
		broadcastMessage(ChatColor.GOLD+">> Classement des joueurs :");
		for (Integer score : top.keySet()) {
			i++;
			if (i > 3) break;
			broadcastMessage(i+" : "+ChatColor.AQUA+top.get(score).getName()+ChatColor.GOLD+" avec "+score+" g/L de sang");
			if (i == 1)
				CoinsManager.creditJoueur(top.get(score), 50, true);
			else if (i == 2)
				CoinsManager.creditJoueur(top.get(score), 30, true);
			else if (i == 3)
				CoinsManager.creditJoueur(top.get(score), 10, true);
		}
		final int nb = (int) (10 * 1.5);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(HangoverGames.instance, new Runnable() {
            int compteur = 0;

            public void run() {

                if (compteur >= nb) {
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
	
	public void stumpPlayer(VirtualPlayer player) {
		this.players.remove(player);
		if (!this.isGameStarted()){
			this.refreshPlayers(false);
			return;
		}
		if (players.size() == 1)
			win(players.get(0));
		else if (players.size() < 1) {
			endGame();
			setupGame();
		}
		HangoverGames.instance.network.sendArenasInfos(false);
	}
	
	public void setupGame() {
		status = Status.Available;
		HangoverGames.instance.network.sendArenasInfos(false);
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
}
