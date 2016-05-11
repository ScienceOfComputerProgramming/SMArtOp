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

/**
 * @author Antonela Tommasel
 *
 */
public class Pair implements Comparable<Pair>{

	private String key;
	private double value;
	
	/**
	 * @param k key to store
	 * @param v value to store
	 */
	public Pair(String k,double v){
		key=k;
		value=v;
	}
	
	public int compareTo(Pair arg0) {
		if(this.value>arg0.getValue())
			return 1;
		else
			if(this.value==arg0.getValue())
				return 0;
			else
				return -1;
	}
	
	/**
	 * @return returns the stored value
	 */
	public double getValue(){
		return value;
	}
	
	/**
	 * @return returns the stored key
	 */
	public String getKey(){
		return key;
	}
	
	public String toString(){
		return key+", "+value;
	}
	
}
