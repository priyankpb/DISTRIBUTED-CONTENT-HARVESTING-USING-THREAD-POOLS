package cs455.harvester;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import cs455.threadPool.Manager;
import cs455.transport.HandoffTaskListener;
import cs455.transport.HandoffTaskRelayer;
import cs455.transport.TCPSender;
import cs455.util.Protocol;
import cs455.wireformat.TaskHandoff;

/**
 * 
 * @author YANK
 */
public class Crawler {

	private int noOfThreads = 50;
	private Manager poolManager;
	private static String initialURL;
	// private String initialURL = Protocol.URLS.STAT;
	private File nodesPointer;
	private File brokenLinksPointer;
	private File subgraphPointer;
	public int urlsToCrawl = 0;
	public Integer recursionLevel = 0;
	public HashMap<String, TCPSender> connectionCache = new HashMap<String, TCPSender>();
	public Integer handoffCounter = 0;
	public static String rootDomain;
	public Set<String> handoffURLs = new HashSet<String>();
	public Boolean crawlingComplete = false;
	public Integer crawlerCompleteCounter = 0;
	public String filePath = "/s/chopin/b/grad/priyankb/workspace/DC/src/configuration";
	public int portNum;
	public Map<String, Map<String, Set<String>>> fileStructure = new HashMap<>();
	public Set<String> brokenLinks = new HashSet<String>(); 

	public String getFilePath() {
		return filePath;
	}

	public Set<String> getBrokenLinks() {
		return brokenLinks;
	}

	public void setBrokenLinks(Set<String> brokenLinks) {
		this.brokenLinks = brokenLinks;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Map<String, Map<String, Set<String>>> getFileStructure() {
		return fileStructure;
	}

	public Crawler() {
		try {
			rootDomain = new URL(initialURL).getHost();
			System.out.println("CRAWLING: " + rootDomain);
		} catch (MalformedURLException ex) {
			Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null,
					ex);
		}
	}

	public static String getRootDomain() {
		return rootDomain;
	}

	public int getNoOfThreads() {
		return noOfThreads;
	}

	public void setNoOfThreads(int noOfThreads) {
		this.noOfThreads = noOfThreads;
	}

	public String getInitialURL() {
		return initialURL;
	}

	public void setInitialURL(String initialURL) {
		this.initialURL = initialURL;
	}

	public static void main(String[] args) {
		int port = 0;
		int threadSize = 0;
		String root = "";
		String path = "";
		port = Integer.parseInt(args[0]);
		threadSize = Integer.parseInt(args[1]);
		root = args[2];
		path = args[3];
		initialURL = root;
		Protocol.URLS.init();
		boolean cmd = false;
		if (args.length > 0) {
			cmd = true;
			// System.out.println("-true");
		}
		new Crawler().start(cmd, port, threadSize, root, path);
	}

