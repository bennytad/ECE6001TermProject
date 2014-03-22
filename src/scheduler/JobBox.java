package scheduler;

import java.io.DataOutputStream;

//job encapsulation for tracking
public class JobBox
{
	  private String className;
	  //job is done when numTasks == 0
	  private int numTasks;
	  private int originalNumTasks;
	  private DataOutputStream dos;
	  private int jobId;
	  
	  private int current_task;
	  
	  public JobBox(String className, int numTasks, int jobId, DataOutputStream dos)
	  {
		  this.className = className;
		  this.numTasks = numTasks;
		  this.originalNumTasks = numTasks;
		  this.dos = dos;
		  this.jobId = jobId;
		  this.current_task = numTasks - 1;
	  }
	  
	  public boolean isJobDone()
	  {
		  synchronized(this)
		  {
			  return numTasks <= 0; 
		  }
	  }
	  
	  public String getClassName()
	  {
		  return this.className;
	  }
	  
	  public DataOutputStream getStream()
	  {
		  return this.dos;
	  }
	  
	  public int getNextTask()
	  {
		  synchronized(this)
		  {
			  return this.current_task--;
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
			  --numTasks;
		  }
	  }
	  
	  public boolean taskInProgress()
	  {
		  return current_task != originalNumTasks -1;
	  }
}
