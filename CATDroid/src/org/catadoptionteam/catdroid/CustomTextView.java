/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author bessermt
 *
 */

public class CustomTextView extends TextView
{
	private int angle_;

	public CustomTextView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		if (attrs != null)
		{
			final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomTextView);

			angle_ = a.getInt(R.styleable.CustomTextView_angle, 0);

			a.recycle();
		}
	}

	public CustomTextView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public CustomTextView(Context context)
	{
		this(context, null);
	}

	/**
	 * @see android.view.View#measure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		final int width = getMeasuredHeight();
		final int height = getMeasuredWidth();
		setMeasuredDimension(width, height);
	}

	/* (non-Javadoc)
	 * @see android.view.View#onSizeChanged(int, int, int, int)
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(-h, w, oldh, oldw);// super.onSizeChanged(h, w, oldh, oldw);
	}

	/**
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas)
	{
		final TextPaint textPaint = getPaint(); 
		textPaint.setColor(getCurrentTextColor());
		textPaint.drawableState = getDrawableState();

		canvas.save();

		canvas.translate(0, getHeight());
		canvas.rotate(angle_);

		canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());

		getLayout().draw(canvas);
		canvas.restore();
	}
}
