package ro.fmi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Node {
    public final int port;
    public int nodeId;
    public List<Element> view;
    public int clock = 0;

    public Map<Integer, Integer> t = new HashMap<>(); // key este id-ul altui nod x, value este valoarea clockului nodului x la momentul ultimei inserari cunoscute

    public Map<Element, Integer> del = new HashMap<>();

    public List<ClientHandler> clients = new ArrayList<>();

    private Gson json = new GsonBuilder().enableComplexMapKeySerialization().create();

    public void insert(int x) {
        t.put(nodeId, clock); // se creeaza cheia daca nu exista sau se actualizeaza valoarea
        Element newElement = new Element(nodeId, clock, x);
        view.add(newElement);

        clock++;

        send(new Message(view, t, del));
    }

    public void delete(int x) {
        Element e = view.stream()
                .filter((elem) -> elem.value == x)
                .findFirst()
                .orElse(null);

        if (e == null) {
            System.out.println("Nu exista elementul " + x + " in baza de date");
            return;
        }

        del.put(e, clock);

        clock++;

        send(new Message(view, t, del));
        view.remove(e);
    }

    public void list() {
        System.out.println("--------------------------");
        for (Element e : view) {
            System.out.printf("(%d, clock: %d, cre: %d)%n", e.value, e.T, e.cre);
        }
        System.out.println("--------------------------");
    }

    public void send(Message m) {
        String jsonString = json.toJson(m);
        System.out.println("Trimit la alte noduri: " + jsonString);
        for (ClientHandler client : clients) {
            /*try {
                Thread.sleep((long)(Math.random() * 10000));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
             */
            client.sendMessage(jsonString);
        }
    }

    public void receive(String message) {
        System.out.println("Am primit mesajul: " + message);
        Message m = json.fromJson(message, Message.class);
        for (Element e : m.view) {
            if (t.containsKey(e.cre)) {
                if (!del.containsKey(e) && !m.del.containsKey(e) && !view.contains(e) && t.get(e.cre) < m.t.get(e.cre)) {
                    view.add(e);
                    update_t(m);
                    send(m);
                } else if((del.containsKey(e) || m.del.containsKey(e)) && view.contains(e)) {
                    view.remove(e);
                    update_t(m);
                    send(m);
                }
            } else if (!del.containsKey(e) && !m.del.containsKey(e)){
                view.add(e);
                update_t(m);
                send(m);
            }
        }
    }

    private void update_t(Message m) {
        for (Integer nodeId : m.t.keySet()) {
            if (t.containsKey(nodeId)) {
                this.t.put(nodeId, Math.max(m.t.get(nodeId), t.get(nodeId)));
            } else {
                this.t.put(nodeId, m.t.get(nodeId));
            }
        }
    }

    public Node(int NodeId) {
        this.view = new ArrayList<>();
        this.nodeId = NodeId;
        this.port = 8080 + NodeId;
    }

    public static void main(String[] args) {
        Node node = new Node(Integer.parseInt(args[0]));

        Thread consoleReading = new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (true) {
                try {
                    if (sc.hasNextLine()) {
                        String line = sc.nextLine();
                        System.out.println(line);
                        String[] split = line.split(" ");
                        if (split[0].equals("i") || split[0].equals("insert")) {
                            node.insert(Integer.parseInt(split[1]));
                        } else if (split[0].equals("d") || split[0].equals("delete")) {
                            node.delete(Integer.parseInt(split[1]));
                        } else if (split[0].equals("list")) {
                            node.list();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Nu merge citirea vericule ai imbulinat-o: " + e.getMessage());
                }
            }
        });
        consoleReading.start();

        try {
            // ----------- nodul curent se conecteaza la nodurile cu care trebuie sa fie conectat ----------
            for (int i = 1; i < args.length; i++) {
                int port = 8080 + Integer.parseInt(args[i]);
                Socket clientSocket = new Socket("localhost", port);
                ClientHandler ch = new ClientHandler(clientSocket, node);
                node.clients.add(ch);
                ch.start();
            }

            // ----------- nodul curent asculta daca vor alte noduri sa se conecteze cu el -----------
            ServerSocket server = new ServerSocket(node.port);

            while (true) {
                // Wait for a client to connect
                Socket socket = server.accept();
                System.out.println("Accepted connection from " + socket.getInetAddress());

                // Start a new thread to handle this client
                ClientHandler client = new ClientHandler(socket, node);
                client.start();

                // Add the new client to the list
                node.clients.add(client);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}