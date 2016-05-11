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
import matrix.matrixImpl.MatrixSparseHash;

/**
 * This class creates matrices represented as HashMap<Integer,HashMap<Integer,Float>>
 * The first map represents the rows, while the second contains the values for a column in a given row. 
 * If one of both maps returns null for a given position, the values of such position is assumed to be 0.
 * @see MatrixSparseHash
 * @author Antonela Tommasel
 *
 */
public class FactoryMatrixSparseHash implements FactoryMatrix {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3526181635365641362L;

	public Matrix createMatrix(int i, int j){
		return new MatrixSparseHash(i,j);
	}

	@Override
	public Matrix createMatrix(Matrix toCopy) {
		return new MatrixSparseHash(toCopy);
	}

	@Override
	public Matrix createIdentity(int rowSize) {
		Matrix identity = new MatrixSparseHash(rowSize,rowSize);
		for(int i=0;i<rowSize;i++)
			identity.setValue(i, i, 1);
		return identity;
	}
	
}
