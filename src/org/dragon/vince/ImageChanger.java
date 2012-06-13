package org.dragon.vince;

public class ImageChanger extends Thread {
	private WikitalkActivity activity;
	private boolean run;
	
	public ImageChanger(WikitalkActivity activity) {
		this.activity = activity;
	}
	
	@Override
	public void run() {
		run = true;
		while (run) {
			try {
				Thread.sleep(10000);
				if (System.currentTimeMillis() - this.activity.getImageShown() > 10000) {
					this.activity.nextImage();
				}				
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
		}
		super.run();
	}

	@Override
	public void interrupt() {
		this.run = false;
		super.interrupt();
	}	
}
