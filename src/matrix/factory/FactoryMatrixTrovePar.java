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
import matrix.matrixImpl.MatrixSparseTrovePar;

/**
 * The matrix constructed by this factory is a subclass of the matrix created by {@link FactoryMatrixTrove}.
 * The only difference is that this sub class implements {@link Matrix#fastMult(Matrix)} and {@link Matrix#fastMult(float)}
 * in a multi-threaded fashion.
 * @see MatrixSparseTrovePar
 * @author Antonela Tommasel
 *
 */
public class FactoryMatrixTrovePar implements FactoryMatrix{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5746045203835894200L;

	@Override
	public Matrix createMatrix(int rowSize, int colSize) {
		return new MatrixSparseTrovePar(rowSize,colSize);
	}

	@Override
	public Matrix createMatrix(Matrix toCopy) {
		return new MatrixSparseTrovePar(toCopy);
	}

	@Override
	public Matrix createIdentity(int rowSize) {
		Matrix identity = new MatrixSparseTrovePar(rowSize,rowSize);
		for(int i=0;i<rowSize;i++)
			identity.setValue(i, i, 1);
		return identity;
	}

}
