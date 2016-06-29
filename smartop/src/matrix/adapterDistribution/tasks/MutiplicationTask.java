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

import java.util.Collection;

import matrix.adapterDistribution.IDataShared;
import matrix.adapterDistribution.ITask;
import matrix.factory.FactoryMatrix;
import matrix.matrixComp.MatrixComputationSparseDistributed;
import matrix.matrixImpl.Matrix;

/**
 * This task is used for distributing the matrix multiplication operation. It only multiplies the non-zero rows
 * in the left matrix that is set in the constructor.
 * 
 * <p>It requires the following data to be shared by means of a {@link IDataShared}
 * <ul>
 * <li>The {@link Matrix} at the right of the multiplication associated to the key <tt>"sec"</tt></li>
 * <li>A {@link FactoryMatrix} for creating the result matrix associated to the key <tt>"factory"</tt></li>
 * </ul>
 * </p>
 * @see MatrixComputationSparseDistributed#multiply(Matrix, Matrix)
 * @author Antonela Tommasel
 *
 */
public class MutiplicationTask extends ITask{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Matrix leftMatrix = null;

	/**
	 * Creates the multiplication task.
	 * @param leftMatrix the rows of the left Matrix to multiply
	 */
	public MutiplicationTask(Matrix leftMatrix) {
		this.leftMatrix = leftMatrix;
	}

	@Override
	public void run() {
		Matrix rightMatrix = (Matrix) dataShared.getValue(MatrixComputationSparseDistributed.SECOND_MATRIX);
		res=((FactoryMatrix)dataShared.getValue(MatrixComputationSparseDistributed.FACTORY)).createMatrix(leftMatrix.rowSize(), rightMatrix.columnSize());
		Collection<Integer> filas=leftMatrix.getRows();

		for(Integer row:filas){

			Collection<Integer> col = leftMatrix.getColumns(row);

			for(int j=0; j<rightMatrix.columnSize(); j++){
				float aux=0;
				for(int k:col)
					aux+=leftMatrix.getValue(row,k)*rightMatrix.getValue(k, j);
				if (aux != 0) 
					res.setValue(row, j, aux);
			}
		}
	}

}
