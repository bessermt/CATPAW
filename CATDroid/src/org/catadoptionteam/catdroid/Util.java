/**
 * 
 */
package org.catadoptionteam.catdroid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

/**
 * @author bessermt
 *
 */
public final class Util
{
	public static final String EMPTY_STRING = "";
	public static final int INVALID_RESID = View.NO_ID;

	public static final int MILSEC_PER_SEC = 1000;
	public static final int SEC_PER_MIN = 60;
	public static final int MIN_PER_HR = 60;
	public static final int HR_PER_DAY =24;

	public static final int DAY_PER_WK = 7;
	public static final int MONTH_PER_YR = 12;

	public static final int JAN = 0;
	public static final int FEB = 1;
	public static final int MAR = 2;
	public static final int APR = 3;
	public static final int MAY = 4;
	public static final int JUN = 5;
	public static final int JUL = 6;
	public static final int AUG = 7;
	public static final int SEP = 8;
	public static final int OCT = 9;
	public static final int NOV = 10;
	public static final int DEC = 11;

//	private static final int MIN_PER_DAY = MIN_PER_HR*HR_PER_DAY;
	public static final int MILSEC_PER_DAY = MILSEC_PER_SEC*SEC_PER_MIN*MIN_PER_HR*HR_PER_DAY;

	public static final String JSON_TAG_PLACEHOLDER = "$t"; // see: http://code.google.com/apis/gdata/docs/json.html

	public static Context getSafeContext(final Context context)
	{
		Context result = context;
		if (result == null)
		{
			result = CATApp.getAppContext();
		}
		return result;
	}

	private static boolean isSpanish()
	{
		final String language = Locale.getDefault().getISO3Language();
		final boolean result = language.equals("spa");
		return result;
	}

	public static String concatNoTranslation(final Context context, final String text)
	{
		String result;

		if (isSpanish())
		{
			final Context ctx = getSafeContext(context);

			final String no_translation = ctx.getString(R.string.no_translation);

			result = no_translation;

			if (!TextUtils.isEmpty(text))
			{
				result += "\n\n" + text;
			}
		}
		else
		{
			result = text;
		}

		return result;
	}

	public static int toMinutes(final int hour, final int minute)
	{
		int result = hour*MIN_PER_HR + minute;
		return result;
	}

	public static int getNowDayMinutes()
	{
		final Time time = new Time();
		time.setToNow();
		final int hour = time.hour;
		final int minute = time.minute;
		final int result = toMinutes(hour, minute);
		return result;
	}

	public static Time MidnightToday()
	{
		final Time now = new Time();
		now.setToNow();
		final Time today = new Time();
		final int monthDay = now.monthDay;
		final int month = now.month;
		final int year = now.year;
		today.set(monthDay, month, year);
		today.normalize(true);
		return today;
	}

	public static int getNextDayMinute(final Context context)
	{
		int result = 0;

		final Context ctx = getSafeContext(context);
		if (ctx != null)
		{
			final String key = ctx.getString(R.string.preference_key_update_time);
			final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
			final int defaultNextDayMinute = 0 * MIN_PER_HR; // 00:00 am
			result = preferences.getInt(key, defaultNextDayMinute);
		}
		return result;
	}

	public static int getDayOfYear(final Context context)
	{
		final Time time = new Time();
		time.setToNow();
		final int hour = time.hour;
		final int minute = time.minute;
		final int nowMinutes = toMinutes(hour, minute);
		final int nextDayMinutes = getNextDayMinute(context);
		int delta = 0;
		if (nowMinutes >= nextDayMinutes)
		{
			delta = 1;
		}
		final int result = time.yearDay + delta;
		return result;
	}

	public static boolean isNotifyEnabled(final Context context)
	{
		final String key = context.getString(R.string.preference_key_enable_notification);
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final boolean defaultNotifyEnabled = true;
		final boolean result = preferences.getBoolean(key, defaultNotifyEnabled);
		return result;
	}

	public static boolean isGameHintEnabled(final Context context)
	{
		final String key = context.getString(R.string.preference_key_enable_m9l_hint);
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final boolean defaultGameHintEnabled = true;
		final boolean result = preferences.getBoolean(key, defaultGameHintEnabled);
		return result;
	}

