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

import java.util.Collection;
import java.util.List;

/**
 * This matrix automatically changes it representation between {@link MatrixSparseHash} and {@link MatrixFloat}
 * when its sparseness is lower or higher, respectively, than a threshold.
 * @author Antonela Tommasel
 *
 */
public class MatrixThreshold extends Matrix{

	/**
	 * 
	 */
	Matrix elements;
	float threshold;
	
	private static final long serialVersionUID = 2406469154084825923L;

	/**
	 * Class constructor
	 * @param f number of rows of the matrix to create.
	 * @param c number of columns of the matrix to create.
	 * @param thre threshold that determines the type of matrix to build
	 */
	public MatrixThreshold(int f, int c,float thre) {
		super(f, c);
		threshold=thre;
		elements=new MatrixSparseHash(f,c);
	}


	/** 
	 * Creates a Matrix by copying the values of the other Matrix
	 * @param toCopy Matrix to copy the values in the new Matrix
	 * @param thre threshold that determines the type of matrix to build
	 */
	public MatrixThreshold(Matrix toCopy,float thre){
		super(toCopy.rowSize(),toCopy.columnSize());
		threshold = thre;
		elements = new MatrixSparseHash(toCopy);
	}
	
	@Override
	public void setValue(int i, int j, float v) {
		double sant=elements.getSparsity();
		
		elements.setValue(i, j, v);
		
		if(sant>threshold && elements.getSparsity()<threshold){ //Si supera el threshold de sparsity
			float [][] a=elements.getMatrix();
			elements=new MatrixFloat(rows, columns);
			elements.setValues(a);
			a=null;
		}
		else
			if(sant<threshold && elements.getSparsity()>threshold){ //Si el sparsity se corresponde con el spare
				float [][] a=elements.getMatrix();
				elements=new MatrixSparseHash(rows, columns);
				elements.setValues(a);
				a=null;
			}
				
	}

	@Override
	public float getValue(int i, int j) {
		return elements.getValue(i, j);
	}

	@Override
	public float[][] getMatrix() {
		return elements.getMatrix();
	}

	@Override
	public void setValues(float[][] m) { 
		elements.setValues(m);
	}

	@Override
	public double getSparsity(){ 
		return elements.getSparsity();
	}

	@Override
	public float getSparcityRow(int i) {
		return elements.getSparcityRow(i);
	}

	@Override
	public float[] getSparcityRowAvg() {
		return elements.getSparcityRowAvg();
	}

	@Override
	public Collection<Integer> getRows() {
		return elements.getRows();
	}

	@Override
	public void setRow(int k, Matrix m) {
		elements.setRow(k, m);
		
	}

	@Override
	public Collection<Integer> getColumns(int row) {
		return elements.getColumns(row);
	}

	@Override
	public void updateSparsity() {
		elements.updateSparsity();
		
	}

	@Override
	public Matrix fastMult(float alfa) {
		return elements.fastMult(alfa);
	}

	@Override
	public Matrix fastTrans() {
		return elements.fastTrans();
	}

	@Override
	public Matrix fastMult(Matrix m1) {
		return elements.fastMult(m1);
	}

	@Override
	public List<Integer> getSortedRows() {
		return elements.getSortedRows();
	}

	@Override
	public float getNonZerosRow(int i) {
		return elements.getNonZerosRow(i);
	}

	@Override
	public boolean isDiagonal() {
		return elements.isDiagonal();
	}


	@Override
	public Matrix fastLU(int[] piv) {
		return elements.fastLU(piv);
	}


	@Override
	public Matrix fastQR(float[] Rdiag) {
		return elements.fastQR(Rdiag);
	}


	@Override
	public void findFastSolution(Matrix LU, Matrix B, int[] piv) {
		elements.findFastSolution(LU, B, piv);
	}


	@Override
	public void findFastSolutionSquares(Matrix QR, float[] Rdiag) {
		elements.findFastSolutionSquares(QR, Rdiag);
	}


	@Override
	public void invertDiagonal() {
		elements.invertDiagonal();
	}


	@Override
	public Matrix fastCholesky() {
		return elements.fastCholesky();
	}


	@Override
	public void findFastSolution(Matrix cholesky) {
		elements.findFastSolution(cholesky);
	}


	@Override
	public boolean isSingular() {
		return elements.isSingular();
	}

}
