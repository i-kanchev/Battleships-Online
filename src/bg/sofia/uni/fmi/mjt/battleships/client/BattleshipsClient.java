package bg.sofia.uni.fmi.mjt.battleships.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class BattleshipsClient {
    private final DataOutputStream dos;
    private final DataInputStream dis;
    private boolean shouldRun;
    private static final String HOST = "localhost";
    private static final int SERVER_PORT = 4747;
    private static final Scanner INPUT = new Scanner(System.in);

    public BattleshipsClient(Socket socket) throws IOException {
        this.dos = new DataOutputStream(socket.getOutputStream());
        this.dis = new DataInputStream(socket.getInputStream());
        this.shouldRun = true;
    }

    public static void main(String[] args) throws IOException {
        try {
            Socket socket = new Socket(HOST, SERVER_PORT);
            BattleshipsClient client = new BattleshipsClient(socket);
            System.out.println("Enter nickname:");

            new Thread(() -> {
                while (client.shouldRun) {
                    try {
                        String response = client.dis.readUTF();
                        System.out.println(response);
                        if (response.equals("You have exited the game")) {
                            client.shouldRun = false;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

            while (client.shouldRun) {
                String command = INPUT.nextLine();
                client.dos.writeUTF(command);
            }
            client.dos.close();
            client.dis.close();
            socket.close();
        } catch (Exception e) {
            System.out.println("Server offline");
        }

    }

}