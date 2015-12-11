package at.ac.tuwien.thesis.caddc.persistence;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import at.ac.tuwien.thesis.caddc.model.DAPrice;
import at.ac.tuwien.thesis.caddc.model.Location;
import at.ac.tuwien.thesis.caddc.persistence.exception.LocationNotFoundException;
import at.ac.tuwien.thesis.caddc.util.DateParser;
import at.ac.tuwien.thesis.caddc.util.DateUtils;

/**
 * 
 * @author Andreas Egger
 */
@Stateless
public class DAPricePersistence {

	@Inject
	private EntityManager em;
	
	public void saveDAPrice(DAPrice price) {
    	em.persist(price);
    }
    
    public void saveDAPriceList(List<DAPrice> daPriceList) {
    	for(DAPrice daPrice : daPriceList)
    		saveDAPrice(daPrice);
    }
    
    /**
     * Method to save a list of prices encoded in Strings
     * @param prices a String array containing prices encoded in Strings
     * 			The encoding for a single price needs to have the following form: 
     * 			datestring;hour;price
     * 			Example: 2015-07-11;02;44,23
     * @param location a location Object of one of the locations stored in the 
     * 			database
     * @param lastDate the last bidding date saved for the given location
     * @throws LocationNotFoundException is thrown when the given location is not registered
     */
    public void saveDAPrices(List<String> priceData, Location location, Date lastDate) throws LocationNotFoundException {
		if(location == null) {
			throw new LocationNotFoundException("Please provide a registered location");
		}
		
		String tz = location.getTimeZone();
		TimeZone timeZone;
		if(!tz.isEmpty()) {
			timeZone = TimeZone.getTimeZone(tz);
		}
		else {
			timeZone = TimeZone.getDefault();
		}
		Calendar cal = Calendar.getInstance(timeZone);
		Calendar temp = Calendar.getInstance();
    	
    	for(int i = 0; i < priceData.size(); i++) {
    		String[] split = priceData.get(i).split(";");
    		if(split.length != 3) {
    			System.err.println("Price data format at index "+i+" is invalid: "+priceData.get(i));
    			continue;
    		}
    		String dateString = split[0];
    		String timeString = split[1];
    		String price = split[2];
    		
    		int finalPrice;
    		boolean negative = false;
    		if(price.contains("-")) {
    			negative = true;
    		}
    		if(!price.contains(".") && !price.contains(",")) {
    			finalPrice = Integer.parseInt(price) * 100;
    		}
    		else {
    			String[] priceParts = null;
    			if(price.contains(","))
        			priceParts = price.split(",");
        		if(price.contains("."))
        			priceParts = price.split("\\.");
        		
        		if(priceParts[1].length() == 1) {
        			priceParts[1] = priceParts[1] + "0";
        		}
        		
        		int priceBeforeComma = Integer.parseInt(priceParts[0])*100;
        		int priceAfterComma = Integer.parseInt(priceParts[1]);
        		finalPrice = priceBeforeComma + priceAfterComma; // price in integer, multiplied by 100
        		if(negative)
        			finalPrice *= -1;
    		}
    		if(i < 25) {
    			System.out.println("price for location "+location.getId()+": "+dateString+", "+timeString+", "+price);
    			System.out.println("price for location "+location.getId()+": "+dateString+", "+timeString+", "+finalPrice);
    		}
    		
    		Date d = DateParser.parseDate(dateString);
    		temp.setTime(d);
    		cal.set(temp.get(Calendar.YEAR), temp.get(Calendar.MONTH), temp.get(Calendar.DATE));
    		
    		Integer hour = (int)Double.parseDouble(timeString);
    		cal.set(Calendar.HOUR_OF_DAY, hour); // hour is set on the calendar with local timezone
    		cal.set(Calendar.MINUTE, 0);
    		cal.set(Calendar.SECOND, 0);
    		cal.set(Calendar.MILLISECOND, 0);
    		
    		int timeLag = timeZone.getOffset(cal.getTimeInMillis()) / 3600000; // 3600000 is the minimum offset for a 
																		// dst time (one hour in milliseconds)

    		// skip saving data for dates before the last saved date
    		if(!cal.getTime().after(lastDate)) {
    			continue;
    		}
    		
    		DAPrice daPrice = new DAPrice();
        	daPrice.setBiddingDate(cal.getTime());
        	daPrice.setInterval(1);
        	daPrice.setIntervalUnit("hour");
        	daPrice.setLocation(location);
        	daPrice.setPrice(finalPrice);
        	daPrice.setTimelag(timeLag);
        	
        	saveDAPrice(daPrice);
    	}
    }
    
    public Date findMaxDate(Location location) {
    	TypedQuery<Date> q = em.createNamedQuery("DAPrice.findMaxDate", Date.class);
    	q.setParameter("locationId", location.getId());
    	return q.getSingleResult();
    }
}
