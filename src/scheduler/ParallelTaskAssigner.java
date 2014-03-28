package scheduler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import scheduler.Scheduler.Cluster;
import scheduler.Scheduler.WorkerNode;
import common.Opcode;

public class ParallelTaskAssigner extends Thread{

	private String className;
	public WorkerNode n;
	private int jobId;
	private int taskIdStart;
	private int _1_task = 1;
	private DataOutputStream dos;
	private JobBox job;
    public Cluster cluster = null;
	

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
	
	 public void run() {

		 //we want to track if we have done the job (even if the socket closing dies)
		 boolean task_done = false;
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
