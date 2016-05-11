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

/**
 * This interface provides a common protocol for sharing data across
 * the cluster. Implementations of {@link AdapterDistribution} can be used
 * for retrieving the appropriate implementation of this class.
 * @author Antonela Tommasel
 */

public interface IDataShared extends Serializable{

	/**
	 * Retrieves the shared object that is mapped to a particular key.
	 * @param key the key to the object to retrieve
	 * @return the retrieved object or null if the object does not exist
	 */
	public Object getValue(String key);
	
	/**
	 * Shares a object across the cluster and associates it to a key.
	 * @param key the key of the object to store
	 * @param value the value to score
	 */
	public void putValue(String key, Object value);

}
