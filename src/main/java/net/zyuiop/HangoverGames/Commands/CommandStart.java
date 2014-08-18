package net.zyuiop.HangoverGames.Commands;

import net.zyuiop.HangoverGames.HangoverGames;
import net.zyuiop.HangoverGames.Arena.Arena;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandStart implements CommandExecutor {

	public boolean onCommand(CommandSender arg0, Command arg1, String arg2,
			String[] arg3) {
		Arena ar = HangoverGames.instance.arenasManager.getPlayerArena(((Player)arg0).getUniqueId());
		ar.start();
		return true;
	}

}
