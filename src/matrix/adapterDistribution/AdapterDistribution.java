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

import java.io.Closeable;
import java.util.List;

import matrix.matrixImpl.Matrix;
import matrix.reconstructionStrategy.MatrixReconstructionStrategy;

/**
 * <tt>AdapterDistribution</tt> defines the methods for executing an operation
 * in a distributed fashion.
 * Example of usage:
 * <pre>
 * Matrix res = ...; //creates a matrix in which the result is reconstructed
 * MatrixReconstructionStrategy strategy = ...; //Defines how tasks' results are combined
 * AdapterDistribution adapter = ...;
 * IDataShared dataShared = adapter.getDataShared();
 * 
 * List<ITask> tasks = getTasks(); // returns the task to be executed in the cluster
 * 
 * adapter.createJob(tasks.size(), res, strategy):
 * adapter.addTasks(tasks);
 * adapter.submit();
 * adapter.waitUntilComplete();
 * </pre>
 * After the method {@link #waitUntilComplete()} returns the {@link Matrix} <tt>res</tt> should contain
 * the reconstructed result of executing all tasks.
 * @author Antonela Tommasel
 */
public interface AdapterDistribution extends Closeable {

	/**
	 * Adds a task to a job.
	 * @param task newly created task to be added to the job
	 */
	public void addTask(ITask task);
	
	
	/**
	 * Adds a list of tasks to a job.
	 * @param task newly created task to be added to the job
	 */
	public void addTasks(List<ITask> task);
	
	/**
	 * Executes the created job
	 */
	public void submit();
		
	/**
	 * Initialises the adapter to submit a new job, which comprises a set of tasks, to be executed in the cluster.
	 * @param taskNumber number of tasks in the job
	 * @param res {@link Matrix} in which the result would be place
	 * @param matrixReconstructionStrategy strategy for reducing the results of each task into a single result.
	 */
	public void createJob(int taskNumber, Matrix res,MatrixReconstructionStrategy matrixReconstructionStrategy);
	
	/**
	 * Gets the data shared mechanism.
	 * @return an instance of {@link IDataShared} for sharing data within the cluster.
	 */
	public IDataShared getDataShared();

	/**
	 * Blocks the execution of the current thread until all tasks are completed.
	 */
	public void waitUntilComplete();

	/**
	 * Closes the underline connection with the cluster.
	 */
	@Override
	public void close();
			
}
