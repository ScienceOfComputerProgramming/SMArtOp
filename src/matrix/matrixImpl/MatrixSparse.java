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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import matrix.factory.FactoryMatrixHolder;
import matrix.matrixComp.MatrixComputationFD;

/** Provides implementation of a sparse 2d matrix. Its non-zero elements are store in a Map associated with a {@link Terna} as key.
 * Although this implementation is space efficient, it adds complexities to several matrix operations.
 * @author Antonela Tommasel
 * */
public class MatrixSparse extends Matrix {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5429978613943846202L;
	
	private Map<Terna,Float> elements;
	Map<Integer,Integer> sparcity; 


	/**
	 * Class constructor
	 * @param f number of rows of the matrix to create.
	 * @param c number of columns of the matrix to create.
	 */
	public MatrixSparse(int f, int c) {
		super(f, c);
		elements=new HashMap<Terna,Float>();
		sparcity=new HashMap<Integer,Integer>();
	}

	/** Creates a Matrix by copying the values of the other Matrix
	 * @param toCopy Matrix to copy the values in the new Matrix
	 */
	public MatrixSparse(Matrix toCopy){
		super(toCopy.rowSize(),toCopy.columnSize());
		elements=new HashMap<Terna,Float>();
		sparcity=new HashMap<Integer,Integer>();
		nonZeros = toCopy.getNonZeros();
		Collection<Integer> rows = toCopy.getRows();
		for(Integer i:rows){
			Collection<Integer> col = toCopy.getColumns(i);
			sparcity.put(i, col.size());
			for(Integer j:col){
				Terna t = new Terna(i,j,columns);
				elements.put(t, toCopy.getValue(i,j));
			}
		}
	}
	
	@Override
	public void setValue(int i, int j, float v) {
		if(i<rows && j<columns){
			Terna t=new Terna(i,j,columns);
			if(v==0.0f){
				if(elements.get(t)!=null)
					nonZeros--;
				elements.remove(t);
				Integer ii=sparcity.get(i);
				if(ii!=null)
					ii--;
				else
					ii=1;
				sparcity.put(i, ii);
			}
			else{
				elements.put(t,v);
				nonZeros++;
				Integer ii=sparcity.get(i);
				if(ii!=null)
					ii++;
				else
					ii=1;
				sparcity.put(i, ii);
			}
		}			
	}

	@Override
	public float getValue(int i, int j) {
		Terna t=new Terna(i,j,columns);
		if(elements.containsKey(t))
			return elements.get(t);
		return 0;
	}

	@Override
	public float[][] getMatrix() {
		float [][] aux=new float[rows][columns];
		for(Terna t:elements.keySet())
			aux[t.getRow()][t.getColumn()]=elements.get(t);
		return aux;
	}

	@Override
	public void setValues(float[][] m) {
		for(int i=0;i<m.length;i++)
			for(int j=0;j<m[i].length;j++)
				if(m[i][j]!=0.0f){
					nonZeros++;
					elements.put(new Terna(i,j,columns), m[i][j]);
				}
	}

	@Override
	public float getSparcityRow(int i) {	
		Integer s=sparcity.get(i);
		if(s==null){
			return 1;
		}
		else{
			return ((float)(columns-s))/(float)columns;
		}

	}

	@Override
	public Collection<Integer> getRows() {
		Collection<Integer> aux = new ArrayList<Integer>();
		for(int i=0;i<rows;i++){
			boolean found = false;
			for(int j=0;j<columns && !found;j++){
				Terna t = new Terna(i,j,columns);
				if(elements.containsKey(t)){
					found = true;
					aux.add(i);
				}
			}
		}

		return aux;
	}
	
	@Override
	public void setRow(int k, Matrix m) {
		if(k<rows && k<m.rowSize() && columns == m.columnSize()){
			for(int j=0;j<columns;j++){
				this.setValue(k, j, m.getValue(k, j));
			}
		}
		else
			throw new ArrayIndexOutOfBoundsException();
	}

