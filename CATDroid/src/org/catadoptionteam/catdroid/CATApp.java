/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.app.Application;
import android.content.Context;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;

/**
 * @author bessermt
 *
 */
public class CATApp extends Application
{
	private static Context context_;

	public CATApp() // TODO: Delete all empty ctors?
	{
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		try
		{
			Class.forName("android.os.AsyncTask"); // Workaround for Issue 20915:  AsyncTask can get initialized with wrong Looper
		} 
		catch (Throwable t)
		{
		}
		context_ = getApplicationContext();
	}

	public static Context getAppContext() // TODO: Make this return a casted CATApp.
	{
		return context_;
	}

	public enum JSONFactory // TODO: See where used and make code consistently use best practice.
	{
		INSTANCE;

		private static JsonFactory jsonFactory;

		static
		{
			jsonFactory = new JacksonFactory();
		}

		public static JsonFactory getJsonFactory()
		{
			final JsonFactory result = jsonFactory;
			return result;
		}
	}
}
