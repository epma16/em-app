package at.ac.tuwien.thesis.caddc.data.market.rt;

import at.ac.tuwien.thesis.caddc.data.market.MarketDataRT;
import at.ac.tuwien.thesis.caddc.data.resource.rt.ResourceManagerMichiganRT;
import at.ac.tuwien.thesis.caddc.model.Location;
import at.ac.tuwien.thesis.caddc.persistence.RTPricePersistence;




/**
 * Defines a MarketData Instance responsible for retrieving real time 
 * energy market data from Maine for different data sources
 */
public class MarketDataMichiganRT extends MarketDataRT {
	
	/**
	 * Create a MarketData Instance with the given location
	 * @param location the location for this MarketData Instance
	 */
	public MarketDataMichiganRT(Location location, RTPricePersistence persistence) {
		super(location, persistence);
		this.resourceManager = new ResourceManagerMichiganRT();
	}
}
