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
package matrix.matrixComp;

import java.util.logging.Level;

import matrix.adapterDistribution.Configuration;
import matrix.matrixImpl.Matrix;
import matrix.factory.FactoryMatrixHolder; 

/**
 * @author Antonela Tommasel
 *
 */

/** 
 * Provides the implementation for performing arithmetic operations between sparse matrices. All operations are performed in a single thread.
 * */
public abstract class MatrixComputationSparse implements MatrixComputation{

	protected String name = "delegate-matrix";

	@Override
	public Matrix transpose(Matrix m){
		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("transpose-"+name, m, null, null));
		Matrix aux = m.fastTrans();
		Configuration.logger.log(Level.INFO, Configuration.getLogTime("transpose-"+name,System.currentTimeMillis()-startTime, null, aux));
		return aux;
	}

	@Override
	public Matrix multiply(Matrix m, Matrix m1){
		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("multiply-"+name, m, m1, null));
		Matrix aux = m.fastMult(m1);
		Configuration.logger.log(Level.INFO, Configuration.getLogTime("multiply-"+name,System.currentTimeMillis()-startTime, null, aux));
		return aux;
	}

	@Override
	public Matrix multiplyByTranspose(Matrix m){
		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("multiply-by-transpose-"+name, m,null, null));
		Matrix m1=this.transpose(m);
		Matrix aux = this.multiply(m, m1);
		Configuration.logger.log(Level.INFO, Configuration.getLogTime("multiply-by-transpose-"+name,System.currentTimeMillis()-startTime, null, aux));
		return aux;
	}

	@Override
	public Matrix multiply(float alfa, Matrix m){
		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("multiply-scalar-"+name, m,null, null));
		Matrix aux = m.fastMult(alfa);
		Configuration.logger.log(Level.INFO, Configuration.getLogTime("multiply-scalar-"+name,System.currentTimeMillis()-startTime, null, aux));
		return aux;
	}

	@Override
	public Matrix invert(Matrix m){
		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("invert-"+name, m,null, null));
		if(m.isSquare() && m.isDiagonal()){
			Matrix inv = FactoryMatrixHolder.getFactory().createMatrix(m);
			inv.invertDiagonal();
			return inv;
		}
		Matrix inverse = solve(m);
		Configuration.logger.log(Level.INFO, Configuration.getLogTime("invert-"+name,System.currentTimeMillis()-startTime, null, inverse));
		return inverse;

	}

	private Matrix solve(Matrix m) {

		
		Matrix identity = FactoryMatrixHolder.getFactory().createIdentity(m.rowSize());
		if(m.isSquare()){ //exact inverse
			
			Matrix cholesky = m.fastCholesky();
			if(cholesky!=null){
				identity.findFastSolution(cholesky);
				return identity;
			}
			else{
				int [] piv = new int[m.rowSize()];
				
				long startTime = System.currentTimeMillis();
				Configuration.logger.log(Level.INFO,Configuration.getLogString("fastLU-"+name, m,null, null));
				
				Matrix LU = m.fastLU(piv);
				
				Configuration.logger.log(Level.INFO, Configuration.getLogTime("fastLU-"+name,System.currentTimeMillis()-startTime, null, LU));
				
				startTime = System.currentTimeMillis();
				Configuration.logger.log(Level.INFO,Configuration.getLogString("solutionLinearSystem-"+name, m,null, null));
				if(LU.isSingular())
					throw new IllegalArgumentException("Matrix is Singular!");
				Matrix inverse = FactoryMatrixHolder.getFactory().createMatrix(m.rowSize(), identity.columnSize());
				inverse.findFastSolution(LU,identity,piv);
				
				Configuration.logger.log(Level.INFO, Configuration.getLogTime("solutionLinearSystem-"+name,System.currentTimeMillis()-startTime, null, inverse));
				return inverse;
			}
		}
		else{ //pseudo-inverse
			float [] Rdiag = new float[m.columnSize()];
			
			long startTime = System.currentTimeMillis();
			Configuration.logger.log(Level.INFO,Configuration.getLogString("fastQR-"+name, m,null, null));
			
			Matrix QR = m.fastQR(Rdiag);
			
			Configuration.logger.log(Level.INFO, Configuration.getLogTime("fastQR-"+name,System.currentTimeMillis()-startTime, null, identity));
			
			startTime = System.currentTimeMillis();
			Configuration.logger.log(Level.INFO,Configuration.getLogString("solutionLeastSquares-"+name, m,null, null));
			if(QR.isSingular())
				throw new IllegalArgumentException("Matrix is Singular!");
			identity.findFastSolutionSquares(QR, Rdiag);
			
			Configuration.logger.log(Level.INFO, Configuration.getLogTime("solutionLeastSquares-"+name,System.currentTimeMillis()-startTime, null, identity));
			return identity;
		}
	}
	
	@Override
	public Matrix invertByCholesky(Matrix m) {
		
		if(m.rowSize()!=m.columnSize()){
			throw new IllegalArgumentException("Matrix must be square.");
		}
		
		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("invert-by-Cholesky-"+name, m,null, null));

		Matrix hermitian = multiplyByTranspose(m);
		Matrix cholesky = hermitian.fastCholesky();
		
		if(cholesky.isSingular())
			throw new IllegalArgumentException("Matrix is Singular!");
		
		Matrix identity = FactoryMatrixHolder.getFactory().createIdentity(m.rowSize());
		identity.findFastSolution(cholesky);
		identity = m.fastTrans().fastMult(identity);

		Configuration.logger.log(Level.INFO, Configuration.getLogTime("invert-by-Cholesky-"+name,System.currentTimeMillis()-startTime, null, identity));
		return identity;
	}
	
}
