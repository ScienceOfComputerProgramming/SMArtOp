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

import matrix.matrixImpl.Matrix;

/**
 * @author Antonela Tommasel
 *  
 */

/** Interface to provide the signature for the arithmetic operations that can be performed between matrices. */
public interface MatrixComputation {
	
	/**
	 * Returns the transpose of the Matrix m. M^t
	 * The returned Matrix is created with the same type than the original.
	 * @param m the Matrix to transpose
	 * @return the transpose of the Matrix m
	 */
	public Matrix transpose(Matrix m);
	
	/**
	 * Separates the special case of multiplying a Matrix by its transpose in order to avoid creating an extra Matrix in memory.
	 * The returned Matrix is created with the same type than the original.
	 * @param m the Matrix to multiply by its transpose
	 * @return the result of multiplying the Matrix m by its transpose
	 */
	public Matrix multiplyByTranspose(Matrix m);
	
	/**
	 * Multiplies a matrix by a scalar value. 
	 * The returned Matrix is created with the same type than the original.
	 * @param alfa the scalar value to multiply
	 * @param m the matrix to multiply by the scalar value
	 * @return the result of multiplying a matrix by a scalar
	 */
	public Matrix multiply(float alfa, Matrix m);
	
	/**
	 * Multiplies two matrices.
	 * The returned Matrix is created with the same type than the original.
	 * @param m the left matrix of the multiplication
	 * @param m1 the right matrix of the multiplication
	 * @return the result of multiplying the two matrices
	 */
	public Matrix multiply(Matrix m, Matrix m1);

	/**
	 * Adds two matrices.
	 * The returned Matrix is created with the same type than the original.
	 * @param m the first summand of the addition
	 * @param m1 the second summand of the addition
	 * @return the result of adding the two matrices
	 */
	public Matrix add(Matrix m, Matrix m1);
	
	/**
	 * Returns the inverse or pseudo-inverse of matrix <tt>m</tt>.
	 * The implementation is based on the Colt implementation, which uses the LU or QR method.
	 * The returned Matrix is created with the same type than the original.
	 * @param m the matrix to be inverted
	 * @return the inverse or pseudo-inverse of the matrix
	 * @throws IllegalArgumentException when the matrix <tt>m>/tt> is singular
	 */
	public Matrix invert(Matrix m);
	
	/**
	 * Returns the inverse computed by the Cholesky method for an arbitrary square Matrix.
	 * The implementation is based on the Colt implementation and the paper "Matrix Inversion Using Cholesky Decomposition".
	 * The returned Matrix is created with the same type than the original.
	 * Matrix m is first transformed into an Hermitian Matrix in order to the Cholesky to work. 
	 * When it is applicable, the Cholesky decomposition is roughly twice as efficient as the LU decomposition for solving systems of linear equations.
	 * Consider Matrix D, the Hermitian A is computed as A = D*transpose(D).
	 * Then inverse(D) = transpose(D)*inverse(A)
	 * @param m the matrix to be inverted
	 * @return the inverse 
	 */
	public Matrix invertByCholesky(Matrix m);
	
	/**
	 * Subtracts two matrices.
	 * The returned Matrix is created with the same type than the original.
	 * @param m the minuend of the subtraction
	 * @param m1 the subtrahend of the subtraction
	 * @return the results of subtracting two matrices
	 */
	public Matrix subtract(Matrix m, Matrix m1); 

	/**
	 * Computes the Laplacian of a matrix
	 * La= Da - a
	 * Da(i,i)= Sum(A(j,i))
	 * The returned Matrix is created with the same type than the original.
	 * @param m the matrix to compute the Laplacian
	 * @return the Laplacian of the matrix
	 */
	public Matrix laplacian(Matrix m); 
	
}
