package jobs;

import java.util.Random;

import common.*;

//Hello World
public class Randomsleep extends Job {

  @Override
  public void config() {
    setNumTasks(5); //set the number of tasks
  }

  @Override
  /*
   * This job mimics various tasks that take from 0 - 10 given the task ID
   * 
   * The start of the task is noted by taskID# started and when done, it is noted by
   * taskID# Done! This helps us verify that a task is re-submitted and completed when
   * a worker fails in the middle of processing a job. 
   * 
   * (non-Javadoc)
   * @see common.Job#task(int)
   */
  public void task(int tId) {
	  
	  Random rand = new Random();
	  int n = rand.nextInt(1);
	  System.out.println("task"+tId+": Started"); 
	  
    try{
    	
      Thread.sleep(10000 * n);//gives 0 - 10 seconds of sleep time
      
    } catch(Exception e) {
    	
      e.printStackTrace();
    }
    
    System.out.println("task"+tId+": Done!");
  }
}
