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

    @Override
    public void onEnable()
    {
        instance = this;

        Arena arena = new ArenaManager(this).loadArena();
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this, arena), this);
        SamaGamesAPI.get().getGameManager().registerGame(arena);
    }

    public static HangoverGames getInstance()
    {
        return instance;
    }
}