	public void start(boolean cmd, int port, int threadSize, String root,
			String path) {

		if (cmd) {
			System.out.println("-true");
			portNum = port;
			noOfThreads = threadSize;
			initialURL = root;
			filePath = path;
		}

		HandoffTaskListener handoffTaskListener = new HandoffTaskListener(this);
		Thread handoffTaskListenerThread = new Thread(handoffTaskListener);
		handoffTaskListenerThread.start();
		poolManager = new Manager(this);
		Thread poolManagerThread = new Thread(poolManager);
		Task task = new Task();
		task.setUrl(initialURL);
		task.setRecursionLevel(0);
		HandoffTaskRelayer handoffTaskRelayer = new HandoffTaskRelayer(this);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException ex) {
			Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null,
					ex);
		}

		Queue<Task> tasksToPerform = poolManager.getTasksToPerform();
		synchronized (tasksToPerform) {
			++urlsToCrawl;
			tasksToPerform.add(task);
			tasksToPerform.notify();
		}
		Set<Task> alreadyAddedTask = poolManager.getAlreadyAddedTask();
		synchronized (alreadyAddedTask) {
			alreadyAddedTask.add(task);
		}
		poolManagerThread.start();
	}

	public void createFileStructure() {
		try {
			// System.out.println("-creating file structure-");
			String nodes = File.separator + "tmp" + File.separator + "cs455-priyankb"
					+ File.separator + rootDomain + File.separator + "nodes";
			nodesPointer = new File(nodes);
			nodesPointer.mkdirs();
			subgraphPointer = new File(File.separator +"tmp" + File.separator
					+ "cs455-priyankb" + File.separator + rootDomain
					+ File.separator + "disjoint-subgraphs");
			subgraphPointer.mkdirs();
			brokenLinksPointer = new File(File.separator + "tmp" + File.separator
					+ "cs455-priyankb" + File.separator + rootDomain
					+ File.separator + "broken-links");
			brokenLinksPointer.createNewFile();

			synchronized (fileStructure) {
				for (Map.Entry<String, Map<String, Set<String>>> entrySet : fileStructure
						.entrySet()) {
					String node = entrySet.getKey();
					if (node.isEmpty()) {
						node = rootDomain;
					}
					Map<String, Set<String>> in_out = entrySet.getValue();
					for (Map.Entry<String, Set<String>> entrySet1 : in_out.entrySet()) {
						String key = entrySet1.getKey();
						Set<String> value = entrySet1.getValue();

						File inOutFile = new File(nodes + File.separator + node);
						inOutFile.mkdirs();
						File file = new File(nodes + File.separator + node
								+ File.separator + key);
						file.createNewFile();
						FileOutputStream fwrite = new FileOutputStream(file);
						for (String urls : value) {
							fwrite.write(urls.getBytes());
							fwrite.write(System.getProperty("line.separator")
									.getBytes());
						}
					}
				}
			}
			
			synchronized (brokenLinks) {
				FileOutputStream fout = new FileOutputStream(brokenLinksPointer);
				for (String link : brokenLinks) {
					fout.write(link.getBytes());
					fout.write(System.getProperty("line.separator")
							.getBytes());
				}
			}
			
			System.out.println("---file structure created---");
		} catch (MalformedURLException ex) {
			System.err.println("ERROR : domain name");
		} catch (IOException ex) {
			System.err.println("ERROR : creating file");
			ex.printStackTrace();
		}
	}

	public void updateDataStructure(Set<String> extractedURLs) {
		System.out.println("-updating structures-");
	}

	public void onEvent(byte[] data, Socket socket) throws IOException {

		switch (data[0]) {
		case Protocol.TASK_HANDOFF:
			TaskHandoff taskHandoff = new TaskHandoff(data);
			String taskURL = taskHandoff.getInfo();
			// System.out.println("-rootURL: " + taskHandoff.getRootURL());
			String rootURL = new URL(taskHandoff.getRootURL()).getHost();
			Task task = new Task(taskURL, 5);
			Set<Task> alreadyAddedTask = poolManager.getAlreadyAddedTask();
			Queue<Task> tasksToPerform = poolManager.getTasksToPerform();
			// System.err.println("-task handoff recieved");
			boolean result = false;
			
			synchronized (alreadyAddedTask) {
				if (!alreadyAddedTask.contains(task)) {
					alreadyAddedTask.add(task);
					result = true;
				}
			}
			if (result) {
				synchronized (crawlingComplete) {
					if (crawlingComplete) {
						crawlingComplete = false;
						TaskHandoff taskHandoff3 = new TaskHandoff();
						taskHandoff3.setType(Protocol.TASK_INCOMPLETE);
						taskHandoff3.setRootURL(rootDomain);
						taskHandoff3.setInfo("incomplete -handoff");
						synchronized (crawlerCompleteCounter) {
							crawlerCompleteCounter--;
						}
						for (Map.Entry<String, TCPSender> entrySet : connectionCache
								.entrySet()) {
							String key = entrySet.getKey();
							TCPSender sender = entrySet.getValue();
							sender.sendData(taskHandoff3.getByte());
						}
					}
				}

				synchronized (tasksToPerform) {

					tasksToPerform.add(task);
					tasksToPerform.notify();
				}

			}
			sendAcknowledgement(task, rootURL);
			break;

		case Protocol.ACKNOWLEDGEMENT:
			TaskHandoff taskHandoff2 = new TaskHandoff(data);
			String taskURL1 = taskHandoff2.getInfo();
			String rootURL1 = taskHandoff2.getRootURL();

			if (!rootURL1.equalsIgnoreCase(initialURL)) {
				System.err.println("Wrong Ack recieved");
				break;
			}
			// System.err.println("-Ack recieved");
			synchronized (handoffCounter) {
				handoffCounter--;
			}
			break;

		case Protocol.TASK_COMPLETE:
			TaskHandoff taskHandoff1 = new TaskHandoff(data);
			System.out.println("-crawling completed-"
					+ taskHandoff1.getRootURL());
			synchronized (crawlerCompleteCounter) {
				crawlerCompleteCounter++;
				// if (crawlerCompleteCounter == Protocol.URLS.urls.size() &&
				// handoffCounter == 0) {
				if (crawlerCompleteCounter == connectionCache.size()+1) {
					// Crawler complete
					System.out.println("-threads released");
					createFileStructure();

					System.out
							.println("---------------------END---------------------");
					System.exit(0);
				}

			}
			break;

		case Protocol.TASK_INCOMPLETE:
			synchronized (crawlerCompleteCounter) {
				crawlerCompleteCounter--;
			}
			break;
		}
	}

	private void sendAcknowledgement(Task task, String rootURL)
			throws IOException {
		TaskHandoff taskHandoff = new TaskHandoff();
		taskHandoff.setType(Protocol.ACKNOWLEDGEMENT);
		taskHandoff.setInfo(task.getUrl());
		taskHandoff.setRootURL(rootURL);
		String rootDomain = new URL(rootURL).getHost();
		// System.out.println("--rooturl2: " + rootURL);
		TCPSender sender = connectionCache.get(rootDomain);
		sender.sendData(taskHandoff.getByte());

	}

}
