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
package matrix.matrixImpl;

import java.io.Serializable;

/**
 * This class is used to efficiently recover a value in a MatrixSparse.
 * It stores the coordinates and the number of columns of the matrix. This is done
 * to define a hashCode without collisions with the matrix context.
 * @author Antonela Tommasel
 *
 */
public class Terna implements Serializable{
	
	private static final long serialVersionUID = -367742630248541086L;
	
	/**
	 * Index of the row coordinate
	 */
	private int row;
	
	/**
	 * Index of the column coordinate
	 */
	private int column;
	
	/**
	 * Number of columns in the Matrix the Terna belongs to. It is used for computing the number of the
	 * element the Terna holds if all Matrix elements were stored in a 1D array.
	 */
	private int m;
	
	/**
	 * @param f the index of the row coordinate
	 * @param c the index of the column coordinate
	 * @param m number of columns of the Matrix the Terna belongs to.
	 */
	public Terna(int f,int c,int m){
		row=f;
		column=c;
		this.m=m;
	}

	/**
	 * @return the index of the row coordinate
	 */
	public int getRow(){
		return row;
	}
	
	/**
	 * @return the index of the column coordinate
	 */
	public int getColumn(){
		return column;
	}
	
	/**
	 * @return the number of columns the Terna belongs to
	 */
	public int getM(){
		return m;
	}
	
	@Override
	public boolean equals(Object t){
		if(t instanceof Terna){
			if(row==((Terna)t).getRow() && column==((Terna)t).getColumn() && m==((Terna)t).getM())
				return true;
		}
		return false;
	}
	
	/**
	 * As the instances of this class aimed to be used as keys in Hash structures, its hashcode is redefined
	 * considering a perfect hashing function, i.e. only one Terna per matrix would have the same hashcode.
	 */
	@Override
	public int hashCode(){
		return row*m+column;
	}
	
}
