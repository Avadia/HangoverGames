package fr.blueslime.hangover.tasks;

import fr.blueslime.hangover.arena.Arena;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DrinkTimer extends BukkitRunnable
{
	private Arena parent;
	private Player player;
	private Integer time = 20;
	
	public DrinkTimer(Arena arena, Player player)
    {
		this.parent = arena;
		this.player = player;
	}

    @Override
	public void run()
    {
		if (this.time == 0)
        {
			this.parent.forceDrink(this.player);
			this.cancel();
			return;
		}
		
		if (this.time == 10)
            this.player.sendMessage(ChatColor.GOLD + "Vous commencez à crever de soif...");

        this.player.setLevel(this.time);
		
		if (this.time <= 5)
            this.player.playSound(this.player.getLocation(), Sound.NOTE_PLING, 1, 1);

        this.time--;
	}
}
