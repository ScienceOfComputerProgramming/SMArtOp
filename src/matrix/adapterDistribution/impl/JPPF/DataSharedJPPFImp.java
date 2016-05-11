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

import org.jppf.task.storage.DataProvider;

/**
 * This class provides the implementation that adapts {@link DataProvider} from JPPF to
 * the {@link IDataShared} interface.
 * @author Antonela Tommasel
 *
 */
public class DataSharedJPPFImp implements IDataShared{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	DataProvider dataProvider;
	
	/**
	 * Creates an {@link IDataShared} adapter for a {@link DataProvider}.
	 * @param dp instance of the JPPF data provider
	 */
	public DataSharedJPPFImp(DataProvider dp){
		this.dataProvider = dp;
	}
	
	@Override
	public Object getValue(String o1) {
		try {
			return this.dataProvider.getValue(o1);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void putValue(String key, Object value) {
		try {
			this.dataProvider.setValue(key, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the JPPF DataProvider
	 * @return JPPF DataProvider
	 */
	public DataProvider getDataProvider() {
		return this.dataProvider;
	}

}
