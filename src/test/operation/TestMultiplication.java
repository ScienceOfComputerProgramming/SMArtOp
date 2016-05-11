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
package test.operation;

import matrix.factory.FactoryMatrixHolder;
import matrix.factory.FactoryMatrixSparseHash;
import matrix.factory.FactoryMatrixTrovePar;
import matrix.matrixComp.MatrixComputation;
import matrix.matrixComp.MatrixComputationFD;
import matrix.matrixComp.MatrixComputationSparsePar;
import matrix.matrixImpl.Matrix;

/**
 * Class created with tesing purposes
 * @author Antonela Tommasel
 *
 */
public class TestMultiplication {

	/**
	 * @param args no parameters are needed
	 */
	public static void main(String[] args) {
		
		//setting of the matrix holder
		FactoryMatrixHolder.setFactory(new FactoryMatrixSparseHash());
		
		Matrix A = FactoryMatrixHolder.getFactory().createMatrix(3, 3);
		
		A.setValue(0, 0, 1);
		A.setValue(0, 1, 2);
		A.setValue(0, 2, 3);
		A.setValue(1, 0, 4);
		A.setValue(1, 1, 5);
		A.setValue(1, 2, 6);
		A.setValue(2, 0, 7);
		A.setValue(2, 1, 8);
		A.setValue(2, 2, 9);
		
		Matrix B = FactoryMatrixHolder.getFactory().createMatrix(3, 3);
		
		B.setValue(0, 0, 11);
		B.setValue(0, 1, 12);
		B.setValue(0, 2, 13);
		B.setValue(1, 0, 14);
		B.setValue(1, 1, 15);
		B.setValue(1, 2, 16);
		B.setValue(2, 0, 17);
		B.setValue(2, 1, 18);
		B.setValue(2, 2, 19);
		
		MatrixComputation algebraFD = new MatrixComputationFD();
		
		System.out.println(algebraFD.multiply(A, B));
				
		//Changing the matrix factory
		FactoryMatrixHolder.setFactory(new FactoryMatrixTrovePar());
		
		Matrix Acopy = FactoryMatrixHolder.getFactory().createMatrix(A);
		Matrix Bcopy = FactoryMatrixHolder.getFactory().createMatrix(B);
		
		System.out.println(algebraFD.multiply(Acopy, Bcopy));
		
		//Changing the matrix computation
		MatrixComputation algebraPar = new MatrixComputationSparsePar();

		System.out.println(algebraPar.multiply(Acopy, Bcopy));
	}
}
