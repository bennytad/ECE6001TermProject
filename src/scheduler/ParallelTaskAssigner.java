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
	private int numTasks;
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
		this.numTasks = 1;
		this.dos = job.getStream();
		this.job = job;
		
		//if it has more tasks, put it back
		if(taskIdStart!=0)
        {
       	 QueueMonitor.addJob(job);
        }
	}
	
	 public void run() {

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
	         wos.writeInt(numTasks);
	         wos.flush();
	
	       //repeatedly process the worker's feedback
			while(wis.readInt() == Opcode.task_finish) {
			  synchronized (dos) {
				  dos.writeInt(Opcode.job_print);
				  dos.writeUTF("task "+wis.readInt()+" finished on worker "+n.id);
				  dos.flush();
			  }
			}
			job.taskDone();
	         //disconnect and free the worker
	         wis.close();
	         wos.close();
	         workerSocket.close();
	         
	         //put back the cluster we used
	         this.cluster.addFreeWorkerNode(n);
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
	 }

}
