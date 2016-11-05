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

import java.util.logging.Level;

import matrix.adapterDistribution.Configuration;
import matrix.adapterDistribution.impl.JPPF.AdapterDistributionJPPFImp;
import matrix.distributionPolicy.TaskSplitPolicyMode;
import matrix.factory.FactoryMatrixHolder;
import matrix.factory.FactoryMatrixSparseHash;
import matrix.factory.FactoryMatrixSparseHashPar;
import matrix.factory.FactoryMatrixTrovePar;
import matrix.matrixComp.MatrixComputation;
import matrix.matrixComp.MatrixComputationFD;
import matrix.matrixComp.MatrixComputationSparseDistributed;
import matrix.matrixComp.MatrixComputationSparsePar;
import matrix.matrixImpl.Matrix;

/**
 * Class created with tesing purposes
 * @author Antonela Tommasel
 *
 */
public class TestMultiplicationDistributed {

	/**
	 * @param args no parameters are needed
	 */
	public static void main(String[] args) {
			
		//setting of the matrix holder
		FactoryMatrixHolder.setFactory(new FactoryMatrixSparseHashPar());
		
		Configuration.logger.setLevel(Level.INFO);
		
		Matrix A = FactoryMatrixHolder.getFactory().createMatrix("example-data/matrixA.csv");
		
		Matrix B = FactoryMatrixHolder.getFactory().createMatrix("example-data/matrixB.csv");
		
		MatrixComputation algebraJPPF = new MatrixComputationSparseDistributed(new AdapterDistributionJPPFImp());
		
		//Remember that each setting has a defult value
		Configuration.policy = new TaskSplitPolicyMode();
		Configuration.CLUSTER_PHYSICAL_CORES = 1;
		
		Matrix C = algebraJPPF.multiply(A, B);
		
		System.out.println(C);
		
		MatrixComputation algebra = new MatrixComputationFD();
		C = algebra.multiply(A, B);
		
		System.out.println(C);
		
	}
	
}
