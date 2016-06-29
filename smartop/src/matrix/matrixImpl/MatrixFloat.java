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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import matrix.matrixComp.MatrixComputationFD;

import matrix.factory.FactoryMatrixHolder;

/** 
 * Provides implementation of a dense 2d matrix of float elements. As it is a dense memory structure, it is assumed to be fully completed.
 * @author Antonela Tommasel
 * */
public class MatrixFloat extends Matrix{

	private static final long serialVersionUID = 1L;

	/**
	 * Internal representation of the matrix
	 */
	private float [][] elements;

	/**
	 * Class constructor
	 * @param f number of rows of the matrix to create.
	 * @param c number of columns of the matrix to create.
	 */
	public MatrixFloat(int f, int c) {
		super(f, c);
		elements=new float[f][c];
		nonZeros=0;
	}

	/** 
	 * Creates a Matrix by copying the values of the other Matrix
	 * @param toCopy Matrix to copy the values in the new Matrix
	 */
	public MatrixFloat(Matrix toCopy){
		super(toCopy.rowSize(),toCopy.columnSize());
		elements = new float[rows][columns];
		nonZeros = 0;
		Collection<Integer> rows = toCopy.getRows();
		for(Integer i:rows){
			Collection<Integer> col = toCopy.getColumns(i);
			for(Integer j:col)
				elements[i][j] = toCopy.getValue(i, j);
		}
	}

	@Override
	public void setValue(int i, int j, float v) {
		if(i<rows && j<columns){
			if(v!=0.0f){
				nonZeros++;
			}
			else
				if(elements[i][j]!=0.0f)
					nonZeros--;
			elements[i][j]=v;
		}


	}

	@Override
	public float getValue(int i, int j) {
		if(i<rows && j<columns)
			return elements[i][j];
		return 0;
	}

	@Override
	public float[][] getMatrix() {

		float [][] aux=new float[rows][columns];
		for(int i=0;i<rows;i++)
			for(int j=0;j<columns;j++)
				aux[i][j]=elements[i][j];

		return aux;
	}

	@Override
	public void setValues(float[][] m) {
		for(int i=0;i<m.length;i++)
			for(int j=0;j<m[i].length;j++)
				if(m[i][j]!=0.0f){
					nonZeros++;
					elements[i][j]=m[i][j];
				}

	}

	@Override
	public float getSparcityRow(int i) {
		return 0;
	}

	@Override
	public float[] getSparcityRowAvg() {
		float [] aux=new float[3];
		aux[0] = 0;
		aux[1] = 0;
		aux[2] = 0;
		return aux;
	}

	@Override
	public Collection<Integer> getRows() {
		Collection<Integer> fi= new HashSet<Integer>();
		for(int i=0;i<rows;i++)
			fi.add(i);
		return fi;
	}

	@Override
	public Collection<Integer> getColumns(int row) {
		Collection<Integer> fi= new HashSet<Integer>();
		for(int i=0;i<columns;i++)
			fi.add(i);
		return fi;
	}

	@Override
	public void updateSparsity() {

	}

	@Override
	public void setRow(int k, Matrix m) {
		if(k<rows && k<m.rowSize() && columns == m.columnSize()){
			for(int i=0;i<columns;i++)
				elements[k][i] = m.getValue(k, i);
		}
		else
			throw new ArrayIndexOutOfBoundsException();

	}

	/**
	 * As this type of matrix represents a dense memory structure, it invokes the methods in @MatrixComputationFD
	 */
	@Override
	public Matrix fastMult(float alfa) {
		MatrixComputationFD fd = new MatrixComputationFD();
		return fd.multiply(alfa, this);
	}

	/**
	 * As this type of matrix represents a dense memory structure, it invokes the methods in @MatrixComputationFD
	 */
	@Override
	public Matrix fastTrans() {
		MatrixComputationFD fd = new MatrixComputationFD();
		return fd.transpose(this);
	}

	/**
	 * As this type of matrix represents a dense memory structure, it invokes the methods in @MatrixComputationFD
	 */
	@Override
	public Matrix fastMult(Matrix m1) {
		MatrixComputationFD fd = new MatrixComputationFD();
		return fd.multiply(this,m1);
	}

	/**
	 * As all rows are assumed to contain all values, this method returns the index of the row coordinates in their natural order.
	 */
	@Override
	public List<Integer> getSortedRows() {
		List<Integer> rowsL = new ArrayList<Integer>();
		for (int i = 0; i < rows; i++) {
			rowsL.add(i);
		}
		return rowsL;
	}

	/**
	 * As the matrix is assumed to be dense, all rows are assumed to have all non zero elements.
	 */
	@Override
	public float getNonZerosRow(int i) {
		return 0;
	}

	@Override
	public boolean isDiagonal() {
		for(int i=0;i<rows;i++)
			for(int j=0;j<columns;j++)
				if(i!=j && elements[i][j]!=0)
					return false;
		return true;
	}

	@Override
	public Matrix fastLU(int[] piv) {
		return new MatrixComputationFD().solveLU(this, piv);
	}

	@Override
	public Matrix fastQR(float[] Rdiag) {
		return new MatrixComputationFD().solveQR(this, Rdiag);
	}

	@Override
	public void findFastSolution(Matrix LU, Matrix B,int[] piv) {
		Matrix inverse = new MatrixComputationFD().solve(LU, B, piv);
		for(int i=0;i<inverse.rowSize();i++)
			for(int j=0;j<inverse.columnSize();j++)
				elements[i][j] = inverse.getValue(i,j);
	}

	@Override
	public void findFastSolutionSquares(Matrix QR,float[] Rdiag) {

		Matrix inverse = new MatrixComputationFD().solveLeastSquare(QR, FactoryMatrixHolder.getFactory().createIdentity(QR.rowSize()), Rdiag);
		rows = inverse.rowSize();
		columns = inverse.columnSize();

		elements = new float[rows][columns];

		for(int i=0;i<inverse.rowSize();i++)
			for(int j=0;j<inverse.columnSize();j++)
				elements[i][j] = inverse.getValue(i,j);

	}

	@Override
	public void invertDiagonal() {
		for(int i=0;i<rows;i++)
			elements[i][i] = 1/elements[i][i]; 
	}

	@Override
	public Matrix fastCholesky() {
		return new MatrixComputationFD().solveCholesky(this);
	}

	@Override
	public void findFastSolution(Matrix cholesky) {

		Matrix inverse = new MatrixComputationFD().solve(cholesky, FactoryMatrixHolder.getFactory().createIdentity(cholesky.rowSize()));
		for(int i=0;i<inverse.rowSize();i++)
			for(int j=0;j<inverse.columnSize();j++)
				elements[i][j] = inverse.getValue(i,j);
	}

	@Override
	public boolean isSingular() {
		int index = Math.min(rows,columns);
		for(int i=0;i<index;i++)
			if(elements[i][i]==0)
				return true;
		return false;
	}

}
