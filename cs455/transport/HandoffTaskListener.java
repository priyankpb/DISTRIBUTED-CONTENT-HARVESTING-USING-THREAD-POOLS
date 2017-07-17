/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.transport;

import cs455.harvester.Crawler;
import cs455.util.Protocol;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author YANK
 */
public class HandoffTaskListener implements Runnable {

    private Crawler crawler;
    private HashMap<String, String> configMap = new HashMap<String, String>();

    public HandoffTaskListener(Crawler crawler) {
        this.crawler = crawler;
    }

    @Override
    public void run() {
        try {
            int port = crawler.portNum;
            if(port == 0 && crawler.getInitialURL().equalsIgnoreCase(Protocol.URLS.MATH)){
                port = 42461;
            }
            if(port == 0 && crawler.getInitialURL().equalsIgnoreCase(Protocol.URLS.STAT)){
                port = 42461;
            }
            ServerSocket listener = new ServerSocket(port);
            for (int i = 0; i < Protocol.URLS.urls.size(); i++) {
                
                Socket connected = listener.accept();
                TCPReceiver receiver = new TCPReceiver(connected, crawler);
                new Thread(receiver).start();
                
            }
        } catch (IOException ex) {
            System.err.println("Error: accepting connection");
        }

    }
}




//
// int port = 0;
//        Properties prop = new Properties();
//        InputStream input = null;
//
//        try {
//
//            String filename = "cs455" + File.separator + "util" + File.separator + "localConfig.properties";
//            input = getClass().getClassLoader().getResourceAsStream(filename);
//            if (input == null) {
//                System.out.println("Sorry, unable to find " + filename);
//                return;
//            }
//
//            //load a properties file from class path, inside static method
//            prop.load(input);
//            Enumeration<?> propertyNames = prop.propertyNames();
//            while (propertyNames.hasMoreElements()) {
//                String key = (String) propertyNames.nextElement();
//                String value = prop.getProperty(key);
//                configMap.put(key, value);
//                if (value.contains(new URL(crawler.getInitialURL()).getHost())) {
//                    String[] split = key.split("\\.");
//                    if (split[1] != null) {
//                        port = Integer.parseInt(split[1]);
//                    }
//                }
//            }
//            ServerSocket listener = new ServerSocket(port);
//            for (int i = 0; i < Protocol.URLS.urls.size(); i++) {
//
//                Socket connected = listener.accept();
//                TCPReceiver receiver = new TCPReceiver(connected, crawler);
//                new Thread(receiver).start();
//
//            }
//
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        } finally {
//            if (input != null) {
//                try {
//                    input.close();
//                } catch (IOException ex) {
//                    Logger.getLogger(HandoffTaskListener.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
