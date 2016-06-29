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

import java.util.Collection;

import matrix.matrixComp.MatrixComputationSparseDistributed;
import matrix.matrixImpl.Matrix;

/**
 * Defines per row reconstruction strategy used in the following operations.
 * @see MatrixComputationSparseDistributed#add(Matrix, Matrix)
 * @see MatrixComputationSparseDistributed#multiply(Matrix, Matrix)
 * @see MatrixComputationSparseDistributed#subtract(Matrix, Matrix)
 * @author Antonela Tommasel
 *
 */
public class RowMatrixReconstructionStrategy implements MatrixReconstructionStrategy{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void buildMatrix(Matrix res, Matrix taskResult) {
			Collection<Integer> filas=taskResult.getRows();
			for (int k:filas){
//				System.out.println(k);
				res.setRow(k, taskResult);
			}
	
	}

}
