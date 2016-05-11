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

import matrix.matrixImpl.Matrix;

/**
 * The goal of this class is to abstract how data is passed to the Feature Selection algorithm.
 * Several implementations could be provided.
 *   
 * @author Antonela Tommasel
 *
 */
public interface IntData {

	/**
	 * @return  For each post p_i, f_i in R^m are the set of feature values where f_i(j) is the frequency of f_j used by p_i . 
	 * F = {f1,f2, . . . ,fNl } in R n x m denotes the whole dataset.
	 */
	Matrix getFpostFeature();
	
	/**
	 * @return Let H in R N x n be an indicator matrix where H(i,j) = 1/|F_j| if u_j is the author of p_i.
	 */
	Matrix getH();

	/**
	 * @return Let P in R n xN denote user-post relationships where P(i, j) = 1 if p_j is posted by u_i, zero otherwise.
	 */
	Matrix getPcoPost();
	
	/**
	 * @return S a graph with adjacency matrix, where S(i, j) = 1 if there is a following relationship from u_j to u_i and zero otherwise.
	 */
	Matrix getSuserUser();
	
	/**
	 * @return  X is the matrix for labelled data. It represents a sub-matrix of F. 
	 */
	Matrix getXpostFeature();
	
	/**
	 * @return Let c = {c_1, c_2, . . . , c_k} denote the class label set where k is the number of classes. Y in R Nl x k is the class label matrix for labelled data 
	 * where Y(i, j) = 1 if p_i is labelled as c_j , otherwise zero.
	 */
	Matrix	getYpostCats();

}
