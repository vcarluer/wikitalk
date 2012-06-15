package org.dragon.vince;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class FadeOutAnimationListener implements AnimationListener {
	
	private View myView;

	public FadeOutAnimationListener(View view) {
		this.myView = view;
	}
	
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	public void onAnimationStart(Animation animation) {
		this.myView.setVisibility(View.GONE);
	}		
}
