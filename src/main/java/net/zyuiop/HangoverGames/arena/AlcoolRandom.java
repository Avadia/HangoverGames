package net.zyuiop.HangoverGames.arena;

import java.util.Random;

import org.bukkit.ChatColor;

public class AlcoolRandom {
	public static Alcool getRandom() {
		Random rnd = new Random();
		Integer nb = rnd.nextInt(100);
		Integer current = 0;
		for (Alcool a : Alcool.values()) {
			if (current + a.getChance() >= nb) {
				return a;
			} else {
				current += a.getChance();
			}
		}
		return Alcool.Bierre;
	}
	
	public static Alcool getByItemName(String itemName) {
		String[] data = itemName.split(ChatColor.WHITE+" \\(");
		for (Alcool a : Alcool.values()) {
			if (a.getNom().equals(data[0])) return a;
		} 
		return null;
	}
}
