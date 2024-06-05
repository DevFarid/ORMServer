package hive.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A command to be executed by the network thread.
 * Created by SixEyes on 06/03/2024.
 */
public abstract class ConsoleCommand {

    private final String commandName;
    private Runnable runnableAction;
    private final List<String> params = new ArrayList<>();

    public ConsoleCommand(String commandName) {
        this.commandName = commandName;
        this.runnableAction = () -> {};
    }

    public String getCommandName() {
        return this.commandName;
    }

    public void setParams(List<String> params) {
        this.params.addAll(params);
    }

    public List<String> getParams() {
        return this.params;
    }

    public void setRunnableAction(Runnable runnableAction) throws IOException {
        this.runnableAction = runnableAction;
    }

    public void execute() {
        this.runnableAction.run();
    }

    public abstract boolean requiresParams();

}
