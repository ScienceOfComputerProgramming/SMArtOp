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
package matrix.distributionPolicy;

import matrix.matrixComp.MatrixComputationSparseDistributed;
import matrix.matrixImpl.Matrix;

/**
 * This interface defines the protocol for implementing strategies
 * to decide in how many task an arithmetic operation should be divided into.
 * Each strategy must define three sub-strategies, one per each of the following operations:
 * 
 * <ul>
 * <li>Addition ({@link MatrixComputationSparseDistributed#add(Matrix, Matrix)}) and Subtraction ({@link MatrixComputationSparseDistributed#subtract(Matrix, Matrix)})</li>
 * <li>Multiplication ({@link MatrixComputationSparseDistributed#multiply(Matrix, Matrix)})</li>
 * <li>Laplacian ({@link MatrixComputationSparseDistributed#laplacian(Matrix)})</li>
 * </ul>
 * 
 * Each sub-strategy returns an integer called PARALLEL_FACTOR that is the approximated number of task in which the operation should be divided into. 
 * In some cases, the actual number of generated sub task could be PARRALEL_FACTOR + 1
 * @author Antonela Tommasel
 *
 */
public interface TaskSplitPolicy {

	/**
	 * Returns the parallel factor for add and subtract operations.
	 * @param left the left summand/minuend of the operation
	 * @param right the right summand/subtrahend of the operation
	 * @return the PARALLEL_FACTOR used for computing the work to be assigned to each task
	 */
	int getTasksForAddOrSubstract(Matrix left, Matrix right);
	
	/**
	 * Returns the parallel factor for multiply operation.
	 * @param left the left matrix of the multiplication
	 * @param right the right matrix of the multiplication
	 * @return the PARALLEL_FACTOR used for computing the work to be assigned to each task
	 */
	int getTasksForMultiply(Matrix left, Matrix right);
		
	/**
	 * Returns the parallel factor for laplacian operation.
	 * @param matrix the matrix to compute the Laplacian of
	 * @return  the PARALLEL_FACTOR used for computing the work to be assigned to each task
	 */
	int getTasksForLaplacian(Matrix matrix);

}
