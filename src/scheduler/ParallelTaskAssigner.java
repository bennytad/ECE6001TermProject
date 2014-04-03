package scheduler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import scheduler.Scheduler.Cluster;
import scheduler.Scheduler.WorkerNode;
import common.Opcode;

/**
 * This class will be spawned by the <code>QueueMonitor</code> whenever
 * there is a free worker node and a job with a task. 
 * 
 * This class will:
 * 1. Retrieve a task from the job and a worker node from the cluster
 * 2. Put back the job on the queue (goes to the tail of the queue for fairness)
 * 3. Submit the task to the worker and block until the task is done
 * 4. If the worker fails, the <code>ParallelTaskAssigner</code> will ensure the worker node 
 * 	  won't go to the cluster and that the task previously retrieved from the job is put back on
 * 	  the job so that it can be processed by another worker node
 * 
 * Note: there are two main ways for the worker node to fail: 
 * 1.	Since there is no active connection to the scheduler, it might have failed before this class
 * 		tried to retrieve and connect to it. This will cause the initial connection attempt from this class
 * 		to fail, which will cause an exception, allowing this class to catch the exception and cope with the error.
 * 
 * 2.	Once the task is submitted, the worker node can fail while processing the task. This also will cause the read 
 * 		attempt from this class to fail while waiting for feedback. Again, this will cause an exception allowing the class
 * 		once again to catch the exception and cope with the error. 
 *
 */
public class ParallelTaskAssigner extends Thread{

	private String className;
	public WorkerNode n;
	private int jobId;
	private int taskIdStart;
	private int _1_task = 1;
	private DataOutputStream dos;
	private JobBox job;
    public Cluster cluster = null;
	
    /**
     * Initalize the class
     * @param job
     * 			The packaged job <code>JobBox</code>
     * @param cluster
     * 			The cluster containing worker nodes. Note: by the time this is called,
     * 			we are guaranteed there is a free worker node since the <code>QueueMonitor</code>
     * 			calls this method only once it verifies the availability of a worker node.
     */
	public ParallelTaskAssigner(
				JobBox job,
				Cluster cluster)
	{
		this.className = job.getClassName();
		this.cluster = cluster;
		this.n = this.cluster.getFreeWorkerNode();
		this.jobId = job.getJobId();
		this.taskIdStart = job.getNextTask();
		this.dos = job.getStream();
		this.job = job;
		
		//put back the job
		QueueMonitor.addJob(job);
	}
	
	/**
	 * The run method connects to the worker node and submits a job. 
	 * Once a job is submitted, it will wait for it until the worker is done. 
	 * 
	 * Once a task is finished, it will notify the Job that a task is completed and put
	 * back the worker node in the cluster.
	 * 
	 * If the worker fails for some reason, the task is put back on the Job and worker node
	 * is NOT put back in the cluster since it is assumed dead.  
	 */
	 public void run() {

		 //we want to track if we have done the job (even if the socket closing dies)
		 boolean task_done = false;
		 
		 //This try/catch block will allow us to cope with the worker node connection dying either
		 //1.	At initial connection (which means it was a stale worker in the cluster)
		 //2.	After initial connection/(task submission) which means it died while processing our task
		 try
		 {
	         //assign the tasks to the worker
	         Socket workerSocket = new Socket(n.addr, n.port);
	         DataInputStream wis = new DataInputStream(workerSocket.getInputStream());
	         DataOutputStream wos = new DataOutputStream(workerSocket.getOutputStream());
	         
	         wos.writeInt(Opcode.new_tasks);
	         wos.writeInt(jobId);
	         wos.writeUTF(className);
	         wos.writeInt(taskIdStart);
	         wos.writeInt(_1_task);
	         wos.flush();
	
	       //repeatedly process the worker's feedback
			while(wis.readInt() == Opcode.task_finish) {
			  synchronized (dos) {
				  dos.writeInt(Opcode.job_print);
				  dos.writeUTF("task "+wis.readInt()+" finished on worker "+n.id);
				  dos.flush();
			  }
			}
			task_done = true;
	         //disconnect and free the worker
	         wis.close();
	         wos.close();
	         workerSocket.close();
	         
	         //put back the cluster we used because if we reached here, it means we survived
	         this.cluster.addFreeWorkerNode(n);
		 }
		 catch(Exception e)
		 {
			 System.out.println("*****Lost Worker: " + this.n.id);
			 e.printStackTrace();
		 }
		 
		 //make sure the task is done, if not we need to put it back in the job package
		 if(task_done)
		 {
			job.taskDone(); 
		 }
		 else
		 {
			 //put back the task
			 job.putBackTask(taskIdStart);
		 }
	 }

}
