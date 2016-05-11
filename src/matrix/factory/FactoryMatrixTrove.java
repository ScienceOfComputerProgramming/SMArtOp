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
package matrix.factory;

import matrix.matrixImpl.Matrix;
import matrix.matrixImpl.MatrixSparseTrove;

/**
 * This class creates matrices represented as TIntObjectHashMap<TIntFloatHashMap>. Such classes are 
 * Trove maps that provide efficient implementation for storing primitive types.
 * The first map represents the rows, while the second contains the values for a column in a given row. 
 * If one of both maps returns null for a given position, the values of such position is assumed to be 0.
 * @see MatrixSparseTrove
 * @author Antonela Tommasel
 *
 */
public class FactoryMatrixTrove implements FactoryMatrix{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4034035288405621224L;

	@Override
	public Matrix createMatrix(int rowSize, int colSize) {
		return new MatrixSparseTrove(rowSize, colSize);
	}

	@Override
	public Matrix createMatrix(Matrix toCopy) {
		return new MatrixSparseTrove(toCopy);
	}

	@Override
	public Matrix createIdentity(int rowSize) {
		Matrix identity = new MatrixSparseTrove(rowSize,rowSize);
		for(int i=0;i<rowSize;i++)
			identity.setValue(i, i, 1);
		return identity;
	}

}
