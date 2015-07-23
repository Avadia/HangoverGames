package fr.blueslime.hangover.arena;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.samagames.tools.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class ArenaManager
{
	public Arena loadArena()
    {
        File worldFolder = Bukkit.getWorlds().get(0).getWorldFolder();
        File arenaFile = new File(worldFolder, "arena.json");

        try
        {
            JsonObject jsonArena = new JsonParser().parse(new FileReader(arenaFile)).getAsJsonObject();

            JsonArray jsonCauldrons = jsonArena.get("cauldrons").getAsJsonArray();
            ArrayList<Location> cauldrons = new ArrayList<>();

            for(int i = 0; i < jsonCauldrons.size(); i++)
                cauldrons.add(LocationUtils.str2loc(jsonCauldrons.get(i).getAsString()));

            return new Arena(LocationUtils.str2loc(jsonArena.get("spawn").getAsString()), cauldrons);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        return null;
	}
}
