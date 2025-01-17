package at.ac.tuwien.thesis.caddc.model.handler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import at.ac.tuwien.thesis.caddc.model.Location;
import at.ac.tuwien.thesis.caddc.model.type.EnergyMarketType;
import at.ac.tuwien.thesis.caddc.model.type.EnergyPriceType;
import at.ac.tuwien.thesis.caddc.util.DateParser;

/**
 * 
 */
public class EnergyPriceHandlerBelgium extends EnergyPriceHandler {
	
	

	@Override
	public List<EnergyPriceType> parseEnergyPriceData(List<String> priceData, Location location, Date lastDate, boolean debug) {
    	
    	List<EnergyPriceType> energyPrices = new ArrayList<EnergyPriceType>();
    	
    	TimeZone timeZone = TimeZoneDSTHandler.getTimeZone(location);
		Calendar cal = Calendar.getInstance(timeZone);
		Calendar temp = Calendar.getInstance();
		
		Integer timeLag;
		Integer finalPrice;
		boolean dstOn = false;
		boolean dstOff = false;
		
    	for(int i = 0; i < priceData.size(); i++) {
    		String[] split = priceData.get(i).split(";");
    		if(split.length != 3) {
    			System.err.println("Price data format at index "+i+" is invalid: "+priceData.get(i));
    			continue;
    		}
    		String dateString = split[0];
    		String timeString = split[1];
    		String price = split[2];
    		
    		finalPrice = parsePrice(price);
    		
    		Date d = DateParser.parseDate(dateString);
    		temp.setTime(d);
    		cal.set(temp.get(Calendar.YEAR), temp.get(Calendar.MONTH), temp.get(Calendar.DATE));
    		
    		Integer hour = Integer.parseInt(timeString);
    		cal.set(Calendar.HOUR_OF_DAY, hour); // hour is set on the calendar with local timezone
    		cal.set(Calendar.MINUTE, 0);
    		cal.set(Calendar.SECOND, 0);
    		cal.set(Calendar.MILLISECOND, 0);
    		
    		
    		// Location specific import changes
			if(isDSTDateOff(d, location)) {
				if(dstOff  &&  hour == 2) {
					dstOff = false;
				}
				else if(hour == 2) {
					cal.set(Calendar.HOUR_OF_DAY, hour-1);
					cal.add(Calendar.HOUR_OF_DAY, 1);
					dstOff = true;
				}
			}
    		
			// skip saving data for dates before the last saved date
    		if(lastDate != null  &&  !cal.getTime().after(lastDate)) {
    			continue;
    		}
    		
    		// getTimeInMillis() will always return number of milliseconds since 1970 for UTC time
    		timeLag = (int) (timeZone.getOffset(cal.getTimeInMillis()) / EnergyMarketType.MIN_DST_TIME);    	
    		
    		// debug output
    		if(debug) {
    			
    			if(i < 25) {
        			System.out.println("price for location "+location.getId()+": "+dateString+", "+timeString+", "+price);
        			System.out.println("price for location "+location.getId()+": "+dateString+", "+timeString+", "+finalPrice);
        		}
    			
    			if((isDSTDateOn(d, location) || isDSTDateOff(d, location))  
    									&&  hour < 6) {
    				
    				DateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
                	formatter.setTimeZone(timeZone);
        			System.out.println("price for location "+location.getId()+": "+dateString+", "+timeString+", "+finalPrice+", "+timeLag
        					+", "+formatter.format(cal.getTime()) + ", "+cal.getTimeInMillis());
    			}
    		}
    		
    		EnergyPriceType energyPrice = new EnergyPriceType();
    		energyPrice.setDate(cal.getTime());
    		energyPrice.setPrice(finalPrice);
    		energyPrice.setTimeLag(timeLag);
    		energyPrices.add(energyPrice);
    	}
    	
    	return energyPrices;
    }
	
	@Override
	protected Locale getLocale() {
		return Locale.ENGLISH;
	}
}
