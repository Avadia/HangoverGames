package fr.blueslime.hangover.arena;

import org.bukkit.ChatColor;

import java.util.Random;

public class AlcoolRandom
{
	public static Alcool getRandom()
    {
		Random random = new Random();
		int number = random.nextInt(100);
		int current = 0;

		for (Alcool alcool : Alcool.values())
        {
			if (current + alcool.getChance() >= number)
            {
				return alcool;
			}
            else
            {
				current += alcool.getChance();
			}
		}

		return Alcool.BEER;
	}
	
	public static Alcool getAlcoolByName(String alcoolName)
    {
		String[] data = alcoolName.split(ChatColor.WHITE + " \\(");

		for (Alcool alcool : Alcool.values())
			if (alcool.getName().equals(data[0]))
                return alcool;

		return null;
	}
}