	// TODO: Move to CatDayActivity iff it exists.  It lives here for now.  
	public static final int getCOTDID(final Activity activity)
	{
		int result = AppDBAdapter.INVALID_KEY_ID;

		final int nextDayMinute = getNextDayMinute(activity);
		final int nowMinute = Util.getNowDayMinutes();
		final Time currentDay = new Time();
		currentDay.setToNow();
		if (nowMinute < nextDayMinute)
		{
			currentDay.set(currentDay.toMillis(false) - MILSEC_PER_SEC*SEC_PER_MIN*MIN_PER_HR*HR_PER_DAY);
		}
		final String currentDate = currentDay.format3339(true);

		final String[] resultColumns = 
			new String[]
			{
				AppDBAdapter.KEY_ID
			};

		final String where = 
			AppDBAdapter.CATEGORY + "=" + AppDBAdapter.CATEGORY_ANIMAL_OF_THE_DAY + " and " + 
			AppDBAdapter.EXPIRE_DATE + "<=" + "\'" + currentDate + "\'";

		final String order = 
			AppDBAdapter.EXPIRE_DATE + " DESC";

		AppDBAdapter db = null;

		Cursor cursor = null;

		try
		{
			db = new AppDBAdapter(activity);

			cursor = db.query(activity, resultColumns, where, order);

			boolean success = false;
			if (cursor != null)
			{
				success = cursor.moveToFirst();
			}

			if (success)
			{
				result = cursor.getInt(0);
			}
			else
			{
				cursor = db.query(activity, resultColumns, null, AppDBAdapter.EXPIRE_DATE);
				if (cursor != null)
				{
					success = cursor.moveToFirst();
					if (success)
					{
						result = cursor.getInt(0);
					}
				}
			}
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
			if (db != null)
			{
				db.close();
			}
		}

		return result;
	}

	public static final void random_shuffle(final int[] array, final Random randomGenerator)
	{
		int i = array.length;
		while (i > 1)
		{
			final int n = randomGenerator.nextInt(i);
			--i;
			final int tmp = array[i];
			array[i] = array[n];
			array[n] = tmp;
		}
	}

	public static final int nBits(final int value)
	{
		int result = 0;

		int v = value;

		while (v != 0)
		{
			++result;
			v &= v - 1;
		}

		return result;
	}

	public static void startActivity(final Context context, final Intent intent)
	{
		if (context != null && intent != null)
		{
			try
			{
				context.startActivity(intent);
			}
			catch (Throwable t)
			{
//				String message = "null exception thrown";
//				if (t != null)
//				{
//					final String msg = t.getMessage();
//					if (msg != null)
//					{
//						message = msg;
//					}
//				}
//				Log.e("Util.startActivity()", message);
			}
		}
	}

	public static void startActivityForResult(final Activity activity, final Intent intent, final int requestCode)
	{
		if (activity != null && intent != null)
		{
			try
			{
				activity.startActivityForResult(intent, requestCode);
			}
			catch (Throwable t)
			{
// TODO:  Catching this might be useful...
//				String message = "null exception thrown";
//				if (t != null)
//				{
//					final String msg = t.getMessage();
//					if (msg != null)
//					{
//						message = msg;
//					}
//				}
//				Log.e("Util.startActivity()", message);
			}
		}
	}

	public static void displayToast(final Context context, final String msg, final int duration)
	{
		final Context ctx = getSafeContext(context);
		final Toast toast = Toast.makeText(ctx, msg, duration);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	public static void displayToast(final Context context, final int stringResId, final int duration)
	{
		final String message = context.getString(stringResId);
		displayToast(context, message, duration);
	}

	public static String toString(final InputStream inStream)
	{
		String result = null;

		final StringBuilder strBuilder = new StringBuilder();

		final InputStreamReader inStreamReader = new InputStreamReader(inStream);
		final BufferedReader bufReader = new BufferedReader(inStreamReader);

		char[] buffer = new char[1024];
		final int bufferLen = buffer.length;

		try
		{
			while (true)
			{
				final int n = bufReader.read(buffer, 0, bufferLen);
				if (n < 0)
				{
					break;
				}
				else if (n > 0)
				{
					strBuilder.append(buffer, 0, n);
				}
			}

			result = strBuilder.toString();
		}
		catch(Exception ex)
		{
		}

		return result;
	}

	public static JSONArray getSafeJSONArray(final JSONObject jsonObject, final String name)
	{
		JSONArray result = null;

		try
		{
			int length = 0;
			JSONArray jsonArray = jsonObject.optJSONArray(name);
			if (jsonArray != null)
			{
				length = jsonArray.length();
				if (length != 0)
				{
					result = jsonArray;
				}
			}
		}
		catch (Throwable e)
		{
			// empty array is normal.
		}

		return result;
	}

	public static String getSafeJSONString(final JSONObject jsonObject, final String name)
	{
		String result = null;

		try
		{
			final JSONObject jsonSubObject = jsonObject.getJSONObject(name);
			result = jsonSubObject.getString(JSON_TAG_PLACEHOLDER);
		}
		catch (Throwable e)
		{
		}

		return result;
	}

//  TODO:  See if this can be generic.  I think it may not be possible to use primitive types for generic functions.  
//	private static <T> void save(final SharedPreferences preferences, final String key, final T[] array)
//	{
//		final JSONArray jsonArray = new JSONArray();
//		for (final T value: array)
//		{
//			jsonArray.put(value);
//		}
//		final String storeString = jsonArray.toString();
//
//		final SharedPreferences.Editor editor = preferences.edit();
//		editor.putString(key, storeString);
//		editor.commit();
//	}

	public static void save(final SharedPreferences preferences, final String key, final int[] array)
	{
		final JSONArray jsonArray = new JSONArray();
		for (final int value: array)
		{
			jsonArray.put(value);
		}
		final String storeString = jsonArray.toString();

		final SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key, storeString);
		editor.commit();
	}

