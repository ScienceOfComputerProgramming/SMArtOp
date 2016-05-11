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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import matrix.adapterDistribution.Configuration;
import matrix.factory.FactoryMatrixHolder;
import matrix.matrixImpl.Matrix;

/**
 * @author Antonela Tommasel
 *
 */

/** Provides the implementation for performing arithmetic operations between dense float matrices in a multi-threaded manner.*/
public class MatrixComputationSparsePar extends MatrixComputationSparse {

	/**
	 * 
	 */
	public MatrixComputationSparsePar(){
		name = "multi-thread";
	}
	
	@Override
	public Matrix subtract(final Matrix m, final Matrix m1) {

		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("subtract-"+name, m, m1,Configuration.MAX_THREADS* Runtime.getRuntime().availableProcessors()));
		
		final Matrix aux = FactoryMatrixHolder.getFactory().createMatrix(m.rowSize(), m.columnSize());
		
		ExecutorService th = Executors.newFixedThreadPool(Configuration.MAX_THREADS* Runtime.getRuntime().availableProcessors());
		
		final Semaphore sema = new Semaphore(Configuration.MAX_THREADS* Runtime.getRuntime().availableProcessors() + 1);

		for (int i = 0; i < m.rowSize(); i++) {
			final int i1 = i;
			try {
				sema.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Runnable r = new Runnable() {

				@Override
				public void run() {
					for (int j = 0; j < m.columnSize(); j++)
						aux.setValue(i1, j,
								m.getValue(i1, j) - m1.getValue(i1, j));
					sema.release();
				}
			};
			th.submit(r);
		}

		try {
			sema.acquire(Configuration.MAX_THREADS * Runtime.getRuntime().availableProcessors() + 1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		th.shutdown();
		
		Configuration.logger.log(Level.INFO, Configuration.getLogTime("subtract-"+name,System.currentTimeMillis()-startTime, "Threads: "+Configuration.MAX_THREADS, aux));
		
		return aux;
	}

	@Override
	public Matrix add(final Matrix m, final Matrix m1) {

		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("adding-"+name, m, m1,Configuration.MAX_THREADS* Runtime.getRuntime().availableProcessors()));

		final Matrix aux = FactoryMatrixHolder.getFactory().createMatrix(m.rowSize(), m.columnSize());

		ExecutorService th = Executors.newFixedThreadPool(Configuration.MAX_THREADS* Runtime.getRuntime().availableProcessors());
		final Semaphore sema = new Semaphore(Configuration.MAX_THREADS* Runtime.getRuntime().availableProcessors() + 1);

		for (int i = 0; i < m.rowSize(); i++) {
			final int i1 = i;
			try {
				sema.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Runnable r = new Runnable() {

				@Override
				public void run() { 
					for (int j = 0; j < m.columnSize(); j++)
						aux.setValue(i1, j,
								m.getValue(i1, j) + m1.getValue(i1, j));

					sema.release();
				}
			};
			th.submit(r);
		}

		try {
			sema.acquire(Configuration.MAX_THREADS* Runtime.getRuntime().availableProcessors() + 1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		th.shutdown();

		Configuration.logger.log(Level.INFO, Configuration.getLogTime("addition-multi-thread",System.currentTimeMillis()-startTime, "Threads: "+Configuration.MAX_THREADS, aux));

		return aux;
	}

	@Override
	public Matrix laplacian(final Matrix m) {

		long startTime = System.currentTimeMillis();
		Configuration.logger.log(Level.INFO,Configuration.getLogString("laplacian-"+name, m, null,Configuration.MAX_THREADS* Runtime.getRuntime().availableProcessors()));
		
		final Matrix Da = FactoryMatrixHolder.getFactory().createMatrix(m.rowSize(), m.columnSize());
		ExecutorService th = Executors.newFixedThreadPool(Configuration.MAX_THREADS* Runtime.getRuntime().availableProcessors());
		
		final Semaphore sema = new Semaphore(Configuration.MAX_THREADS* Runtime.getRuntime().availableProcessors() + 1);

		for (int i = 0; i < Da.rowSize(); i++) {

			final int i1 = i;
			try {
				sema.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Runnable r = new Runnable() {

				@Override
				public void run() {
					float sum = 0;
					for (int j = 0; j < Da.columnSize(); j++)
						sum += m.getValue(j, i1);
					Da.setValue(i1, i1, sum);
					sema.release();
				}
			};

			th.submit(r);
		}

		try {
			sema.acquire(Configuration.MAX_THREADS* Runtime.getRuntime().availableProcessors() + 1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		th.shutdown();

		Matrix aux = subtract(Da, m);
		
		Configuration.logger.log(Level.INFO, Configuration.getLogTime("laplacian-"+name,System.currentTimeMillis()-startTime, "Threads: "+Configuration.MAX_THREADS, aux));
		
		return aux;
	}
	
}
