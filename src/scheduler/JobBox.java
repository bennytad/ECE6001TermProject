package scheduler;

import java.io.DataOutputStream;
import java.util.LinkedList;

//job encapsulation for tracking
public class JobBox
{
	  private String className;
	  private int numTasks;
	  private DataOutputStream dos;
	  private int jobId;
	  
	  private int tasks_done = 0;
	  
	  private LinkedList<Integer> tasks = new LinkedList<>();
	  
	  
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
	  
	  /*
	   * For us to be done, we must have completed
	   * the numTasks
	   */
	  public boolean isJobDone()
	  {
		  return tasks_done == numTasks;
	  }
	  
	  public String getClassName()
	  {
		  return this.className;
	  }
	  
	  public DataOutputStream getStream()
	  {
		  return this.dos;
	  }
	  
	  /*
	   * removes the task number from the linked list and gives it to the
	   * scheduler
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
	  
	  /*
	   * Checks if there are more tasks to do
	   */
	  public boolean hasTasksToProcess()
	  {
		  return !tasks.isEmpty();
	  }
	  
	  /*
	   * if the task/worker fails, we need to put back the task
	   *
	   */
	  public void putBackTask(int i)
	  {
		  synchronized(this)
		  {
			  tasks.add(new Integer(i));
		  }
	  }
	  
	  public int getJobId()
	  {
		  return this.jobId;
	  }
	  
	  public void taskDone()
	  {
		  synchronized(this)
		  {
			  ++tasks_done;
		  }
	  }
	  
	  public boolean isFirstTask()
	  {
		  return tasks.size() == numTasks;
	  }
}
