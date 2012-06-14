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
import android.net.Uri;
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
    	search = Uri.encode(params[0]);
    	HttpGet uri = new HttpGet("http://" + this.mainActivity.getLanguageLc() + ".wikipedia.org/w/api.php?format=xml&action=query&titles=" + search + "&prop=revisions&rvprop=content");
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
		Node pageNode = null;
		for (int i = 0; i < nodes.getLength(); i++) {
			pageNode = nodes.item(i).getAttributes().getNamedItem("pageid");
			// Handle here multiple results (take first or propose)
			if(pageNode != null) {
				this.pageId = pageNode.getNodeValue();
				 // Only first one for now
				 break;
			}
		}
		
		// Use pageNode here to get image only from node with pageid?
		nodes = doc.getElementsByTagName("rev"); 	
		if (nodes != null && nodes.getLength() > 0) {
			for (int i = 0; i < nodes.getLength(); i++) {
				 String line = nodes.item(i).getTextContent();
				 if (line.contains("#REDIRECT")) {					 
					 int posS = line.indexOf("[[");
					 if (posS > -1) {
						 posS = posS + 2;
					 }
					 
					 int posE = line.indexOf("]]");
					 if (posE > -1 && posS > -1) {
						 String redirect = line.substring(posS, posE);
						 this.mainActivity.search(redirect);
					 }
				 } else {
					 this.mainActivity.addTextToRead(line);
					 
					 if (this.pageId != null) {
						 this.mainActivity.beginSearchImages();
						 this.retrieveImage.execute(pageId);
					 }
					 
					 this.mainActivity.readText();
				 }			 			 
			}	 
		} else {
			this.mainActivity.stopSearchBar();
		}
		 						
		super.onPostExecute(result);
	}
}
