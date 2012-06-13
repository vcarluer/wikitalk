package org.dragon.vince;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class RetrieveImageTask extends AsyncTask<String, Void, Bitmap> {
	private WikitalkActivity mainActivity;

	public RetrieveImageTask(WikitalkActivity activity) {
		this.mainActivity = activity;
	}
	
	@Override
	protected Bitmap doInBackground(String... params) {
		 try {
			String imageUrl = params[0];
			
			HttpGet uri = new HttpGet(new URI(imageUrl));
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
			
			// Handle 2 redirections (in case of use of special:filepath
			if (response != null) {
				StatusLine status = response.getStatusLine();
				if (status.getStatusCode() == 302 || status.getStatusCode() == 301) {
					imageUrl = response.getFirstHeader("location").getValue();
					
					uri = new HttpGet(new URI(imageUrl));
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
							imageUrl = response.getFirstHeader("location").getValue();
						}
					}
				}
			}
			
			Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(imageUrl).getContent());
			return bitmap;
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
	protected void onPostExecute(Bitmap result) {
		this.mainActivity.showImage(result);
		super.onPostExecute(result);
	}
}