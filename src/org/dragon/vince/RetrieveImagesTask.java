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

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class RetrieveImagesTask extends AsyncTask<String, Void, Void> {
	private WikitalkActivity mainActivity;
	private RetrieveImageTask retrieveImage;
	private List<ImageInfo> images;
	
	public RetrieveImagesTask(WikitalkActivity activity) {
		this.mainActivity = activity;
		this.retrieveImage = new RetrieveImageTask(this.mainActivity);
		this.images = new ArrayList<ImageInfo>();
	}
	
	@Override
	protected Void doInBackground(String... params) {
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
		
		if (line != null) {
			Document doc = XmlHelper.xmlfromString(line);
			NodeList nodes = doc.getElementsByTagName("im");
			for (int i = 0; i < nodes.getLength(); i++) {
				String title = nodes.item(i).getAttributes().getNamedItem("title").getNodeValue();
				title = Uri.encode(title.replace("Fichier:", ""));
				int imgFormat = 200;
				String imageUrl = "http://fr.wikipedia.org/w/api.php?action=query&titles=Image:" + title +"&prop=imageinfo&iiprop=url&iiurlwidth=" + String.valueOf(imgFormat) + "&format=xml";
				uri = new HttpGet(imageUrl);
				
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
							if (line != null) {
								uri = null;
								
								this.images.clear();
								doc = XmlHelper.xmlfromString(line);
								nodes = doc.getElementsByTagName("ii"); 
								 for (int j = 0; j < nodes.getLength(); j++) {
									 ImageInfo ii = new ImageInfo();
									 ii.url = nodes.item(j).getAttributes().getNamedItem("url").getNodeValue();
									 ii.thumbUrl = nodes.item(j).getAttributes().getNamedItem("thumburl").getNodeValue();
									 
									 this.images.add(ii);
								 }
							}
						} catch (ParseException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
			    	}
				}
			}
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		for(ImageInfo ii : this.images)
		{
			 this.retrieveImage.execute(ii.thumbUrl);
			 // Only first one for now
			 break;
		}
		
		super.onPostExecute(result);
	}

}
