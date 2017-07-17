package cs455.threadPool;

import cs455.harvester.Crawler;
import cs455.harvester.Task;
import cs455.transport.TCPSender;
import cs455.util.Protocol;
import cs455.wireformat.TaskHandoff;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author YANK
 */
public class ThreadPool {

    private int noOfThreads;
    private Queue<WorkerThread> workerThreads;
    private Manager poolManager;

    public int getNoOfThreads() {
        return noOfThreads;
    }

    public void setNoOfThreads(int noOfThreads) {
        this.noOfThreads = noOfThreads;
    }

    public Queue<WorkerThread> getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(Queue<WorkerThread> workerThreads) {
        this.workerThreads = workerThreads;
    }

    public ThreadPool() {
        workerThreads = new LinkedList<>();
    }

    public Manager getPoolManager() {
        return poolManager;
    }

    public void setPoolManager(Manager poolManager) {
        this.poolManager = poolManager;
    }

    public ThreadPool(int noOfThreads, Manager poolManager) {
        this.noOfThreads = noOfThreads;
        workerThreads = new LinkedList<>();
        synchronized (workerThreads) {
            for (int i = 0; i < this.noOfThreads; i++) {
                WorkerThread workerThread = new WorkerThread(this);
                Thread thread = new Thread(workerThread);
                thread.start();
                workerThreads.add(workerThread);
            }
        }
        this.poolManager = poolManager;
    }

    public WorkerThread retrieveThread() {
        synchronized (workerThreads) {
            if (workerThreads.isEmpty()) {
                try {
//                    System.out.println("-waiting for worker-");
                    workerThreads.wait();
                } catch (InterruptedException ex) {
                    System.err.println("ERROR : wait in woker threads");
                }
            }
            return workerThreads.poll();
        }
    }

    public void releaseThread(WorkerThread thread) {
        synchronized (workerThreads) {
            workerThreads.add(thread);
            workerThreads.notifyAll();

            if (workerThreads.size() == poolManager.getCrawler().getNoOfThreads()) {
                Queue<Task> tasksToPerform = poolManager.getTasksToPerform();
                synchronized (tasksToPerform) {
//                    System.out.println("==========");
//                    System.out.println(tasksToPerform.size() + " " + poolManager.getCrawler().recursionLevel);
                    synchronized (poolManager.getCrawler().recursionLevel) {
                        if (tasksToPerform.isEmpty()) {
//                        if (tasksToPerform.isEmpty() && poolManager.getCrawler().recursionLevel == Protocol.RECURSION_LEVEL) {
                            Set<Task> compleTedTasks = poolManager.getCompleTedTasks();
//                        synchronized (compleTedTasks) {
                            System.out.println("-own tasks completed-" + compleTedTasks.size());
                            synchronized (poolManager.getCrawler().crawlingComplete) {
                                poolManager.getCrawler().crawlingComplete = true;
                            }
//                            for (Task c : compleTedTasks) {
//                                System.out.println(c.getUrl());
//                            }
                            //}
//                            poolManager.getCrawler().connectionCache
                            TaskHandoff taskHandoff = new TaskHandoff();
                            taskHandoff.setType(Protocol.TASK_COMPLETE);
                            taskHandoff.setInfo("Crawling Complete");
                            taskHandoff.setRootURL(Crawler.getRootDomain());
                            for (Map.Entry<String, TCPSender> entrySet : poolManager.getCrawler().connectionCache.entrySet()) {
                                String key = entrySet.getKey();
                                TCPSender sender = entrySet.getValue();
                                try {
                                    sender.sendData(taskHandoff.getByte());
                                } catch (IOException ex) {
                                    System.err.println("Error: sending task complete");
                                }

                            }
                            synchronized (poolManager.getCrawler().crawlerCompleteCounter) {
//                                    System.err.println("--attempting");
                                    poolManager.getCrawler().crawlerCompleteCounter++;
//                                if (poolManager.getCrawler().crawlerCompleteCounter == Protocol.URLS.urls.size() - 1 && poolManager.getCrawler().handoffCounter == 0) {
                                    if (poolManager.getCrawler().crawlerCompleteCounter == getPoolManager().getCrawler().connectionCache.size()+1) {
//                        Crawler complete
                                        System.out.println("-threads released");
                                        getPoolManager().getCrawler().createFileStructure();
                                        System.out.println("---------------------END---------------------");
                                        System.exit(0);
                                    
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
