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
package matrix.matrixImpl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import matrix.factory.FactoryMatrixHolder;

/**
 *  Abstract class to represent 2D matrices Provides the signature of all methods and several common implementations.
 *  @author Antonela Tommasel
 *  */
public abstract class Matrix implements Serializable {

	private static final long serialVersionUID = -2639768680738023439L;

	/**
	 * Number of rows of the Matrix.
	 */
	protected int rows;

	/**
	 * Number of columns of the Matrix.
	 */
	protected int columns;

	/**
	 * Number of non zero elements in the matrix.
	 */
	protected long nonZeros;

	/**
	 * Class constructor
	 * @param f number of rows of the matrix to create.
	 * @param c number of columns of the matrix to create.
	 */
	public Matrix(int f, int c) {
		rows = f;
		columns = c;
		nonZeros = 0;
	}
	
	/**
	 * @return the number of rows of the matrix
	 */
	public int rowSize() {
		return rows;
	}

	/**
	 * @return the number of columns of the matrix
	 */
	public int columnSize() {
		return columns;
	}

	/**
	 * @param i the index of the row-coordinate.
	 * @param j the index of the column-coordinate.
	 * @param v the value to be filled into the specified cell.
	 */
	public abstract void setValue(int i, int j, float v);

	/**
	 * @param i the index of the row-coordinate.
	 * @param j the index of the column-coordinate.
	 * @return the value of the specified cell.
	 */
	public abstract float getValue(int i, int j);

	/**
	 * @return a float[][] representation of the Matrix
	 */
	public abstract float[][] getMatrix();

	/**
	 * @param m values to set in the Matrix
	 */
	public abstract void setValues(float[][] m);

	/**
	 * @param i the index of the row
	 * @return the sparsity of the specified row.
	 */
	public abstract float getSparcityRow(int i);

	/**
	 * @param i the index of the row
	 * @return the number of non zeros in the specified row
	 */
	public abstract float getNonZerosRow(int i);

	/**
	 * Delegates the computation of the multiplication by an scalar to the Matrix to leverage on the internal structure of the Matrix.
	 * @param alfa the scalar to multiply
	 * @return the result of multiplying the matrix by an scalar
	 */
	public abstract Matrix fastMult(float alfa);

	/**
	 * Delegates the computation of the transpose to the Matrix to leverage on the internal structure of the Matrix.
	 * @return the transpose of the Matrix
	 */
	public abstract Matrix fastTrans();

	/**
	 * Delegates the computation of the multiplication to leverage on the internal structure and level of sparsity of the left Matrix.
	 * @param m1 the right matrix of the multiplication
	 * @return the result of multiplying the two matrices.
	 */
	public abstract Matrix fastMult(Matrix m1);

	/**
	 * Computes sparseness statistics of the matrix. This method is used for computing the Parallel Factor for distributing the matrix operations.
	 * @return statistics of the matrix sparseness. The array has length 3.
	 * Position 0: Average of the row sparseness.
	 * Position 1: Standard Deviation of the row sparseness.
	 * Position 2: Mode of the row sparseness.
	 */
	public float[] getSparcityRowAvg() {

		double[] aux = new double[3];

		float[] auxf = new float[3];

		Map<Double, Integer> moda = new HashMap<Double, Integer>();

		// deviation = sqrt ( (sum(X^2)-n*mean(X)^2) / n )

		double sum = 0;
		double sumSquare = 0;

		for (int i = 0; i < rows; i++) {
			double sp = getSparcityRow(i);
			sum += sp;
			sumSquare += sp * sp;

			Integer o = moda.get(sp);
			if (o != null)
				o++;
			else
				o = 1;
			moda.put(sp, o);
		}

		aux[0] = sum / (double) rows;

		Double fst = new Double(
				(double) Math.sqrt((double) (sumSquare / (double) rows)
						- aux[0] * aux[0]));

		aux[1] = fst.doubleValue();

		if (fst.isNaN())
			aux[1] = 0;

		aux[2] = moda.keySet().iterator().next();

		for (Double k : moda.keySet())
			if (moda.get(k) >= moda.get(aux[2]))
				aux[2] = k;

		auxf[0] = (float) aux[0];
		auxf[1] = (float) aux[1];
		auxf[2] = (float) aux[2];

		return auxf;
	}

