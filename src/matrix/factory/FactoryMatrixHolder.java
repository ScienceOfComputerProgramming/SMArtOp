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
package matrix.factory;

/**
 * Singleton that contains the default matrix factory.
 * @see FactoryMatrix
 * @author Antonela Tommasel
 *
 */
public class FactoryMatrixHolder {
	private static FactoryMatrix factory = null;
	
	/**
	 * Returns the factory.
	 * @return the factory to create all matrices
	 */
	public static FactoryMatrix getFactory() {
		return factory;
	}
	
	/**
	 * Sets a new default factory.
	 * @param factory the factory from which create all matrices
	 */
	public static void setFactory(FactoryMatrix factory) {
		FactoryMatrixHolder.factory = factory;
	}

}
