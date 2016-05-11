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

import matrix.adapterDistribution.Configuration;
import matrix.matrixImpl.Matrix;
import matrix.matrixImpl.MatrixFloat;
import matrix.matrixImpl.MatrixSparseHash;
import matrix.matrixImpl.MatrixThreshold;

/**
 * This class creates a matrix that dynamically changes it representation
 * between {@link MatrixFloat} and {@link MatrixSparseHash}
 * @see MatrixThreshold
 * @author Antonela Tommasel
 *
 */
public class FactoryMatrixThreshold implements FactoryMatrix {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 463816389340234059L;
	
	private float threshold;
	
	/**
	 * Creates the factory with a threshold defined in {@link Configuration}.
	 */
	public FactoryMatrixThreshold() {
		this(Configuration.sparsenessThreshold);
	}

	/**
	 * Creates a factory.
	 * @param threshold the switch threshold between implementations.
	 */
	private FactoryMatrixThreshold(float threshold) {
		this.threshold = threshold;
	}

	public Matrix createMatrix(int i, int j){
		return new MatrixThreshold(i,j,threshold);
	}

	@Override
	public Matrix createMatrix(Matrix toCopy) {
		return new MatrixThreshold(toCopy,threshold);
	}

	@Override
	public Matrix createIdentity(int rowSize) {
		Matrix identity = new MatrixThreshold(rowSize,rowSize,threshold);
		for(int i=0;i<rowSize;i++)
			identity.setValue(i, i, 1);
		return identity;
	}

}
