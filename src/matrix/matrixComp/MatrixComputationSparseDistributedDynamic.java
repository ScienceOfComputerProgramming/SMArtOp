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
import matrix.adapterDistribution.ITask;
import matrix.adapterDistribution.tasks.MutiplicationTask;
import matrix.matrixImpl.Matrix;

/**
 * @author Antonela Tommasel
 *
 */
public class MatrixComputationSparseDistributedDynamic extends MatrixComputationSparseDistributed{

	/**
	 * @param adapter adapter for the middleware
	 */
	public MatrixComputationSparseDistributedDynamic(AdapterDistribution adapter) {
		super(adapter);
	}

	/**
	 * Creates the number of tasks that results of dividing the number of non-zero elements by the PARALLEL_FACTOR. 
	 * If the division is not exact, it creates another task with the remaining non-zero rows.
	 * 
	 * @param m
	 * @param PARALLEL_FACTOR
	 * @return a {@link List} with the tasks to be executed in parallel
	 */
	@Override
	protected List<ITask> createTasks(Matrix m, int PARALLEL_FACTOR) {

		long nonZerosPerTask = m.getNonZeros() / PARALLEL_FACTOR;
		
		if(nonZerosPerTask==0)
			nonZerosPerTask++;

		int endRow = 0;

		List<ITask> tasks = new ArrayList<ITask>();

		for(int startRow=0;startRow<m.rowSize();startRow=endRow){

			long nonzeros=0;
			while(endRow<m.rowSize() && (nonzeros<nonZerosPerTask || (m.getSparcityRow(endRow)==1.0))){				

				nonzeros+=m.getNonZerosRow(endRow);

				endRow++;

			}

			Matrix leftSubMatrix = m.getSubmatrix(startRow, endRow);

			long nonZerosZ = leftSubMatrix.getNonZeros();
			ITask task = new MutiplicationTask(leftSubMatrix);
			tasks.add(task);
			Configuration.logger.log(Level.INFO,"Creating multiply task for: [" + startRow + "," + endRow + "]" + " nonZeros=" + nonZerosZ + " sparseness= "+leftSubMatrix.getSparsity());
			
		}
		return tasks;
	}
}