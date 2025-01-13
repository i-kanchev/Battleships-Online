package bg.sofia.uni.fmi.mjt.battleships.server;

import java.io.IOException;
import java.net.ServerSocket;

public class BattleshipsServer {
    private static final int SERVER_PORT = 4747;

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {

            System.out.println("Server online");
            while (true) {

                Player player = new Player(serverSocket.accept());
                new Thread(player).start();

            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Server did not start");
        }

    }

}