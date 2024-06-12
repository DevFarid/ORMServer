package hive.commands;

import hive.HiveClient;
import hive.Server;
import hive.console.Console;
import hive.packets.AuthPacket;
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

                ConsoleCommand login = new ConsoleCommand(console, "login") {
                    @Override
                    public boolean requiresParams() {
                        return true;
                    }
                };
                login.setRunnableAction(() -> {
                    HiveClient client = (HiveClient) login.getConsole();
                    client.sendPacket(new AuthPacket(login.getParams().get(0), login.getParams().get(1)));
                });

                commands.add(login);
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
