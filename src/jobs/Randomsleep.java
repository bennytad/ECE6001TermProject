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
	  int n = rand.nextInt(100);
    System.out.println("task"+tId+": Hello World"); //this string will be printed out from worker instead of client
    try{
      Thread.sleep(1000*n);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}
