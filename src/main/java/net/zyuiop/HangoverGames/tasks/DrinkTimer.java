package net.zyuiop.HangoverGames.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import net.zyuiop.HangoverGames.Messages;
import net.zyuiop.HangoverGames.arena.Arena;
import net.zyuiop.HangoverGames.arena.VirtualPlayer;

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
