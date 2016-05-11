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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import matrix.adapterDistribution.AdapterDistribution;
import matrix.adapterDistribution.Configuration;
import matrix.adapterDistribution.IDataShared;
import matrix.adapterDistribution.ITask;
import matrix.adapterDistribution.tasks.AddRowTask;
import matrix.adapterDistribution.tasks.LaplacianTask;
import matrix.adapterDistribution.tasks.MutiplicationTask;
import matrix.adapterDistribution.tasks.SubtractRowTask;
import matrix.factory.FactoryMatrixHolder;
import matrix.matrixImpl.Matrix;
import matrix.reconstructionStrategy.LaplacianMatrixReconstructionStrategy;
import matrix.reconstructionStrategy.RowMatrixReconstructionStrategy;

/**
 * 
 * Provides the implementation for performing arithmetic operations between sparse matrices. 
 * All operations are performed in a cluster. It uses an {@link AdapterDistribution} as an interface for different middlewares.
 * @author Antonela Tommasel
 *
 */
public class MatrixComputationSparseDistributed extends MatrixComputationSparse {
	
	AdapterDistribution adapter;
	
	/**
	 * 
	 */
	public static final String FACTORY = "factory";
	
	/**
	 * 
	 */
	public static final String SECOND_MATRIX = "right-matrix";
	
	/**
	 * 
	 */
	public static final String LAPLACIAN_MATRIX = "A";
	
	/**
	 * @param adapter adapter for the middleware
	 */
	public MatrixComputationSparseDistributed(AdapterDistribution adapter){
		this.adapter = adapter;
	}
	
