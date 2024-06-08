package hive.console;

import hive.commands.ConsoleCommand;
import hive.event.NetworkEventNotifier;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A console that listens for user input and executes commands.
 * Created by SixEyes on 06/03/2024.
 */
public abstract class Console extends NetworkEventNotifier {

    private final Logger logger = Logger.getLogger(Console.class.getName());
    private final Selector selector;
    private final AbstractSelectableChannel channel;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final List<ConsoleCommand> consoleCmd = new ArrayList<>();
    private final Thread consoleThread = consoleThread();

    public Console(boolean isHost, int port) throws IOException {
        this.selector = Selector.open();
        this.channel = isHost ? ServerSocketChannel.open() : SocketChannel.open();
        this.channel.configureBlocking(false);

        if(isHost) {
            ((ServerSocketChannel) this.channel).socket().bind(new InetSocketAddress(port));
            this.channel.register(selector, SelectionKey.OP_ACCEPT);
        } else {
            ((SocketChannel) this.channel).connect(new InetSocketAddress("localhost", port));
            this.channel.register(selector, SelectionKey.OP_CONNECT);
        }

    }

    public AbstractSelectableChannel getChannel() {
        return this.channel;
    }

    private Thread consoleThread() {
        return new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    if(scanner.hasNextLine()) {
                        String[] cmdLineArg = scanner.nextLine().split(" ");
                        if (cmdLineArg.length == 0) {
                            continue;
                        }

                        if(Arrays.stream(cmdLineArg).anyMatch(s -> s.equalsIgnoreCase("stop"))) {
                            stop();
                            break;
                        }

                        String cmd = cmdLineArg[0];
                        consoleCmd.stream().filter(command -> command.getCommandName().equals(cmd))
                                .findFirst().ifPresentOrElse(
                                        command -> {
                                            if (command.requiresParams()) {
                                                command.setParams(Arrays.asList(cmdLineArg).subList(1, cmdLineArg.length));
                                            }
                                            runCommand(command);
                                        },
                                        () -> logger.log(Level.WARNING, "Command not found: " + cmd)
                                );
                    }
                }
            }
        });
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Selector getSelector() {
        return this.selector;
    }

    public AtomicBoolean getState() {
        return this.running;
    }

    public void startConsole() {
        this.consoleThread.start();
    }

    public void stopConsole() {
        this.consoleThread.interrupt();
    }

    public void addCommand(ConsoleCommand command) {
        this.consoleCmd.add(command);
    }

    public void addCommands(List<ConsoleCommand> commands) {
        for (ConsoleCommand command : commands) {
            addCommand(command);
        }
    }

    public void runCommand(ConsoleCommand command) {
        command.execute();
    }

    public abstract void start();
    public abstract void stop();

}
