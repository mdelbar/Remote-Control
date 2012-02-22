
package remotecontrolserver;

import java.net.Socket;

/**
 * Encapsulating class for a command. This will be extended/implemented by all other commands
 * @author Matthias Delbar
 */
public abstract class Command {

    protected String cmdString;
    public boolean canClose;

    public Command() {
        this("");
    }

    public Command(String cmdString) {
        this.cmdString = cmdString;
    }

    public abstract void execute(Socket sock);
}
