package cs455.transport;

import cs455.harvester.Crawler;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author YANK
 */
public class TCPReceiver implements Runnable {

    Socket socket;
    DataInputStream din;
    Crawler crawler;
//    Node node;

    public TCPReceiver(Socket socket, Crawler crawler) {
        this.socket = socket;
        this.crawler = crawler;
        try {
            this.din = new DataInputStream(socket.getInputStream());
        } catch (IOException ex) {
            System.out.println("--exception in output stream--");
        }
    }

    @Override
    public void run() {
        boolean terminated = false;
        while (!terminated) {
            int dataLength = 0;
            while (socket != null) {
                try {
//                System.out.println("--data length--" + dataLength);
                    dataLength = din.readInt();
                    byte[] data = new byte[dataLength];
                    din.readFully(data, 0, dataLength);
                    crawler.onEvent(data, this.socket);
                } catch (IOException se) {
//                    System.out.println("--Error in receiving data--");
//                    se.printStackTrace();
                    terminated = true;
                }
            }
//        return null;
        }
    }
}
