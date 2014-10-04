package net.zyuiop.HangoverGames.arena;

import net.zyuiop.HangoverGames.HangoverGames;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpecialEffect extends PotionEffect {

	private int ecart;
	private int duration;
	private int occurences;
	
	public SpecialEffect(PotionEffectType type, int singleDuration, int amplifier, int ecart, int occurences) {
		super(type, singleDuration, amplifier);
		this.duration = singleDuration;
		this.occurences = occurences;
		this.ecart = ecart;
	}
	
	public boolean apply(final LivingEntity e) {
		final int max = occurences;
		final int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(HangoverGames.instance, new Runnable() {
			private int cur = 0;
			public void run() {
				if (cur > max) {
					return;
				}
				_apply(e);
				cur++;
			}
		}, 0L, (duration+ecart));
		return true;
	}
	
	public void _apply(LivingEntity e) {
		super.apply(e);
	}

}
