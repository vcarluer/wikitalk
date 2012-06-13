package org.dragon.vince;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.util.Log;

public class RetrieveImageTask extends AsyncTask<String, Void, Bitmap> {
	private WikitalkActivity mainActivity;

	public RetrieveImageTask(WikitalkActivity activity) {
		this.mainActivity = activity;
	}
	
	@Override
	protected Bitmap doInBackground(String... params) {
//		Bitmap bitmap = null;
//		InputStream imageContent = null;
			String imageUrl = params[0];
//			HttpGet uri = new HttpGet(new URI(imageUrl));
//	    	// close client request?
//	    	DefaultHttpClient client = new DefaultHttpClient();
//	    	HttpResponse response = null;
//	    	response = client.execute(uri);
//			if (response != null) {
//				BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(response.getEntity());
//				imageContent = bufHttpEntity.getContent();	                
//				bitmap = BitmapFactory.decodeStream(imageContent);
//			}
			
				URL url = null;
				try {
					url = new URL(imageUrl);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				HttpURLConnection connection = null;
				try {
					connection = (HttpURLConnection) url.openConnection();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				InputStream is = null;
				try {
					is = connection.getInputStream();					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				Bitmap img = BitmapFactory.decodeStream(is, null, options);
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return img;
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
//		finally {
//	        if (imageContent != null)
//	        {
//	            try {
//	            	imageContent.close();
//	            } catch (IOException e) {
//	                Log.e(WikitalkActivity.WIKITALK, "error closing stream");
//	            }
//	        }
//	    }
			
//			// Handle 2 redirections (in case of use of special:filepath
//			if (response != null) {
//				StatusLine status = response.getStatusLine();
//				if (status.getStatusCode() == 302 || status.getStatusCode() == 301) {
//					imageUrl = response.getFirstHeader("location").getValue();
//					
//					uri = new HttpGet(new URI(imageUrl));
//			    	// close client request?			    	
//			    	response = null;
//					try {
//						response = client.execute(uri);
//					} catch (ClientProtocolException e) {
//						e.printStackTrace();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//					
//					if (response != null) {
//						status = response.getStatusLine();
//						if (status.getStatusCode() == 302 || status.getStatusCode() == 301) {
//							imageUrl = response.getFirstHeader("location").getValue();
//							uri = new HttpGet(new URI(imageUrl));
//							try {
//								response = client.execute(uri);
//							} catch (ClientProtocolException e) {
//								e.printStackTrace();
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
//							
//							response = null;
//							try {
//								response = client.execute(uri);
//							} catch (ClientProtocolException e) {
//								e.printStackTrace();
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
//						}
//					}
//				}
				
				
//				}
//			}
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		this.mainActivity.showImage(result);
		super.onPostExecute(result);
	}

	@Override
	protected void onCancelled() {
		// TODO Auto-generated method stub
		super.onCancelled();
	}

	@Override
	protected void onCancelled(Bitmap result) {
		// TODO Auto-generated method stub
		super.onCancelled(result);
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}
}