	public static void save(final SharedPreferences preferences, final String key, final boolean[] array)
	{
		final JSONArray jsonArray = new JSONArray();
		for (final boolean value: array)
		{
			jsonArray.put(value);
		}
		final String storeString = jsonArray.toString();

		final SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key, storeString);
		editor.commit();
	}

//  TODO:  See if this can be generic.  I think it may not be possible to use primitive types for generic functions.  
//	private static <T> void loadT(final SharedPreferences preferences, final String key, final T[] array) throws JSONException
//	{
//		final String storeString = preferences.getString(key, EMPTY_STRING);
//		if (storeString != EMPTY_STRING)
//		{
//			final JSONArray jsonArray = new JSONArray(storeString);
//	
//			int i = array.length;
//			while (i != 0)
//			{
//				--i;
//				array[i] = (T)jsonArray.get(i);
//			}
//		}
//	}

	public static void load(final SharedPreferences preferences, final String key, final int[] array) throws JSONException
	{
		final String storeString = preferences.getString(key, EMPTY_STRING);
		if (storeString != EMPTY_STRING)
		{
			final JSONArray jsonArray = new JSONArray(storeString);
	
			int i = array.length;
			while (i != 0)
			{
				--i;
				array[i] = jsonArray.getInt(i);
			}
		}
	}

	public static void load(final SharedPreferences preferences, final String key, final boolean[] array) throws JSONException
	{
		final String storeString = preferences.getString(key, EMPTY_STRING);
		if (storeString != EMPTY_STRING)
		{
			final JSONArray jsonArray = new JSONArray(storeString);
	
			int i = array.length;
			while (i != 0)
			{
				--i;
				array[i] = jsonArray.getBoolean(i);
			}
		}
	}

	public static Bitmap getBitmap(final Context context, final String pathFilename)
	{
		FileInputStream fileInputStream = null;
		if (pathFilename != null)
		{
			try
			{
				fileInputStream = new FileInputStream(pathFilename);
			}
			catch (FileNotFoundException e)
			{
			}
		}

		Bitmap result = null;

		if (fileInputStream != null)
		{
			result = BitmapFactory.decodeStream(fileInputStream, null, null);

			try
			{
				fileInputStream.close();
			}
			catch (IOException e)
			{
				// TODO: Diagnose?
			}
		}

		if (result == null)
		{
			final int nullPhotoId = R.drawable.missing_photo;
			final Context ctx = getSafeContext(context);
			final Resources resources = ctx.getResources();
			result = BitmapFactory.decodeResource(resources, nullPhotoId);
		}

		return result;
	}

	public static Bitmap getBitmap(final Context context, final String path, final String baseFilename)
	{
		final Bitmap result = getBitmap(context, path + baseFilename + ".jpg");

		return result;
	}

	public static boolean delDirs(final File dir)
	{
		boolean result = false;
		if (dir!=null && dir.exists())
		{
			result = true;
			if (dir.isDirectory())
			{
				final File[] files = dir.listFiles();
				for (final File file: files)
				{
					if (file.isDirectory())
					{
						result = delDirs(file) && result;
					}
					else
					{
						result = file.delete() && result;
					}
				}
			}
			result = dir.delete() && result;
		}
		return result;
	}

	public static String getAppStorageDirectory(final Context context)
	{
		String result = null;

		final Context ctx = getSafeContext(context);

		if (ctx != null)
		{
			final String extStorageState = Environment.getExternalStorageState();
			if (extStorageState.equals(Environment.MEDIA_MOUNTED))
			{
				final File externalDirectory = Environment.getExternalStorageDirectory();
				if (externalDirectory != null)
				{
					final String packageName = ctx.getString(R.string.package_name); // TODO: Replace R.string.package_name with a system call?
					result = externalDirectory.getAbsolutePath() + "/Android/data/" + packageName + "/files/";
				}
			}
			else
			{
				Util.displayToast(ctx, R.string.sdcard_unavailable, Toast.LENGTH_LONG);
			}
		}

		return result;
	}

	public static void setUpdateRepeat(final Context context)
	{
		final Context ctx = getSafeContext(context);

		final Time midnightToday = MidnightToday();
		final int nextDayMinute = getNextDayMinute(ctx);
		final long triggerAtTime = midnightToday.toMillis(false) + nextDayMinute*Util.SEC_PER_MIN*Util.MILSEC_PER_SEC;

		final Intent intent = new Intent(ctx, UpdateBroadcastReceiver.class);
		intent.setAction(UpdateService.ACTION);
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		final AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

		alarmManager.cancel(pendingIntent);
		alarmManager.setRepeating(AlarmManager.RTC, triggerAtTime, AlarmManager.INTERVAL_DAY, pendingIntent);
	}

	public static void viewAction(final Context context, final int stringUrlId)
	{
		final Context ctx = Util.getSafeContext(context);
		if (ctx != null)
		{
			final String url = ctx.getString(stringUrlId);
			final Uri uri = Uri.parse(url);
			final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(ctx, intent);
		}
	}
}
