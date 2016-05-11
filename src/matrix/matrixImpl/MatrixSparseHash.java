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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import matrix.factory.FactoryMatrixHolder;

/**
 * @author Antonela Tommasel
 *
 */

/** Provides implementation of a sparse 2d matrix. It is elements are store in a {@link Map}{@literal <}Integer,{@link Map}{@literal <}Integer, Float>>. 
 * The first map represents the rows, while the second contains the non-zero values columns for that row. When the first matrix does not have a particular row key,
 * such rows have only zeros in its columns.
 * Although this implementation is space efficient, it adds complexities to several matrix operations.
 * <br> All the maps are instantiated using {@link HashMap}*/
public class MatrixSparseHash extends Matrix{

	private static final long serialVersionUID = -820562433266520474L;

	protected Map<Integer, Map<Integer,Float>> elements;

	protected Set<Integer> columnsIndex;

	/**
	 * Class constructor
	 * @param f number of rows of the matrix to create.
	 * @param c number of columns of the matrix to create.
	 */
	public MatrixSparseHash(int f, int c) {
		super(f, c);
		elements=new HashMap<Integer,Map<Integer,Float>>(0);
		columnsIndex = new HashSet<Integer>();
	}

	/** 
	 * Creates a Matrix by copying the values of the other Matrix
	 * @param toCopy Matrix to copy the values in the new Matrix
	 */
	public MatrixSparseHash(Matrix toCopy){
		super(toCopy.rowSize(),toCopy.columnSize());
		elements=new HashMap<Integer,Map<Integer,Float>>(0);
		columnsIndex = new HashSet<Integer>();
		nonZeros = toCopy.getNonZeros();
		Collection<Integer> rows = toCopy.getRows();
		for(Integer i:rows){
			Collection<Integer> col = toCopy.getColumns(i);
			Map<Integer,Float> newColumn = new HashMap<Integer,Float>();
			for(Integer j:col){
				columnsIndex.add(j);
				newColumn.put(j,toCopy.getValue(i, j));
			}
			elements.put(i, newColumn);
		}

	}

	@Override
	public void setValue(int i, int j, float v) {
		if(v!=0.0f){
			nonZeros++;
			Map<Integer,Float> c=elements.get(i);
			if(c==null)
				c=new HashMap<Integer,Float>(0);
			c.put(j, v);

			elements.put(i, c);
			columnsIndex.add(j);
		}
		else{
			Map<Integer,Float> c=elements.get(i);
			if(c!=null)
				if(c.get(j)!=null){
					nonZeros--;
					c.remove(j);
					if(c.size()==0)
						elements.remove(i);
				}
		}
	}

	@Override
	public float getValue(int i, int j) {
		HashMap<Integer, Float> col = (HashMap<Integer, Float>)elements.get(i);
		if (col == null)
			return 0;
		Float value = col.get(j);
		if (value == null)
			return 0;
		return value;
	}

	@Override
	public float[][] getMatrix() {

		float [][] aux=new float[rows][];
		float [] nullRow=new float[columns];

		for(int i:elements.keySet()) {
			Map<Integer, Float> element_i = elements.get(i);
			float[] row = new float[columns];
			for(int j:element_i.keySet())
				row[j]=element_i.get(j);
			aux[i]=row;
		}

		for (int i=0; i<aux.length; i++)
			if (aux[i]==null)
				aux[i]=nullRow;
		return aux;
	}	

	@Override
	public void setValues(float[][] m) {
		nonZeros=0;
		for(int i=0;i<m.length;i++)
			for(int j=0;j<m[i].length;j++)
				if(m[i][j]!=0.0f){
					nonZeros++;
					columnsIndex.add(j);
					if(elements.containsKey(i))
						elements.get(i).put(j, m[i][j]);
					else{
						HashMap<Integer,Float> aux=new HashMap<Integer,Float>();
						aux.put(j, m[i][j]);
						elements.put(i, aux);
					}

				}
	}

	@Override
	public Matrix fastTrans(){
		Matrix res=FactoryMatrixHolder.getFactory().createMatrix(columns,rows);
		for(int i:elements.keySet()){
			Map<Integer,Float> col=elements.get(i);
			for(int j:col.keySet())
				res.setValue(j, i, col.get(j));
		}

		return res;
	}

