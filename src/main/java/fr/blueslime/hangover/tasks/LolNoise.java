package fr.blueslime.hangover.tasks;

import fr.blueslime.hangover.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

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
public class LolNoise extends Thread {
    private final Arena parent;
    private Sound effect = null;
    private int left = 0;

    public LolNoise(Arena parent) {
        this.parent = parent;
        this.randomLeft();
    }

    public void randomLeft() {
        Random random = new Random();
        this.left = random.nextInt(30);

        Random eff = new Random();
        ArrayList<Sound> effect = new ArrayList<>();
        effect.add(Sound.ENTITY_HORSE_ANGRY);
        effect.add(Sound.ENTITY_ZOMBIE_AMBIENT);
        effect.add(Sound.ENTITY_ZOMBIE_STEP);
        effect.add(Sound.ENTITY_CREEPER_HURT);
        effect.add(Sound.ENTITY_SKELETON_AMBIENT);
        effect.add(Sound.ENTITY_GHAST_SCREAM);

        this.effect = effect.get(eff.nextInt(effect.size()));
    }

    @SuppressWarnings("BusyWait")
    public void run() {
        while (true) {
            try {
                sleep(1000);
                this.left--;

                if (!this.parent.isGameStarted())
                    return;

                if (this.left == 0) {
                    this.randomLeft();

                    for (UUID uuid : this.parent.getEffectLevel().keySet()) {
                        Player player = Bukkit.getPlayer(uuid);

                        if (player == null)
                            continue;

                        if (this.parent.getEffectLevel().get(uuid) != null && this.parent.getEffectLevel().get(uuid) > 0)
                            player.playSound(player.getLocation(), this.effect, 1, 1);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
