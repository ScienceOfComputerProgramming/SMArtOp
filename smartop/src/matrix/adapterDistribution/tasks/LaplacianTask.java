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
 * This class calculates the values of the Laplacian matrix L[i,i] for startRow <= i < endRow.
 * It is used for distributing the Laplacian operation.
 * 
 * <p>It requires the following data to be shared by means of a {@link IDataShared}
 * <ul>
 * <li>The input {@link Matrix} associated to the key <tt>"A"</tt></li>
 * <li>A {@link FactoryMatrix} for creating the result matrix associated to the key <tt>"factory"</tt></li>
 * </ul>
 * </p>
 * 
 * @see MatrixComputationSparseDistributed#laplacian(Matrix)
 * @author Antonela Tommasel
 *
 */
public class LaplacianTask extends ITask{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1236378936007624403L;
	private int startRow = 0;
	private int endRow = 0;
	private int colSize = 0;
	
	/**
	 * Creates task that calculates the Laplacian values for rows ranging between startRow and endRow-1.
	 * The matrix is shared using {@link IDataShared} and the key <tt>"A"</tt>.
	 * 
	 * @param startRow coordinate of the first row to compute the Laplacian
	 * @param endRow coordinate of the last row to compute the Laplacian
	 * @param colSize column size of the matrix to compute the laplacian
	 */
	public LaplacianTask(int startRow, int endRow, int colSize) {
		this.startRow = startRow;
		this.endRow = endRow;
		this.colSize = colSize;
		
		this.res = FactoryMatrixHolder.getFactory().createMatrix(endRow-startRow, colSize);
	}
	
	@Override
	public void run() {
		
		this.res=((FactoryMatrix)dataShared.getValue(MatrixComputationSparseDistributed.FACTORY)).createMatrix(1,colSize);
		
		Matrix AMatrix = (Matrix) dataShared.getValue(MatrixComputationSparseDistributed.LAPLACIAN_MATRIX);
		
		for (int row = startRow; row < endRow; row++) {
			float sum = 0;
			for (int j = 0; j < colSize; j++) {
				sum += AMatrix.getValue(j, row);
			}
			res.setValue(0, row, sum);
		}
		
	}

}
