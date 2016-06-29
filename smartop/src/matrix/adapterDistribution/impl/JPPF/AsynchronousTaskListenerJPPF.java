/* 
 *  Copyright 2016 ISISTAN - UNICEN - CONICET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package matrix.adapterDistribution.impl.JPPF;

import matrix.adapterDistribution.AsynchronousTaskResultListener;
import matrix.matrixImpl.Matrix;
import matrix.reconstructionStrategy.MatrixReconstructionStrategy;

import org.jppf.client.event.TaskResultEvent;
import org.jppf.client.event.TaskResultListener;
import org.jppf.server.protocol.JPPFTask;

/**
 * This class extends {@link AsynchronousTaskResultListener} associated with {@link AdapterDistributionJPPFImp}. 
 * This class also implements {@link TaskResultListener} from JPPF.
 * @author Antonela Tommasel
 *
 */
public class AsynchronousTaskListenerJPPF extends AsynchronousTaskResultListener implements TaskResultListener{

	/**
	 * @param taskNumber the total number of tasks to be executed
	 * @param res the matrix where to build the result
	 * @param matrixReconstructionStrategy strategy for building the result matrix
	 */
	public AsynchronousTaskListenerJPPF(int taskNumber, Matrix res,MatrixReconstructionStrategy matrixReconstructionStrategy) {
		super(taskNumber, res, matrixReconstructionStrategy);
	}
	
	@Override
	public void resultsReceived(TaskResultEvent arg0) {
		
		for(JPPFTask jt:arg0.getTaskList()){			
			Matrix taskResult=(Matrix)jt.getResult();
			matrixReconstruction.buildMatrix(res, taskResult);
		}
		update(arg0.getTaskList().size());
		
	}

}
