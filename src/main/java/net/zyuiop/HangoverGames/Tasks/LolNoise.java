package net.zyuiop.HangoverGames.Tasks;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import net.zyuiop.HangoverGames.HangoverGames;
import net.zyuiop.HangoverGames.Messages;
import net.zyuiop.HangoverGames.Arena.Arena;
import net.zyuiop.HangoverGames.Arena.VirtualPlayer;

public class LolNoise extends Thread {

	private Arena parent;
	private boolean cont = true;
	private int left = 0;
	private Sound effect = null;
	
	public LolNoise(Arena parent) {
		this.parent = parent;
		randomLeft();
	}
	
	public void end() {
		cont = false;
	}

	public void randomLeft() {
		Random r = new Random();
		left = r.nextInt(30);
		
		Random eff = new Random();
		ArrayList<Sound> effect = new ArrayList<Sound>();
		effect.add(Sound.HORSE_ANGRY);
		effect.add(Sound.ZOMBIE_IDLE);
		effect.add(Sound.ZOMBIE_WALK);
		effect.add(Sound.CREEPER_HISS);
		effect.add(Sound.SKELETON_IDLE);
		effect.add(Sound.GHAST_SCREAM);
		
		this.effect = effect.get(eff.nextInt(effect.size()));
	}
	
	public void run() {
		// TODO Auto-generated method stub
		while (cont) {
	    	try {
	    		
				sleep(1000);
				left--;
				if (parent.isGameStarted() == false)
					return;
				
				if (left == 0) {
					randomLeft();
					for (UUID id : parent.effectLevel.keySet()) {
						Player p = Bukkit.getPlayer(id);
						if (p == null) continue;
						if (parent.effectLevel.get(id) != null && parent.effectLevel.get(id) > 0) {
							p.playSound(p.getLocation(), effect, 1, 1);
						}
					}
				}
				
					
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
}
