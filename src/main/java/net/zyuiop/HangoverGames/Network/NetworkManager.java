package net.zyuiop.HangoverGames.Network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import net.zyuiop.HangoverGames.HangoverGames;
import net.zyuiop.HangoverGames.Arena.Arena;

public class NetworkManager {
	
	private HangoverGames plugin;
	private ServerSocket sock;
	private SocketListener listener;
	private Thread listenThread;
	private BukkitTask sendThread;
	
	public NetworkManager(HangoverGames plugin) {
		this.plugin = plugin;
	}
	
	public void initListener() {
	    try {
	    	Bukkit.getLogger().info("Started listener.");
	        sock = new ServerSocket(plugin.comPort);
	        listener = new SocketListener(plugin, sock);
	        listenThread = new Thread(listener);
	        listenThread.start();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	public void initInfosSender() {
	    sendThread = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
	            public void run() {
	                sendArenasInfos(true);
	            }
	        }, 10L, 120*20L);
	}

	public String InputMessage(String msg) {
	    String data[] = msg.split(":");
	    System.out.println(msg);
	        if (data.length < 1)
	            return "bad";
	        try {
	            if (data[0].equalsIgnoreCase("Join")) {
	                /*
					 * 0: Type de packet
					 * 1: Nom du joueur
					 * 2: UUID
					 * 3: Arène
					 */
	                if (data.length < 4)
	                    return "bad";

	                String name = data[1];
	                UUID uuid = UUID.fromString(data[2]);

	                String ar = data[3];
	                Arena arena = plugin.arenasManager.getArena(ar);

	                if (arena == null)
	                    return "no arena";

	                return plugin.arenasManager.prepareJoin(uuid, arena.arenaName);
	            }


	        } catch (ArrayIndexOutOfBoundsException e) {
	            plugin.getLogger().log(Level.SEVERE, "Data lengh :" + data.length);
	            plugin.getLogger().log(Level.SEVERE, "msg :" + msg);
	        }
	        return "";
	    }

	public void disable() {

	   new Thread(new Runnable() {
	       public void run() {
	           sendArenasInfos(true);
	       }
	   }).run();

	   sendThread.cancel();

	   try {
	       sock.close();
	   } catch (IOException e) {
	       e.printStackTrace();
	   }
	   listener.stop();
	}

	public void sendArenasInfos(final boolean first) {
	    List<String> serversLobby = plugin.getConfig().getStringList("Lobbys");

	    for (String serv : serversLobby) {
	        final String[] s = serv.split(":");
	        Bukkit.getScheduler().runTaskAsynchronously(HangoverGames.instance, new Runnable() {
	        	public void run() {
	        		sendArenasInfosToServ(s[0], Integer.valueOf(s[1]), first);
	        	}
	        });
	    }
	}

	public void sendArenasInfosToServ(String ip, int port, boolean first) {
	    first = true;
	    try {
	        InetAddress MainServer = InetAddress.getByName(ip);
	        Socket sock = new Socket(MainServer, port);
	        PrintWriter out = new PrintWriter(sock.getOutputStream());
	        BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

	        out.println("56465894869dsfg"); // Authentification de lol.
	        out.flush();

	        out.println("hangovergames");
	        out.flush();

	        String b = in.readLine();

	        if (!b.equals("good")) {
	            sock.close();
	            return;
	        }

	        if (first) {
	                out.println("Infos" + ":" + plugin.BungeeName + ":" + Bukkit.getIp() + ":" + plugin.comPort + ":");
	                out.flush();
	        }/*else {
					//0 : Type ID du serv
					//1 : nom bungee du serveur
					
					out.println("ID" + ":" + plugin.BungeeName);
					out.flush();
			}*/

	        for (Arena arena : plugin.arenasManager.getArenas().values()) {
	                //0 : Type
	                //1 : Arena name
	                //2 : Nb players
	                //3 : Max player
	                //4 : Map Name
	                //5 : ETAT
	                //6 : CountDown Time

	                out.println("Arena"
	                        + ":" + arena.arenaId.toString()
	                        + ":" + arena.arenaName
	                        + ":" + arena.players.size()
	                        + ":" + arena.maxPlayers 
	                        + ":" + arena.mapName
	                        + ":" + arena.status.getString()
	                        + ":" + arena.getCount()
	                        + ":");
	                out.flush();
	            }

	            out.println("-thisisthend-");
	            out.flush();

	            sock.close();
	            
	            //Bukkit.getLogger().log(Level.INFO, "Socket : sent data to " + ip + ":" + port); -- S'affiche correctement

	        } catch (UnknownHostException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            System.out.print("Erreur envoi au serveur: " + ip + ":" + port);
	            //e.printStackTrace();
	        }
	    }
}
