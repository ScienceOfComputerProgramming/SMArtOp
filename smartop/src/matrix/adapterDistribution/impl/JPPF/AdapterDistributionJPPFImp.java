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

import java.util.List;

import matrix.adapterDistribution.AdapterDistribution;
import matrix.adapterDistribution.AsynchronousTaskResultListener;
import matrix.adapterDistribution.Configuration;
import matrix.adapterDistribution.IDataShared;
import matrix.adapterDistribution.ITask;
import matrix.matrixImpl.Matrix;
import matrix.reconstructionStrategy.MatrixReconstructionStrategy;

import org.jppf.JPPFException;
import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.server.protocol.JPPFJobSLA;
import org.jppf.task.storage.MemoryMapDataProvider;

/**
 * Implements an adapter for JPPF
 * @author Antonela Tommasel
 *
 */
public class AdapterDistributionJPPFImp implements AdapterDistribution{

	transient IDataShared datashared;

	private JPPFJob job;
	
	static private JPPFClient client = null;
	static private Object lock = new Object();
	static private int counter = 0; //Reference counter for closing the connection when there are no more adapters
	
	/**
	 * class constructor
	 */
	public AdapterDistributionJPPFImp(){
		datashared = new DataSharedJPPFImp(new MemoryMapDataProvider());
		synchronized (lock) {
			if(client==null)
				client = new JPPFClient();
			counter++;
		}
	}

	@Override
	public void createJob(int taskNumber, Matrix res,MatrixReconstructionStrategy matrixReconstructionStrategy) {

		AsynchronousTaskResultListener listener = new AsynchronousTaskListenerJPPF(taskNumber,res,matrixReconstructionStrategy);
		
		job = new JPPFJob((AsynchronousTaskListenerJPPF)listener);
		job.setDataProvider(((DataSharedJPPFImp)datashared).getDataProvider());

		if(Configuration.MAX_NODOS!=0){
			JPPFJobSLA sla=new JPPFJobSLA();
			sla.setMaxNodes(Configuration.MAX_NODOS);
			job.setSLA(sla);
			System.out.println("MaxNodos Job -- "+job.getSLA().getMaxNodes());
		}
	}

	@Override
	public void addTask(ITask task) {
		
		TaskJPPF t = new TaskJPPF(task);
		
		try {
			job.addTask(t);
		} catch (JPPFException e) {
			e.printStackTrace();
		}
	
	}

	@Override
	public void submit() {
		try {
			client.submit(job);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public IDataShared getDataShared() {
		return datashared;
	}

	@Override
	public void waitUntilComplete() {
		((AsynchronousTaskResultListener) job.getResultListener()).waitUntilComplete();
	}

	@Override
	public void addTasks(List<ITask> task) {
		
		try {
			for(ITask t:task){
				job.addTask(new TaskJPPF(t));
			}
		} catch (JPPFException e) {
			e.printStackTrace();
		}
	
	}

	@Override
	public synchronized void close(){
		counter --;
		if(counter == 0){
			client.close();
			client=null;
		}
			
	}
	
}
