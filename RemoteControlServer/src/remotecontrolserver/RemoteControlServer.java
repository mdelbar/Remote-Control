
package remotecontrolserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author Matthias Delbar
 */
public class RemoteControlServer implements Runnable {

    private ServerSocket server;
    public boolean monitoring;
    public ArrayList<MonitorThread> threads;

    public RemoteControlServer() {
        try {
            server = new ServerSocket(42000);
            new Thread(this).start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        monitoring = false;
        threads = new ArrayList<MonitorThread>();
    }

    public void run() {
        while(true) {
            try {
                Socket client = server.accept();
                InputStream in = client.getInputStream();
                byte[] buffer = new byte[1024];
                in.read(buffer);
                String message = new String(buffer);
                executeCmd(message.trim(), client);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void executeCmd(String message, final Socket sock) {
        final Command cmd;
        
        if(message.startsWith("music ")) {
            cmd = new MusicCommand(message, this);
        }
        else {
            cmd = null;
            System.err.println("Received unknown command: " + message);
            System.exit(1);
        }

        cmd.execute(sock);
    }
}