	public Matrix fastMult(Matrix sec){
		if(this.columns!=sec.rows) 
			throw new ArrayIndexOutOfBoundsException();

		Matrix res=new MatrixSparseHash(rows, sec.columnSize());
		for(int i:elements.keySet()){
			Map<Integer,Float> col=elements.get(i);
			for(int j=0;j<sec.columnSize();j++){
				float aux=0;
				for(int k:col.keySet())
					aux+=col.get(k)*sec.getValue(k, j);
				if(aux!=0) 
					res.setValue(i, j, aux);
			}
		}
		return res;
	}

	public Matrix fastMult(float alfa) {

		Matrix res=FactoryMatrixHolder.getFactory().createMatrix(rows, columns);

		float aux=0;
		for(int i:elements.keySet()){
			Map<Integer,Float> col=elements.get(i);
			for(int j:col.keySet()){
				aux=col.get(j)*alfa;
				if(aux!=0) {
					res.setValue(i, j, aux);
				}						
			}
		}
		return res;
	}

	@Override
	public float getSparcityRow(int i) {
		Map<Integer,Float> el=elements.get(i);
		if(el==null)
			return 1;
		else
			return ((float)(columns-el.size()))/(float)columns;
	}

	@Override
	public void setRow(int k, Matrix m) {
		this.setRow(k, ((MatrixSparseHash)m).getRowH(k));	
	}

	/**
	 * This method forces the synchronisation of the operation as the operations on this type of Matrix could be performed in a multi-thread or distributed manner. 
	 * @param row the index coordinate of the row to set
	 * @param values a map with the index coordinates and the values of the row to set
	 */
	public synchronized void setRow(int row, Map<Integer, Float> values) {

		Map<Integer,Float> v=elements.get(row);
		if(v!=null)
			nonZeros-=v.size();

		if(values!=null){
			nonZeros+=values.size();
			elements.put(row, values);
			columnsIndex.addAll(values.keySet());
		}
	}

	/**
	 * @param row the index coordinate of the row to return
	 * @return a map with the index of the column coordinates in the row and their values
	 */
	public Map<Integer, Float> getRowH(int row) {
		return elements.get(row);
	}

	@Override
	public Collection<Integer> getColumns(int row){
		Map<Integer,Float> aux=elements.get(row);
		if(aux!=null)
			return elements.get(row).keySet();
		else
			return new HashSet<Integer>(0);
	}

	@Override
	public Collection<Integer> getRows(){
		return elements.keySet();
	}

	public void updateSparsity() {
		this.nonZeros = 0;
		for (Integer row: elements.keySet()) {
			nonZeros+=elements.get(row).keySet().size();
		}
	}

