package com.hci.prototype.mobilereceipts;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AspectRatioImage extends ImageView {

	public AspectRatioImage(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public AspectRatioImage(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}

	public AspectRatioImage(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    int width = MeasureSpec.getSize(widthMeasureSpec);
	    int height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();
	    setMeasuredDimension(width, height);
	}

}
