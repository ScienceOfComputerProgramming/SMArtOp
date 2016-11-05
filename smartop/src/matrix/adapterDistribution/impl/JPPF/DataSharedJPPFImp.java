package matrix.adapterDistribution.impl.JPPF;

import org.jppf.node.protocol.DataProvider;

import matrix.adapterDistribution.IDataShared;


/**
 * This class provides the implementation that adapts {@link DataProvider} from JPPF to
 * the {@link IDataShared} interface.
 * @author Anto
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
			return this.dataProvider.getParameter(o1);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void putValue(String key, Object value) {
		try {
			this.dataProvider.setParameter(key, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns JPPF DataProvider
	 * @return JPPF DataProvider
	 */
	public DataProvider getDataProvider() {
		return this.dataProvider;
	}

}
