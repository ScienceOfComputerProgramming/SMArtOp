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
 * This class defines the PARALLEL_FACTOR based on the number of physical cores, and the average sparseness of the rows and its Standard Deviation.
 * @author Antonela Tommasel
 */
public class TaskSplitPolicyST extends TaskSplitPolicyRowSparseness{

	/**
	 * Defines alpha as 1-avgSparseness-stdDevSparseness
	 */
	@Override
	protected float getAlpha(Matrix left) {
		float[] s=left.getSparcityRowAvg();
		Configuration.logger.log(Level.INFO,"----- SPARSITY - Avg "+s[0]+" - st "+s[1]+" - mode "+s[2]);
		return 1-(s[0]-s[1]);
	}
	
}
