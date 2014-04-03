package scheduler;

import java.io.DataOutputStream;
import java.util.LinkedList;

/**
 * A Job encapsulation class that will provide the following services
 * on top of the job: 
 * 	1. Task tracking (check out and check-in of tasks to make sure they are completed)
 * 	2. Track whether a job is done or not (based on whether all tasks are done or not)
 * 	3. Associate the IO stream of where the job was submitted so that we can report back when done
 * 	4. Track the Job ID
 *
 */
public class JobBox
{
	  private String className;
	  private int numTasks;
	  private DataOutputStream dos;
	  private int jobId;
	  
	  private int tasks_done = 0;
	  
	  private LinkedList<Integer> tasks = new LinkedList<>();
	  
	  /**
	   * Initializes the JobBox with the details of the job
	   * @param className
	   * 			The class name of the job
	   * @param numTasks
	   * 			The number of tasks in the job
	   * @param jobId
	   * 			The ID of the job
	   * @param dos
	   * 			The IO stream associated with the job
	   */
	  
	  public JobBox(String className, int numTasks, int jobId, DataOutputStream dos)
	  {
		  this.className = className;
		  this.numTasks = numTasks;
		  this.dos = dos;
		  this.jobId = jobId;
		  
		  for(int i = 0; i < numTasks; ++i)
		  {
			  tasks.add(new Integer(i));
		  }
	  }
	  
	  /**
	   * Checks whether the job is done by ensuring the number of tasks completed
	   * equal to the number of tasks the job has
	   * 
	   * @return
	   * 	returns <code>true</code> when the job is done
	   */
	  public boolean isJobDone()
	  {
		  return tasks_done == numTasks;
	  }
	  
	  /**
	   * Returns the class name of the job
	   * @return
	   * 	The class name of the job
	   */
	  public String getClassName()
	  {
		  return this.className;
	  }
	  
	  /**
	   * Returns the IO socket stream for the job
	   * @return
	   * 	IO Stream 
	   */
	  public DataOutputStream getStream()
	  {
		  return this.dos;
	  }
	  
	  /**
	   * removes the task number from the linked list and gives it to the
	   * scheduler
	   * @return
	   * 	Task ID 
	   */
	  public int getNextTask()
	  {
		  synchronized(this)
		  {
			  //there should not be a case where this method is called while the task is empty
			  if(!tasks.isEmpty())
				  return this.tasks.remove().intValue();
			  else
				  return -1;
		  }
	  }
	  
	  /**
	   * Checks if there are more tasks to do. Note, even if there are no more tasks
	   * to process, it doesn't mean the job is done. We need the tasks to complete before
	   * the job gets done.
	   * 
	   * @return
	   * 	returns <code>true</code> if there are more tasks to process
	   */
	  public boolean hasTasksToProcess()
	  {
		  return !tasks.isEmpty();
	  }
	  
	 /**
	  * Puts back a task to the job. This is useful when putting back a task that was retrieved
	  * for processing, but the worker fails. 
	  * 
	  * @param i
	  * 	task ID
	  */
	  public void putBackTask(int i)
	  {
		  synchronized(this)
		  {
			  tasks.add(new Integer(i));
		  }
	  }
	  
	  /**
	   * Returns the job ID
	   * @return
	   * 	Job ID
	   */
	  public int getJobId()
	  {
		  return this.jobId;
	  }
	  
	  /**
	   * This method is called by the scheduler when a task is completed for this job
	   */
	  public void taskDone()
	  {
		  synchronized(this)
		  {
			  ++tasks_done;
		  }
	  }
	  
	  /**
	   * Checks if we are about to retrieve the very first task from this Job
	   * @return
	   * 	<code>true</code> if we are about to retrieve the very first task
	   */
	  public boolean isFirstTask()
	  {
		  return tasks.size() == numTasks;
	  }
}
