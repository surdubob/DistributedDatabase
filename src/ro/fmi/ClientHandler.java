package ro.fmi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final Node node;

    private PrintWriter writer;
    private BufferedReader reader;

    public ClientHandler(Socket socket, Node node) {
        this.socket = socket;
        this.node = node;
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Read a message from the client
                String message = reader.readLine();
                if (message == null) {
                    break;
                }
                System.out.println("Am primit mesajul: " + message);
                node.receive(message);
            }
        } catch (Exception e) {
            // Ignore any errors, the client might have disconnected
            System.err.println(e);
        } finally {

        }
    }
    public void sendMessage(String message) {
        try {
            writer.println(message);
        } catch (Exception e) {
            System.err.println(e);
            // Ignore any errors, the client might have disconnected
        }
    }
}
