package net.zyuiop.HangoverGames.Network;

import net.zyuiop.HangoverGames.Arena.Arena;
import net.zyuiop.HangoverGames.HangoverGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NetworkManager {

    private HangoverGames plugin;
    private ServerSocket sock;
    private SocketListener listener;
    private Thread listenThread;
    private BukkitTask sendThread;
    private int comport;
    private String bungeeName;
    private String server;

    public NetworkManager(HangoverGames plugin, int comPort, String bungeeName) {
        this.plugin = plugin;
        this.comport = comPort;
        this.bungeeName = bungeeName;
        this.server = new Gson().toJson(new JsonServer(comport, bungeeName, Bukkit.getIp(), "hangovergames"));
    }

    public void initListener() {
        try {
            Bukkit.getLogger().info("Starting listener on port 0.0.0.0:" + comport + "...");
            sock = new ServerSocket(comport);
            listener = new SocketListener(plugin, sock, this);
            listenThread = new Thread(listener);
            listenThread.start();
            Bukkit.getLogger().info("Listener successfully. Listening on 0.0.0.0:" + comport);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initInfosSender() {
        sendThread = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            public void run() {
                sendArenas();
            }
        }, 10L, 120 * 20L);
    }

    public void sendArenas() {
        sendArenaInfos(plugin.arenasManager.getArenas().values());
    }

    public JoinResponse InputMessage(String msg) {
        String data[] = msg.split("::");
        System.out.println(msg);

        JoinResponse ret = new JoinResponse();
        ret.errorMessage = null;

        if (data.length < 1) {
            ret.accepted = false;
            ret.errorMessage = ChatColor.RED+"Une erreur s'est produite durant la communication avec le serveur de jeu.";
            return ret;
        }

        try {
            if (data[0].equalsIgnoreCase("Join")) {

                if (data.length < 2) {
                    ret.accepted = false;
                    ret.errorMessage = ChatColor.RED+"Une erreur s'est produite durant la communication avec le serveur de jeu.";
                    return ret;
                }

                JoinMessage message = new Gson().fromJson(data[1], JoinMessage.class);
                if (message.reason.equals("game")) {
                    Arena arena = plugin.arenasManager.getArena(message.getTargetArena());

                    if (arena == null) {
                        ret.accepted = false;
                        ret.errorMessage = ChatColor.RED+"Le serveur de jeu n'est pas prêt a accueillir des joueurs.";
                        return ret;
                    }

                    String resp = plugin.arenasManager.prepareJoin(message.getPlayerId(), message.getTargetArena());
                    if (resp.equals("OK")) {
                        ret.accepted = true;
                    } else {
                        ret.accepted = false;
                        ret.errorMessage = resp;
                    }

                    return ret;
                } else {
                    ret.accepted = false;
                    ret.errorMessage = ChatColor.RED+"Mode de connexion refusé par le serveur {ERR : Mod join not implemented}";
                    return ret;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        ret.accepted = false;
        ret.errorMessage = ChatColor.RED+"Une erreur s'est produite durant la communication avec le serveur de jeu.";
        return ret;
    }

    public void disable() {
        sendThread.cancel();
        for (Arena ar : plugin.arenasManager.getArenas().values())
            ar.status = Status.Stopping;
        sendArenas();
        try {
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        listener.stop();
    }

    public void sendArenaInfos(final Collection<Arena> send) {
        List<String> serversLobby = plugin.getConfig().getStringList("Lobbys");

        for (String serv : serversLobby) {
            final String[] s = serv.split(":");
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                public void run() {
                    sendArenasInfosToServ(s[0], Integer.valueOf(s[1]), send);
                }
            });
        }
    }

    public void refreshArena(final Arena send) {
        ArrayList<Arena> snd = new ArrayList<Arena>();
        snd.add(send);
        sendArenaInfos(snd);
    }

    public void sendArenasInfosToServ(String ip, int port, Collection<Arena> sendList) {
        try {
            InetAddress MainServer = InetAddress.getByName(ip);
            Socket sock = new Socket(MainServer, port);
            PrintWriter out = new PrintWriter(sock.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            out.println("56465894869dsfg"); // Authentification de lol.
            out.flush();

            out.println("hangovergames"); // C'est le chanel
            out.flush();

            String b = in.readLine();

            if (!b.equals("good")) {
                sock.close();
                return;
            }

            out.println("Infos" + "::" + server);
            out.flush();

            for (Arena send : sendList) {
                Gson gson = new Gson();
                JsonArena arena = new JsonArena(
                        send.arenaId,
                        send.mapName,
                        send.maxPlayers,
                        2,
                        send.status.getString(),
                        send.players.size(),
                        false
                );

                out.println("Arena" + "::" + gson.toJson(arena));
                out.flush();
            }

            out.println("EndOfLine");

            sock.close();
        } catch (UnknownHostException e) {
            Bukkit.getLogger().severe("UnknownHostException fired...");
            e.printStackTrace();
        } catch (IOException e) {
            Bukkit.getLogger().severe("IOException fired...");
            e.printStackTrace();
        }
    }
}
