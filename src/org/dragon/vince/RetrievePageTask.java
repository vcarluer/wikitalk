package org.dragon.vince;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class RetrievePageTask extends AsyncTask<String, Void, String> {
	private WikitalkActivity mainActivity;
	private RetrieveImagesTask retrieveImage;
	private String search;
	private String pageId;
	
	public RetrievePageTask(WikitalkActivity activity) {
		this.mainActivity = activity;
		this.retrieveImage = new RetrieveImagesTask(this.mainActivity);
	}
	
	@Override	
	protected String doInBackground(String... params) {
		String line = null;
    	search = params[0];
    	HttpGet uri = new HttpGet("http://fr.wikipedia.org/w/api.php?format=xml&action=query&titles=" + search + "&prop=revisions&rvprop=content");
    	// close client request?
    	DefaultHttpClient client = new DefaultHttpClient();
    	HttpResponse response = null;
		try {
			response = client.execute(uri);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (response != null) {
			StatusLine status = response.getStatusLine();
	    	if (status.getStatusCode() != 200) {
	    	    Log.d(WikitalkActivity.WIKITALK, "HTTP error, invalid server status code: " + response.getStatusLine());  
	    	} else {
	    		HttpEntity entity = response.getEntity();
	    		try {
					line = EntityUtils.toString(entity);															
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}
		}
		
    	return line;
	}

	@Override
	protected void onPostExecute(String result) {
		Document doc = XmlHelper.xmlfromString(result);
		NodeList nodes = doc.getElementsByTagName("page");
		for (int i = 0; i < nodes.getLength(); i++) {
			 this.pageId = nodes.item(i).getAttributes().getNamedItem("pageid").getNodeValue();
			 // Only first one for now
			 break;
		}
		
		nodes = doc.getElementsByTagName("rev"); 
		 for (int i = 0; i < nodes.getLength(); i++) {
			 String line = nodes.item(i).getTextContent();
			 this.mainActivity.addTextToRead(line);			 
		}
		 
		 this.retrieveImage.execute(pageId);
						
		super.onPostExecute(result);
	}
}
