package net.zyuiop.HangoverGames.Network;

import net.zyuiop.HangoverGames.HangoverGames;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketListener implements Runnable {
    private HangoverGames plugin;

    private ServerSocket socketserver;
    private Socket socket;
    private BufferedReader in = null;
    private PrintWriter out = null;

    private String Auth;
    private String buff;

    private NetworkManager networkManager;

    private boolean run = true;

    public SocketListener(HangoverGames plugin, ServerSocket sock, NetworkManager networkManager) {
        this.plugin = plugin;
        socketserver = sock;
        this.networkManager = networkManager;
    }

    public void run() {
        try {
            while (run) {
                socket = socketserver.accept(); // Un client se connecte on l'accepte

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream());

                Auth = in.readLine();
                if (!Auth.equalsIgnoreCase("56465894869dsfg")) {
                    socket.close();
                    continue;
                }

                buff = in.readLine();

                out.println(new Gson().toJson(networkManager.InputMessage(buff)));
                out.flush();


                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        run = false;
    }


}
