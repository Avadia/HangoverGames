package fr.blueslime.hangover.arena;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.blueslime.hangover.HangoverGames;
import net.samagames.api.SamaGamesAPI;
import net.samagames.tools.LocationUtils;
import org.bukkit.Location;

import java.util.ArrayList;

public class ArenaManager
{
    private final HangoverGames plugin;

    public ArenaManager(HangoverGames plugin)
    {
        this.plugin = plugin;
    }

    public Arena loadArena()
    {
        JsonObject jsonArena = SamaGamesAPI.get().getGameManager().getGameProperties().getConfigs();

        JsonArray jsonCauldrons = jsonArena.get("cauldrons").getAsJsonArray();
        ArrayList<Location> cauldrons = new ArrayList<>();

        for(int i = 0; i < jsonCauldrons.size(); i++)
            cauldrons.add(LocationUtils.str2loc(jsonCauldrons.get(i).getAsString()));

        return new Arena(this.plugin, LocationUtils.str2loc(jsonArena.get("spawn").getAsString()), cauldrons);
    }
}
