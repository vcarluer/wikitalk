package org.dragon.vince;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class RetrieveImageTask extends AsyncTask<ImageInfo, Void, ImageInfo> {
	private WikitalkActivity mainActivity;

	public RetrieveImageTask(WikitalkActivity activity) {
		this.mainActivity = activity;
	}
	
	@Override
	protected ImageInfo doInBackground(ImageInfo... params) {		
		try {
			ImageInfo imageInfo = params[0];
			// Cached image system
			if (imageInfo.bitmap != null) return imageInfo;
			
			HttpGet uri = null;
			DefaultHttpClient client = new DefaultHttpClient();
	    	HttpResponse response = null;
	    	
	    	if (imageInfo.thumbUrl == null || imageInfo.thumbUrl == "") {
	    		// Get image urls
				String titleBase = imageInfo.name;
				String title = Uri.encode(titleBase);
				WindowManager wm = (WindowManager) this.mainActivity.getSystemService(Context.WINDOW_SERVICE);
				Display display = wm.getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				int width = size.x;
				int height = size.y;
								
				int imgFormat = width; // Always width?
				String imageUrl = "http://" + this.mainActivity.getLanguageLc() + ".wikipedia.org/w/api.php?action=query&titles=Image:" + title +"&prop=imageinfo&iiprop=url&iiurlwidth=" + String.valueOf(imgFormat) + "&format=xml";
				uri = new HttpGet(imageUrl);			
		    	response = null;
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
			    		WikitalkActivity.Logd(WikitalkActivity.WIKITALK, "HTTP error, invalid server status code: " + response.getStatusLine());  
			    	} else {
			    		HttpEntity entity = response.getEntity();
			    		try {
							String line = EntityUtils.toString(entity);
							if (line != null) {
								uri = null;
								
								Document doc2 = XmlHelper.xmlfromString(line);
								NodeList nodes2 = doc2.getElementsByTagName("ii"); 
								 for (int j = 0; j < nodes2.getLength(); j++) {								 								 
									 imageInfo.url = nodes2.item(j).getAttributes().getNamedItem("url").getNodeValue();
									 imageInfo.thumbUrl = nodes2.item(j).getAttributes().getNamedItem("thumburl").getNodeValue();								 
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
			
			String realUrl = imageInfo.thumbUrl;
			if (realUrl != null) {
				uri = new HttpGet(new URI(realUrl));		    	
		    	response = null;
				try {
					response = client.execute(uri);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				// Handle 2 redirections (in case of use of special:filepath
				if (response != null) {
					StatusLine status = response.getStatusLine();
					if (status.getStatusCode() == 302 || status.getStatusCode() == 301) {
						realUrl = response.getFirstHeader("location").getValue();
						
						uri = new HttpGet(new URI(realUrl));
				    	// close client request?			    	
				    	response = null;
						try {
							response = client.execute(uri);
						} catch (ClientProtocolException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						if (response != null) {
							status = response.getStatusLine();
							if (status.getStatusCode() == 302 || status.getStatusCode() == 301) {
								realUrl = response.getFirstHeader("location").getValue();
							}
						}
					}
				}
				
				Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(realUrl).getContent());
				imageInfo.bitmap = bitmap;
				
				return imageInfo;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		 
		 return null;
	}

	@Override
	protected void onPostExecute(ImageInfo result) {
		this.mainActivity.showImage(result);
		super.onPostExecute(result);
	}
}