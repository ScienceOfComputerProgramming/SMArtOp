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
package matrix.distributionPolicy;

import java.util.logging.Level;

import matrix.adapterDistribution.Configuration;
import matrix.matrixImpl.Matrix;

/**
 * This class defines the PARALLEL_FACTOR based on the average sparseness of the rows and
 * the number of physical cores.
 * @author Antonela Tommasel
 *
 */
public class TaskSplitPolicyRowSparseness implements TaskSplitPolicy{
	
	/**
	 * alpha as defined by getAlpha of the first matrix.
	 * 
	 * <br>beta is 1 minus the ln of the relation between the number of rows and columns of the first matrix when #rows < #columns
	 * <br>beta is 1 minus the ln of the relation between the number of rows and columns of the first matrix when #rows > #columns
	 * <br>beta is 1 when  #rows == #columns
	 * 
	 * <br>the PARALLEL_FACTOR is max(1,round(#physical_cores*(1+alpha+beta))
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public int getTasksForAddOrSubstract(Matrix left,Matrix right) {
		
		float alfa=getAlpha(left);
		float beta=0;
		
		if(left.rowSize()<left.columnSize()){
			beta=(float) (1-Math.log(left.rowSize()/(float)left.columnSize()));
		}
		else
			if(left.rowSize()>left.columnSize()){
				beta=(float) (1+Math.log(left.columnSize()/(float)left.rowSize()));
			}
			else
				beta=1; //in case of square matrix
				
		
		float denominador = Configuration.CLUSTER_PHYSICAL_CORES*(1+alfa+beta);

		int res=Math.round(denominador);
		
		if(res==0)
			res=1;
		
		Configuration.logger.log(Level.INFO,"----- ALFA - "+alfa+" - BETA - "+beta+" - DEN "+denominador+" - RES: "+res);
		
		return res;
	}

	/**
	 * Defines alpha as 1 - avgSparseness.
	 * @param left
	 * @return alpha
	 */
	protected float getAlpha(Matrix left) {
		float []aux=left.getSparcityRowAvg();
		Configuration.logger.log(Level.INFO,"----- SPARSITY - Avg "+aux[0]+" - st "+aux[1]+" - mode "+aux[2]);
		return 1-aux[0];
	}

	/**
	 * Uses the same strategy as {@link #getTasksForAddOrSubstract(Matrix, Matrix)}
	 * {@inheritDoc}
	 */
	@Override
	public int getTasksForMultiply(Matrix left,Matrix right) {
		return getTasksForAddOrSubstract(left, right);
	}

	/**
	 * Returns the number of physical cores.
	 */
	@Override
	public int getTasksForLaplacian(Matrix matrix) {
		return Configuration.CLUSTER_PHYSICAL_CORES;
	}

}
