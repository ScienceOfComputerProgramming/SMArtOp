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
package test.matrixCreation;

import matrix.factory.FactoryMatrixHolder;
import matrix.factory.FactoryMatrixSparseHash;
import matrix.matrixImpl.Matrix;

/**
 * Example of how to clone and print a Matrix.
 * @author Anto
 *
 */
public class TestCloningMatrix {

	/**
	 * @param args no parameters are needed
	 */
	public static void main(String[] args) {
		
		//setting of the matrix holder
		FactoryMatrixHolder.setFactory(new FactoryMatrixSparseHash());
		
		//creating the first Matrix
		Matrix A = FactoryMatrixHolder.getFactory().createMatrix(2, 2);
		
		A.setValue(0, 0, 1);
		A.setValue(0, 1, 2);
		A.setValue(1, 0, 4);
		A.setValue(1, 1, 5);
			
		System.out.println("Matrix A\n"+A);
		
		//cloning Matrix A in B
		Matrix B = FactoryMatrixHolder.getFactory().createMatrix(A);
		
		System.out.println("Matrix B\n"+B);
		
		
	}
	
}
