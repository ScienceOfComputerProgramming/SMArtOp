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
package matrix.adapterDistribution;

import java.util.Date;

import matrix.matrixImpl.Matrix;
import matrix.reconstructionStrategy.MatrixReconstructionStrategy;

/**
 * This class provides a helper for implementing the asynchronous result matrix reconstruction.
 * @author Antonela Tommasel
 *
 */
public class AsynchronousTaskResultListener {

	private int taskNumber = 0;
	private int complete = 0;
	
	protected Matrix res = null;
	protected MatrixReconstructionStrategy matrixReconstruction;
	
	/**
	 * @param taskNumber the total number of tasks to be executed
	 * @param res the matrix where to build the result
	 * @param matrixReconstructionStrategy strategy to build the result matrix
	 */
	public AsynchronousTaskResultListener(int taskNumber,Matrix res,MatrixReconstructionStrategy matrixReconstructionStrategy) {
		this.taskNumber = taskNumber;
		this.complete = 0;
		this.res = res;
		this.matrixReconstruction = matrixReconstructionStrategy;
	}

	/**
	 * Informs that a round of tasks was completed. If all tasks has been completed this method
	 * notify for the {@link #waitUntilComplete()}
	 * @param round
	 */
	protected synchronized void update(int round) {
		this.complete = complete+round;	
		System.out.println("Complete: " + complete + " out of " + taskNumber +" -- "+new Date());
		if(isJobComplete()){
			res.updateSparsity();
			System.out.println("updateSparsity():" + getResultMatrix().getSparsity());
			this.notifyAll();
		}		
	}
	
	/**
	 * Returns <tt>true</tt> if all tasks have finished.
	 * @return true is all tasks finished their execution
	 */
	public synchronized boolean isJobComplete() {
		return taskNumber == complete;
	}

	/**
	 * Returns the job result as defined in 
	 * {@link AdapterDistribution#createJob(int, Matrix, MatrixReconstructionStrategy)}
	 * @return the result of the operation
	 */
	public Matrix getResultMatrix(){
		return res;
	}
	
	/**
	 * Blocks the execution until all tasks have been completed
	 */
	public synchronized void waitUntilComplete(){
		while(!this.isJobComplete())
			try {
				wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
	}

	/**
	 * @param number the number of tasks to be executed
	 */
	public void setTaskNumber(int number){
		taskNumber = number;
	}
	
}
