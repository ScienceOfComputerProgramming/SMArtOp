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

import java.io.Serializable;

import matrix.matrixImpl.Matrix;

/**
 * This interface defines the method for the matrix factories to create matrices with different implementations.
 * @author Antonela Tommasel
 *
 */
public interface FactoryMatrix extends Serializable{
	
	/**
	 * Creates a matrix of rowSize x colSize
	 * @param rowSize number of rows of the matrix to be created
	 * @param colSize number of columns of the matrix to be created
	 * @return the created matrix
	 */
	public Matrix createMatrix(int rowSize, int colSize);
	
	/** 
	 * Creates a Matrix by copying the values of the other Matrix
	 * @param toCopy Matrix to copy the values in the new Matrix
	 * @return the created matrix
	 */
	public Matrix createMatrix(Matrix toCopy);
	
	/** 
	 * Creates a Matrix by loading the Matrix from a file
	 * @param pathToCopy path to the file containing the Matrix
	 * @return the created matrix
	 */
	public Matrix createMatrix(String pathToCopy);
	
	/**
	 * Creates an Identity Matrix of rowSize x rowSize
	 * @param rowSize dimensionality of the Identity
	 * @return the Identity of dimensionality rowSize
	 */
	public Matrix createIdentity(int rowSize);

}
