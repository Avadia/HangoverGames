package fr.blueslime.hangover.arena;

import fr.blueslime.hangover.HangoverGames;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/*
 * This file is part of HangoverGames.
 *
 * HangoverGames is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HangoverGames is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HangoverGames.  If not, see <http://www.gnu.org/licenses/>.
 */
public class SpecialEffect extends PotionEffect {
    private final int spacing;
    private final int duration;
    private final int occurences;

    public SpecialEffect(PotionEffectType type, int singleDuration, int amplifier, int spacing, int occurences) {
        super(type, singleDuration, amplifier);

        this.duration = singleDuration;
        this.spacing = spacing;
        this.occurences = occurences;
    }

    @Override
    public boolean apply(final LivingEntity entity) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(HangoverGames.getInstance(), new Runnable() {
            private int current = 0;

            public void run() {
                if (this.current > occurences)
                    return;

                _apply(entity);
                current++;
            }
        }, 0L, (this.duration + this.spacing));

        return true;
    }

    public void _apply(LivingEntity entity) {
        super.apply(entity);
    }
}
