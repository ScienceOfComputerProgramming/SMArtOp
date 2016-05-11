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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

import matrix.adapterDistribution.Configuration;
import matrix.factory.FactoryMatrixHolder;

/**
 * @author Antonela Tommasel
 *
 */

/** Implementation similar to that of {@link MatrixSparseHash} only that the {@link #fastMult(float)} and
 * {@link #fastMult(Matrix)} operations are implemented in a multi-threaded manner*/
public class MatrixSparseHashPar extends MatrixSparseHash{

	private static final long serialVersionUID = 1L;

	/**
	 * Class constructor
	 * @param f number of rows of the matrix to create.
	 * @param c number of columns of the matrix to create.
	 */
	public MatrixSparseHashPar(int f, int c) {
		super(f, c);
	}

	/** 
	 * Creates a Matrix by copying the values of the other Matrix
	 * @param toCopy Matrix to copy the values in the new Matrix
	 */
	public MatrixSparseHashPar(Matrix toCopy) {
		super(toCopy);
	}

	@Override
	public synchronized void setValue(int i, int j, float v) {
		super.setValue(i,j,v);
	}

	public Matrix fastMult(final Matrix sec){

		if(this.columns!=sec.rows) 
			throw new ArrayIndexOutOfBoundsException();

		final Matrix res=FactoryMatrixHolder.getFactory().createMatrix(rows, sec.columnSize());

		ExecutorService th=Executors.newFixedThreadPool(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors());

		final Semaphore sema=new Semaphore(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors()+1);

		for(int i:elements.keySet()){
			final int i1=i;
			try {
				sema.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Runnable r=new Runnable() {

				@Override
				public void run() {

					Map<Integer,Float> col=elements.get(i1);
					for(int j=0;j<sec.columnSize();j++){
						float aux=0;
						for(int k:col.keySet())
							aux+=col.get(k)*sec.getValue(k, j);
						res.setValue(i1, j, aux);
					}
					sema.release();
				}
			};

			th.submit(r);
		}

		try {
			sema.acquire(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors()+1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		th.shutdown();

		return res;
	}

	public Matrix fastMult(final float alfa) {

		final Matrix res=FactoryMatrixHolder.getFactory().createMatrix(rows, columns);

		ExecutorService th=Executors.newFixedThreadPool(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors());
		final Semaphore sema=new Semaphore(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors()+1);

		for(int i:elements.keySet()){

			final int i1=i;
			try {
				sema.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Runnable r=new Runnable() {

				@Override
				public void run() {
					Map<Integer,Float> col=elements.get(i1);
					for(int j:col.keySet()){
						float aux=col.get(j)*alfa;						
						res.setValue(i1, j, aux);
					}
					sema.release();
				}
			};
			th.submit(r);	
		}
		try {
			sema.acquire(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors()+1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		th.shutdown();

		return res;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void transformIntoLU(final int[] piv) {
		
		ExecutorService th=Executors.newFixedThreadPool(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors());
		final Semaphore sema=new Semaphore(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors()+1);
		
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

			final int j = sortedColumnIndex.get(ij);

			// Apply previous transformations.

			for (int ii=0;ii<sortedRowIndex.size();ii++) { //loop over rows

				int i = sortedRowIndex.get(ii);
				final Map<Integer,Float> rowI = elements.get(i);
				// Most of the time is spent in the following dot product.

				int kmax = Math.min(i,j);
				float s = 0f;
				Future<Float>[] ff = new Future[kmax];
				for (int k1 = 0; k1 < kmax; k1++) { //TODO
					final int k = k1;
					
					ff[k] = th.submit(new Callable<Float>() {
						
						@Override
						public Float call() {
							float s = 0;
							Map<Integer,Float> rowK = elements.get(k);
							if(rowK!=null){
								Float ik = rowI.get(k);
								if(ik!=null){
									s += ik * rowK.get(j);
								}
							}
							return s;
						}
					});
				}

				for (Future<Float> f:ff)
					try {
						s+=f.get();
					} catch (Exception e) {
						throw new RuntimeException(e);
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
			final float jj = this.getValue(j, j);
			if (j < rows & jj != 0.0) {
				for (int i1 = j+1; i1 < rows; i1++) {
					final int i = i1;
					try {
						sema.acquire();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					th.submit(new Runnable() {
						
						@Override
						public void run() {
							Map<Integer,Float> rowi = elements.get(i);
							Float rowIJ = rowi.get(j);
							if(rowIJ==null)
								rowIJ = 0f;

							rowi.put(j,rowIJ/jj );
							sema.release();
						}
					});
				}
				try {
					sema.acquire(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors()+1);
					sema.release(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors()+1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	protected void transformIntoQR(final float[] Rdiag) {
		final List<Integer> sortedColumnIndex = new ArrayList<Integer>(columnsIndex);
		Collections.sort(sortedColumnIndex);

		ExecutorService th=Executors.newFixedThreadPool(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors());
		final Semaphore sema=new Semaphore(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors()+1);

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
				final int kf = k ;
				for (int jj1 = ik+1; jj1 < sortedColumnIndex.size(); jj1++) {
					final int jj = jj1;
					try {
						sema.acquire();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					th.submit(new Runnable() {

						@Override
						public void run() {
							try {
								int j = sortedColumnIndex.get(jj);

								Set<Integer> rowsK = new HashSet<Integer>();

								float s = 0f;
								for (int i = kf; i < rows; i++) { 

									Map<Integer, Float> rowI = elements.get(i);
									if(rowI != null){
										rowsK.add(i);
										Float ikv = rowI.get(kf);
										if(ikv!=null){
											Float ijv = rowI.get(j);
											if(ijv!=null)
												s+= ikv * ijv;
										}
									}
								}

								s = -s/elements.get(kf).get(kf);

								for(Integer i:rowsK){

									Map<Integer,Float> rowI = elements.get(i);

									Float ijv = rowI.get(j);
									Float ikv = rowI.get(kf);

									if(ijv==null)
										ijv = 0f;
									if(ikv==null)
										ikv = 0f;

									MatrixSparseHashPar.this.setValue(i,j,ijv+s*ikv);
								}	
							} catch(Exception e){
								e.printStackTrace();
							}
							sema.release();
						}
					});
				}

				try {
					sema.acquire(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors()+1);
					sema.release(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors()+1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Rdiag[k] = -nrm;
		}
		th.shutdown();
	}


	@Override
	public void findFastSolution(Matrix LU,Matrix B,int [] piv){
		super.findFastSolution(LU, B, piv);
	}

	@Override
	public void findFastSolutionSquares(final Matrix QR,final float [] Rdiag){
		
		ExecutorService th=Executors.newFixedThreadPool(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors());
		final Semaphore sema=new Semaphore(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors()+1);
		
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
			
			final ReentrantLock lock = new ReentrantLock();
			final int kf = k;
			for (int i1 = 0; i1 < k; i1++) {
				final int i = i1;
				try {
					sema.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				th.submit(new Runnable() {

					@Override
					public void run() {


						Map<Integer,Float> rowI = elements.get(i);
						for (int j = 0; j < columns; j++) {

							if(rowI!=null){

								Float kjv = rowI.get(j);
								if(kjv==null)
									kjv = 0f;
								kjv = kjv - getValue(kf, j) * QR.getValue(i,kf);
								rowI.put(j, kjv);
							}
							else{ //If it does not exists, we have to check whether the value has to be added!

								Float kjv = getValue(kf, j);
								if(kjv!=0){
									rowI = new HashMap<Integer,Float>();
									kjv = kjv * QR.getValue(i, kf);
									if(kjv!=0){
										rowI.put(j, kjv*QR.getValue(i,kf));
										lock.lock();
										elements.put(i,rowI);
										columnsIndex.add(j);
										lock.unlock();
									}
								}
							}

						}
						sema.release();
					}
				});
			}
			try {
				sema.acquire(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors()+1);
				sema.release(Configuration.MAX_THREADS*Runtime.getRuntime().availableProcessors()+1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		for(int i=columsQR;i<rows;i++){
			elements.remove(i);
		}

		th.shutdown();
		rows = columsQR;

	}

}
