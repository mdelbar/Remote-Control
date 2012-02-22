
package remotecontrolserver;

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

/**
 *
 * @author Matthias Delbar
 */
public class MonitorThread extends Thread {

    public Socket sock;
    private MusicCommand parent;
    private Robot robot;

    public MonitorThread(Socket sock, MusicCommand parent, Robot robot) {
        this.sock = sock;
        this.parent = parent;
        this.robot = robot;
    }

    public boolean shouldStop;
    public void run() {
        try {
            robot.waitForIdle();

            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_C);

            robot.keyRelease(KeyEvent.VK_C);
            robot.keyRelease(KeyEvent.VK_ALT);
            robot.keyRelease(KeyEvent.VK_CONTROL);

            
            shouldStop = false;
            String oldIdent = readFromClipboard();
            String ident = "";

            while(!shouldStop) {
                robot.waitForIdle();

                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_C);

                robot.keyRelease(KeyEvent.VK_C);
                robot.keyRelease(KeyEvent.VK_ALT);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                
                Thread.sleep(5000);
                
                ident = readFromClipboard();
                if(!ident.equals(oldIdent)) {
                    OutputStream out = sock.getOutputStream();
                    out.write(ident.getBytes());
                    out.flush();
                    oldIdent = ident;
                }
            }

            OutputStream out = sock.getOutputStream();
            out.write("CLOSETHREAD".getBytes());
            out.flush();
            sock.close();
            parent.threadEnded(this);

        } catch (IOException ex) {
            System.err.println("IO Exception!");
            ex.printStackTrace();
            parent.threadEnded(this);
        } catch (InterruptedException ex) {
            System.err.println("Thread was interrupted.");
            ex.printStackTrace();
            parent.threadEnded(this);
        }
    }

    public String readFromClipboard() {
        String content = "";

        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
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
        } catch (IllegalStateException ex) {
            System.err.println("Illegal State!");
            ex.printStackTrace();
        }

        return content;
    }
}
