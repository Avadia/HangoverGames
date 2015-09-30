package fr.blueslime.hangover;

import net.samagames.api.SamaGamesAPI;
import org.bukkit.ChatColor;

public enum Messages
{
    pointsGained(ChatColor.AQUA + "${PLAYER}" + ChatColor.YELLOW + " " + ChatColor.GREEN + "a gagné ${NUMBER} gramme(s)" + ChatColor.YELLOW + " en buvant : ${ALCOOL}.", true),
	pointsLost(ChatColor.AQUA + "${PLAYER}" + ChatColor.YELLOW + " vomit et " + ChatColor.RED + "perd ${NUMBER} gramme(s)" + ChatColor.YELLOW + " en buvant : ${ALCOOL}.", true),
	tooLateAlcool(ChatColor.AQUA + "${PLAYER}" + ChatColor.YELLOW + " ne peut plus se retenir et boit sa bouteille.", true),
	warningAlccol(ChatColor.GOLD + "Vous commencez à crever de soif...", false),
	mapEnd(ChatColor.GOLD + "He ! Ho ! Où tu vas ? Reviens boire avec nous !", true),
    alcoolWarning(ChatColor.RED + "" + ChatColor.BOLD + "Attention !" + ChatColor.RESET + ChatColor.RED + " L'abus d'alcool est dangereux pour la santé ! De plus, certains effets de lumière peuvent porter atteinte aux personnes épileptiques !", false),
    actionBarWarning(ChatColor.RED + "L'abus d'alcool est dangereux pour la santé ;)", false);

    private String message;
    private boolean tag;

    Messages(String message, boolean tag)
    {
        this.message = message;
        this.tag = tag;
    }

    @Override
    public String toString()
    {
        return (this.tag ? SamaGamesAPI.get().getGameManager().getCoherenceMachine().getNameShortcut() + " " : "") + this.message;
    }
}