	@Override
	public List<Integer> getSortedRows() {
		List<Integer> aux=new ArrayList<Integer>(rows);
		for(int i=0;i<rows;i++)
			aux.add(i,i);

		Collections.sort(aux, new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				return new Float(MatrixSparseHash.this.getSparcityRow(o1)).compareTo(new Float(MatrixSparseHash.this.getSparcityRow(o2)));
			}
		});

		return aux;
	}

	@Override
	public float getNonZerosRow(int i) {
		Map<Integer,Float> el=elements.get(i);
		if(el==null)
			return 0;
		else
			return el.size();
	}

	@Override
	public boolean isDiagonal() {
		Set<Integer> rows = elements.keySet();
		for(Integer i:rows){
			Map<Integer,Float> col = elements.get(i);
			if(col.size()>1 || i!=col.keySet().iterator().next())
				return false;
		}
		return true;
	}

	@Override
	public Matrix fastLU(int[] piv) {

		Matrix LU = FactoryMatrixHolder.getFactory().createMatrix(this);
		((MatrixSparseHash)LU).transformIntoLU(piv);

		return LU;

	}

	protected void transformIntoLU(int[] piv) {

		for (int i = 0; i < rows; i++) {
			piv[i] = i;
		}

		int pivsign = 1;

		// Outer loop.

		List<Integer> sortedColumnIndex = new ArrayList<Integer>(columnsIndex);
		Collections.sort(sortedColumnIndex);

		List<Integer> sortedRowIndex = new ArrayList<Integer>(elements.keySet());
		Collections.sort(sortedRowIndex);

		for (int ij=0;ij<sortedColumnIndex.size();ij++) { //loop over columns

			int j = sortedColumnIndex.get(ij);

			// Apply previous transformations.

			for (int ii=0;ii<sortedRowIndex.size();ii++) { //loop over rows

				int i = sortedRowIndex.get(ii);
				Map<Integer,Float> rowI = elements.get(i);
				
				// Most of the time is spent in the following dot product.

				int kmax = Math.min(i,j);
				float s = 0f;
				for (int k = 0; k < kmax; k++) { //TODO

					Map<Integer,Float> rowK = elements.get(k);
					if(rowK!=null){
						Float ik = rowI.get(k);
						if(ik!=null){
							s += ik * rowK.get(j);
						}
					}
				}

				Float auxF = rowI.get(j);
				if (auxF==null)
					auxF = 0f;
				float aux = auxF - s;
				rowI.put(j,aux);

			}

			// Find pivot and exchange if necessary.

			int p = j; //j represents a column
			Map<Integer,Float> rowP = elements.get(p);
			for (int i = j+1; i < rows; i++) { //TODO
				if(rowP!=null){
					if (Math.abs(elements.get(i).get(j)) > Math.abs(rowP.get(j))) {	
						p = i;
						rowP = elements.get(p);
					}
				}
				else{
					p = i;
					rowP = elements.get(p);
				}

			}
			if (p != j) {

				if(rowP!=null){ //just in case

					for (int ik=0;ik<sortedColumnIndex.size();ik++) { 

						int k = sortedColumnIndex.get(ik);

						Float t = rowP.get(k);

						if(t == null)
							t = 0f;

						this.setValue(p, k, this.getValue(j, k));
						this.setValue(j, k, t);

					}
				}

				int k = piv[p]; 
				piv[p] = piv[j]; 
				piv[j] = k;
				pivsign = -pivsign;

			}

			// Compute multipliers.
			float jj = this.getValue(j, j);
			if (j < rows & jj != 0.0) {
				for (int i = j+1; i < rows; i++) {
					Map<Integer,Float> rowi = elements.get(i);
					Float rowIJ = rowi.get(j);
					if(rowIJ==null)
						rowIJ = 0f;

					rowi.put(j,rowIJ/jj );

				}
			}
		}

	}

	@Override
	public Matrix fastQR(float[] Rdiag) {

		Matrix QR = FactoryMatrixHolder.getFactory().createMatrix(this);
		((MatrixSparseHash)QR).transformIntoQR(Rdiag);

		return QR;
	}

	protected void transformIntoQR(float[] Rdiag) {

		List<Integer> sortedColumnIndex = new ArrayList<Integer>(columnsIndex);
		Collections.sort(sortedColumnIndex);

		// Main loop.
		for (int ik=0;ik<sortedColumnIndex.size();ik++) {

			int k = sortedColumnIndex.get(ik);

			// Compute 2-norm of k-th column without under/overflow.
			float nrm = 0;

			for (int i = k; i < rows; i++) {
				nrm = (float) Math.hypot(nrm,this.getValue(i,k));
			}

			if (nrm != 0.0) {
				// Form k-th Householder vector.
				if (this.getValue(k,k) < 0) {
					nrm = -nrm;
				}

				for (int i = k; i < rows; i++) {
					Map<Integer,Float> rowI = elements.get(i);
					if(rowI!=null){
						Float kv = rowI.get(k);
						if(kv!=null){
							kv = kv / nrm;
							rowI.put(k, kv);
						}
					}
				}

				Map<Integer,Float> rowK = elements.get(k);
				if(rowK!=null){
					Float kkv = rowK.get(k);
					if(kkv==null)
						kkv = 0f;
					rowK.put(k, kkv+1);
				}
				else{
					Map<Integer,Float> colK = new HashMap<Integer,Float>();
					colK.put(k, 1f);
					elements.put(k,colK);
				}

				// Apply transformation to remaining columns.

				for (int jj = ik+1; jj < sortedColumnIndex.size(); jj++) {

					int j = sortedColumnIndex.get(jj);

					Set<Integer> rowsK = new HashSet<Integer>();

					float s = 0f;
					for (int i = k; i < rows; i++) {

						Map<Integer, Float> rowI = elements.get(i);
						if(rowI != null){
							rowsK.add(i);
							Float ikv = rowI.get(k);
							if(ikv!=null){
								Float ijv = rowI.get(j);
								if(ijv!=null)
									s+= ikv * ijv;
							}
						}
					}

					s = -s/elements.get(k).get(k);

					for(Integer i:rowsK){
						//					for (int i = k; i < rows; i++) {

						Map<Integer,Float> rowI = elements.get(i);

						Float ijv = rowI.get(j);
						Float ikv = rowI.get(k);

						if(ijv==null)
							ijv = 0f;
						if(ikv==null)
							ikv = 0f;

						//						float aux = this.getValue(i,j) + s*this.getValue(i,k);
						this.setValue(i,j,ijv+s*ikv);
					}
				}
			}
			Rdiag[k] = -nrm;
		}

		//						//R 
		//						Matrix R = FactoryMatrixHolder.getFactory().createMatrix(columns,columns);
		//						for (int i = 0; i < columns; i++) {
		//							for (int j = 0; j < columns; j++) {
		//								if (i < j) {
		//									R.setValue(i,j,this.getValue(i,j));
		//								} else if (i == j) {
		//									R.setValue(i,j,Rdiag[i]);
		//								} else {
		//									R.setValue(i,j,0);
		//								}
		//							}
		//						}
		//		
		//						System.out.println("R\n"+R);
		//		
		//						//H
		//						Matrix H = FactoryMatrixHolder.getFactory().createMatrix(rows,columns);
		//						for (int i = 0; i < rows; i++) {
		//							for (int j = 0; j < columns; j++) {
		//								if (i >= j) {
		//									H.setValue(i,j,this.getValue(i,j));
		//								} else {
		//									H.setValue(i,j,0);
		//								}
		//							}
		//						}
		//		
		//						System.out.println("H\n"+H);  
		//		
		//						//Q
		//		
		//						Matrix Q = FactoryMatrixHolder.getFactory().createMatrix(rows,columns);
		//		
		//						for (int k = columns-1; k >= 0; k--) {
		//							for (int i = 0; i < rows; i++) {
		//								Q.setValue(i,k,0);
		//							}
		//							Q.setValue(k,k,1);
		//		
		//							for (int j = k; j < columns; j++) {
		//								if (this.getValue(k,k) != 0) {
		//									float s = 0f;
		//									for (int i = k; i < rows; i++) {
		//										s += this.getValue(i, k)*Q.getValue(i,j);
		//									}
		//									s = -s/this.getValue(k,k);
		//									for (int i = k; i < rows; i++) {
		//										float aux = Q.getValue(i, j) + s*Q.getValue(i, k);
		//										Q.setValue(i,j,aux);
		//									}
		//								}
		//							}
		//						}
		//		
		//						System.out.println("Q\n"+Q);

	}

	@Override
	public void findFastSolution(Matrix LU, Matrix B,int[] piv) {

		// Copy right hand side with pivoting
		for(int i=0;i<piv.length;i++){
			Map<Integer,Float> rowI = new HashMap<Integer,Float>();
			for(int j=0;j<columns;j++)
				rowI.put(j, B.getValue(piv[i],j));
			elements.put(i,rowI);
		}

		// Solve L*Y = B(piv,:)

		List<Integer> rowsLU = new ArrayList<Integer>(LU.getRows());
		Collections.sort(rowsLU);

		for(int ik = 0;ik<rowsLU.size();ik++){
			int k = rowsLU.get(ik);
			for(int ii = ik+1;ii<rowsLU.size();ii++){
				int i = rowsLU.get(ii);
				//		for (int k = 0; k < rows; k++) {
				//			for (int i = k+1; i < LU.rowSize(); i++) {
				for (int j = 0; j < B.columnSize(); j++) {
					//					float aux = this.getValue(i,j)-this.getValue(k, j)*LU.getValue(i,k);
					//					this.setValue(i, j, aux);
					Map<Integer,Float> rowI = elements.get(i);
					Float ijv = null;
					if(rowI!=null)
						ijv = rowI.get(j);
					if(ijv==null)
						ijv = 0f;

					float aux = ijv - this.getValue(k,j)*LU.getValue(i,k);
					rowI.put(j, aux);
					//					this.setValue(i, j, aux);
				}
			}
		}
		// Solve U*X = Y;
		for (int k = rows-1; k >= 0; k--) {

			Map<Integer,Float> rowk = elements.get(k);
			if(rowk!=null){
				float LUValue = LU.getValue(k,k);
				for (int j = 0; j < columns; j++) {
					Float kjv = rowk.get(j);
					if(kjv!=null){
						float aux = kjv / LUValue;
						this.setValue(k,j,aux);
						rowk.put(j,aux);
					}
				}
			}

			//			for (int j = 0; j < B.columnSize(); j++) {
			//				float aux = this.getValue(k,j) / LU.getValue(k,k); 
			//				this.setValue(k,j,aux);
			//			}
			//			for (int i = 0; i < k; i++) {
			//				for (int j = 0; j < B.columnSize(); j++) {
			//					float aux = this.getValue(i, j)-this.getValue(k,j)*LU.getValue(i, k);
			//					this.setValue(i,j,aux);
			//				}
			//			}
			for (int i = 0; i < k; i++) {

				Map<Integer,Float> rowI = elements.get(i);
				if(rowI!=null){
					float LUik = LU.getValue(i,k);
					for (int j = 0; j < columns; j++) { //TODO!
						Float ijv = rowI.get(j);
						if(ijv==null)
							ijv = 0f;
						float aux = ijv - this.getValue(k,j)*LUik;
						rowI.put(j, aux);
					}
				}
			}
		}
	}

	@Override
	public void findFastSolutionSquares(Matrix QR,float[] Rdiag) {

		int rowsQR = QR.rowSize();
		int columsQR = QR.columnSize();

		List<Integer> sortedColumnIndexes = new ArrayList<Integer>(columnsIndex);
		Collections.sort(sortedColumnIndexes);

		// Compute Y = transpose(Q)*B
		for (int k = 0; k < columsQR; k++) {
			for(int ij:sortedColumnIndexes){
				//			for (int j = 0; j < columns; j++) {

				int j = sortedColumnIndexes.get(ij);

				float s = 0f;
				for (int i = k; i < rowsQR; i++) {
					s += QR.getValue(i,k)*this.getValue(i, j);
				}
				s = -s/QR.getValue(k, k);
				for (int i = k; i < rowsQR; i++) {
					Map<Integer,Float> rowI = elements.get(i);
					Float ijv = 0f;
					if(rowI!=null){
						ijv=rowI.get(j);
						if(ijv==null)
							ijv = 0f;
						ijv += s*QR.getValue(i,k);
						rowI.put(j,ijv);
					}
					else
						this.setValue(i,j,s*QR.getValue(i,k));
				}
			}
		}
		// Solve R*X = Y;
		for (int k = columsQR-1; k >= 0; k--) {
			for (int j = 0; j < columns; j++) {
				Map<Integer,Float> rowK = elements.get(k);
				if(rowK!=null){
					Float kjv = rowK.get(j);
					if(kjv!=null){
						kjv = kjv / Rdiag[k];
						rowK.put(j, kjv);
					}
				}
			}

			for (int i = 0; i < k; i++) {
				Map<Integer,Float> rowI = elements.get(i);
				for (int j = 0; j < columns; j++) {

					if(rowI!=null){

						Float kjv = rowI.get(j);
						if(kjv==null)
							kjv = 0f;
						kjv = kjv - this.getValue(k, j) * QR.getValue(i,k);
						rowI.put(j, kjv);
					}
					else{ //If it does not exists, we have to check whether de value has to ve added!

						Float kjv = this.getValue(k, j);
						if(kjv!=0){
							rowI = new HashMap<Integer,Float>();
							kjv = kjv * QR.getValue(i, k);
							if(kjv!=0){
								rowI.put(j, kjv*QR.getValue(i,k));
								elements.put(i,rowI);
								columnsIndex.add(j);
							}
						}
					}
					//					float aux = this.getValue(i,j) - this.getValue(k,j)*QR.getValue(i,k);
					//					this.setValue(i,j,aux);
				}
			}

		}

		for(int i=columsQR;i<rows;i++){
			elements.remove(i);
		}

		rows = columsQR;

	}

	@Override
	public void invertDiagonal() {
		for(Integer i:elements.keySet()){
			Map<Integer,Float> rowI = elements.get(i);
			Float iiv = rowI.get(i);
			rowI.put(i, 1/iiv);
		}
	}

	@Override
	public Matrix fastCholesky() {
		Matrix cholesky = FactoryMatrixHolder.getFactory().createMatrix(this);
		boolean finished = ((MatrixSparseHash)cholesky).transformIntoCholesky(elements);
		if(finished)
			return cholesky;
		return null;
	}

	/**
	 * A copy of the matrix is needed in order to use the unmodified values.
	 * @param mElements The original matrix from which to compute the Cholesky decomposition.
	 * @return The Cholesky decomposition.
	 */
	private boolean transformIntoCholesky(Map<Integer,Map<Integer,Float>> mElements) {
		
		boolean symetricPositiveDefinite = rows == columns;

		// Main loop.
		for (int j = 0; j < rows; j++) {

			float d = 0f;
			Map<Integer,Float> mRowJ = mElements.get(j);

			Map<Integer,Float> cholRowJ = elements.get(j);

			for (int k = 0; k < j; k++) {

				Map<Integer,Float> cholRowK = elements.get(k);

				float s = 0f;
				for (int i = 0; i < k; i++) {

					Float kiv = cholRowK.get(i);
					if(kiv!=null){
						kiv = kiv * cholRowJ.get(i);
						if(kiv!=null)
							s += kiv;
					}
				}

				Float mjk = mRowJ.get(k);
				if(mjk==null)
					mjk = 0f;

				Float kkv = cholRowK.get(k);
				if(kkv!=null){
					s = (mjk - s)/kkv;
					if(s!=0)
						cholRowJ.put(k, s);
				}
				else
					s = 0;

				d = d + s*s;

				symetricPositiveDefinite = symetricPositiveDefinite & (mElements.get(k).get(j).equals(mRowJ.get(k))); 

				if(!symetricPositiveDefinite)
					return symetricPositiveDefinite;
			}
			d = mRowJ.get(j) - d;

			symetricPositiveDefinite = symetricPositiveDefinite & (d > 0.0);

			if(!symetricPositiveDefinite)
				return symetricPositiveDefinite;

			cholRowJ.put(j,(float)Math.sqrt(Math.max(d,0.0)));

			for (int k = j+1; k < rows; k++) {
				cholRowJ.remove(k);
			}
		}
		return symetricPositiveDefinite;
	}

	@Override
	public void findFastSolution(Matrix cholesky) {

		int n = cholesky.rowSize();

		// Solve L*Y = B;
		for (int k = 0; k < n; k++) {
			Map<Integer,Float> rowK = elements.get(k);
			for (int j = 0; j < columns; j++) {
				
				Float aux = rowK.get(j);
				if(aux==null)
					aux = 0f;
				for (int i = 0; i < k ; i++) {
					aux -= this.getValue(i, j)*cholesky.getValue(k,i);
				}
				aux = aux / cholesky.getValue(k,k);
				rowK.put(j,aux);
			}
		}

		// Solve L'*X = Y;
		for (int k = n-1; k >= 0; k--) {
			Map<Integer,Float> rowK = elements.get(k);
			for (int j = 0; j < columns; j++) {
				Float aux = rowK.get(j);
				if(aux == null)
					aux = 0f;
				for (int i = k+1; i < n ; i++) {
					aux -= this.getValue(i,j)*cholesky.getValue(i,k);
				}
				aux = aux / cholesky.getValue(k, k);
				rowK.put(j, aux);
			}
		}
	}

	@Override
	public boolean isSingular() {
		Set<Integer> rows = elements.keySet();
		for(Integer i:rows){
			if(elements.get(i).get(i)==null)
				return true;
		}
		return false;
	}
	
}
