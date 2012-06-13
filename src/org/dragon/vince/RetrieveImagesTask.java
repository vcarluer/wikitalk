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

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class RetrieveImagesTask extends AsyncTask<String, Void, List<ImageInfo>> {
	private WikitalkActivity mainActivity;
	
	public RetrieveImagesTask(WikitalkActivity activity) {
		this.mainActivity = activity;
	}
	
	@Override
	protected List<ImageInfo> doInBackground(String... params) {
		List<ImageInfo> images = new ArrayList<ImageInfo>();
		String line = null;
    	String pageId = params[0];
    	// imlimit=500 for regular users
    	HttpGet uri = new HttpGet("http://" + this.mainActivity.getLanguageLc() + ".wikipedia.org/w/api.php?format=xml&action=query&pageids=" + pageId + "&prop=images&imlimit=max");
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
				int pos = title.indexOf(":");				
				title = Uri.encode(title.substring(pos + 1));
				WindowManager wm = (WindowManager) this.mainActivity.getSystemService(Context.WINDOW_SERVICE);
				Display display = wm.getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				int width = size.x;
				int height = size.y;
								
				int imgFormat = width; // Always width?
				String imageUrl = "http://" + this.mainActivity.getLanguageLc() + ".wikipedia.org/w/api.php?action=query&titles=Image:" + title +"&prop=imageinfo&iiprop=url&iiurlwidth=" + String.valueOf(imgFormat) + "&format=xml";
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
								
								Document doc2 = XmlHelper.xmlfromString(line);
								NodeList nodes2 = doc2.getElementsByTagName("ii"); 
								 for (int j = 0; j < nodes2.getLength(); j++) {
									 ImageInfo ii = new ImageInfo();
									 ii.name = title;
									 ii.url = nodes2.item(j).getAttributes().getNamedItem("url").getNodeValue();
									 ii.thumbUrl = nodes2.item(j).getAttributes().getNamedItem("thumburl").getNodeValue();									 
									 
									 images.add(ii);
									 // To link with ditcionnary
									 ii.idx = images.size() - 1;
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
		
		return images;
	}

	@Override
	protected void onPostExecute(List<ImageInfo> result) {
		super.onPostExecute(result);
		this.mainActivity.setImages(result);
	}

}
