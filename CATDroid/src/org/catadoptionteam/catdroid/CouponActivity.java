/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * @author bessermt
 *
 */
public class CouponActivity extends Activity
{
	private final OnClickListener okClickListener = 
		new OnClickListener()
		{
			public void onClick(View v)
			{
				ok();
			}
		};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.coupon);
		final Button buttonOK = (Button)findViewById(R.id.buttonOK);
		buttonOK.setOnClickListener(okClickListener);
	}

	private void ok()
	{
		finish();
	}
}
