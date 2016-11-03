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
import matrix.matrixComp.MatrixComputation;
import matrix.matrixComp.MatrixComputationFD;
import matrix.matrixImpl.Matrix;

/**
 * Using SMArtOp for the first time!
 * @author Antonela Tommasel
 *
 */
public class FirstUse {

	/**
	 * @param args no parameters are needed
	 */
	public static void main(String[] args) {
		
		FactoryMatrixHolder.setFactory(new FactoryMatrixSparseHash()); //setting the Factory class

		Matrix A = FactoryMatrixHolder.getFactory().createMatrix(2, 2); //creating two matrices
		Matrix B = FactoryMatrixHolder.getFactory().createMatrix(2, 2);
		
		A.setValue(0, 0, 1); //setting values
		A.setValue(0, 1, 2);
		A.setValue(1, 0, 4);
		A.setValue(1, 1, 5);

		B.setValue(0, 0, 11);
		B.setValue(0, 1, 12);
		B.setValue(1, 0, 14);
		B.setValue(1, 1, 15);
		
		MatrixComputation algebraFD = new MatrixComputationFD(); //defining how operations are performed
		
		Matrix C = algebraFD.multiply(A,B); //operating
		
		System.out.println(C); //printing the result
		
	}
	
}
