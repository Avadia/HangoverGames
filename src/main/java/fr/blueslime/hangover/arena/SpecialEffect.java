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
        Bukkit.getScheduler().scheduleSyncRepeatingTask(HangoverGames.getInstance(), new Runnable()
        {
            private int current = 0;

            public void run()
            {
                if (this.current > occurences)
                    return;

                _apply(entity);
				current++;
            }
        }, 0L, (this.duration + this.spacing));

        return true;
    }

    public void _apply(LivingEntity entity)
    {
        super.apply(entity);
    }
}
