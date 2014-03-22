package scheduler;

import java.net.*;
import java.util.ArrayList;
import java.util.LinkedList;


public class Scheduler{

  int schedulerPort;
  Cluster cluster;
  int jobIdNext;
  
  public static void debug(String data)
  {
	  if(false) System.out.println(data);
  }
  
  Scheduler(int p) {
    schedulerPort = p;
    cluster = new Cluster();
    jobIdNext = 1;
  }

  public static void main(String[] args) {
    Scheduler scheduler = new Scheduler(Integer.parseInt(args[0]));
    scheduler.run();
  }
  
  public void run() {
    try{
      //create a ServerSocket listening at specified port
      ServerSocket serverSocket = new ServerSocket(schedulerPort);
	  QueueMonitor queue_monitor = new QueueMonitor(this.cluster);
	  queue_monitor.start();
      while(true){
        //accept connection from worker or client
        new ParallelScheduler(serverSocket.accept(), cluster).start();
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  

  //the data structure for a cluster of worker nodes
  public class Cluster {
    ArrayList<WorkerNode> workers; //all the workers
    LinkedList<WorkerNode> freeWorkers; //the free workers
    
    Cluster() {
      workers = new ArrayList<WorkerNode>();
      freeWorkers = new LinkedList<WorkerNode>();
    }

    WorkerNode createWorkerNode(String addr, int port) {
      WorkerNode n = null;

      synchronized(workers) {
        n = new WorkerNode(workers.size(), addr, port);
        workers.add(n);
      }
      addFreeWorkerNode(n);

      return n;
    }

    WorkerNode getFreeWorkerNode() {
      WorkerNode n = null;

      try{
        synchronized(freeWorkers) {
          while(freeWorkers.size() == 0) {
            freeWorkers.wait();
          }
          n = freeWorkers.remove();
        }
        n.status = 2;
      } catch(Exception e) {
        e.printStackTrace();
      }

      return n;
    }

    void addFreeWorkerNode(WorkerNode n) {
      n.status = 1;
      synchronized(freeWorkers) {
        freeWorkers.add(n);
        freeWorkers.notifyAll();
      }
    }
  }

  //the data structure of a worker node
  public class WorkerNode {
    int id;
    String addr;
    int port;
    int status; //WorkerNode status: 0-sleep, 1-free, 2-busy, 4-failed

    WorkerNode(int i, String a, int p) {
      id = i;
      addr = a;
      port = p;
      status = 0;
    }
  }

  

}