	@Override
	public Collection<Integer> getColumns(int row) {
		Collection<Integer> aux = new ArrayList<Integer>();

		for (int j = 0; j < columns; j++) {
			Terna t = new Terna(row,j,columns);
			if(elements.containsKey(t))
				aux.add(j);
		}

		return aux;
	}
	
	@Override
	public void updateSparsity(){
		this.nonZeros = elements.size();
	}

	@Override
	public Matrix fastMult(float alfa) {
		Matrix m = new MatrixSparse(rows, columns);
		for(Terna e:elements.keySet())
			m.setValue(e.getRow(),e.getColumn(),alfa*elements.get(e));
		return m;
	}

	@Override
	public Matrix fastTrans() {
		Matrix m = new MatrixSparse(rows, columns);
		for(Terna e:elements.keySet())
			m.setValue(e.getColumn(),e.getRow(),elements.get(e));
		return m;
	}

	@Override
	public Matrix fastMult(Matrix m1) {
		MatrixComputationFD fd = new MatrixComputationFD(); 
		return fd.multiply(this, m1);
	}

	@Override
	public List<Integer> getSortedRows() {
		List<Integer> aux = new ArrayList<Integer>();
		
		for(int i=0;i<rows;i++){
			boolean found = false;
			for(int j=0;j<columns && !found;i++){
				Terna t = new Terna(i,j,columns);
				if(elements.containsKey(t)){
					found = true;
					aux.add(i);
				}
			}
		}
		
		Collections.sort(aux,new Comparator<Integer>(){

			@Override
			public int compare(Integer arg0, Integer arg1) {
				return new Float(getSparcityRow(arg0)).compareTo(new Float(getSparcityRow(arg1)));
			}
		});
		
		return aux;
	}

	@Override
	public float getNonZerosRow(int i) {
		float nonZeros = 0;
		for(int j=0;j<columns;j++){
			Terna t = new Terna(i,j,columns);
			if(elements.containsKey(t))
				nonZeros++;
		}
		return nonZeros;
	}

	@Override
	public boolean isDiagonal() {
		Set<Terna> keys = elements.keySet();
		for(Terna t:keys)
			if(t.getRow()!=t.getColumn() && elements.get(t)!=0)
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
			for(int j=0;j<inverse.columnSize();j++){
				Float value = inverse.getValue(i,j);
				if(value!=null)
					elements.put(new Terna(i,j,columns), value);
			}
	}

	@Override
	public void findFastSolutionSquares(Matrix QR,float[] Rdiag) {
		Matrix inverse = new MatrixComputationFD().solveLeastSquare(QR,FactoryMatrixHolder.getFactory().createIdentity(QR.rowSize()), Rdiag);
		for(int i=0;i<inverse.rowSize();i++)
			for(int j=0;j<inverse.columnSize();j++){
				Float value = inverse.getValue(i,j);
				if(value!=null)
					elements.put(new Terna(i,j,columns), value);
			}				
	}

	@Override
	public void invertDiagonal() {
		Map<Terna,Float> aux = new HashMap<Terna,Float>();
		for(Terna t:elements.keySet())
			aux.put(t, 1/elements.get(t));
		
		elements.clear();
		elements.putAll(aux);
		aux = null;
	}
	
	@Override
	public Matrix fastCholesky() {
		return new MatrixComputationFD().solveCholesky(this);
	}

	@Override
	public void findFastSolution(Matrix cholesky) {
		
		Matrix inverse = new MatrixComputationFD().solve(cholesky, FactoryMatrixHolder.getFactory().createIdentity(cholesky.rowSize()));
		for(int i=0;i<inverse.rowSize();i++)
			for(int j=0;j<inverse.columnSize();j++){
				elements.put(new Terna(i,j,columns), inverse.getValue(i,j));
			}
	}
	
	@Override
	public boolean isSingular() {
		Set<Terna> keys = elements.keySet();
		for(Terna t:keys)
			if(t.getRow()==t.getColumn() && elements.get(t)==0)
				return true;
		return false;
	}
	
}
