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
package matrix.adapterDistribution.impl.JPPF;

import matrix.adapterDistribution.IDataShared;
import matrix.adapterDistribution.ITask;

import org.jppf.client.JPPFJob;
import org.jppf.server.protocol.JPPFTask;

/**
 * This class adapts {@link ITask} to be run as part of a {@link JPPFJob}.
 * @author Antonela Tommasel
 *
 */
public class TaskJPPF extends JPPFTask{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8052135224252452492L;
	
	ITask task;
	
	/**
	 * Creates a new task for JPPF that wraps a {@link ITask}
	 * @param task task to be executed
	 */
	public TaskJPPF(ITask task){
		this.task = task;
	}
	
	@Override
	public void run() {
		try{
			IDataShared ds = new DataSharedJPPFImp(getDataProvider());
			task.setDataShared(ds);
			task.run();
		}
		catch(Exception e){
			setException(e);
			return;
		}
		setResult(task.getResult());
	}

}
