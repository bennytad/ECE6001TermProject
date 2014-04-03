package scheduler;

import java.util.LinkedList;

import common.Opcode;

import scheduler.Scheduler.Cluster;

/**
 * <code>QueueMonitor</code> has a FIFO type queue (<code>LinkedList</code>) to store 
 * jobs. It will then constantly iterate through the linked list to find a job. 
 * When it finds a job as well as an available <code>WorkerNode<code>, it will remove 
 * the job from the queue (which will minimize contention) and spawn a new thread
 * <code>ParallelTaskAssigner</code> which will take a task from the job and give it 
 * to the available worker thread.  
 *
 */
public class QueueMonitor extends Thread{

	  
	  //Linked List serving as queue for the jobs
	  public static LinkedList<JobBox> job_queue = new LinkedList<JobBox>();
	    public Cluster cluster = null;

	    /**
	     * Initialize the QueueMonitor
	     * @param cluster
	     */
	    public QueueMonitor(Cluster cluster)
	    {
	    	this.cluster = cluster;
	    }
	    

	  /**
	   * safely adds a job to the end of the queue
	   * @param job_box
	   * 			The Job
	   */
	  public static void addJob(JobBox job_box)
	  {
		  synchronized(job_queue)
		  {
			  job_queue.add(job_box);
		  }
	  }
	  
	  /**
	   * Safely removes a job from the head of the queue
	   * @return
	   */
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
	  
	/**
	 * Main method that will retrieve jobs from the queue and spawn a <code>ParallelTaskAssigner</code> thread
	 * to process a task from the job  
	 */
	public void run() {
		try
		{
		while(true){
				//when either the job queue is empty or there are no free worker nodes, there is nothing
				//to do.
				while(job_queue.isEmpty() || cluster.freeWorkers.size() == 0)
				{
					Thread.sleep(100);
				}
				
				//once we have a job, we need to remove it from the queue so that there is no task assignment 
				//contention
				JobBox current_job = removeJob();
				
				//if the job is done, there is nothing else to do
				if(current_job.isJobDone()) continue;
				
		    	//if this is the first task, notify the client
				if(current_job.isFirstTask())
				{
			        current_job.getStream().writeInt(Opcode.job_start);
			        current_job.getStream().flush();
				}
				//if there are no more tasks to do, put it back so that the pending tasks can be done
				//Note: since the isJobDone returned false above, there must be tasks being processed
				else if(!current_job.hasTasksToProcess())
				{
					addJob(current_job);
					continue;//no tasks to do
				}
				//the ParallelTaskAssigner needs to put back the job on the queue once it retrieves its task
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
