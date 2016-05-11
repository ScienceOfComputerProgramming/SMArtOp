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

import java.util.Date;

import matrix.adapterDistribution.Configuration;
import matrix.matrixComp.MatrixComputation;
import matrix.matrixImpl.Matrix;

/**
 * One of the homophily hypothesis presented in "Feature Selection with Linked Data in Social Media".
 * This hypothesis assumes that posts by the same user are of similar topics. 
 * In other words, the posts of a user are more similar, in terms of topics (say, "sports", "music"), than those randomly selected posts
 * @author Antonela Tommasel
 *
 */
public class CoPost extends TangSelection{

	/**
	 * Class Constructor
	 * @param algebra Instance of {@link MatrixComputation} to use
	 * @param epsilon defines the threshold for convergence
	 * @param alfa controls the row sparseness of W
	 * @param beta adjusts the contribution from the homophily hypothesis
	 */
	public CoPost(MatrixComputation algebra, float epsilon, float alfa,float beta) {
		super(algebra, epsilon, alfa, beta);
	}

	@Override
	protected Matrix computeB(IntData data) {

		Configuration.logger.info("---- Starts B "+new Date());
		
		Matrix A = data.getPcoPost();
		A = algebra.multiplyByTranspose(algebra.transpose(A)); // Pt x P -- Matrix Multiplication I
		A = algebra.laplacian(A); // Laplacian A -- Addition-Subtraction I & Addition-Subtraction II

		Matrix aux = algebra.multiply(beta, data.getXpostFeature()); //beta x F 
		aux = algebra.multiply(aux, A); //Matrix Multiplication II

		Matrix Ftrans = algebra.transpose(data.getFpostFeature()); 
		aux = algebra.multiply(aux,Ftrans); //Matrix Multiplication III

		Matrix X = algebra.multiplyByTranspose(data.getXpostFeature()); //Matrix Multiplication IV 
		Matrix B = algebra.add(X, aux); //Addition-Subtraction III

		aux=null;
		X=null;
		A=null;

		Configuration.logger.info("---- Finishes B "+new Date());
		
		return B;
	}

}
