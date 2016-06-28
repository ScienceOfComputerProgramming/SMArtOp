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
 * Example of how to load and save a Matrix from a CSV file.
 * @author Anto
 *
 */
public class TestLoadingMatrix {

	/**
	 * @param args no parameters are needed
	 */
	public static void main(String[] args) {

		
		//setting of the matrix holder
		FactoryMatrixHolder.setFactory(new FactoryMatrixSparseHash());
		
		//Manually creating a Matrix
		Matrix A = FactoryMatrixHolder.getFactory().createIdentity(3);
			
		//Saving the Matrix to disk
		A.saveToCSV("matrixA.csv");
		
		//Loading the Matrix
		Matrix B = FactoryMatrixHolder.getFactory().createMatrix("matrixA.csv");
		
		System.out.println(B);
		
	}
	
}
