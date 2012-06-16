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

import android.os.AsyncTask;

public class RetrieveImagesTask extends AsyncTask<String, Void, ImageRepository> {
	private WikitalkActivity mainActivity;
	
	public RetrieveImagesTask(WikitalkActivity activity) {
		this.mainActivity = activity;
	}
	
	@Override
	protected ImageRepository doInBackground(String... params) {
		List<ImageInfo> images = new ArrayList<ImageInfo>();
		String line = null;
    	String pageId = params[0];
    	// imlimit=500 for regular users
    	HttpGet uri = new HttpGet("http://" + this.mainActivity.getWikipediaLanguageLc() + ".wikipedia.org/w/api.php?format=xml&action=query&pageids=" + pageId + "&prop=images&imlimit=max");
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
	    	    WikitalkActivity.Logd(WikitalkActivity.WIKITALK, "HTTP error, invalid server status code: " + response.getStatusLine());  
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
				String titleBase = nodes.item(i).getAttributes().getNamedItem("title").getNodeValue();
				int pos = titleBase.indexOf(":");				
				titleBase = titleBase.substring(pos + 1);
				ImageInfo ii = new ImageInfo();
				ii.name = titleBase;
				images.add(ii);
				// To link with ditcionnary
				ii.idx = images.size() - 1;
			}
		}
		
		ImageRepository ir = new ImageRepository();
		if (images != null) {
			int idx = 0;
			List<ImageInfo> newImages = new ArrayList<ImageInfo>();
			if (this.mainActivity.getPage() != null && this.mainActivity.getPage().splitSentence != null) {
				for(String sentence : this.mainActivity.getPage().splitSentence) {
					
					List<ImageInfo> iis = new ArrayList<ImageInfo>();
					
					for(ImageInfo ii : images) {
						if (sentence.toUpperCase().contains(ii.name.toUpperCase())) {
							newImages.add(ii);
							ii.idx = newImages.size() - 1;
							iis.add(ii);					
						}
					}
					
					if (iis.size() > 0) {
						ir.imagesIndexed.put(idx, iis);
					}
					
					idx++;
				}
				
				ir.images = newImages;
			}
		}
		
		return ir;
	}

	@Override
	protected void onPostExecute(ImageRepository result) {
		super.onPostExecute(result);
		this.mainActivity.setImages(result);
	}

}
