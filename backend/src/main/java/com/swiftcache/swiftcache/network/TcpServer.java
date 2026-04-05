package com.swiftcache.swiftcache.network;
import com.swiftcache.swiftcache.core.MemoryEngine;
import com.swiftcache.swiftcache.core.durability.PersistenceManager;
import com.swiftcache.swiftcache.datastructures.ReasoningGraph;
import com.swiftcache.swiftcache.enums.TaskStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TcpServer {

    @Value("${agentmemory.security.password}")
    private String serverPassword;

    private final MemoryEngine engine;
    private final PersistenceManager persistenceManager;
    private static final int PORT = 7777;

    // Creates a pool of threads based on how many CPU cores your computer has
    private final ExecutorService workerPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public TcpServer(MemoryEngine engine, PersistenceManager persistenceManager) {
        this.engine = engine;
        this.persistenceManager = persistenceManager;
    }

    @PostConstruct
    public void startServer() {
        System.out.println("💾 Recovering Agent Memory from Disk...");
        List<String> pastCommands = persistenceManager.readAllCommands();
        for (String command : pastCommands) {
            // Replay without a session (null) and mark as recovery (true)
            processCommand(command, null, true);
        }
        System.out.println("✅ Recovered " + pastCommands.size() + " commands.");

        new Thread(() -> {
            try {
                Selector selector = Selector.open();
                ServerSocketChannel serverSocket = ServerSocketChannel.open();
                serverSocket.bind(new InetSocketAddress(PORT));
                serverSocket.configureBlocking(false);
                serverSocket.register(selector, SelectionKey.OP_ACCEPT);

                System.out.println("🚀 AgentMemory TCP Engine started on port " + PORT + " (Secured)");

                while (true) {
                    selector.select();
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();

                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();

                        if (key.isAcceptable()) {
                            acceptClient(serverSocket, selector);
                        } else if (key.isReadable()) {
                            readCommand(key);
                        }
                        iter.remove();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void acceptClient(ServerSocketChannel serverSocket, Selector selector) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        ClientSession session = new ClientSession(client);
        client.register(selector, SelectionKey.OP_READ, session);

        System.out.println("🤖 New Agent Connected: " + client.getRemoteAddress());
        client.write(ByteBuffer.wrap("CONNECTED TO AGENT_MEMORY. Please AUTH.\n".getBytes()));
    }

    private void readCommand(SelectionKey key) throws IOException {
        ClientSession session = (ClientSession) key.attachment();
        SocketChannel client = session.channel;

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = client.read(buffer);

        if (bytesRead == -1) {
            client.close();
            return;
        }

        buffer.flip();
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        session.buffer.append(new String(bytes));

        int newlineIndex;
        while ((newlineIndex = session.buffer.indexOf("\n")) != -1) {
            String fullCommand = session.buffer.substring(0, newlineIndex).trim();
            session.buffer.delete(0, newlineIndex + 1);

            if (!fullCommand.isEmpty()) {
                // --- NEW: Offload the execution to the background thread pool ---
                workerPool.submit(() -> {
                    String response = processCommand(fullCommand, session, false);
                    try {
                        // Write the response back to the client from the worker thread
                        client.write(ByteBuffer.wrap((response + "\n").getBytes()));
                    } catch (IOException e) {
                        System.err.println("Failed to send response: " + e.getMessage());
                    }
                });
            }
        }
    }

    // --- UPDATED: Now requires the ClientSession to verify security ---
    private String processCommand(String input, ClientSession session, boolean isRecovery) {
        String[] parts = input.split(" ", 3);
        String command = parts[0].toUpperCase();

        try {
            // 1. Handle Authentication Command
            if (command.equals("AUTH")) {
                if (parts.length < 2) return "ERROR: Missing password.";
                if (parts[1].equals(serverPassword)) {
                    if (session != null) session.isAuthenticated = true;
                    return "OK: Authenticated.";
                } else {
                    return "ERROR: Invalid password.";
                }
            }

            // 2. Security Check! If not recovering from disk, and not authenticated, block them.
            if (!isRecovery && session != null && !session.isAuthenticated && !command.equals("PING")) {
                return "ERROR: Unauthenticated connection. Please send AUTH <password>.";
            }

            // 3. Execute normal commands
            switch (command) {
                case "PING":
                    return "PONG";

                case "PUSH":
                    if (parts.length < 3) return "ERROR: PUSH requires a key and a message.";
                    engine.pushToContext(parts[1], parts[2], 50, true);
                    if (!isRecovery) persistenceManager.appendCommand(input);
                    return "OK";

                case "GET_CONTEXT":
                    if (parts.length < 2) return "ERROR: Missing key.";
                    var context = engine.getContext(parts[1]);
                    return context == null ? "EMPTY" : String.join(" | ", context);

                case "GRAPH_ADD":
                    String[] payload = parts[2].split(" ", 2);
                    engine.addGraphTask(parts[1], payload[0], payload[1]);
                    if (!isRecovery) persistenceManager.appendCommand(input);
                    return "OK";

                case "GRAPH_UPDATE":
                    String[] updatePayload = parts[2].split(" ");
                    engine.updateGraphTask(parts[1], updatePayload[0], TaskStatus.valueOf(updatePayload[1].toUpperCase()));
                    if (!isRecovery) persistenceManager.appendCommand(input);
                    return "OK";

                case "GRAPH_GET":
                    if (parts.length < 2) return "ERROR: Missing key.";
                    Object graphObj = engine.get(parts[1]);
                    if (graphObj instanceof ReasoningGraph) {
                        return ((ReasoningGraph) graphObj).getGraphState().toString();
                    }
                    return "EMPTY or NOT A GRAPH";

                default:
                    return "ERROR: Unknown command.";
            }
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}