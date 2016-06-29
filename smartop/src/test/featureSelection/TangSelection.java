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
package test.featureSelection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import matrix.adapterDistribution.AdapterDistribution;
import matrix.adapterDistribution.Configuration;
import matrix.adapterDistribution.impl.JPPF.AdapterDistributionJPPFImp;
import matrix.distributionPolicy.TaskSplitPolicyMode;
import matrix.distributionPolicy.TaskSplitPolicyRowSparseness;
import matrix.distributionPolicy.TaskSplitPolicyST;
import matrix.distributionPolicy.TaskSplitPolicyStatic;
import matrix.factory.FactoryMatrixHolder;
import matrix.factory.FactoryMatrixSparse;
import matrix.factory.FactoryMatrixSparseHash;
import matrix.factory.FactoryMatrixSparseHashPar;
import matrix.factory.FactoryMatrixThreshold;
import matrix.factory.FactoryMatrixTrove;
import matrix.factory.FactoryMatrixTrovePar;
import matrix.matrixComp.MatrixComputation;
import matrix.matrixComp.MatrixComputationFD;
import matrix.matrixComp.MatrixComputationSparseDistributedDynamic;
import matrix.matrixComp.MatrixComputationSparseDistributedDynamicSorted;
import matrix.matrixComp.MatrixComputationSparsePar;
import matrix.matrixImpl.Matrix;

/**
 * This class implements the Feature Selection technique as described in "Feature Selection with Linked Data in Social Media".
 * 
 * Given labelled data X and its label indicator matrix Y, the whole dataset F, its 
 * social context(or social correlations) including user-user following relationships S and user-post relationships P, the goal is 
 * to select K most relevant features from m features on the dataset F with its social context S and P.
 * 
 * The purpose of this class is to allow the invocation of the operations needed to replicate the experimental evaluation performed
 * to assess the performance of the software.
 * 
 * @author Antonela Tommasel
 *
 */
public abstract class TangSelection {

	MatrixComputation algebra;
	private float epsilon;
	private float alfa;
	protected float beta;
	private int max_iterations = 100;

	/**
	 * Class Constructor
	 * @param algebra Instance of {@link MatrixComputation} to use
	 * @param epsilon defines the threshold for convergence
	 * @param alfa controls the row sparseness of W
	 * @param beta adjusts the contribution from the homophily hypothesis
	 */
	public TangSelection(MatrixComputation algebra, float epsilon, float alfa,float beta) {
		this.algebra = algebra;
		this.epsilon = epsilon;
		this.alfa = alfa;
		this.beta = beta;
	}

	/**
	 * Implements the complete Feature Selection algorithm returning a {@link List} with the codes of the selected features
	 * @param data contains all the needed information regarding the data involved in the computation
	 * @param K Number of features to select
	 * @return List of selected features
	 */
	public List<Integer> getKrelevantFeatures(IntData data, int K){

		Matrix B = computeB(data);

		
//		Matrix Dw = FactoryMatrixHolder.getFactory().createMatrix(B.rowSize(), B.columnSize());
//		setIdentity(Dw);
//
//		Matrix E = computeE(data);
//
//		Matrix Wt = FactoryMatrixHolder.getFactory().createMatrix(B.rowSize(), E.columnSize());
//
//		Wt.setValue(0, 0, 1000);
//
//		Matrix Wt1 = null;
//
//		boolean convergencia = false;
//
//		int i = 0; 
//
//		while (!convergencia && i< max_iterations ) {
//
//			Configuration.logger.info(new Date() + " Iteration Number: " + i + " "+ System.currentTimeMillis());
//
//			Configuration.logger.info("Starts Wt1 " + new Date() + " "+ System.currentTimeMillis());
//			Wt1 = computeWt1(B, Dw, E);
//			Configuration.logger.info("Finishes Wt1 " + new Date() + " "+ System.currentTimeMillis());
//
//			Configuration.logger.info("Starts Update Dw " + new Date() + " "+ System.currentTimeMillis());
//			updateDw(Wt1, Dw);
//			Configuration.logger.info("Finishes Update Dw " + new Date() + " "+ System.currentTimeMillis());
//
//			convergencia = converge(Wt, Wt1);
//			copy(Wt, Wt1);
//
//			i++;
//		}

//		return new ArrayList<Integer>(sortFeatures(Wt1, K).subList(0, K));
		return new ArrayList<Integer>();
	}

