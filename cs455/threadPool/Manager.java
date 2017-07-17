package cs455.threadPool;

import cs455.harvester.Crawler;
import cs455.harvester.Task;
import cs455.util.Protocol;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author YANK
 */
public class Manager implements Runnable {

    private Crawler crawler;
    private Queue<Task> tasksToPerform;
    private ThreadPool threadPool;
    private Set<Task> compleTedTasks = new HashSet<>();
    private Set<Task> alreadyAddedTask = new HashSet<>();
    private int counter = 0;

    public Manager() {
        this.tasksToPerform = new LinkedList<>();
    }

    public Manager(Crawler crawler) {
        this.tasksToPerform = new LinkedList<>();
        this.crawler = crawler;
        this.threadPool = new ThreadPool(crawler.getNoOfThreads(), this);
    }

    public Set<Task> getAlreadyAddedTask() {
        return alreadyAddedTask;
    }

    public void setAlreadyAddedTask(Set<Task> alreadyAddedTask) {
        this.alreadyAddedTask = alreadyAddedTask;
    }

    public Crawler getCrawler() {
        return crawler;
    }

    public void setCrawler(Crawler crawler) {
        this.crawler = crawler;
    }

    public Queue<Task> getTasksToPerform() {
        return tasksToPerform;
    }

    public void setTasksToPerform(Queue<Task> tasksToPerform) {
        this.tasksToPerform = tasksToPerform;
    }

    public Set<Task> getCompleTedTasks() {
        return compleTedTasks;
    }

    public void setCompleTedTasks(Set<Task> compleTedTasks) {
        this.compleTedTasks = compleTedTasks;
    }

    @Override
    public void run() {
        System.out.println("-Thread pool manager started.-");
        Task taskToAssign;
        while (true) {
            synchronized (tasksToPerform) {
                if (tasksToPerform.isEmpty()) {
                    try {
//                        System.out.println("-waiting for task-");
                        tasksToPerform.wait();
                    } catch (InterruptedException ex) {
                        System.err.println("ERROR : Task to perform - wait()");
                    }
                }
                taskToAssign = tasksToPerform.poll();
            }
            if (taskToAssign != null) {
                
//                System.out.println("-Remaining-" + tasksToPerform.size());
                WorkerThread retrieveThread = threadPool.retrieveThread();
                retrieveThread.executeTask(taskToAssign);
            }
        }

    }

}
