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
package matrix.adapterDistribution;

import java.util.Date;
import java.util.logging.Logger;

import matrix.distributionPolicy.TaskSplitPolicy;
import matrix.distributionPolicy.TaskSplitPolicyStatic;
import matrix.factory.FactoryMatrixHolder;
import matrix.matrixImpl.Matrix;
import matrix.matrixImpl.MatrixThreshold;

/**
 * @author Antonela Tommasel
 *
 */
public class Configuration {
	
	/**
	 * 
	 */
	public static int GRANULARITY_FACTOR = 1;
	
	/**
	 * 
	 */
	public static int MAX_NODOS = 5;
	
	/**
	 * 
	 */
	public static int CLUSTER_PHYSICAL_CORES = 5;
	
	/**
	 * Used for computing the number of threads to create. The default value is 1.
	 */
	public static int MAX_THREADS = 1;
	
	/**
	 * 
	 */
	public static TaskSplitPolicy policy = new TaskSplitPolicyStatic();
	
	/**
	 * 
	 */
	public static float tolerance = 0.0001f;

	/**
	 * Threshold to be use for deciding when to change the internal representation of the
	 * {@link MatrixThreshold}.
	 */
	public static float sparsenessThreshold = 0.25f;
	
	/**
	 * 
	 */
	public static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	/**
	 * To be used for logging the start of an operation.
	 * @param opName operation to be performed
	 * @param m left matrix
	 * @param m1 right matrix
	 * @param PARALLEL_FACTOR factor used for distributing the operation
	 * @return String containing all the information to log
	 */
	public static String getLogString(String opName,Matrix m,Matrix m1,Integer PARALLEL_FACTOR){
		StringBuffer sb = new StringBuffer();
		String sep = "; ";
		sb.append("opName: ");
		sb.append(opName);
		sb.append(sep);
		sb.append("matrixType: ");
		sb.append(FactoryMatrixHolder.getFactory().getClass().toString());
		sb.append(sep);
		sb.append("leftMatrixDim: ");
		sb.append(m.rowSize()+ "x" + m.columnSize());
		sb.append(sep);
		sb.append("leftMatrixSparsity: ");
		sb.append(m.getSparsity());
		sb.append(sep);
		if(m1!=null){
			sb.append("rightMatrixDim: ");
			sb.append(m1.rowSize()+ "x" + m1.columnSize());
			sb.append(sep);
			sb.append("rightMatrixSparsity: ");
			sb.append(m1.getSparsity());
			sb.append(sep);
		}
		if(PARALLEL_FACTOR!=null){
			int rowsPerCore = m.rowSize() / PARALLEL_FACTOR;
			sb.append("RowsPerCore :");
			sb.append(rowsPerCore);
			sb.append(sep);
			long nonZerosPerCore = m.getNonZeros() / PARALLEL_FACTOR;
			sb.append("NonZerosPerCore :");
			sb.append(nonZerosPerCore);
			sb.append(sep);
		}
		
		sb.append(new Date());
		
		return sb.toString();
	}
	
	/**
	 * To be used for logging the result of an operation. 
	 * @param opName performed Operation
	 * @param time milliseconds taken by the operation
	 * @param policy distribution policy
	 * @param m result matrix
	 * @return String containing all the information to log
	 */
	public static String getLogTime(String opName, long time, String policy,Matrix m){
		StringBuffer sb = new StringBuffer();
		String sep = "; ";
		sb.append("opName: ");
		sb.append(opName);
		sb.append(sep);
		sb.append("time: ");
		sb.append(time);
		sb.append(sep);
		sb.append("policy ");
		sb.append(policy);
		sb.append(sep);
		sb.append("Result dimensions: ");
		sb.append(m.rowSize()+ "x" + m.columnSize());
		sb.append(sep);
		sb.append("Result sparsity: ");
		sb.append(m.getSparsity());
		sb.append(sep);
		sb.append(new Date());
		return sb.toString();
	}
	
}
