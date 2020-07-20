package fr.blueslime.hangover.tasks;

import fr.blueslime.hangover.Messages;
import fr.blueslime.hangover.arena.Arena;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
public class DrinkTimer extends BukkitRunnable {
    private final Arena parent;
    private final Player player;
    private Integer time = 20;

    public DrinkTimer(Arena arena, Player player) {
        this.parent = arena;
        this.player = player;
    }

    @Override
    public void run() {
        if (this.time == 0) {
            this.parent.forceDrink(this.player);
            this.cancel();
            return;
        }

        if (this.time == 10)
            this.player.sendMessage(Messages.warningAlccol.toString());

        this.player.setLevel(this.time);

        if (this.time <= 5)
            this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);

        this.time--;
    }
}
