package org.dragon.vince;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class RetrievePoiTask extends AsyncTask<Location, Void, List<String>> {
	private DanyActivity mainActivity;
	
	public RetrievePoiTask(DanyActivity activity) {
		this.mainActivity = activity;
	}
	
	@Override
	protected List<String> doInBackground(Location... params) {
		String text = null;
		DefaultHttpClient client = new DefaultHttpClient();
    	HttpResponse response = null;
		Location location = params[0];
		List<String> keywords = new ArrayList<String>();
    	// Open search and take first result, always		
		HttpGet uri = null;
		try {
			uri = new HttpGet("http://api.geonames.org/findNearbyWikipedia?lat=" + String.valueOf(location.getLatitude()) + "&lng=" + String.valueOf(location.getLongitude()) + "&username=vcarluer&lang=" + this.mainActivity.getLanguage().getLanguage().toLowerCase());
			response = client.execute(uri);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			if (location == null) {
				DanyActivity.Loge(DanyActivity.DANY, "Location is null");
			} else {
				DanyActivity.Loge(DanyActivity.DANY, "location: " + location.toString());
			}
			
			if (this.mainActivity.getLanguage() == null) {
				DanyActivity.Loge(DanyActivity.DANY, "Language is null");
			} else {
				DanyActivity.Loge(DanyActivity.DANY, "Language: " + this.mainActivity.getLanguage().getLanguage().toString());
			}
			
			e.printStackTrace();
		}
    	
		if (response != null) {
			StatusLine status = response.getStatusLine();
	    	if (status.getStatusCode() != 200) {
	    		DanyActivity.Logd(DanyActivity.DANY, "HTTP error, invalid server status code: " + response.getStatusLine());  
	    	} else {
	    		HttpEntity entity = response.getEntity();
	    		try {
	    			text = EntityUtils.toString(entity);															
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}
		}
		
		if (text != null) {			
			Document doc = XmlHelper.xmlfromString(text);
			NodeList nodes = doc.getElementsByTagName("title");			
			for (int i = 0; i < nodes.getLength(); i++) {
				String keyword = nodes.item(i).getTextContent();
				if(keyword != null) {					
					 keywords.add(keyword);
				}								
			}
		}
		
		return keywords;
	}

	@Override
	protected void onPostExecute(List<String> result) {
		this.mainActivity.stopSearchBar();
		if (result != null && result.size() > 0) {
			String keyword = result.get(0);
			SearchParam search = new SearchParam();
			search.searchWord = keyword;
			search.isStrictSearch = true;
			this.mainActivity.search(search);
			this.mainActivity.setPois(result);
		} else {
			this.mainActivity.setHasResult(false);
		}
	}
}
