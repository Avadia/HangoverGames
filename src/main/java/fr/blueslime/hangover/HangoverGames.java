package fr.blueslime.hangover;

import fr.blueslime.hangover.arena.Arena;
import fr.blueslime.hangover.arena.ArenaManager;
import fr.blueslime.hangover.listeners.PlayerListener;
import net.samagames.api.SamaGamesAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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
        SamaGamesAPI.get().getGameManager().setKeepPlayerCache(true);
    }

    public static HangoverGames getInstance()
    {
        return instance;
    }
}
