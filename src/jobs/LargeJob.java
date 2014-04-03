package jobs;

import common.*;

//Hello World
public class LargeJob extends Job {

  @Override
  public void config() {
    setNumTasks(20); //set the number of tasks
  }

  @Override
  public void task(int tId) {
	  
    System.out.println("task"+tId+": from large job"); //this string will be printed out from worker instead of client
    
    try{
    
    	Thread.sleep(10000);
    	
    } catch(Exception e) {
      
    	e.printStackTrace();
    }
  }
}
