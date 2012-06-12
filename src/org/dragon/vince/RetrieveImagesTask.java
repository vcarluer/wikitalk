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
import org.w3c.dom.NodeList;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class RetrieveImagesTask extends AsyncTask<String, Void, String> {
	private WikitalkActivity mainActivity;
	private RetrieveImageTask retrieveImage;
	
	public RetrieveImagesTask(WikitalkActivity activity) {
		this.mainActivity = activity;
		this.retrieveImage = new RetrieveImageTask(this.mainActivity);
	}
	
	@Override
	protected String doInBackground(String... params) {
		String line = null;
    	String pageId = params[0];
    	HttpGet uri = new HttpGet("http://fr.wikipedia.org/w/api.php?format=xml&action=query&pageids=" + pageId + "&prop=images");
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
		Document doc = this.mainActivity.XMLfromString(result);
		
		NodeList nodes = doc.getElementsByTagName("im"); 
		 for (int i = 0; i < nodes.getLength(); i++) {
			 String title = nodes.item(i).getAttributes().getNamedItem("title").getNodeValue();
			 
			 title = title.replace("Fichier:", "");
			 String imageUrl = "http://fr.wikipedia.org/wiki/Special:Filepath?file=" + Uri.encode(title);			 
			 this.retrieveImage.execute(imageUrl);
			 // Only first one for now
			 break;
		}
		
		super.onPostExecute(result);
	}

}
