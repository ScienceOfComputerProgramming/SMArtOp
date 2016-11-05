package matrix.adapterDistribution.impl.JPPF;

import matrix.adapterDistribution.IDataShared;
import matrix.adapterDistribution.ITask;
import matrix.matrixImpl.Matrix;

import org.jppf.client.JPPFJob;
import org.jppf.node.protocol.AbstractTask;

/**
 * This class adapts {@link ITask} to be run as part of a {@link JPPFJob}.
 * @author Anto
 *
 */
public class TaskJPPF extends AbstractTask<Matrix>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8052135224252452492L;
	ITask task;
	
	/**
	 * Creates a new task for JPPD that wraps a {@link ITask}
	 * @param task task to be executed
	 */
	public TaskJPPF(ITask task){
		this.task = task;
	}
	
	@Override
	public void run() {
		try{
			IDataShared ds = new DataSharedJPPFImp(getDataProvider());
			task.setDataShared(ds);
			task.run();
		}
		catch(Exception e){
			setThrowable(e);
			return;
		}
		setResult(task.getResult());
	}

}
