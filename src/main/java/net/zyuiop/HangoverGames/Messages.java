package net.zyuiop.HangoverGames;

import org.bukkit.ChatColor;

public class Messages {
	public static final String MANQUE_PIECES = ChatColor.RED+"Vous n'avez pas assez de coins pour participer !";

	public static String PLUGIN_TAG = ChatColor.DARK_AQUA+"["+ChatColor.AQUA+"HangoverGames"+ChatColor.DARK_AQUA+"] "+ChatColor.RESET; 
	
	public static String MANQUE_JOUEURS = PLUGIN_TAG+ChatColor.RED+"Il n'y a plus assez de joueurs pour commencer.";
	
	public static String DEBUT_DANS = PLUGIN_TAG+ChatColor.YELLOW+"Début du jeu dans "+ChatColor.AQUA+"{TIME}";
	
	public static String DEJA_DANS_ARENE = ChatColor.RED+"Vous êtes dèjà dans l'arène. Ceci est une erreur, merci de nous la signaler.";
	
	public static String ARENE_PLEINE = ChatColor.RED+"L'arène est pleine.";
	
	public static String DEJA_EN_JEU = ChatColor.RED+"Vous êtes dèjà en jeu dans une autre arène.";
	
	public static String REJOINT_ARENE = PLUGIN_TAG+ChatColor.GREEN+"Vous avez rejoint l'arène.";
	
	public static String REJOINT_ARENE_BROADCAST = PLUGIN_TAG+ChatColor.YELLOW+"{PSEUDO}"+ChatColor.YELLOW+" a rejoint la partie ! "+ChatColor.DARK_GRAY+"["+ChatColor.RED+"{JOUEURS}"+ChatColor.DARK_GRAY+"/"+ChatColor.RED+"{JOUEURS_MAX}"+ChatColor.DARK_GRAY+"]";

	public static String MESSAGE_VICTOIRE = ChatColor.GREEN+"Vous avez gagné !";
	
	public static String DEBUT_PARTIE = ChatColor.GOLD+"La partie commence !";
	
	public static String POINTS_LOST = PLUGIN_TAG+ChatColor.AQUA+"{PSEUDO}"+ChatColor.YELLOW+" vomit et "+ChatColor.RED+"perd {NB} grammes"+ChatColor.YELLOW+" en buvant : {BOISSON}.";
	
	public static String POINTS_GAINED = PLUGIN_TAG+ChatColor.AQUA+"{PSEUDO}"+ChatColor.YELLOW+" "+ChatColor.GREEN+"a gagné {NB} grammes"+ChatColor.YELLOW+" en buvant : {BOISSON}.";
	
	public static String WIN = PLUGIN_TAG+ChatColor.AQUA+"{PSEUDO}"+ChatColor.GREEN+" a remporté la partie ! Bravo !";
	
	//public static 
	
	//public static String MESSAGE_BOISSON = PLUGIN_TAG+ChatColor.AQUA+"{JOUEUR}"+ChatColor.RED+" s'est déconnecté "+ChatColor.DARK_GRAY+"["+ChatColor.RED+"{JOUEURS}"+ChatColor.DARK_GRAY+"/"+ChatColor.RED+"{JOUEURS_MAX}"+ChatColor.DARK_GRAY+"]";
	
	//public static String MESSAGE_CHAUDRON = ...
}
