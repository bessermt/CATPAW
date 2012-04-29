/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * @author bessermt
 *
 */
public class VerticalSeekBar extends SeekBar
{
	public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public VerticalSeekBar(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public VerticalSeekBar(Context context)
	{
		super(context);
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
		super.onSizeChanged(h, w, oldh, oldw);
	}

	/**
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas c)
	{
		c.rotate(90);
		c.translate(0, -getWidth());

		super.onDraw(c);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		boolean result = super.onTouchEvent(event);

		final boolean enabled = isEnabled();
		if (enabled)
		{
			result = true;

			switch (event.getAction())
			{
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_UP:
				final int max = getMax();
				final int height = getHeight();
				final float y = event.getY();
				final int progress = (int) (max * y / height);
				setProgress(progress);
				onSizeChanged(getWidth(), getHeight(), 0, 0);
				break;

			default:
				break;
			}
		}
		return result;
	}
}
