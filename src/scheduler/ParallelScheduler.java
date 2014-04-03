package scheduler;
import java.io.*;
import java.net.*;

import common.JobFactory;
import common.Opcode;
import scheduler.Scheduler.*;

/**
 * 
 * This class handles the main job of the Scheduler in a parallel fasion:
 * 1.	When a new worker request is received, it will process and add the work to its pool
 * 2.	When a new job is received, it will package the job under <code>JobBox</code> and put
 * 		the <code>JobBox</code> on the <code>QueueMonitor</code> where it will be monitored and
 * 		processed by the <code>QueueMonitor</code>. It will then spin until the job gets done 
 * 		and will return after printing the sucessful message
 *
 * Note: this class extends the <code>Thread</code> superclass and hence will allow the scheduler
 * to service incoming socket requests without blocking.
 */
public class ParallelScheduler extends Thread{
		
		private Socket socket = null;
	    
	    public Cluster cluster = null;
	    public static int jobIdNext;
	 
	    /**
	     * Initializes the <code>ParallelScheduler</code> class 
	     * 
	     * @param socket
	     * @param cluster
	     */
	    public ParallelScheduler(Socket socket, Cluster cluster) {
	        super("ParallelScheduler");
	        this.cluster = cluster;
	        this.socket = socket;
	    }
	    
	    /**
	     * Performs the main task of putting jobs on the queue and monitoring their progress
	     */
	    public void run() {
	      try{
	          DataInputStream dis = new DataInputStream(socket.getInputStream());
	          DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
	          
	          int code = dis.readInt();
	          //We need to create a new thread here. 
	          
	          //a connection from worker reporting itself
	          if(code == Opcode.new_worker){
	            //include the worker into the cluster
	            WorkerNode n = cluster.createWorkerNode( dis.readUTF(), dis.readInt());
	            if( n == null){
	              dos.writeInt(Opcode.error);
	            }
	            else{
	              dos.writeInt(Opcode.success);
	              dos.writeInt(n.id);
	              System.out.println("Worker "+n.id+" "+n.addr+" "+n.port+" created");
	            }
	            dos.flush();
	          }

	          //a connection from client submitting a job
	          if(code == Opcode.new_job){ 
	        	  
	            String className = dis.readUTF();
	            long len = dis.readLong();

	            //send out the jobId
	            int jobId = jobIdNext++;
	            dos.writeInt(jobId);
	            dos.flush();

	            //receive the job file and store it to the shared filesystem
	            String fileName = new String("fs/."+jobId+".jar");
	            FileOutputStream fos = new FileOutputStream(fileName);
	            int count;
	            byte[] buf = new byte[65536];
	            while(len > 0) {
	              count = dis.read(buf);
	              if(count > 0){
	                fos.write(buf, 0, count);
	                len -= count;
	              }
	            }
	            fos.flush();
	            fos.close();
	            
	            int numTasks = JobFactory.getJob(fileName, className).getNumTasks();
	            
	        	JobBox job_box = new JobBox(className, numTasks, jobId, dos); 
	        	
	        	QueueMonitor.addJob(job_box);
	            
	        	while(!job_box.isJobDone())
	        	{
	        		//can improve performance with using CountDownLatch maybe?
	        		Thread.sleep(100);
	        	}
	        	
	            dos.writeInt(Opcode.job_finish);
	            dos.flush();
	          }

	          dis.close();
	          dos.close();
	          socket.close();
	          
	      } catch(Exception e) {
	        e.printStackTrace();
	      }
	    }

	    
}
