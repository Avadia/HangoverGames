package net.zyuiop.HangoverGames.Tasks;

import java.util.ArrayList;

import org.bukkit.scheduler.BukkitRunnable;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.zyuiop.HangoverGames.HangoverGames;
import net.zyuiop.HangoverGames.Messages;
import net.zyuiop.HangoverGames.Arena.Arena;
import net.zyuiop.HangoverGames.Arena.VirtualPlayer;

public class DrinkTimer extends BukkitRunnable {

	private Arena parent;
	private Player player;
	private Integer time = 20;
	
	public DrinkTimer(Arena ar, Player pl) {
		this.parent = ar;
		this.player = pl;
	}
	
	public void run() {		
		if (time == 0) {
			parent.forceDrink(player);
			this.cancel();
			return;
		}
		
		if (time == 10) {
			player.sendMessage(Messages.ATTENTION_RETENUE);
		}
		
		player.setLevel(time);
		
		if (time <= 5) {
			player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
		}
		
		time --;
		
	}

}
