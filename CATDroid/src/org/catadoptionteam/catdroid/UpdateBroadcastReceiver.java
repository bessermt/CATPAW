/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author bessermt
 *
 */
public class UpdateBroadcastReceiver extends BroadcastReceiver
{
	public UpdateBroadcastReceiver()
	{
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		final String action = intent.getAction();
		if (action != null && action.equals(UpdateService.ACTION))
		{
			MainActivity.updateData(context);
		}
	}
}
