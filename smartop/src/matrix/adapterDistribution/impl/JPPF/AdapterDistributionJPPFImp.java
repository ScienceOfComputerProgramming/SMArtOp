package matrix.adapterDistribution.impl.JPPF;

import java.util.List;

import matrix.adapterDistribution.AdapterDistribution;
import matrix.adapterDistribution.Configuration;
import matrix.adapterDistribution.IDataShared;
import matrix.adapterDistribution.ITask;
import matrix.matrixImpl.Matrix;
import matrix.reconstructionStrategy.MatrixReconstructionStrategy;

import org.jppf.JPPFException;
import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.node.protocol.JPPFJobSLA;
import org.jppf.node.protocol.MemoryMapDataProvider;
import org.jppf.node.protocol.Task;

/**
 * Implements an adapter for JPPF
 * @author Antonela Tommasel
 *
 */
public class AdapterDistributionJPPFImp implements AdapterDistribution{

	transient IDataShared datashared;

	private JPPFJob job;

	static private JPPFClient client = null;
	static private Object lock = new Object();
	static private int counter = 0; //Reference counter for closing the connection when there are no more adapters

	Matrix res;
	MatrixReconstructionStrategy matrixReconstruction;
	
	/**
	 * class constructor
	 */
	public AdapterDistributionJPPFImp(){
		datashared = new DataSharedJPPFImp(new MemoryMapDataProvider());
		synchronized (lock) {
			if(client==null)
				client = new JPPFClient();
			counter++;
		}
	}

	@Override
	public void createJob(int taskNumber, Matrix res,MatrixReconstructionStrategy matrixReconstructionStrategy) {

		job = new JPPFJob();
		job.setDataProvider(((DataSharedJPPFImp)datashared).getDataProvider());
//		job.addJobListener((AsynchronousTaskListenerJPPF)listener);

		this.res = res;
		this.matrixReconstruction = matrixReconstructionStrategy; 
		
		if(Configuration.MAX_NODOS!=0){
			JPPFJobSLA sla=new JPPFJobSLA();
			sla.setMaxNodes(Configuration.MAX_NODOS);
			job.setSLA(sla);
			System.out.println("MaxNodos Job -- "+job.getSLA().getMaxNodes());
		}
	}

	@Override
	public void addTask(ITask task) {

		TaskJPPF t = new TaskJPPF(task);

		try {
			job.add(t);
		} catch (JPPFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void submit() {
		try {
			client.submitJob(job);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public IDataShared getDataShared() {
		return datashared;
	}

	@Override
	public void waitUntilComplete() {
		List<Task<?>> tareas = job.awaitResults();
		for(Task<?> jt:tareas){
			Matrix taskResult=(Matrix)jt.getResult();
			matrixReconstruction.buildMatrix(res, taskResult);
		}
			

	}

	@Override
	public void addTasks(List<ITask> task) {

		try {
			for(ITask t:task){
				job.add(new TaskJPPF(t));
			}
		} catch (JPPFException e) {
			e.printStackTrace();
		}

	}

	@Override
	public synchronized void close(){
		counter --;
		if(counter == 0){
			client.close();
			client=null;
		}

	}

}
