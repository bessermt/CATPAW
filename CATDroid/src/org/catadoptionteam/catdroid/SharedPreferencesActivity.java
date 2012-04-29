/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author bessermt
 *
 */
public class SharedPreferencesActivity extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
