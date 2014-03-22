package scheduler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import scheduler.Scheduler.WorkerNode;
import common.Opcode;

public class ParallelTaskAssigner extends Thread{

	private String className;
	public WorkerNode n;
	private int jobId;
	private int taskIdStart;
	private int numTasks;
	private DataOutputStream dos;
	private CountDownLatch doneSignal;
	
	public ParallelTaskAssigner(
				String className, 
				WorkerNode n, 
				int jobId, 
				int taskIdStart, 
				int numTasks, 
				DataOutputStream dos, 
				CountDownLatch doneSignal)
	{
		this.className = className;
		this.n = n;
		this.jobId = jobId;
		this.taskIdStart = taskIdStart;
		this.numTasks = numTasks;
		this.dos = dos; 
		this.doneSignal = doneSignal;
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
	
	         //disconnect and free the worker
	         wis.close();
	         wos.close();
	         workerSocket.close();
	         doneSignal.countDown();
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
	 }

}
