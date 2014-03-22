package scheduler;

import java.util.LinkedList;

import common.Opcode;

import scheduler.Scheduler.Cluster;

public class QueueMonitor extends Thread{

	  
	  //Linked List serving as queue for the jobs
	  public static LinkedList<JobBox> job_queue = new LinkedList<JobBox>();
	    public Cluster cluster = null;

	    public QueueMonitor(Cluster cluster)
	    {
	    	this.cluster = cluster;
	    }
	    

	  //safely adds a job to the end of the queue
	  public static void addJob(JobBox job_box)
	  {
		  synchronized(job_queue)
		  {
			  job_queue.add(job_box);
		  }
	  }
	  
	  public static JobBox removeJob()
	  {
		  synchronized(job_queue)
		  {
			  if(!job_queue.isEmpty())
			  {
				  return job_queue.remove();
			  }
			  else
			  {
				  return null;
			  }
		  }
	  }
	  
	public void run() {
		try
		{
		while(true){
				while(job_queue.isEmpty() || cluster.freeWorkers.size() == 0)
				{
					Thread.sleep(100);
				}
				//once we have a job, we need to assign task
				JobBox current_job = removeJob();
				
		    	//notify the client
				if(!current_job.taskInProgress())
				{
			        current_job.getStream().writeInt(Opcode.job_start);
			        current_job.getStream().flush();
				}
				//the task assigner needs to put back the job on the queue if it's not the last task
				//Also, once done, it needs to put back the worker in the cluster
				
				ParallelTaskAssigner task = new ParallelTaskAssigner(current_job, cluster);
				task.start();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
