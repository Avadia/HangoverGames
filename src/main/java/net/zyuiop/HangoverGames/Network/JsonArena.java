package net.zyuiop.HangoverGames.Network;

import java.util.UUID;

/**
 * Created by zyuiop on 26/09/14.
 */
public class JsonArena {

    private UUID arenaId;
    private String map;
    private int maxPlayers;
    private int vipPlayers;
    private String status;
    private int players;
    public boolean famous;

    public JsonArena() {
    }

    public JsonArena(UUID arenaId, String map, int maxPlayers, int vipPlayers, String status, int players, boolean famous) {
        this.arenaId = arenaId;
        this.map = map;
        this.maxPlayers = maxPlayers;
        this.vipPlayers = vipPlayers;
        this.status = status;
        this.players = players;
        this.famous = famous;
    }

    public boolean isFamous() {
        return famous;
    }

    public void setFamous(boolean famous) {
        this.famous = famous;
    }

    public UUID getArenaId() {
        return arenaId;
    }

    public void setArenaId(UUID arenaId) {
        this.arenaId = arenaId;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getVipPlayers() {
        return vipPlayers;
    }

    public void setVipPlayers(int vipPlayers) {
        this.vipPlayers = vipPlayers;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPlayers() {
        return players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }
}
