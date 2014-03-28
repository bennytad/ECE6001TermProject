package jobs;

import common.*;

//Hello World
public class SmallJob extends Job {

  @Override
  public void config() {
    setNumTasks(5); //set the number of tasks
  }

  @Override
  public void task(int tId) {
	  
    
	  System.out.println("task"+tId+": from small job"); //this string will be printed out from worker instead of client
    
	  try{
      
    	Thread.sleep(1000);
    
	  } catch(Exception e) {
      
    	e.printStackTrace();
    
    }
  }
}
