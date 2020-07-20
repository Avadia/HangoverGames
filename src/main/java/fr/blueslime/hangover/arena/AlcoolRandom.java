package fr.blueslime.hangover.arena;

import org.bukkit.ChatColor;

import java.util.Random;

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
public class AlcoolRandom {
    public static Alcool getRandom() {
        Random random = new Random();
        int number = random.nextInt(100);
        int current = 0;

        for (Alcool alcool : Alcool.values()) {
            if (current + alcool.getChance() >= number) {
                return alcool;
            } else {
                current += alcool.getChance();
            }
        }

        return Alcool.BEER;
    }

    public static Alcool getAlcoolByName(String alcoolName) {
        String[] data = alcoolName.split(ChatColor.WHITE + " \\(");

        for (Alcool alcool : Alcool.values())
            if (alcool.getName().equals(data[0]))
                return alcool;

        return null;
    }
}
