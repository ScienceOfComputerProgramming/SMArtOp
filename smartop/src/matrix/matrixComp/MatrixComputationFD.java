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
import matrix.factory.FactoryMatrixHolder;
import matrix.matrixImpl.Matrix;

/** 
 * Provides the implementation for performing arithmetic operations between dense float matrices. All operations are performed in a single thread.
 * @author Antonela Tommasel
 * */

public class MatrixComputationFD implements MatrixComputation{

	@Override
	public Matrix transpose(Matrix m) {

		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("transpose", m, null, null));

		Matrix aux=FactoryMatrixHolder.getFactory().createMatrix(m.columnSize(), m.rowSize());
		for(int i=0;i<m.rowSize();i++)
			for(int j=0;j<m.columnSize();j++)
				aux.setValue(j,i,m.getValue(i, j));

		Configuration.logger.log(Level.INFO, Configuration.getLogTime("transpose",System.currentTimeMillis()-startTime, null, aux));

		return aux;
	}

	@Override
	public Matrix multiply(Matrix m, Matrix m1) {

		if(m.columnSize()!=m1.rowSize())
			throw new ArrayIndexOutOfBoundsException("The number of columns of the left matrix does not match the number of rows of the right matrix");

		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("multiply-serial", m, m1, null));

		Matrix aux=FactoryMatrixHolder.getFactory().createMatrix(m.rowSize(),m1.columnSize()); 
		float sum=0;
		for(int k=0;k<m.rowSize();k++)
			for(int j=0;j<m1.columnSize();j++){
				sum=0;
				for(int i=0;i<m.columnSize();i++){
					sum+=m.getValue(k, i)*m1.getValue(i, j);
				}
				aux.setValue(k, j, sum);
			}

		Configuration.logger.log(Level.INFO, Configuration.getLogTime("multiply-serial",System.currentTimeMillis()-startTime, null, aux));

		return aux;
	}

	@Override
	public Matrix multiplyByTranspose(Matrix m){

		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("multiply-by-transpose-serial", m,null, null));

		Matrix aux=FactoryMatrixHolder.getFactory().createMatrix(m.rowSize(),m.rowSize());
		float sum;
		for(int k=0;k<m.rowSize();k++)
			for(int i=0;i<m.rowSize();i++){
				sum=0;
				for(int j=0;j<m.columnSize();j++){
					sum+=m.getValue(k, j)*m.getValue(i, j);
				}
				aux.setValue(k, i, sum);
			}

		Configuration.logger.log(Level.INFO, Configuration.getLogTime("multiply-by-transpose-serial",System.currentTimeMillis()-startTime, null, aux));
		return aux;
	}

	@Override
	public Matrix multiply(float alfa, Matrix m) {

		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("multiply-scalar-serial", m,null, null));

		Matrix aux=FactoryMatrixHolder.getFactory().createMatrix(m.rowSize(),m.columnSize());

		for(int i=0;i<m.rowSize();i++)
			for(int j=0;j<m.columnSize();j++)
				aux.setValue(i, j,alfa*m.getValue(i, j));

		Configuration.logger.log(Level.INFO, Configuration.getLogTime("multiply-scalar-serial",System.currentTimeMillis()-startTime, null, aux));

		return aux;
	}

	@Override
	public Matrix add(Matrix m, Matrix m1) {

		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("adding-serial", m,m1, null));

		Matrix aux=FactoryMatrixHolder.getFactory().createMatrix(m.rowSize(), m.columnSize());		
		for(int i=0;i<m.rowSize();i++)
			for(int j=0;j<m.columnSize();j++)
				aux.setValue(i, j, m.getValue(i, j)+m1.getValue(i, j));

		Configuration.logger.log(Level.INFO, Configuration.getLogTime("adding-serial",System.currentTimeMillis()-startTime, null, aux));

		return aux;
	}

	@Override
	public Matrix invert(Matrix m) {
		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("invert-serial", m,null, null));
		if(m.isSquare() && m.isDiagonal()){
			Matrix inv = invertDiagonal(m);
			if(inv==null)
				throw new IllegalArgumentException("Matrix m is singular.");
			return inv;
		}
		Matrix inverse = solve(m);
		Configuration.logger.log(Level.INFO, Configuration.getLogTime("invert-serial-",System.currentTimeMillis()-startTime, null, inverse));
		return inverse;

	}

	private Matrix solve(Matrix m) {

		Matrix inverse = null;
		Matrix identity = FactoryMatrixHolder.getFactory().createIdentity(m.rowSize());
		if(m.isSquare()){ //exact inverse

			//first try Cholesky Decomposition.. if that fails, try LU
			Matrix cholesky = solveCholesky(m);
			if(cholesky!=null){
				inverse = solve(cholesky,identity);
				System.out.println("solved by cholesky");
				return inverse;
			}
			else{
				int [] piv = new int[m.rowSize()];
				Matrix LU = solveLU(m,piv);
				if(LU.isSingular())
					throw new IllegalArgumentException("Matrix is Singular!");
				inverse = solve(LU,identity,piv);
				
			}
		}
		else{ //pseudo-inverse
			float [] Rdiag = new float[m.columnSize()];
			Matrix QR = solveQR(m,Rdiag);
			if(QR.isSingular())
				throw new IllegalArgumentException("Matrix is Singular!");
			inverse = solveLeastSquare(QR,identity,Rdiag);
		}

		return inverse;
	}

	/**
	 * Solves the inverse by computing the Cholesky decomposition
	 * @param cholesky L
	 * @param identity identity 
	 * @return the inverse matrix computed by the Cholesky method
	 */
	public Matrix solve(Matrix cholesky, Matrix identity) {

		Matrix X = FactoryMatrixHolder.getFactory().createMatrix(identity);
		int n = cholesky.rowSize();
		int nx = identity.columnSize();

		// Solve L*Y = B;
		for (int k = 0; k < n; k++) {
			for (int j = 0; j < nx; j++) {
				float aux = X.getValue(k,j);
				for (int i = 0; i < k ; i++) {
					aux -= X.getValue(i, j)*cholesky.getValue(k,i);
				}
				aux = aux / cholesky.getValue(k,k);
				X.setValue(k,j,aux);	
			}
		}

		// Solve L'*X = Y;
		for (int k = n-1; k >= 0; k--) {
			for (int j = 0; j < nx; j++) {
				float aux = X.getValue(k,j);
				for (int i = k+1; i < n ; i++) {
					aux -= X.getValue(i,j)*cholesky.getValue(i,k);
				}
				aux = aux / cholesky.getValue(k, k);
				X.setValue(k, j, aux);				
			}
		}
		return X;
	}

	/**
	 * Returns the Cholesky decomposition of a Matrix if it exists. 
	 * Otherwise, returns null.
	 * @param m matrix to decompose
	 * @return the Cholesky decomposition of Matrix m
	 */
	public Matrix solveCholesky(Matrix m) {

		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("Cholesky-serial", m,null, null));

		Matrix L = FactoryMatrixHolder.getFactory().createMatrix(m.rowSize(),m.rowSize());	
		boolean symetricPositiveDefinite = m.rowSize() == m.columnSize();

		int n = L.rowSize();
		// Main loop.
		for (int j = 0; j < n; j++) {

			float d = 0f;

			for (int k = 0; k < j; k++) {

				float s = 0f;
				for (int i = 0; i < k; i++) {
					s += L.getValue(k, i)*L.getValue(j, i);
				}
				s = (m.getValue(j,k) - s)/L.getValue(k, k);
				L.setValue(j, k,s);
				d = d + s*s;

				symetricPositiveDefinite = symetricPositiveDefinite & (m.getValue(k,j) == m.getValue(j,k)); 

				if(!symetricPositiveDefinite){
					return null;
				}

			}
			d = m.getValue(j, j) - d;

			symetricPositiveDefinite = symetricPositiveDefinite & (d > 0.0);

			if(!symetricPositiveDefinite){
				return null;
			}

			L.setValue(j,j,(float)Math.sqrt(Math.max(d,0.0)));
		}
		
		Configuration.logger.log(Level.INFO, Configuration.getLogTime("Cholesky-serial-",System.currentTimeMillis()-startTime, null, L));

		return L;
	}

	/**
	 * Least squares solution of A*X = B  
	 * @param QR matrix to compute the pseudo-inverse (the A matrix in A*X = B)
	 * @param X matrix where to build the solution
	 * @param Rdiag diagonal values of QR
	 * @return X that minimises the two norm of Q*R*X-B, i.e. the pseudo-inverse of QR
	 */
	public Matrix solveLeastSquare(Matrix QR, Matrix X, float[] Rdiag) {
		if (X.rowSize() != QR.rowSize()) {
			throw new IllegalArgumentException("Matrix row dimensions must agree.");
		}
		if (!isFullRank(Rdiag)) {
			throw new RuntimeException("Matrix is rank deficient.");
		}

		int rowsQR = QR.rowSize();
		int columsQR = QR.columnSize();

		// Copy right hand side
		int nx = X.columnSize();

		// Compute Y = transpose(Q)*B
		for (int k = 0; k < columsQR; k++) {
			for (int j = 0; j < nx; j++) {
				float s = 0f;
				for (int i = k; i < rowsQR; i++) {
					s += QR.getValue(i,k)*X.getValue(i, j);
				}
				s = -s/QR.getValue(k, k);
				for (int i = k; i < rowsQR; i++) {
					float aux = X.getValue(i,j) + s*QR.getValue(i,k);
					X.setValue(i,j,aux);
				}
			}
		}
		// Solve R*X = Y;
		for (int k = columsQR-1; k >= 0; k--) {
			for (int j = 0; j < nx; j++) {
				X.setValue(k,j,X.getValue(k,j)/Rdiag[k]);
			}
			for (int i = 0; i < k; i++) {
				for (int j = 0; j < nx; j++) {
					float aux = X.getValue(i,j) - X.getValue(k,j)*QR.getValue(i,k);
					X.setValue(i,j,aux);
				}
			}
		}
		return X.getSubmatrix(0, QR.columnSize());
	}

	private boolean isFullRank(float[] Rdiag) {
		for (int j = 0; j < Rdiag.length; j++) {
			if (Rdiag[j] == 0)
				return false;
		}
		return true;
	}

	/**
	 * QR Decomposition, computed by Householder reflections.
	 * Implementation based on JAMA and Colt.
	 * For an <tt>rowSize x columnSize</tt> Matrix <tt>A</tt> with <tt>rowSize >= columnSize</tt>, the QR decomposition 
	 * is an <tt>rowSize X columnSize</tt> orthogonal Matrix <tt>Q</tt> and an <tt>columnSize x columnSize</tt> upper triangular matrix <tt>R</tt> 
	 * so that <tt>A = Q*R</tt>. The QR decompostion always exists, even if the matrix does not have full rank.  
	 * The primary use of the QR decomposition is in the least squares solution of nonsquare systems of simultaneous linear equations.
	 * @param m matrix to decompose
	 * @param Rdiag the diagonal of Matrix R
	 * @return the QR decomposition of Matrix m
	 */
	public Matrix solveQR(Matrix m,float[] Rdiag) {

		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("QR-serial", m,null, null));

		// Initialize.
		Matrix QR = FactoryMatrixHolder.getFactory().createMatrix(m);
		int rowQR = QR.rowSize(); //m
		int columnsQR = QR.columnSize(); //n

		// Main loop.
		for (int k = 0; k < columnsQR; k++) {
			// Compute 2-norm of k-th column without under/overflow.
			float nrm = 0;
			for (int i = k; i < rowQR; i++) {
				nrm = (float) Math.hypot(nrm,QR.getValue(i,k));
			}

			if (nrm != 0.0) {
				// Form k-th Householder vector.
				if (QR.getValue(k,k) < 0) {
					nrm = -nrm;
				}
				for (int i = k; i < rowQR; i++) {
					float aux = QR.getValue(i,k) / nrm;
					QR.setValue(i, k, aux);
				}

				QR.setValue(k, k, QR.getValue(k, k)+1);

				// Apply transformation to remaining columns.
				for (int j = k+1; j < columnsQR; j++) {
					float s = 0f;
					for (int i = k; i < rowQR; i++) {
						s += QR.getValue(i,k)*QR.getValue(i,j);
					}
					s = -s/QR.getValue(k,k);

					for (int i = k; i < rowQR; i++) {
						float aux = QR.getValue(i,j) + s*QR.getValue(i,k);
						QR.setValue(i,j,aux);
					}
				}
			}
			Rdiag[k] = -nrm;
		}

		//		//R 
		//		Matrix R = FactoryMatrixHolder.getFactory().createMatrix(columnsQR,columnsQR);
		//		for (int i = 0; i < columnsQR; i++) {
		//			for (int j = 0; j < columnsQR; j++) {
		//				if (i < j) {
		//					R.setValue(i,j,QR.getValue(i,j));
		//				} else if (i == j) {
		//					R.setValue(i,j,Rdiag[i]);
		//				} else {
		//					R.setValue(i,j,0);
		//				}
		//			}
		//		}
		//
		//		System.out.println("R\n"+R);
		//
		//		//H
		//		Matrix H = FactoryMatrixHolder.getFactory().createMatrix(rowQR,columnsQR);
		//		for (int i = 0; i < rowQR; i++) {
		//			for (int j = 0; j < columnsQR; j++) {
		//				if (i >= j) {
		//					H.setValue(i,j,QR.getValue(i,j));
		//				} else {
		//					H.setValue(i,j,0);
		//				}
		//			}
		//		}
		//
		//		System.out.println("H\n"+H);  
		//
		//		//Q
		//
		//		Matrix Q = FactoryMatrixHolder.getFactory().createMatrix(rowQR,columnsQR);
		//
		//		for (int k = columnsQR-1; k >= 0; k--) {
		//			for (int i = 0; i < rowQR; i++) {
		//				Q.setValue(i,k,0);
		//			}
		//			Q.setValue(k,k,1);
		//
		//			for (int j = k; j < columnsQR; j++) {
		//				if (QR.getValue(k,k) != 0) {
		//					float s = 0f;
		//					for (int i = k; i < rowQR; i++) {
		//						s += QR.getValue(i, k)*Q.getValue(i,j);
		//					}
		//					s = -s/QR.getValue(k,k);
		//					for (int i = k; i < rowQR; i++) {
		//						float aux = Q.getValue(i, j) + s*Q.getValue(i, k);
		//						Q.setValue(i,j,aux);
		//					}
		//				}
		//			}
		//		}
		//
		//		System.out.println("Q\n"+Q);
		Configuration.logger.log(Level.INFO, Configuration.getLogTime("QR-serial-",System.currentTimeMillis()-startTime, null, QR));
		return QR;
	}

	/**
	 * Solve A*X = B. It is used for computing the inverse of a matrix.
	 * @param LU matrix for which the inverse if found (represents the A in A*x = B)
	 * @param B the result of the linear system. In the case of the inverse computation is the identity.
	 * @param piv pivot of LU
	 * @return X the solution of the linear system
	 */
	public Matrix solve(Matrix LU, Matrix B,int [] piv) {

		if (B.rowSize() != LU.rowSize()) {
			throw new IllegalArgumentException("Matrix row dimensions must agree.");
		}

		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("AX=B-serial", LU,B, null));

		// Copy right hand side with pivoting
		int nx = B.columnSize();
		Matrix Xmat = FactoryMatrixHolder.getFactory().createMatrix(piv.length, nx); 
		for(int i=0;i<piv.length;i++){
			for(int j=0;j<nx;j++)
				Xmat.setValue(i, j, B.getValue(piv[i],j));
		}

		// Solve L*Y = B(piv,:)
		for (int k = 0; k < LU.rowSize(); k++) {
			for (int i = k+1; i < LU.rowSize(); i++) {
				for (int j = 0; j < nx; j++) {
					float aux = Xmat.getValue(i, j) - Xmat.getValue(k,j)*LU.getValue(i,k);
					Xmat.setValue(i, j, aux);
				}
			}
		}
		// Solve U*X = Y;
		for (int k = LU.rowSize()-1; k >= 0; k--) {
			for (int j = 0; j < nx; j++) {
				float aux = Xmat.getValue(k,j) / LU.getValue(k,k);
				Xmat.setValue(k,j,aux);
			}
			for (int i = 0; i < k; i++) {
				for (int j = 0; j < nx; j++) {
					float aux = Xmat.getValue(i, j) - Xmat.getValue(k,j)*LU.getValue(i,k);
					Xmat.setValue(i, j, aux);
				}
			}
		}
		Configuration.logger.log(Level.INFO, Configuration.getLogTime("AX=B-serial-",System.currentTimeMillis()-startTime, null, Xmat));
		return Xmat;
	}

	/**
	 * Returns the LU decomposition of a Matrix based on the Crout/Dolittle algorithm.
	 * For an <tt>rowSize X columnSize</tt> matrix <tt>A</tt> with <tt>rowSize >= columnSize</tt>, 
	 * the LU decomposition is an <tt>rowSize x columnSize</tt> unit lower triangular matrix <tt>L</tt>, an <tt>columnSize x columnSize</tt> 
	 * upper triangular matrix <tt>U</tt>, and a permutation vector <tt>piv</tt> of length <tt>rowSize</tt> so that <tt>A(piv,:) = L*U</tt>; 
	 * If <tt>rowSize < columnSize</tt>, then <tt>L</tt> is <tt>rowSize X rowSize</tt> and <tt>U</tt> is <tt>rowSize columnSize n</tt>. 
	 * The LU decomposition with pivoting always exists, even if the matrix is singular.  
	 * The primary use of the LU decomposition is in the solution of square systems of simultaneous linear equations.
	 * @param A matrix to decompose
	 * @param piv the pivot array
	 * @return the LU decomposition of Matrix m
	 */
	public Matrix solveLU(Matrix A,int [] piv) {
		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("LU-serial", A,null, null));

		Matrix LU = FactoryMatrixHolder.getFactory().createMatrix(A);
		int rowLU = LU.rowSize(); //m
		int columnsLU = LU.columnSize(); //n

		for (int i = 0; i < rowLU; i++) {
			piv[i] = i;
		}

		// Outer loop.

		for (int j = 0; j < columnsLU; j++) { //loop over columns

			// Apply previous transformations.

			for (int i = 0; i < rowLU; i++) { //loop over rows

				// Most of the time is spent in the following dot product.

				int kmax = Math.min(i,j);
				float s = 0f;
				for (int k = 0; k < kmax; k++) {
					s += LU.getValue(i, k)*LU.getValue(k, j);
				}

				float aux = LU.getValue(i,j) - s;
				LU.setValue(i, j, aux);

			}

			// Find pivot and exchange if necessary.

			int p = j;
			for (int i = j+1; i < rowLU; i++) {
				if (Math.abs(LU.getValue(i,j)) > Math.abs(LU.getValue(p,j))) {	
					p = i;
				}
			}
			if (p != j) {
				for (int k = 0; k < columnsLU; k++) {

					float t = LU.getValue(p, k);
					LU.setValue(p, k, LU.getValue(j, k));
					LU.setValue(j, k, t);

				}

				int k = piv[p]; 
				piv[p] = piv[j]; 
				piv[j] = k;

			}
			
			float jj = LU.getValue(j, j);
			if (j < rowLU & jj != 0.0) {
				for (int i = j+1; i < rowLU; i++) {
					LU.setValue(i, j, LU.getValue(i, j)/jj);
				}
			}

		}
		Configuration.logger.log(Level.INFO, Configuration.getLogTime("LU-serial-",System.currentTimeMillis()-startTime, null, LU));
		return LU;
	}

	private Matrix invertDiagonal(Matrix m) {
		Matrix inv = FactoryMatrixHolder.getFactory().createMatrix(m.rowSize(), m.columnSize());
		for(int i=0;i<m.rowSize();i++){
			float value = m.getValue(i, i);
			if(value==0)
				return null;
			inv.setValue(i, i, 1/value);
		}
		return inv;
	}

	@Override
	public Matrix subtract(Matrix m, Matrix m1) {

		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("subtracting-serial", m,m1, null));

		Matrix aux=FactoryMatrixHolder.getFactory().createMatrix(m.rowSize(), m.columnSize());		
		for(int i=0;i<m.rowSize();i++) {
			for(int j=0;j<m.columnSize();j++)
				aux.setValue(i, j, m.getValue(i, j)-m1.getValue(i, j));
		}

		Configuration.logger.log(Level.INFO, Configuration.getLogTime("subtracting-serial",System.currentTimeMillis()-startTime, null, aux));

		return aux;
	} 


	/**
	 * Converts a Matrix representation into a double[][]
	 * @param m the Matrix to convert
	 * @return the double[][] representation of the Matrix
	 */
	public double[][] convert(Matrix m){
		double [][] aux=new double[m.rowSize()][m.columnSize()];
		for(int i=0;i<m.rowSize();i++)
			for(int j=0;j<m.columnSize();j++)
				aux[i][j]=m.getValue(i, j);

		return aux;
	}

	/**
	 * Converts a double[][] representation into a float[][]
	 * @param m the matrix to convert
	 * @return the float[][] representation of the matrix
	 */
	public float[][] convert(double[][] m) {
		float [][] aux=new float[m.length][m[0].length];
		for(int i=0;i<m.length;i++)
			for(int j=0;j<m[i].length;j++)
				aux[i][j]=new Float(m[i][j]);

		return aux;
	}

	@Override
	public Matrix laplacian(Matrix m) {
		//La= Da - a
		//Da(i,i)= Sum(A(j,i))

		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("laplacian-serial", m,null, null));

		Matrix Da=FactoryMatrixHolder.getFactory().createMatrix(m.rowSize(),m.columnSize());

		float sum;
		for(int i=0;i<Da.rowSize();i++){
			sum=0;
			for(int j=0;j<Da.columnSize();j++)
				sum+=m.getValue(j, i);
			Da.setValue(i, i, sum);
		}

		Da=subtract(Da,m);

		Configuration.logger.log(Level.INFO, Configuration.getLogTime("laplacian-serial",System.currentTimeMillis()-startTime, null, Da));

		return Da;
	}

	@Override
	public Matrix invertByCholesky(Matrix m) {

		if(m.rowSize()!=m.columnSize()){
			throw new IllegalArgumentException("Matrix must be square.");
		}

		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("invert-by-Cholesky-serial", m,null, null));

		Matrix hermitian = multiplyByTranspose(m);
		System.out.println("invert by cholesky - hermitian\n"+hermitian);
		Matrix cholesky = solveCholesky(hermitian);
		if(cholesky==null)
			throw new IllegalArgumentException("Matrix is non-Herminian!");
		Matrix identity = FactoryMatrixHolder.getFactory().createIdentity(m.rowSize());
		System.out.println("invert by cholesky - cholesky\n"+cholesky);
		Matrix inverse = solve(cholesky,identity);
		inverse = multiply(transpose(m), inverse);

		Configuration.logger.log(Level.INFO, Configuration.getLogTime("invert-by-Cholesky-serial-",System.currentTimeMillis()-startTime, null, inverse));
		return inverse;
	}

}
