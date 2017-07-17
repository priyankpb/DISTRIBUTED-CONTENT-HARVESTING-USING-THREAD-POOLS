/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs455.transport;

import cs455.harvester.Crawler;
import cs455.util.Protocol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author YANK
 */
public class HandoffTaskRelayer {

    private Crawler crawler;

    public HandoffTaskRelayer(Crawler crawler) {
        this.crawler = crawler;
        init();
//        init(crawler.getInitialURL());
    }

    private void init() {
        FileReader fin = null;
        BufferedReader br = null;
        try {
            File file = new File(crawler.filePath);
            fin = new FileReader(file);
            String line;
            br = new BufferedReader(fin);
            while ((line = br.readLine()) != null) {
                // process the line.
                String[] splitted = line.split(",");
                if (splitted[1] != null) {
                    if (!Crawler.rootDomain.equalsIgnoreCase(new URL(splitted[1]).getHost())) {
                        String[] splittedIPport = splitted[0].split(":");
                        String host = splittedIPport[0];
                        Integer port = Integer.parseInt(splittedIPport[1]);
                        boolean connected = false;
                        while (!connected) {
                            try {
                                Socket clientSocket = new Socket(host, port);
                                connected = true;
                                System.out.println("connected to : " + host);
                                TCPSender sender = new TCPSender(clientSocket);
                                crawler.connectionCache.put(new URL(splitted[1]).getHost(), sender);
                            } catch (IOException ex) {
                                
                            }
                        }
                    }
                }
//                System.out.println(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                br.close();
                fin.close();
            } catch (IOException ex) {
                System.err.println("ERROR : closing the streams");
            }
        }
    }

    private void init(String url) {
        FileReader fin = null;
        BufferedReader br = null;
        try {
            File file = new File(crawler.filePath);
            fin = new FileReader(file);
            String line;
            br = new BufferedReader(fin);
            while ((line = br.readLine()) != null) {
                // process the line.
                String[] splitted = line.split(",");
                if (splitted[1] != null) {
                    if (!Crawler.rootDomain.equalsIgnoreCase(new URL(splitted[1]).getHost())) {
                        String[] splittedIPport = splitted[0].split(":");
                        String host = splittedIPport[0];
                        Integer port = Integer.parseInt(splittedIPport[1]);
                        
                        if (url.equalsIgnoreCase(Protocol.URLS.MATH)) {
                            if (Protocol.URLS.STAT.equalsIgnoreCase(splitted[1])) {
                                boolean connected = false;
                                while (!connected) {
                                    try {
                                    	Thread.sleep(2000);
                                        Socket clientSocket = new Socket(host, port);
                                        connected = true;
                                        System.out.println("connected to : " + host);
                                        TCPSender sender = new TCPSender(clientSocket);
                                        crawler.connectionCache.put(new URL(splitted[1]).getHost(), sender);
                                        
                                        
                                    } catch (IOException ex) {
//                                        System.err.println("ERROR : connecting to : " + host);
                                    } catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
                                }
                            }
                        }
                        if (url.equalsIgnoreCase(Protocol.URLS.STAT)) {
                            if (Protocol.URLS.MATH.equalsIgnoreCase(splitted[1])) {
                                boolean connected = false;
                                while (!connected) {
                                    try {
                                    	Thread.sleep(2000);
                                        Socket clientSocket = new Socket(host, port);
                                        connected = true;
                                        System.out.println("connected to : " + host);
                                        TCPSender sender = new TCPSender(clientSocket);
                                        crawler.connectionCache.put(new URL(splitted[1]).getHost(), sender);
                                        
                                    } catch (IOException ex) {
//                                        System.err.println("ERROR : connecting to : " + host);
                                    } catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
                                }
                            }
                        }

                    }
                }
//                System.out.println(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                br.close();
                fin.close();
            } catch (IOException ex) {
                System.err.println("ERROR : closing the streams");
            }
        }
    }

}