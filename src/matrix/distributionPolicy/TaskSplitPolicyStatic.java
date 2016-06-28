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

import matrix.adapterDistribution.Configuration;
import matrix.matrixImpl.Matrix;

/**
 * This strategy is defined by a constant value equal to {@link Configuration#CLUSTER_PHYSICAL_CORES} * {@link Configuration#GRANULARITY_FACTOR}.
 * @author Antonela Tommasel
 *
 */
public class TaskSplitPolicyStatic implements TaskSplitPolicy {
	
	@Override
	public int getTasksForAddOrSubstract(Matrix left,Matrix right) {
		return defaultImpl();
	}

	@Override
	public int getTasksForMultiply(Matrix left,Matrix right) {
		return defaultImpl();
	}

	@Override
	public int getTasksForLaplacian(Matrix matrix) {
		return defaultImpl();
	}

	private int defaultImpl(){
		return Configuration.CLUSTER_PHYSICAL_CORES * Configuration.GRANULARITY_FACTOR;
	}
	
}
