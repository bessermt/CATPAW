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
public class BootBroadcastReceiver extends BroadcastReceiver
{
	/**
	 * 
	 */
	public BootBroadcastReceiver()
	{
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		final String action = intent.getAction();
		if (action != null && action.equals("android.intent.action.BOOT_COMPLETED"))
		{
			Util.setUpdateRepeat(context);
		}
	}
}
