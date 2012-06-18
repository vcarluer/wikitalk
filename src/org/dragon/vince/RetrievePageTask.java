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

import android.net.Uri;
import android.os.AsyncTask;

public class RetrievePageTask extends AsyncTask<String, Void, Page> {
	private DanyActivity mainActivity;
	
	public RetrievePageTask(DanyActivity activity) {
		this.mainActivity = activity;		
	}
	
	@Override	
	protected Page doInBackground(String... params) {
		String text = null;
		DefaultHttpClient client = new DefaultHttpClient();
    	HttpResponse response = null;
		String search = Uri.encode(params[0]);
		String keyword = null;
    	// Open search and take first result, always
		HttpGet uri = new HttpGet("http://" + this.mainActivity.getWikipediaLanguageLc() + ".wikipedia.org/w/api.php?action=opensearch&search=" + search + "&limit=1&namespace=0&format=xml");
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
			NodeList nodes = doc.getElementsByTagName("Text");			
			for (int i = 0; i < nodes.getLength(); i++) {
				keyword = Uri.encode(nodes.item(i).getTextContent());
				// Handle here multiple results (take first or propose)
				if(keyword != null) {					
					 // Only first one for now
					 break;
				}								
			}
		}
		
		Page page = new Page();
		page.workedText = "";
		if (keyword != null && keyword != "")
		{
			// Get page text
			text = null;
	    	uri = new HttpGet("http://" + this.mainActivity.getWikipediaLanguageLc() + ".wikipedia.org/w/api.php?format=xml&action=query&titles=" + keyword + "&prop=revisions&rvprop=content");
	    	// close client request?	    	
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
				NodeList nodes = doc.getElementsByTagName("page");
				Node pageNode = null;
				for (int i = 0; i < nodes.getLength(); i++) {
					pageNode = nodes.item(i).getAttributes().getNamedItem("pageid");
					// Handle here multiple results (take first or propose)
					if(pageNode != null) {
						page.id = pageNode.getNodeValue();
						 // Only first one for now
						 break;
					}								
				}
				
				for (int i = 0; i < nodes.getLength(); i++) {
					Node titleNode = nodes.item(i).getAttributes().getNamedItem("title");
					// Handle here multiple results (take first or propose)
					if(titleNode != null) {
						String title = titleNode.getNodeValue();
						if (title != null && title.length() > 0) {
							page.title = title;
							page.workedText = title + ". ";
							 // Only first one for now
							break;
						}										 
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
								 page.redirect = redirect;
							 }
						 } else {
							 page.workedText += line;						 						 
						 }			 			 
					}	 
				}
			}
			
			page.parseOriginalText();		
		}
		
		return page;
	}
	


	@Override
	protected void onPostExecute(Page result) {
		this.mainActivity.stopSearchBar();
		if (result.redirect != null) {
			this.mainActivity.search(result.redirect);
		} else {
			if (result.workedText != "")
			{
				if (result.id != null) {
					 this.mainActivity.beginSearchImages();
					 this.mainActivity.getNewRetrieveImages().execute(result.id);
				 }
				if (result.title != null) {
					this.mainActivity.setCurrentTitle(result.title);
				}
				
				this.mainActivity.readText(result);
				this.mainActivity.showReadImage();
				this.mainActivity.setHasResult(true);
			}
			else
			{
				this.mainActivity.setHasResult(false);
			}			
		}						
		 						
		super.onPostExecute(result);
	}
}
