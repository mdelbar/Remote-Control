
package remotecontrol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Main class. The actual command is given as argument.
 * @author Matthias Delbar
 */
public class Main {

    public static void main(String[] args) {
        if(args.length != 2) {
            System.err.println("Syntax: RemoteControl <target IP> <command>");
            System.exit(1);
        }

        String ip = args[0];
        String cmd = args[1];
      
        checkIP(ip);

        // Send the command to the other side.
        try {
            Socket sock = new Socket(ip, 42000);
            OutputStream out = sock.getOutputStream();
            out.write(cmd.getBytes());
            out.flush();

            final InputStream in = sock.getInputStream();

            if(cmd.replaceAll("music ", "").equals("monitor")) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            while(true) {
                                byte[] buf =  new byte[256];
                                in.read(buf);
                                String s = new String(buf).trim();
                                if(s.equals("CLOSETHREAD")) {
                                    break;
                                }
                                if(!s.isEmpty()) {
                                    String[] commands = new String[]{"/usr/local/bin/growlnotify", "-t", "New song", "-m", s};
                                    Process child = Runtime.getRuntime().exec(commands);
                                }
                            }
                        } catch (UnknownHostException ex) {
                            System.err.println("Unknown Host!");
                            ex.printStackTrace();
                        } catch (IOException ex) {
                            System.err.println("IO Exception!");
                            ex.printStackTrace();
                        }
                    }
                }).start();
            }

        } catch (UnknownHostException ex) {
            System.err.println("Unknown Host!");
        } catch (IOException ex) {
            System.err.println("IO Exception!");
        }

    }

    public static void checkIP(String ip) {
        String[] parts = ip.split("\\.");
        try {
            if(parts.length != 4) {
                System.err.println("Error. Invalid IP address: " + ip
                         + ". IP address contains 4 parts.");
                System.exit(1);
            }
            for(String part : parts) {
                int i = Integer.parseInt(part);
                if(i < 0 || i > 255) {
                    System.err.println("Error. Invalid IP address: " + ip
                            + ". IP address parts must be between 0 and 255 included.");
                    System.exit(1);
                }
            }
        } catch(NumberFormatException e) {
            System.err.println("Error. Invalid IP address: " + ip
                     + ". IP address must contain numerals only.");
            System.exit(1);
        }
    }

}
