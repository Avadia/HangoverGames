package fr.blueslime.hangover;

import net.samagames.api.SamaGamesAPI;
import org.bukkit.ChatColor;

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
public enum Messages {
    pointsGained(ChatColor.AQUA + "${PLAYER}" + ChatColor.YELLOW + " " + ChatColor.GREEN + "a gagné ${NUMBER} gramme(s)" + ChatColor.YELLOW + " en buvant : ${ALCOOL}.", true),
    pointsLost(ChatColor.AQUA + "${PLAYER}" + ChatColor.YELLOW + " vomit et " + ChatColor.RED + "perd ${NUMBER} gramme(s)" + ChatColor.YELLOW + " en buvant : ${ALCOOL}.", true),
    tooLateAlcool(ChatColor.AQUA + "${PLAYER}" + ChatColor.YELLOW + " ne peut plus se retenir et boit sa bouteille.", true),
    warningAlccol(ChatColor.GOLD + "Vous commencez à crever de soif...", false),
    mapEnd(ChatColor.GOLD + "He ! Ho ! Où tu vas ? Reviens boire avec nous !", true),
    alcoolWarning(ChatColor.RED + "" + ChatColor.BOLD + "Attention !" + ChatColor.RESET + ChatColor.RED + " L'abus d'alcool est dangereux pour la santé ! De plus, certains effets de lumière peuvent porter atteinte aux personnes épileptiques !", false),
    actionBarWarning(ChatColor.RED + "L'abus d'alcool est dangereux pour la santé ;)", false);

    private final String message;
    private final boolean tag;

    Messages(String message, boolean tag) {
        this.message = message;
        this.tag = tag;
    }

    @Override
    public String toString() {
        return (this.tag ? SamaGamesAPI.get().getGameManager().getCoherenceMachine().getGameTag() + " " : "") + this.message;
    }
}