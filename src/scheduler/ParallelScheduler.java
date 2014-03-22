package scheduler;
import java.io.*;
import java.net.*;
import java.util.concurrent.CountDownLatch;

import common.JobFactory;
import common.Opcode;
import scheduler.Scheduler.*;
import scheduler.Scheduler;

public class ParallelScheduler extends Thread{
		
		private Socket socket = null;
	    
	    public Cluster cluster = null;
	    public static int jobIdNext;
	 
	    public ParallelScheduler(Socket socket, Cluster cluster) {
	        super("ParallelScheduler");
	    	Scheduler.debug("Started thread");
	        this.cluster = cluster;
	        this.socket = socket;
	    	Scheduler.debug("created thread");
	    }
	    
	    public void run() {
	      try{
		      Scheduler.debug("running thread");
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
	        
	      //serverSocket.close();
	    }

	    
}
