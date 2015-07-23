package fr.blueslime.hangover;

import fr.blueslime.hangover.arena.Arena;
import fr.blueslime.hangover.arena.ArenaManager;
import fr.blueslime.hangover.listeners.PlayerListener;
import net.samagames.api.SamaGamesAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class HangoverGames extends JavaPlugin
{
	private static HangoverGames instance;
    private Arena arena;

    @Override
	public void onEnable()
    {
		instance = this;

        this.arena = new ArenaManager().loadArena();

		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        SamaGamesAPI.get().getGameManager().registerGame(this.arena);
    }

    public Arena getArena()
    {
        return this.arena;
    }

    public static HangoverGames getInstance()
    {
        return instance;
    }
}
