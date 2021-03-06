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
package matrix.adapterDistribution.tasks;

import matrix.adapterDistribution.IDataShared;
import matrix.adapterDistribution.ITask;
import matrix.factory.FactoryMatrix;
import matrix.factory.FactoryMatrixHolder;
import matrix.matrixComp.MatrixComputationSparseDistributed;
import matrix.matrixImpl.Matrix;

/**
 * This class implements the task of subtracting two sub-matrices. The rows in the sub-matrices range 
 * between startRow and endRow. It is used for distributing the operation of subtracting two Matrix.
 * 
 * <p>It requires the following data to be shared by means of a {@link IDataShared}
 * <ul>
 * <li>A {@link FactoryMatrix} for creating the result matrix associated to the key <tt>"factory"</tt></li>
 * </ul>
 * </p> 
 * @see MatrixComputationSparseDistributed#subtract(Matrix, Matrix)
 * @author Antonela Tommasel
 */
public class SubtractRowTask extends ITask{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8723497650888249891L;
	
	protected int startRow = 0;
	protected int endRow = 0;
	protected int colSize = 0;
	protected Matrix leftSubMatrix = null;
	protected Matrix rightSubMatrix = null;
	
	/**
	 * Creates the task that calculates the subtract values for rows between startRow and endRow-1.
	 * @param startRow coordinate of the first row to add
	 * @param endRow coordinate of the last row to add
	 * @param leftSubMatrix left minuend of the operation
	 * @param rightSubMatrix subtrahend of the operation
	 */
	public SubtractRowTask(int startRow, int endRow, Matrix leftSubMatrix, Matrix rightSubMatrix) {
		this.startRow = startRow;
		this.endRow = endRow;
		this.colSize = leftSubMatrix.columnSize();
		this.leftSubMatrix = leftSubMatrix;
		this.rightSubMatrix = rightSubMatrix;
	
	}
	
	@Override
	public void run() {
		
		this.res = FactoryMatrixHolder.getFactory().createMatrix(leftSubMatrix.rowSize(), leftSubMatrix.columnSize());
		
		for (int row = startRow; row < endRow; row++) {
			for (int j = 0; j < colSize; j++) {
				float result = leftSubMatrix.getValue(row, j) - rightSubMatrix.getValue(row, j);
				if (result != 0.0f)
					res.setValue(row, j, result);
			}
		}
	}

}