	private List<Integer> sortFeatures(Matrix wt1, int K) {
		ArrayList<Pair> norms = computeNorms(wt1);
		ArrayList<Integer> order = new ArrayList<Integer>(norms.size());
		Collections.sort(norms, Collections.reverseOrder());

		for (int i = 0; i < K; i++)
			order.add(Integer.parseInt(norms.get(i).getKey()));

		return order;
	}

	private ArrayList<Pair> computeNorms(Matrix wt1) {
		ArrayList<Pair> norms = new ArrayList<Pair>(wt1.rowSize());
		for (int i = 0; i < wt1.rowSize(); i++) {
			float acum = 0;
			for (int j = 0; j < wt1.columnSize(); j++)
				acum += wt1.getValue(i, j) * wt1.getValue(i, j);
			norms.add(new Pair((new Integer(i)).toString(), Math.sqrt(acum)));
		}

		return norms;
	}

	private boolean converge(Matrix wt, Matrix wt1) {
		for (int i = 0; i < wt.rowSize(); i++)
			for (int j = 0; j < wt.columnSize(); j++) {
				float diff = Math.abs(wt1.getValue(i, j) - wt.getValue(i, j));
				if (diff > epsilon)
					return false;
			}

		return true;
	}

	//Note than an additional type of task could be created to distribute this execution in the computer cluster
	private void updateDw(final Matrix wt1, final Matrix dw) {
		// Diagonal is updated as 1/(2*||Wt1(i,:)||2)

		long startTime = System.currentTimeMillis();

		Configuration.logger.log(Level.INFO,Configuration.getLogString("updateDw", wt1, null, null));

		ExecutorService th = Executors.newFixedThreadPool(Configuration.MAX_THREADS* Runtime.getRuntime().availableProcessors());
		final Semaphore sema = new Semaphore(Configuration.MAX_THREADS* Runtime.getRuntime().availableProcessors() + 1);

		for (int i = 0; i < wt1.rowSize(); i++) {

			final int i1 = i;

			try {
				sema.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Runnable r = new Runnable() {

				@Override
				public void run() {
					float val = 0;
					for (int j = 0; j < wt1.columnSize(); j++)
						val += wt1.getValue(i1, j) * wt1.getValue(i1, j);

					val = new Float(Math.sqrt(val));
					val *= 2;
					dw.setValue(i1, i1, new Float(1.0 / val));

					sema.release();
				}
			};
			th.submit(r);
		}

		try {
			sema.acquire(Configuration.MAX_THREADS* Runtime.getRuntime().availableProcessors() + 1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		th.shutdown();

		Configuration.logger.log(Level.INFO, Configuration.getLogTime("updateDw",System.currentTimeMillis()-startTime, null, dw));

	}

	private Matrix computeWt1(Matrix b, Matrix dw, Matrix e) {

		Matrix wt1; // Wt1=((B+alfa*Dw)^-1)*Et

		dw = algebra.multiply(alfa, dw);
		wt1 = algebra.add(b, dw);
		wt1 = algebra.invert(wt1);
		wt1 = algebra.multiply(wt1, e);

		return wt1;

	}

	private void copy(Matrix wt, Matrix wt1) {
		for (int i = 0; i < wt.rowSize(); i++)
			for (int j = 0; j < wt.columnSize(); j++)
				wt.setValue(i, j, wt1.getValue(i, j));
	}

	private void setIdentity(Matrix dw) {
		for (int i = 0; i < dw.rowSize(); i++)
			dw.setValue(i, i, 1);

	}

	protected abstract Matrix computeB(IntData datos);

	private Matrix computeE(IntData datos){

		Matrix Y = datos.getYpostCats();
		Matrix X = datos.getXpostFeature();

		Matrix E = algebra.multiply(Y, X);
		E = algebra.transpose(E);

		return E;

	}

	/**
	 * @param args It needs the path to the configuration file. Such file must contain definitions for:
	 * --> matrix = matrix type to be used in the executions
	 * --> max_threads = in the case of parallel executions, the factor for multiplying the number of cores and obtaining the total number of threads to create.
	 * --> cluster-physical-cores = number of physical cores of the computer cluster to use
	 * --> policy = policy for distributing the tasks among the computer cores - only when using a distributed alternative
	 * --> dataset = name of dataset to use
	 * --> dataset-path = path of the folder containing the dataset
	 * --> stopwords-path = path of the stopword file
	 * --> strategy = strategy of the feature selection approach
	 * --> granularity-factor = factor for configuring the strategy for creating the parallel tasks
	 * --> path-output = where to store the selected features
	 * --> alpha = controls the row sparseness of the matrix of selected features
	 * --> beta = adjusts the contribution from the homophily hypothesis
	 * --> epsilon = threshold for convergence
	 * --> K = number or percentage of features to select
	 */
	public static void main(String[] args){

		args=new String[1];
		args[0]="properties-file.properties";

		if (args.length == 0) {

			System.out.println("Needs the path of the configuration file.\n");

		} else {
			TangSelection t = null;
			MatrixComputation algebra = new MatrixComputationFD();

			Properties prop = new Properties();
			try {
				prop.load(new FileInputStream(args[0]));
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			System.out.println(prop);

			String mat = prop.getProperty("matrix");
			if (mat.length() > 0) {
				if (mat.equalsIgnoreCase("sparse"))
					FactoryMatrixHolder.setFactory(new FactoryMatrixSparse());
				else 
					if (mat.equalsIgnoreCase("threshold"))
						FactoryMatrixHolder.setFactory(new FactoryMatrixThreshold());
					else 
						if (mat.equalsIgnoreCase("sparsehash")) {
							FactoryMatrixHolder.setFactory(new FactoryMatrixSparseHash());
							algebra = new MatrixComputationSparsePar();
						} else 
							if (mat.equalsIgnoreCase("sparsehashpar")) {
								FactoryMatrixHolder.setFactory(new FactoryMatrixSparseHashPar());
								Configuration.MAX_THREADS = Integer.parseInt(prop.getProperty("max_threads"));
								algebra = new MatrixComputationSparsePar();
							} else 
								if(mat.equalsIgnoreCase("sparsetrovepar")){
									FactoryMatrixHolder.setFactory(new FactoryMatrixTrovePar());
									Configuration.MAX_THREADS = Integer.parseInt(prop.getProperty("max_threads"));
									algebra=new MatrixComputationSparsePar();
								} else
									if (mat.equalsIgnoreCase("sparsehashpardistributed")								
											|| mat.equalsIgnoreCase("sparsetrovepardistributed")) {

										AdapterDistribution adapter = new AdapterDistributionJPPFImp();

										String gran = prop.getProperty("granularity-factor");
										String physical = prop.getProperty("cluster-physical-cores");
										if (gran.length() > 0)
											Configuration.GRANULARITY_FACTOR = Integer.parseInt(gran);
										gran = null;
										if (physical.length() > 0)
											Configuration.CLUSTER_PHYSICAL_CORES = Integer.parseInt(physical);
										physical = null;

										String pol = prop.getProperty("policy");
										pol=pol.toLowerCase();
										if (pol.contains("rowsparseness"))
											Configuration.policy = new TaskSplitPolicyRowSparseness();
										else 
											if (pol.contains("rowst"))
												Configuration.policy = new TaskSplitPolicyST();
											else 
												if (pol.contains("mode"))
													Configuration.policy = new TaskSplitPolicyMode();
												else 
													if (pol.contains("static"))
														Configuration.policy = new TaskSplitPolicyStatic();

										String maxNodo=prop.getProperty("max-nodos-jppf");
										if(maxNodo.length()>0)
											Configuration.MAX_NODOS = Integer.parseInt(maxNodo);
										maxNodo=null;


										if (mat.equalsIgnoreCase("sparsehashpardistributed")) {
											FactoryMatrixHolder.setFactory(new FactoryMatrixSparseHash());

											if(pol.contains("dynamic-sorted"))
												algebra = new MatrixComputationSparseDistributedDynamicSorted(adapter);
											else
												if(pol.contains("dynamic"))
													algebra = new MatrixComputationSparseDistributedDynamic(adapter);
												else
													algebra = new MatrixComputationSparsePar();

											Configuration.MAX_THREADS = Integer.parseInt(prop.getProperty("max_threads"));
										} else 
											if(mat.equalsIgnoreCase("sparsetrovejppf")){
												FactoryMatrixHolder.setFactory(new FactoryMatrixTrove());
												//						FactoryMatrixHolder.setFactory(new FactoryMatrixSparseHashParJPPF());

												if(pol.contains("dynamic-sorted"))
													algebra = new MatrixComputationSparseDistributedDynamicSorted(adapter);
												else
													if(pol.contains("dynamic"))
														algebra = new MatrixComputationSparseDistributedDynamic(adapter);
													else
														algebra = new MatrixComputationSparsePar();

												Configuration.MAX_THREADS = Integer.parseInt(prop.getProperty("max_threads"));
											}

									}

			} else {
				System.out.println("The Matrix type is undefined.");
				System.exit(0);
			}

			mat = null;

			IntData p = new DiggParser(prop.getProperty("dataset-path"),prop.getProperty("stopwords-path"));

			Configuration.logger.info("Loading dataset ... "+new Date());

			try {
				((Parser) p).buildUserRelationships();
				((Parser) p).buildPostStructure();
			} catch (IOException e1) {
				e1.printStackTrace();
			}


			float alfa = Float.parseFloat(prop.getProperty("alpha"));
			float beta = Float.parseFloat(prop.getProperty("beta"));
			float epsilon = Float.parseFloat(prop.getProperty("epsilon"));
			int K = 0;


			StringBuilder b = null;


			b = new StringBuilder();
			b.append(prop.getProperty("dataset"));
			b.append("-");
			b.append(prop.getProperty("strategy"));
			b.append("-");

			int numFeatures = ((Parser) p).getNumFeatures();
			if (prop.getProperty("K").contains("."))
				K = Math.round(Float.parseFloat(prop.getProperty("K")) * numFeatures);
			else
				K = Integer.parseInt(prop.getProperty("K"));

			b.append(alfa);
			b.append("-");
			b.append(beta);
			b.append("-");
			b.append(epsilon);
			b.append("-");
			b.append(K);

			File o = new File(prop.getProperty("path-output") + File.separator+ b.toString() + ".output");

			if (!o.exists() || o.length() == 0) {
				try {
					o.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}

				String str = prop.getProperty("strategy");

				if (str.equalsIgnoreCase("copost"))
					t = new CoPost(algebra, epsilon, alfa, beta);

				Configuration.logger.info(t.toString());

				str = null;

				Configuration.logger.info("Starting Feature Selection ... "+new Date());
				List<Integer> features = t.getKrelevantFeatures(p, K);		
				Configuration.logger.info("Finishing Feature Selection ... "+new Date());

				try {
					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(o, false));
					out.writeObject(features);
					out.close();
					Configuration.logger.info("Features Saved on Disk ... "+new Date());
				} catch (IOException e) {
					e.printStackTrace();
				}					
			}
		}
	}

}
