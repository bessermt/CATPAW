/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.TextView;

/**
 * @author bessermt
 *
 */
public class GameClock implements Runnable
{
	public static final int MIN_SECONDS = 0;
	public static final int MAX_SECONDS = 1*Util.SEC_PER_MIN*Util.MIN_PER_HR; // 1 Hour

	private static final String KEY_ELAPSED_MILLISEC = "GAMECLOCK_ELAPSED_MILLISEC";
	private static final String KEY_PREVIOUS_MILLISEC = "GAMECLOCK_PREVIOUS_MILLISEC";
	private static final String KEY_STATE = "GAMECLOCK_STATE";

	private static final int STOPPED = 0;
	private static final int RUNNING = 1;
	private static final int PAUSED  = 2;

	private int state_;

	private int elapsedMillisecs_;
	private long previousMillisecs_;
	private int previousElapsedSeconds_;

	private SharedPreferences preferences_;

	private Handler handlerGameClock_;

	private TextView textGameClock_;

	public GameClock(final Context context, final TextView textGameClock)
	{
		preferences_ = PreferenceManager.getDefaultSharedPreferences(context);

		handlerGameClock_ = new Handler();

		textGameClock_ = textGameClock;

		init();
	}

	private void init()
	{
		state_ = STOPPED;

		elapsedMillisecs_ = 0;
		previousElapsedSeconds_ = 0;
		previousMillisecs_ = 0L;

		update(0);
	}

	@Override
	public void run()
	{
		if (isRunning())
		{
			final long currentMillisecs = clock();
			elapsedMillisecs_ += currentMillisecs - previousMillisecs_;
			previousMillisecs_ = currentMillisecs;

			elapsedMillisecs_ = range(elapsedMillisecs_);

			update();

			handlerGameClock_.postDelayed(this, 200);
		}
	}

	private static int range(final int millisecs)
	{
		int result = millisecs;
		if (result< MIN_SECONDS*Util.MILSEC_PER_SEC)
		{
			result = MIN_SECONDS*Util.MILSEC_PER_SEC;
		}
		if (result > MAX_SECONDS*Util.MILSEC_PER_SEC)
		{
			result = MAX_SECONDS*Util.MILSEC_PER_SEC;
		}
		return result;
	}

	private long clock()
	{
		final long result = SystemClock.uptimeMillis(); // TODO: Use SystemClock.elapsedRealtime()?
		return result;
	}

	public static String timeToString(final int seconds)
	{
		final int minutes = seconds / 60;
		final int ss = seconds % 60;

		String delimiter = ":";
		if (ss < 10)
		{
			delimiter = ":0";
		}

		final String result = String.valueOf(minutes) + delimiter + String.valueOf(ss);
		return result;
	}

	private void update(final int elapsedSeconds)
	{
		final String clockText = timeToString(elapsedSeconds);

		textGameClock_.setText(clockText);
	}

	public void update()
	{
		final int elapsedSeconds = elapsedMillisecs_ / Util.MILSEC_PER_SEC;
		if (elapsedSeconds != previousElapsedSeconds_)
		{
			previousElapsedSeconds_ = elapsedSeconds;

			update(elapsedSeconds);
		}
	}

	public void load()
	{
		final long currentMillisecs = clock();

		elapsedMillisecs_ = preferences_.getInt(KEY_ELAPSED_MILLISEC, MIN_SECONDS*Util.MILSEC_PER_SEC);
		elapsedMillisecs_ = range(elapsedMillisecs_);

		previousMillisecs_ = preferences_.getLong(KEY_PREVIOUS_MILLISEC, currentMillisecs);
		if (previousMillisecs_<0 || previousMillisecs_>currentMillisecs)
		{
			previousMillisecs_ = currentMillisecs;
		}

		state_ = preferences_.getInt(KEY_STATE, STOPPED);

		if (isRunning())
		{
			start();
		}
	}

	public void save()
	{
		final SharedPreferences.Editor editor = preferences_.edit();

		editor.putInt(KEY_ELAPSED_MILLISEC, elapsedMillisecs_);
		editor.putLong(KEY_PREVIOUS_MILLISEC, previousMillisecs_);
		editor.putInt(KEY_STATE, state_);

		editor.commit();
	}

	public int getSeconds()
	{
		final int result = elapsedMillisecs_/Util.MILSEC_PER_SEC;
		return result;
	}

	public boolean isRunning()
	{
		final boolean result = state_==RUNNING;
		return result;
	}

	public void start()
	{
		if (!isRunning())
		{
			state_ = RUNNING;
			previousMillisecs_ = clock();
		}
		run();
	}

	public void pause()
	{
		state_ = PAUSED;
	}

	public void reset()
	{
		init();
	}

	public void stop()
	{
		state_ = STOPPED;
	}
}
