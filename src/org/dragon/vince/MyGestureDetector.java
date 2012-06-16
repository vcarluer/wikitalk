package org.dragon.vince;

import android.util.DisplayMetrics;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class MyGestureDetector  extends SimpleOnGestureListener {
	private static final int SWIPE_MIN_DISTANCE = 100; // original: 120
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private DanyActivity mainActivity;
	    
	 public MyGestureDetector(DanyActivity activity) {
		 this.mainActivity = activity;
	 }
	    
	 @Override
     public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
         try {
        	 DisplayMetrics dm = this.mainActivity.getResources().getDisplayMetrics();

        	 int REL_SWIPE_MIN_DISTANCE = (int)(SWIPE_MIN_DISTANCE * dm.densityDpi / 160.0f);
        	 int REL_SWIPE_MAX_OFF_PATH = (int)(SWIPE_MAX_OFF_PATH * dm.densityDpi / 160.0f);
        	 int REL_SWIPE_THRESHOLD_VELOCITY = (int)(SWIPE_THRESHOLD_VELOCITY * dm.densityDpi / 160.0f);
        	 
             if (Math.abs(e1.getY() - e2.getY()) > REL_SWIPE_MAX_OFF_PATH) {
            	 return false;
             }
             
             // right to left swipe
             if(e1.getX() - e2.getX() > REL_SWIPE_MIN_DISTANCE && Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {
//                 Toast.makeText(this.mainActivity, "Left Swipe", Toast.LENGTH_SHORT).show();
            	 this.mainActivity.nextImage();
             }  else if (e2.getX() - e1.getX() > REL_SWIPE_MIN_DISTANCE && Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {
//                 Toast.makeText(this.mainActivity, "Right Swipe", Toast.LENGTH_SHORT).show();
            	 this.mainActivity.previousImage();
             }
         } catch (Exception e) {
             // nothing
         }
         
         return true;
     }
}
