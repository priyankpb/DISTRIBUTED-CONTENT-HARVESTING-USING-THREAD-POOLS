=====INDEX=====
1.Included files
2.How to run
3.Notes
4.Class Description

-----------------------------------------------------------------------------------------------------------

1.INCLUDED FILES:

All source files are within the following packages.

cs455/harvester/
cs455/threadpool/
cs455/transport/
cs455/util
cs455/wireformat
READ_ME.txt
Makefile

-----------------------------------------------------------------------------------------------------------

2.HOW TO RUN:

Crawler : java cs455.harvester.crawler <listenig_portnum> <thread_pool_size> <url_to_crawl> <configuration_file_path>

-----------------------------------------------------------------------------------------------------------

3.NOTES:

-Thread.sleep occurunce:
cs455.theadPool.WorkerThread.java:151

-Threads Assignment:
cs455.harvester.Crawler.java:134

-URL to be crawled and port number of the Crawler and the coresponding entries in the config file should match.
-Crawler exits itself after all other Crawlers have completed the execution by System.exit(0); method.

-----------------------------------------------------------------------------------------------------------

4.CLASS DESCRIPTION:

***cs455.harvester.Crawler.java***

-Arguements: 
 <portnum> = defines the port on which the registry accepts incoming connections.
 <thread_pool_size> = defines the max number of worker threads
 <url_to_crawl> = enter the url of the webpage you wish to crawl
 <configuration_file_path> = config file path
 
-Responsible for setting up everyting
-Creates the filestructure before exiting
-releases the threads at exit

***cs455.threadPool.WorkerThread.java***

-Executes the tasks assigned by ThreadPool.java
-frees itself after task completion

***cs455.util.URLParamEncoder.java***

-Responsible for parsing a single URL
-returns the links in that url
     
***cs455.wireformat***

-This package contains the wireformat for communication between Crawlers
-----------------------------------------------------------------------------------------------------------
