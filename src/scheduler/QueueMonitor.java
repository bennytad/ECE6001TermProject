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
				//once we have a job, we need to remove it from the queue so that there is no task assignment 
				//contention
				JobBox current_job = removeJob();
				//if there is no more task, continue
				if(current_job.isJobDone()) continue;
				
		    	//if this is the first task, notify the client
				if(current_job.isFirstTask())
				{
			        current_job.getStream().writeInt(Opcode.job_start);
			        current_job.getStream().flush();
				}
				//if there are no more tasks to do, put it back so that the asks can be done
				else if(!current_job.hasTasksToProcess())
				{
					addJob(current_job);
					continue;//no tasks to do
				}
				//the task assigner needs to put back the job on the queue if it's not the last task
				//This ensures that there is no contention while removing a task from the Job
				//Also, once done, it needs to put back the worker in the cluster
				
				ParallelTaskAssigner task = new ParallelTaskAssigner(current_job, cluster);
				task.start();
			}
		}
		catch(Exception e)
		{
			System.out.println("***Fatal ERROR: Main Queue Monitor died. Need to restart scheduler");
			e.printStackTrace();
		}
	}

}
