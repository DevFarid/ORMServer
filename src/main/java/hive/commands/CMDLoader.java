package hive.commands;

import hive.HiveClient;
import hive.Server;
import hive.console.Console;
import hive.packets.MSGPacket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public enum CMDLoader {

    CLIENT,
    SERVER;

    public List<ConsoleCommand> loadCommands(Console console) throws IOException {
        List<ConsoleCommand> commands = new ArrayList<>();
        switch (this) {
            case CLIENT:
                ConsoleCommand chatMsg = new ConsoleCommand(console, "chat") {
                    @Override
                    public boolean requiresParams() {
                        return true;
                    }
                };
                chatMsg.setRunnableAction(() -> {
                    ((HiveClient) chatMsg.getConsole()).sendPacket(new MSGPacket(String.join(" ", chatMsg.getParams())));
                });

                commands.add(chatMsg);
                break;

            case SERVER:
                ConsoleCommand clientList = new ConsoleCommand(console, "clients") {
                    @Override
                    public boolean requiresParams() {
                        return false;
                    }
                };
                clientList.setRunnableAction(() -> {
                    Server server = (Server) clientList.getConsole();
                    server.getLogger().info("Connected clients:");
                    for (int i = 0; i < server.getConnectedClients().size(); i++) {
                        try {
                            server.getLogger().info(String.format("%d. %s", i + 1, server.getConnectedClients().get(i).getRemoteAddress()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                ConsoleCommand broadcast = new ConsoleCommand(console, "broadcast") {
                    @Override
                    public boolean requiresParams() {
                        return true;
                    }
                };

                broadcast.setRunnableAction(() -> {
                    Server server = (Server) broadcast.getConsole();
                    server.broadcastMessage(String.join(" ", broadcast.getParams()));
                });

                commands.add(clientList);
                commands.add(broadcast);
                break;

        }
        return commands;
    }

}