	@Override
	public Matrix add(Matrix m, Matrix m1){

		long startTime = System.currentTimeMillis();
		
		int PARALLEL_FACTOR = Configuration.policy.getTasksForAddOrSubstract(m, m1);
		Matrix result = FactoryMatrixHolder.getFactory().createMatrix(m.rowSize(), m.columnSize());
		IDataShared dataShared = adapter.getDataShared();
		
	
		try {
			
			Configuration.logger.log(Level.INFO,Configuration.getLogString("add-distributed", m, m1, PARALLEL_FACTOR));
			
			dataShared.putValue(FACTORY, FactoryMatrixHolder.getFactory());
			
			int rowsPerCore = m.rowSize() / PARALLEL_FACTOR;
			if(rowsPerCore==0)
				rowsPerCore++;

			int endRow = 0;
			List<ITask> tasks = new ArrayList<ITask>();
			
			for(int startRow=0;startRow<m.rowSize();startRow=endRow){
				endRow = endRow + rowsPerCore;
				
				if(endRow>m.rowSize())
					endRow=m.rowSize();
				
				ITask task = new AddRowTask(startRow, endRow, m.getSubmatrix(startRow, endRow), m1.getSubmatrix(startRow, endRow));
				tasks.add(task);
				System.out.println("Creating add task for: [" + startRow + "," + endRow + "]");
			}
			
			adapter.createJob(tasks.size(),result,new RowMatrixReconstructionStrategy());
			adapter.addTasks(tasks);
			adapter.submit();
			adapter.waitUntilComplete();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		Configuration.logger.log(Level.INFO, Configuration.getLogTime("add-distributed",System.currentTimeMillis()-startTime, Configuration.policy.getClass().toString(), result));
		
		return result;
		
	}
	
	@Override
	public Matrix subtract(Matrix m, Matrix m1){

		long startTime = System.currentTimeMillis();
		
		Matrix result = FactoryMatrixHolder.getFactory().createMatrix(m.rowSize(), m.columnSize());
		IDataShared dataShared = adapter.getDataShared();
		int PARALLEL_FACTOR=Configuration.policy.getTasksForAddOrSubstract(m, m1);
		
		try {
			
			Configuration.logger.log(Level.INFO,Configuration.getLogString("subtract-distributed", m, m1, PARALLEL_FACTOR));
			
			dataShared.putValue(FACTORY, FactoryMatrixHolder.getFactory());
			
			int rowsPerCore = m.rowSize() / PARALLEL_FACTOR;
			if(rowsPerCore==0)
				rowsPerCore++;

			int endRow = 0;
			List<ITask> tasks = new ArrayList<ITask>();
			
			for(int startRow=0;startRow<m.rowSize();startRow=endRow){
				endRow = endRow + rowsPerCore;
				
				if(endRow>m.rowSize())
					endRow=m.rowSize();
				
				ITask task = new SubtractRowTask(startRow, endRow, m.getSubmatrix(startRow, endRow), m1.getSubmatrix(startRow, endRow));
				tasks.add(task);
				System.out.println("Creating subtract task for: [" + startRow + "," + endRow + "]");
			}
			
			adapter.createJob(tasks.size(),result,new RowMatrixReconstructionStrategy());
			adapter.addTasks(tasks);
			adapter.submit();
			adapter.waitUntilComplete();
			
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		Configuration.logger.log(Level.INFO, Configuration.getLogTime("subtract-distributed",System.currentTimeMillis()-startTime, Configuration.policy.getClass().toString(), result));
		
		return result;
		
	}

	/**
	 * The tasks are created following the strategy defined in createTasks(Matrix, int), 
	 * where the parameters are (m, PARALLEL_FACTOR).<br>
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public Matrix multiply(Matrix m, Matrix sec){
		
			if(m.columnSize()!=sec.rowSize()) 
				throw new ArrayIndexOutOfBoundsException();
			
			long startTime = System.currentTimeMillis();
			
			int PARALLEL_FACTOR = Configuration.policy.getTasksForMultiply(m, sec);
			Matrix result = FactoryMatrixHolder.getFactory().createMatrix(m.rowSize(), sec.columnSize());
			IDataShared dp = adapter.getDataShared();
			try {
				
				Configuration.logger.log(Level.INFO,Configuration.getLogString("multiply-distributed", m, sec, PARALLEL_FACTOR));
				
				dp.putValue(SECOND_MATRIX, sec);
				dp.putValue(FACTORY,FactoryMatrixHolder.getFactory());
					
				List<ITask> tasks = createTasks(m, PARALLEL_FACTOR);
				
				adapter.createJob(tasks.size(),result,new RowMatrixReconstructionStrategy());
				adapter.addTasks(tasks);
				adapter.submit();
				adapter.waitUntilComplete();
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			Configuration.logger.log(Level.INFO, Configuration.getLogTime("multiply-distributed",System.currentTimeMillis()-startTime, Configuration.policy.getClass().toString(), result));
			
			return result;
		}

	/**
	 * Creates the number of tasks that results of dividing the number of rows by the PARALLEL_FACTOR. 
	 * If the division is not exact, it creates another task with the remaining rows.
	 * @param m
	 * @param PARALLEL_FACTOR
	 * @return a {@link List} with the tasks to be executed in parallel
	 */
	protected List<ITask> createTasks(Matrix m, int PARALLEL_FACTOR) {
		
		int rowsPerCore = m.rowSize() / PARALLEL_FACTOR;
		
		if(rowsPerCore==0)
			rowsPerCore++;
		
		int endRow = 0;
		List<ITask> tasks = new ArrayList<ITask>();
		
		for(int startRow=0;startRow<m.rowSize();startRow=endRow){
			endRow = endRow + rowsPerCore;
			
			if(endRow>m.rowSize())
				endRow=m.rowSize();
			
			Matrix leftSubMatrix = m.getSubmatrix(startRow, endRow);

			long nonZerosZ = leftSubMatrix.getNonZeros();
			
			ITask task = new MutiplicationTask(leftSubMatrix);
			tasks.add(task);
			Configuration.logger.log(Level.INFO,"Creating multiply task for: [" + startRow + "," + endRow + "]" + " nonZeros=" + nonZerosZ);
		}
		return tasks;
	}

		
	
	@Override
	public Matrix laplacian(Matrix A) {
		
		long startTime = System.currentTimeMillis();
		
		Matrix Da=FactoryMatrixHolder.getFactory().createMatrix(A.rowSize(),A.columnSize());
		IDataShared dp = adapter.getDataShared();
		int PARALLEL_FACTOR = Configuration.policy.getTasksForLaplacian(A);
		
		try {
			
			Configuration.logger.log(Level.INFO,Configuration.getLogString("multiply-distributed", A, null, PARALLEL_FACTOR));
			
			dp.putValue(LAPLACIAN_MATRIX, A);
			dp.putValue(FACTORY, FactoryMatrixHolder.getFactory());
				
			int rowsPerCore = Da.rowSize() / PARALLEL_FACTOR;
			
			if(rowsPerCore==0)
				rowsPerCore++;
			
			int endRow = 0;
			ArrayList<ITask> tasks = new ArrayList<ITask>();
			
			for(int startRow=0;startRow<Da.rowSize();startRow=endRow){
				endRow = endRow + rowsPerCore;
				
				if(endRow>Da.rowSize())
					endRow=Da.rowSize();
				
				ITask task = new LaplacianTask(startRow, endRow, Da.columnSize()); 
				
				tasks.add(task);
				
				System.out.println("Creating laplacian task for: [" + startRow + "," + endRow + "]");
			}
			
			adapter.createJob(tasks.size(),Da,new LaplacianMatrixReconstructionStrategy());
			adapter.addTasks(tasks);
			adapter.submit();
			adapter.waitUntilComplete();
			
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		Da=subtract(Da,A);
		
		Configuration.logger.log(Level.INFO, Configuration.getLogTime("laplacian-distributed",System.currentTimeMillis()-startTime, Configuration.policy.getClass().toString(), Da));
		return Da;
	}

}