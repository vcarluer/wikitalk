package org.dragon.vince;

import android.os.AsyncTask;

public class StepAsync extends AsyncTask<Integer, Void, Void> {
	private DanyActivity mainActivity;
	
	public StepAsync(DanyActivity activity) {
		this.mainActivity = activity;
	}

	@Override
	protected Void doInBackground(Integer... params) {
		int wait = 500;
		if (params.length > 0) {
			wait = params[0];
		}
		
		try {
			Thread.sleep(wait);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		this.mainActivity.step();
		super.onPostExecute(result);
	}
}
