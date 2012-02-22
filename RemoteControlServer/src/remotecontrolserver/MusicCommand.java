
package remotecontrolserver;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Commands the music.
 * @author Matthias Delbar
 */
public class MusicCommand extends Command {

    private Robot robot;
    private ArrayList<MonitorThread> threads;
    private RemoteControlServer server;

    public MusicCommand(String cmdString, RemoteControlServer server) {
        super(cmdString.replaceFirst("music ", ""));
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
        this.server = server;
        threads = new ArrayList<MonitorThread>();
        canClose = false;
    }

    @Override
    public void execute(Socket sock) {
        if(cmdString.equals("start")) {
            musicStart();
        }
        else if(cmdString.equals("stop")) {
            musicStop();
        }
        else if(cmdString.equals("pause")) {
            musicPause();
        }
        else if(cmdString.equals("restart")) {
            musicRestart();
        }
        else if(cmdString.equals("next")) {
            songNext();
        }
        else if(cmdString.equals("previous")) {
            songPrevious();
        }
        else if(cmdString.equals("identify")) {
            songIdentify(sock, true);
        }
        else if(cmdString.equals("monitor")) {
            musicMonitor(sock);
            server.monitoring = true;
        }
        else if(cmdString.equals("stopmonitor")) {
            musicStopMonitor();
            server.monitoring = false;
        }
        else if(cmdString.startsWith("rate ")) {
            songRate(cmdString.replaceFirst("rate ", ""));
        }
        else {
            System.err.println("Received unknown command: " + cmdString);
        }
    }

    // Music commands
    //////////////////

    public void musicMonitor(Socket sock) {
        MonitorThread t = new MonitorThread(sock, this, robot);
        server.threads.add(t);
        t.start();
    }

    public void musicStopMonitor() {
        for(MonitorThread t : server.threads) {
            t.shouldStop = true;
        }
        server.threads.clear();
    }

    public void threadEnded(MonitorThread t) {
        server.threads.remove(t);
        try {
            t.sock.close();
        } catch (IOException ex) {
            System.err.println("IO Exception!");
            ex.printStackTrace();
        }
        if(server.threads.isEmpty()) {
            musicStopMonitor();
        }
    }

    public void musicStart() {
        robot.waitForIdle();

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_INSERT);
        
        robot.keyRelease(KeyEvent.VK_INSERT);
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    public void musicStop() {
        robot.waitForIdle();

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_END);

        robot.keyRelease(KeyEvent.VK_END);
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    public void musicRestart() {
        musicStop();
        musicStart();
    }

    public void musicPause() {
        robot.waitForIdle();

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_INSERT);

        robot.keyRelease(KeyEvent.VK_INSERT);
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }


    // Song commands
    /////////////////

    public void songNext() {
        robot.waitForIdle();

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_PAGE_DOWN);

        robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    public void songPrevious() {
        robot.waitForIdle();

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_PAGE_UP);

        robot.keyRelease(KeyEvent.VK_PAGE_UP);
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    public void songIdentify(Socket sock, boolean waitForIdent) {
        String oldIdent = readFromClipboard();
        
        robot.waitForIdle();

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_C);

        robot.keyRelease(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.keyRelease(KeyEvent.VK_CONTROL);


        try {
            String ident = readFromClipboard();

            int i = 0;
            while(waitForIdent && i < 3 && oldIdent.equals(ident)) {
                Thread.sleep(1000);
                ident = readFromClipboard();
                i++;
            }
            
            OutputStream out = sock.getOutputStream();
            out.write(ident.getBytes());
            out.flush();

        } catch (IOException ex) {
            System.err.println("IO Exception!");
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            System.err.println("Thread was interrupted.");
            ex.printStackTrace();
        }

    }

    public void songRate(String ratingString) {
        robot.waitForIdle();
        try {
            int rating = Integer.parseInt(ratingString);
            
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_ALT);

            robot.keyPress(KeyEvent.VK_NUMPAD0 + rating);
            robot.keyRelease(KeyEvent.VK_NUMPAD0 + rating);

            robot.keyRelease(KeyEvent.VK_ALT);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            
        } catch(NumberFormatException ex) {
            System.err.println("Invalid rating: " + ratingString);
            ex.printStackTrace();
        }
    }


    public String readFromClipboard() {
        String content = "";

        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = cb.getContents(this);
        if(contents != null) {
            try {
                content = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException ex) {
                System.err.println("Unsupported Flavor!");
            ex.printStackTrace();
            } catch (IOException ex) {
                System.err.println("IO Exception!");
            ex.printStackTrace();
            }
        }

        return content;
    }
    
}
