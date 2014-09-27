package net.zyuiop.HangoverGames.Network;

/**
 * Created by zyuiop on 26/09/14.
 */
public class JsonServer {
    private int comport;
    private String bungeename;
    private String serverIp;
    private String gameType;

    public JsonServer(int comport, String bungeename, String serverIp, String gameType) {
        this.comport = comport;
        this.bungeename = bungeename;
        this.serverIp = serverIp;
        this.gameType = gameType;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public JsonServer() {
    }

    public int getComport() {
        return comport;
    }

    public void setComport(int comport) {
        this.comport = comport;
    }

    public String getBungeename() {
        return bungeename;
    }

    public void setBungeename(String bungeename) {
        this.bungeename = bungeename;
    }
}
