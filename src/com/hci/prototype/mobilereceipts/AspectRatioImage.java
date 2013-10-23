package com.hci.prototype.mobilereceipts;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AspectRatioImage extends ImageView {

	public AspectRatioImage(final Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public AspectRatioImage(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public AspectRatioImage(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();
		setMeasuredDimension(width, height);
	}

}
