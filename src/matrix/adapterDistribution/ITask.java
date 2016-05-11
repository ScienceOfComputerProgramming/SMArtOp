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

import java.io.Serializable;

import matrix.matrixImpl.Matrix;

/**
 * 
 * This class provides a common interface for all matrix operation sub-tasks
 * that can be run in a distributed fashion. The task logic should be implemented
 * in the method {@link #run()}. 
 * @author Antonela Tommasel
 */
public abstract class ITask implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1968915110520086581L;
	
	/**
	 * Allows to access shared data in the cluster
	 * through a common interface. It is mostly used for retrieving input
	 * data.
	 */
	protected transient IDataShared dataShared;
	
	/**
	 * Carries the task result. It should be initialised in the 
	 * {@link #run} method to avoid serialising an empty field.
	 */
	protected Matrix res;
	
	/**
	 * Executes the created task.
	 */
	public abstract void run();
	
	/**
	 * @return the matrix holding the result of the task
	 */
	public Matrix getResult(){
		return res;
	}
	
	/**
	 * Sets the shared data structure.
	 * This method is invoked by the middleware adapters to
	 * set an appropriated implementation of the {@link IDataShared} interface. 
	 * @param dataShared data structure to be shared
	 */
	public void setDataShared(IDataShared dataShared){
		this.dataShared = dataShared; 
	}
	
}
