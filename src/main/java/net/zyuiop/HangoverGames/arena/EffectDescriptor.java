package net.zyuiop.HangoverGames.arena;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EffectDescriptor {
	public PotionEffectType type = null;
	public int duration = 0;
	public int force = 0;
	
	public EffectDescriptor(PotionEffectType type, int duration, int force) {
		this.type = type;
		this.duration = duration;
		this.force = force;
	}
	
	public PotionEffect getEffect() {
		return new PotionEffect(type, duration, force);
	}
}
