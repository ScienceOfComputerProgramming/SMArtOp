package matrix.adapterDistribution.impl.JPPF;

import org.jppf.node.event.TaskExecutionEvent;
import org.jppf.node.event.TaskExecutionListener;
import org.jppf.node.protocol.Task;

import matrix.adapterDistribution.AsynchronousTaskResultListener;
import matrix.matrixImpl.Matrix;
import matrix.reconstructionStrategy.MatrixReconstructionStrategy;

/**
 * This class extends {@link AsynchronousTaskResultListener} associated with {@link AdapterDistributionJPPFImp}. 
 * This class also implements {@link TaskExecutionListener} from JPPF.
 * @author Antonela Tommasel
 *
 */
public class AsynchronousTaskListenerJPPF extends AsynchronousTaskResultListener implements TaskExecutionListener{

	/**
	 * @param taskNumber the total number of tasks to be executed
	 * @param res the matrix where to build the result
	 * @param matrixReconstructionStrategy strategy to build the result matrix
	 */
	public AsynchronousTaskListenerJPPF(int taskNumber, Matrix res,MatrixReconstructionStrategy matrixReconstructionStrategy) {
		super(taskNumber, res, matrixReconstructionStrategy);
	}
	

	@Override
	public void taskExecuted(TaskExecutionEvent arg0) {
		Task<?> jt = arg0.getTask();
		Matrix taskResult=(Matrix)jt.getResult();
		matrixReconstruction.buildMatrix(res, taskResult);
		update(1);
		
		
	}

	@Override
	public void taskNotification(TaskExecutionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
