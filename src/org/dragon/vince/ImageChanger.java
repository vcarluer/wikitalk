package org.dragon.vince;

import android.os.AsyncTask;

public class ImageChanger extends AsyncTask<Void, Void, Void> {
	private WikitalkActivity activity;
	private boolean run;
	
	public ImageChanger(WikitalkActivity activity) {
		this.activity = activity;
		run = true;
	}

	@Override
	protected Void doInBackground(Void... params) {		
		while (run) {
			try {
				Thread.sleep(10000);
				if (System.currentTimeMillis() - this.activity.getImageShown() > 10000) {
					this.activity.callNextImage();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				
		}
		
		return null;
	}

	@Override
	protected void onCancelled() {
		this.run = false;
		super.onCancelled();
	}
}