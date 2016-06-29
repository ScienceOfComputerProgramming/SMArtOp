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
package matrix.reconstructionStrategy;

import matrix.adapterDistribution.AdapterDistribution;
import matrix.adapterDistribution.ITask;
import matrix.matrixImpl.Matrix;

/**
 * Defines the interface of a function that maps the results of executing each {@link ITask} in a job (as defined in 
 * {@link AdapterDistribution#createJob(int, Matrix, MatrixReconstructionStrategy)}) 
 * into a single matrix.
 * @author Antonela Tommasel
 *
 */
public interface MatrixReconstructionStrategy {
	
	/**
	 * This method is called after each {@link ITask} in a job is finished to add its partial result to the final result. 
	 * @param res the matrix to be filled
	 * @param taskResult the result matrix of a determined task
	 */
	public void buildMatrix(Matrix res,Matrix taskResult);
	
}
