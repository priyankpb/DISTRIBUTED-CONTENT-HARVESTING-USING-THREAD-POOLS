package cs455.threadPool;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.htmlparser.jericho.Config;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.LoggerProvider;
import net.htmlparser.jericho.Source;
import cs455.harvester.Crawler;
import cs455.harvester.Task;
import cs455.transport.TCPSender;
import cs455.util.Protocol;
import cs455.wireformat.TaskHandoff;

/**
 * 
 * @author YANK
 */
public class WorkerThread implements Runnable {

	private int id;
	private ThreadPool pool;
	private Task taskToPerform;
	private final Object lockObj = new Object();

	public WorkerThread(ThreadPool pool) {
		this.pool = pool;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void run() {

		System.out
				.println("Thread started : " + Thread.currentThread().getId());
		id = (int) Thread.currentThread().getId();
		int count = 0;
		synchronized (lockObj) {
			while (true) {
				try {
////					System.out.println("-waiting-"
//							+ Thread.currentThread().getId() + "--"
//							+ System.currentTimeMillis());
					lockObj.wait();
					if (taskToPerform.getRecursionLevel() <= Protocol.RECURSION_LEVEL) {
//						System.err.println("-PROCESSING-"
//								+ taskToPerform.getUrl() + "-"
//								+ Thread.currentThread().getId());
					}
					String url = taskToPerform.getUrl();
					synchronized (pool.getPoolManager().getCrawler().recursionLevel) {
						if (taskToPerform.getRecursionLevel() > pool
								.getPoolManager().getCrawler().recursionLevel) {
							pool.getPoolManager().getCrawler().recursionLevel = taskToPerform
									.getRecursionLevel();
						}
					}
					Set<String> extractedURLs = null;
					// Go until the recursion level
					if (url != null
							&& taskToPerform.getRecursionLevel() < Protocol.RECURSION_LEVEL) {
						// resolving redirects
						String pageUrl = resolveRedirects(url);
						boolean crawlRedirected = true;
						if (!pageUrl.equalsIgnoreCase(url)) {
//							System.out.println("-redirection-url-" + url
//									+ "-redirected-" + pageUrl + "-"
//									+ Thread.currentThread().getId());
							Set<Task> alreadyAddedTask = pool.getPoolManager()
									.getAlreadyAddedTask();
							synchronized (alreadyAddedTask) {
								alreadyAddedTask.add(new Task(url,
										taskToPerform.getRecursionLevel()));
								String redirectedDomain = new URL(pageUrl)
										.getHost();
								if (Protocol.URLS.domainNames
										.contains(redirectedDomain)) {
									taskToPerform.setUrl(pageUrl);
								} else {
									// domain name out of scope
								}
								// Don't crawl if redirected URL has already
								// been crawled
								if (alreadyAddedTask.contains(taskToPerform)) {
									crawlRedirected = false;
								}
							}
							// adding redirected url to alreadyAddedTask
							// Set<Task> alreadyAddedTask =
							// pool.getPoolManager().getAlreadyAddedTask();
							// synchronized (alreadyAddedTask) {
							// alreadyAddedTask.add(new Task(pageUrl,
							// taskToPerform.getRecursionLevel()));
							// }

						}
						// extracting urls
						if (crawlRedirected) {
							String newUrl = taskToPerform.getUrl();
							// newUrl.trim();
							extractedURLs = this.extractURL(newUrl);
							// pool.getPoolManager().getCrawler().updateDataStructure(extractedURLs);
							// System.out.println(extractedURLs.size() +
							// "--tasks with level--" +
							// (taskToPerform.getRecursionLevel() + 1));
							this.processTaskOutput(extractedURLs, newUrl);
						}
					} else {
						++count;
						// System.out.println("-Recursion lvel reached--" +
						// Thread.currentThread().getId() + "--" + count);
					}

				} catch (InterruptedException ex) {
					System.err.println("ERROR : waiting for lock." + "-"
							+ Thread.currentThread().getId());
				} catch (IOException ex) {
					this.manageBrokenLinkWhileCrawling(taskToPerform.getUrl());
				} catch (Exception ex) {
					System.err.println("ERROR : Unknown exception "
							+ ex.getClass() + "-"
							+ Thread.currentThread().getId());
					ex.printStackTrace();
				} finally {
					// adding task to completed task list
					synchronized (pool.getPoolManager().getCompleTedTasks()) {
						pool.getPoolManager().getCompleTedTasks()
								.add(taskToPerform);
					}
					try {
						Thread.sleep(1000);
						// Thread.sleep(0);
					} catch (InterruptedException ex) {
						System.err.println("ERROR : Thread.sleep().");
					}

					this.releaseMe();

				}
			}
		}
	}

	public void executeTask(Task task) {
		taskToPerform = task;
		synchronized (lockObj) {
			lockObj.notify();
			// System.out.println("-notifying-" + this.getId() + "--" +
			// System.currentTimeMillis());
		}
	}

	public void releaseMe() {
		pool.releaseThread(this);
	}

	public Set<String> extractURL(String url) throws IOException {
		// System.out.println("-extracting url-");
		// web page that needs to be parsed
		Config.LoggerProvider = LoggerProvider.DISABLED;
		Source source = new Source(new URL(url));
		int count = 0;
		// get all 'a' tags
		List<Element> aTags = source.getAllElements(HTMLElementName.A);
		// List<String> authenticated = new ArrayList<String>();
		// authenticated.add("http://www.bmb.colostate.edu/index.cfm");
		Set<String> uniqueLinks = new HashSet<>();
		// get the URL ("href" attribute) in each 'a' tag
		for (Element aTag : aTags) {
			// print the url
			String link = aTag.getAttributeValue("href");
			// System.out.println(link);
			uniqueLinks.add(link);
			// count++;
		}
		// System.out.println("Unique links : ");
		// for (String uniqueLink : uniqueLinks) {
		// System.out.println(uniqueLink);
		// ++count;
		// }
		// System.out.println("Count : " + uniqueLinks.size());
		return uniqueLinks;

	}

	
	public void processTaskOutput(Set<String> futureTasks, String actualURL){
	       if (futureTasks != null && !futureTasks.isEmpty()) {
	            int count = 0;
	            Queue<Task> tasksToPerform = pool.getPoolManager().getTasksToPerform();
	            Set<Task> alreadyAddedTask = pool.getPoolManager().getAlreadyAddedTask();
	            Crawler crawler = pool.getPoolManager().getCrawler();
	            if (taskToPerform.getRecursionLevel() <= Protocol.RECURSION_LEVEL) {
//	                List<Task> compleTedTasks = pool.getPoolManager().getCompleTedTasks();
	                List<Task> tasksToAdd = new ArrayList<>();
	                Set<String> outputLinks = new HashSet<>();
	                Set<String> inputLinks = new HashSet<>();
	                //converting in absolute URL
	                for (String futureTask : futureTasks) {
	                    try {
	 
//	                    ++count;
	                        futureTask = normalized(futureTask);
	                        if (futureTask != null) {
	                            boolean addTask = true;
//	                            boolean addLink = false;
	                            if (futureTask.startsWith(Protocol.MAIL_SCHEME)) {
	                                addTask = false;
//	                                addLink = false;
	                            } else if (!new URI(futureTask).isAbsolute()) {
//	                            old logic of absolution of url
//	                            URI resolvedUrl = new URI(actualURL).resolve(futureTask);
//	                            futureTask = resolvedUrl.toString();
//	                            changed logic of making url absolute
	                                URL absURL = new URL(new URL(actualURL), futureTask);
	                                futureTask = absURL.toString();
//	                            removing same page crawling
//	                                if (futureTask.contains("#") && new URL(futureTask).getHost().equalsIgnoreCase(new URL(taskToPerform.getUrl()).getHost())) {
//	                                    addTask = false;
////	                                    addLink = true;
//	                                }
	 
//	                            System.out.println("Resolved URL: " + futureTask);
	                            } else if (futureTask.startsWith("http://")) {
//	                                try {
	                                String domainName = new URL(futureTask).getHost();
//	                                Do task-handoff
	                                if (Protocol.URLS.domainNames.contains(domainName)) {
//	                                    addLink = true;
	                                    if (!domainName.equals(Crawler.rootDomain)) {
//	                                        addLink = false;
	                                        
//	                                            System.out.println("-handoff-" + futureTask);
//	                                                synchronized (crawler.handOffCounter) {
	                                            synchronized (crawler.handoffCounter) {
	                                                ++crawler.handoffCounter;
	                                            }
	                                            
//	                                                if (!alreadyHandedOff.contains(futureTask) && SystemFunctions.checkURL(futureTask)) {
	                                                if (checkURL(futureTask)) {
//	                                                    alreadyHandedOff.add(futureTask);
//	                                                    System.out.println("-sending handoff-" + futureTask);
	                                                    Map<String, TCPSender> crawlerToSenderMap = crawler.connectionCache;
	                                                    TCPSender sender = crawlerToSenderMap.get(domainName);
//	                                                System.out.println("-sender obj-" + sender);
//	                                                    HandoffWireFormat handoffWireFormat = new HandoffWireFormat(Constants.MESSAGE_TYPES.HANDOFF_TASK, taskToPerform.getUrl(), futureTask);
	                                                    TaskHandoff handoff = new TaskHandoff();
	                                                    handoff.setType(Protocol.TASK_HANDOFF);
	                                                    handoff.setInfo(futureTask);
	                                                    handoff.setRootURL(taskToPerform.getUrl());
	                                                    synchronized (sender) {
//	                                                    System.out.println("-sending-");
	                                                        sender.sendData(handoff.getByte());
	                                                    }
	                                                }
	                                            
	                                        
	                                        addTask = false;
	                                    }
	                                } else {
	                                    addTask = false;
//	                                    addLink = false;
	                                }
//	                                } catch (MalformedURLException ex) {
//	                                    System.err.println("Malformed domain name");
//	                                }
	                            } else if (futureTask.startsWith("https:") || !futureTask.startsWith("http://")) {
//	                            System.err.println("-Removing https-" + futureTask);
	                                addTask = false;
//	                                addLink = false;
//	                                if (futureTask.startsWith("https:")) {
//	                                    addLink = true;
//	                                }
	                            }
	                            //determine outer links
	                            if (futureTask.startsWith("http://")) {
	                                if (Protocol.URLS.domainNames.contains(new URL(futureTask).getHost())) {
	                                    outputLinks.add(futureTask);
	                                }
	                            }
	                            if (addTask) {
	                                addTask = checkURL(futureTask);
	                                if (addTask) {
	                                    inputLinks.add(futureTask);
	                                }
	                            }
	                            //Add tasks only if its not last level of recursion
	                            if (taskToPerform.getRecursionLevel() < Protocol.RECURSION_LEVEL) {
	                                if (addTask) {
//	                            ++count;
//	                                    inputLinks.add(futureTask);
	                                    Task taskToAdd = new Task();
	                                    if (futureTask.lastIndexOf("/") == 6) {
	                                        futureTask += "/";
	                                    }
	                                    taskToAdd.setUrl(futureTask);
	                                    taskToAdd.setRecursionLevel(taskToPerform.getRecursionLevel() + 1);
	                                    tasksToAdd.add(taskToAdd);
	                                }
	                            }
	                        }
	                    } catch (URISyntaxException ex) {
//	                        System.err.println("ERROR : processing - " + ex.getClass());
//	                        ex.printStackTrace();
	                    } catch (IOException ex) {
//	                        System.err.println("ERROR : malformed URL - processing - " + taskToPerform.getUrl() + "-" + Thread.currentThread().getId());
//	                        ex.printStackTrace();
	                    } catch (Exception ex) {
//	                        System.err.println("ERROR : processing - Unknown exception" + ex.getClass() + "-" + Thread.currentThread().getId());
	                    }
	                }
	                Map<String, Map<String, Set<String>>> folderStructureMap = crawler.getFileStructure();
	                String nodeName = convertToRelativeURL(taskToPerform.getUrl());
	                synchronized (folderStructureMap) {
	                    Map<String, Set<String>> innerMap = folderStructureMap.get(nodeName);
	                    if (innerMap == null) {
	                        innerMap = new HashMap<>();
	                        folderStructureMap.put(nodeName, innerMap);
	                    }
	                    innerMap.put(Protocol.FILE_STRUCTURE_OUT, outputLinks);
	                    if (!inputLinks.isEmpty()) {
	                        for (String inputLink : inputLinks) {
	                            Set<String> inLinks = innerMap.get(Protocol.FILE_STRUCTURE_IN);
	                            if (inLinks == null) {
	                                inLinks = new HashSet<>();
	                                innerMap.put(Protocol.FILE_STRUCTURE_IN, inLinks);
	                            }
	                            inLinks.add(inputLink);
	                        }
	                    }
	                }
	                if (taskToPerform.getRecursionLevel() < Protocol.RECURSION_LEVEL) {
	                    List<Task> filteredTasksToAdd = new ArrayList<>();
	                    synchronized (alreadyAddedTask) {
	                        for (Task taskToAdd : tasksToAdd) {
	                            if (!alreadyAddedTask.contains(taskToAdd)) {
//	                            ++count;
	                                ++crawler.urlsToCrawl;
	                                //Checking redirection
	                                alreadyAddedTask.add(taskToAdd);
	                                filteredTasksToAdd.add(taskToAdd);
	                            }
	                        }
//	                    System.out.println("-urls crawled-" + crawler.urlsToCrawl + "-" + Thread.currentThread().getId());
//	                    System.out.println("-Already added size-" + alreadyAddedTask.size() + "-" + Thread.currentThread().getId());
	                    }
	                    synchronized (tasksToPerform) {
	                        tasksToPerform.addAll(filteredTasksToAdd);
//	                    for (Task filteredTaskToAdd : filteredTasksToAdd) {
//	                        tasksToPerform.add(filteredTaskToAdd);
//	                    }
	                        tasksToPerform.notifyAll();
//	                    System.out.println("-New tasks to create - " + filteredTasksToAdd.size() + "-" + Thread.currentThread().getId());
	                    }
	                }
	            }
	        }
	}

	
	
	public String resolveRedirects(String url) throws IOException {
		HttpURLConnection con = (HttpURLConnection) (new URL(url)
				.openConnection());
		con.setInstanceFollowRedirects(false);
		con.connect();
		int responseCode = con.getResponseCode();
		if (responseCode == 301) {
			String s = con.getHeaderField("Location");
			if (!s.startsWith("http://")) {
				s = "http://" + new URL(url).getHost() + s;
			}
			return s;
		} else {
			return url;
		}
	}

	public static boolean checkURL(String url) {
		// return url.matches("(.\/[^\.]*|.(htm|html|aspx|asp|cfm|jsp|php))$");
		int lastIndexOf = url.lastIndexOf("/");
		String tmp = url.substring(lastIndexOf, url.length());
		// System.out.println("-tmp-" + tmp);
		if (tmp.contains(".")) {
			if (tmp.endsWith("html") || tmp.endsWith("htm")
					|| tmp.endsWith("cfm") || tmp.endsWith("aspx")
					|| tmp.endsWith("php") || tmp.endsWith("asp")
					|| tmp.endsWith("jsp")) {
				return true;
			}
		} else {
			return true;
		}
		return false;
	}

	public static String normalize(String normalize) {
		if (normalize == null) {
			return null;
		}

		normalize = normalize.trim();

		if (normalize.contains("]")) {
			normalize = normalize.replaceAll("]", "");
		}
		if (normalize.contains("\n")) {
			normalize = normalize.replaceAll("\n", "");
		}
		if (normalize.contains(" ")) {
			normalize = normalize.replaceAll(" ", "%20");
		}
		if (normalize.contains("#")) {
			normalize = normalize.substring(0, normalize.indexOf("#"));
		}
		if (normalize.contains("?")) {
			normalize = normalize.substring(0, normalize.indexOf("?"));
		}

		// if (crawled.contains("[\]"))
		// crawled = crawled.replace("[\\]", "");
		if (normalize.contains("\\")) {
			normalize = normalize.substring(0, normalize.indexOf("\\"))
					+ normalize.substring(normalize.indexOf("\\") + 1,
							normalize.length());
		}
		if (normalize.contains("\"")) {
			normalize = normalize.replaceAll("\"", "");
		}
		if (normalize.contains("[")) {
			normalize = normalize.replace("[", "");
		}
		if ((normalize.length() == 7 && normalize.contains("http:"))
				|| normalize.contains("file://")) {
			normalize = null;
		}

		return normalize;
	}

	public static String normalize2(String normalized) {

		if (normalized == null) {
			return null;
		}

		// If the buffer begins with "./" or "../", the "." or ".." is removed.
		if (normalized.startsWith("./")) {
			normalized = normalized.substring(1);
		} else if (normalized.startsWith("../")) {
			normalized = normalized.substring(2);
		} else if (normalized.startsWith("..")) {
			normalized = normalized.substring(2);
		}

		// All occurrences of "/./" in the buffer are replaced with "/"
		int index = -1;
		while ((index = normalized.indexOf("/./")) != -1) {
			normalized = normalized.substring(0, index)
					+ normalized.substring(index + 2);
		}

		// If the buffer ends with "/.", the "." is removed.
		if (normalized.endsWith("/.")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}

		int startIndex = 0;

		// All occurrences of "/<segment>/../" in the buffer, where ".."
		// and <segment> are complete path segments, are iteratively replaced
		// with "/" in order from left to right until no matching pattern
		// remains.
		// If the buffer ends with "/<segment>/..", that is also replaced
		// with "/". Note that <segment> may be empty.
		while ((index = normalized.indexOf("/../", startIndex)) != -1) {
			int slashIndex = normalized.lastIndexOf('/', index - 1);
			if (slashIndex >= 0) {
				normalized = normalized.substring(0, slashIndex)
						+ normalized.substring(index + 3);
			} else {
				startIndex = index + 3;
			}
		}
		if (normalized.endsWith("/..")) {
			int slashIndex = normalized.lastIndexOf('/',
					normalized.length() - 4);
			if (slashIndex >= 0) {
				normalized = normalized.substring(0, slashIndex + 1);
			}
		}

		// All prefixes of "<segment>/../" in the buffer, where ".."
		// and <segment> are complete path segments, are iteratively replaced
		// with "/" in order from left to right until no matching pattern
		// remains.
		// If the buffer ends with "<segment>/..", that is also replaced
		// with "/". Note that <segment> may be empty.
		while ((index = normalized.indexOf("/../")) != -1) {
			int slashIndex = normalized.lastIndexOf('/', index - 1);
			if (slashIndex >= 0) {
				break;
			} else {
				normalized = normalized.substring(index + 3);
			}
		}
		if (normalized.endsWith("/..")) {
			int slashIndex = normalized.lastIndexOf('/',
					normalized.length() - 4);
			if (slashIndex < 0) {
				normalized = "/";
			}
		}

		return normalized;
	}

	private void handoff(String futureTask) throws MalformedURLException {
		// String domain = new URL(taskToPerform.getUrl()).getHost();
		if (checkURL(futureTask)) {
			if (!pool.getPoolManager().getCrawler().handoffURLs
					.contains(futureTask)) {
				pool.getPoolManager().getCrawler().handoffURLs.add(futureTask);
				String domain = new URL(futureTask).getHost();
				TaskHandoff taskHandoff = new TaskHandoff();
				taskHandoff.setType(Protocol.TASK_HANDOFF);
				taskHandoff.setRootURL(pool.getPoolManager().getCrawler()
						.getInitialURL());

				taskHandoff.setInfo(futureTask);
//				System.err.println("-domain: " + domain);
				TCPSender sender = pool.getPoolManager().getCrawler().connectionCache
						.get(domain);
				System.err
						.println(pool.getPoolManager().getCrawler().connectionCache);
				System.err.println(sender);
				try {
					synchronized (sender) {
						sender.sendData(taskHandoff.getByte());
					}
				} catch (IOException ex) {
					System.err.println("Error: task handoff");
				}
				synchronized (pool.getPoolManager().getCrawler().handoffCounter) {
					pool.getPoolManager().getCrawler().handoffCounter++;
				}
			}
		}

	}

	public String convertToRelativeURL(String url) {
		int length = url.length();
		int j = 0;
		String result = null;
		for (int i = 0; i < length; i++) {
			if (url.charAt(i) == '/') {
				++j;
				if (j == 3) {
					result = url.substring(i + 1, length);
					break;
				}
			}
		}
		if (result != null) {
			if (result.contains("/")) {
				result = result.replaceAll("/", "-");
			}
		}
		return result;
	}
	
    private static String normalized(String futureTask) {
    
        if (futureTask != null) {
            futureTask = futureTask.trim();
 
            if (futureTask.contains("]")) {
                futureTask = futureTask.replaceAll("]", "");
            }
 
            if (futureTask.contains(" ")) {
                futureTask = futureTask.replaceAll(" ", "%20");
            }
            if (futureTask.contains("\n")) {
                futureTask = futureTask.replaceAll("\n", "");
            }
            if (futureTask.contains("\r")) {
                futureTask = futureTask.replaceAll("\r", "");
            }
            if (futureTask.contains("#")) {
                futureTask = futureTask.substring(0, futureTask.indexOf("#"));
            }
            if (futureTask.contains("?")) {
                futureTask = futureTask.substring(0, futureTask.indexOf("?"));
            }
 
//          if (crawled.contains("[\]"))
//              crawled = crawled.replace("[\\]", "");
            if (futureTask.contains("\\")) {
                futureTask = futureTask.substring(0, futureTask.indexOf("\\")) + futureTask.substring(futureTask.indexOf("\\") + 1, futureTask.length());
            }
            if (futureTask.contains("\"")) {
                futureTask = futureTask.replaceAll("\"", "");
            }
            if (futureTask.contains("\t")) {
                futureTask = futureTask.replaceAll("\t", "");
            }
            if (futureTask.contains("[")) {
                futureTask = futureTask.replace("[", "");
            }
            if (futureTask.length() > 4 && futureTask.charAt(4) == '|') {
                futureTask = futureTask.replace("|", ":");
            }
 
            if ((futureTask.length() == 7 && futureTask.contains("http:")) || futureTask.contains("file://") || (futureTask.length() == 7 && futureTask.contains("mailto:")) || futureTask.contains("mail%20to:")) {
                futureTask = null;
            }
        }
        return futureTask;
    }
    public void manageBrokenLinkWhileCrawling(String brokenLinkURL) {
//      System.out.println("-broken link-" + brokenLinkURL);
      Set<String> brokenLinks = pool.getPoolManager().getCrawler().brokenLinks;
      Map<String, Map<String, Set<String>>> folderStructureMap = pool.getPoolManager().getCrawler().getFileStructure();
      synchronized (brokenLinks) {
          brokenLinks.add(taskToPerform.getUrl());
      }
      String convertAbsoluteURLToRelativeForNodeName = convertToRelativeURL(taskToPerform.getUrl());
      synchronized (folderStructureMap) {
//          Map<String, Map<String, Set<String>>> folderStructureMap = crawler.getFolderStructureMap();
//          Map<String, Set<String>> innerMap = folderStructureMap.get(convertAbsoluteURLToRelativeForNodeName);
//          if (innerMap != null) {
////              System.out.println("-found broken link node-" + convertAbsoluteURLToRelativeForNodeName);
//              folderStructureMap.remove(convertAbsoluteURLToRelativeForNodeName);
//          }
          folderStructureMap.remove(convertAbsoluteURLToRelativeForNodeName);
      }
  }

}
