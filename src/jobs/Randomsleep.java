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
  public void task(int tId) {
	  Random rand = new Random();
	  int n = rand.nextInt(5);
    System.out.println("task"+tId+": Started"); 
    try{
      Thread.sleep(5000);
    } catch(Exception e) {
      e.printStackTrace();
    }
    System.out.println("task"+tId+": Done!");
  }
}