	/**
	 * @return the sparseness of the matrix
	 */
	public double getSparsity() {
		return 1.0d - ((double) nonZeros / (double) ((long) rows * (long) columns));
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++)
				sb.append(this.getValue(i, j) + " ");
			sb.append("\n");

		}
		return sb.toString();
	}

	/**
	 * @param j the index of the column to retrieve
	 * @return the retrieved column or null if the index does not exists
	 */
	public Matrix getColumn(int j) {

		Matrix m = null;

		if (j < columns) {
			m = FactoryMatrixHolder.getFactory().createMatrix(rows, 1);
			for (int i = 0; i < rows; i++)
				m.setValue(i, 1, this.getValue(i, j));
		}

		return m;
	}

	/**
	 * @param i the index of the row to retrieve
	 * @return the retrieved row or null if the index does not exists
	 */
	public Matrix getRow(int i) {

		Matrix m = null;

		if (i < rows) {
			m = FactoryMatrixHolder.getFactory().createMatrix(1, columns);
			for (int j = 0; j < columns; j++)
				m.setValue(i, 1, this.getValue(i, j));
		}

		return m;
	}
	
	/**
	 * @return number of non zero elements in the matrix
	 */
	public long getNonZeros() {
		return nonZeros;
	}

	/**
	 * @return row coordinates
	 */
	public abstract Collection<Integer> getRows();

	/**
	 * @param row the index of the row coordinate
	 * @return column coordinates of the specified row
	 */
	public abstract Collection<Integer> getColumns(int row);

	/**
	 * Updates the sparseness statistics of the matrix
	 */
	public abstract void updateSparsity();

	/**
	 * @return the row coordinates sorted according to their sparseness
	 */
	public abstract List<Integer> getSortedRows();

	/**
	 * Copies the rows between startRow and endRow into a new matrix;
	 * @param startRow the index of the first row of the sub-matrix to return
	 * @param endRow the index of the last row of the sub-matrix to return
	 * @return Matrix with the rows between startRow and endRow
	 */
	public Matrix getSubmatrix(int startRow, int endRow) {
		Matrix submatrix = FactoryMatrixHolder.getFactory().createMatrix(endRow-startRow, this.columns);
		for (int i=startRow; i<endRow; i++) {
			submatrix.setRow(i, this);
		}
		return submatrix;
	}

	/**
	 * Sets the value for a row using as such row from the parameter matrix.
	 * @param i the index of the row coordinate
	 * @param matrix Matrix from which to obtain the row 
	 */
	public abstract void setRow(int i, Matrix matrix);
		
	/**
	 * Copies the rows within the list into a new matrix of the same dimensions
	 * of the current matrix. All row values that are not in the list are zero.
	 * @param rows the indexes of the rows of the sub-matrix to return
	 * @return Matrix with the specified rows 
	 */
	public Matrix getSubmatrix(List<Integer> rows) {
		Matrix submatrix = FactoryMatrixHolder.getFactory().createMatrix(
				this.rows, this.columns);
		for (int i : rows) {
			submatrix.setRow(i, this);
		}
		return submatrix;
	}

	/** Analyses whether the matrix is square, i.e. the number of rows is equal to the number of columns
	 * @return true if the matrix is square
	 */
	public boolean isSquare(){
		return rows == columns;
	}

	/** Analyses whether the matrix is diagonal, i.e. only the elements in the diagonal are non-zero
	 * @return true if the matrix is diagonal
	 */
	public abstract boolean isDiagonal();
	
	
	/**
	 * Computes the LU decomposition of the Matrix leveraging the internal structure of the Matrix
	 * @param piv pivot array 
	 * @return the LU decomposition of the Matrix
	 */
	public abstract Matrix fastLU(int [] piv);
	
	/**
	 * Computes the QR decomposition of the Matrix leveraging the internal structure of the Matrix
	 * @param Rdiag internal storage of the diagonal of R.
	 * @return the QR decomposition of the Matrix
	 */
	public abstract Matrix fastQR(float [] Rdiag);
	
	/**
	 * @param LU A*X = B
	 * @param B ??
	 * @param piv pivot array
	 */
	public abstract void findFastSolution(Matrix LU,Matrix B,int [] piv);
	
	/**
	 * @param QR A*X = B
	 * @param Rdiag diagonal of R
	 */
	public abstract void findFastSolutionSquares(Matrix QR,float [] Rdiag);

	
	/**
	 * Computes the Cholesky decomposition of the Matrix in case it exists. Otherwise it returns null.
	 * @return the Cholesky decomposition of the Matrix
	 */
	public abstract Matrix fastCholesky();
	
	/**
	 * Computes A*X = B for solving the inverse by means of the Cholesky decomposition
	 * @param cholesky A
	 */
	public abstract void findFastSolution(Matrix cholesky);
	
	/**
	 * Inverts the diagonal matrix
	 */
	public abstract void invertDiagonal();
	
	
	/**
	 * Analyses whether a triangular matrix is singular, i.e. its diagonal is non-zero
	 * @return whether the triangular Matrix is singular 
	 */
	public abstract boolean isSingular();
}
