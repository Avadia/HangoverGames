package fr.blueslime.hangover.arena;

import fr.blueslime.hangover.HangoverGames;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpecialEffect extends PotionEffect
{
	private int spacing;
	private int duration;
	private int occurences;
	
	public SpecialEffect(PotionEffectType type, int singleDuration, int amplifier, int spacing, int occurences)
    {
		super(type, singleDuration, amplifier);

		this.duration = singleDuration;
        this.spacing = spacing;
		this.occurences = occurences;
	}

    @Override
	public boolean apply(final LivingEntity entity)
    {
		final int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(HangoverGames.getInstance(), new Runnable()
        {
			private int cur = 0;

			public void run()
            {
				if (this.cur > occurences)
					return;

				_apply(entity);
				cur++;
			}
		}, 0L, (this.duration + this.spacing));

		return true;
	}
	
	public void _apply(LivingEntity entity)
    {
		super.apply(entity);
	}
}
