/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

/**
 * @author bessermt
 *
 */

public class TimePickerPreference extends DialogPreference implements OnTimeChangedListener
{
	private TimePicker timePicker_;
	private int currentMinutes_;

	public TimePickerPreference(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initialize();
	}

	public TimePickerPreference(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

//	public TimePickerPreference(Context context)
//	{
//		this(context, null);
//	}

	private void initialize()
	{
		setDialogLayoutResource(R.layout.time_picker_preference);
		currentMinutes_ = 0;
	}

	@Override
	protected View onCreateDialogView()
	{
		final View result = super.onCreateDialogView();

		currentMinutes_ = getPersistedMinutes();

		return result;
	}

	@Override
	protected void onBindDialogView(View view)
	{
		super.onBindDialogView(view);
		final int hour = currentMinutes_/Util.MIN_PER_HR;
		final int minute = currentMinutes_%Util.MIN_PER_HR;
		timePicker_ = (TimePicker) view.findViewById(R.id.timePicker);
		timePicker_.setCurrentMinute(minute);
		timePicker_.setCurrentHour(hour);
		timePicker_.setOnTimeChangedListener(this);
	}

	// Bug Patch for when the user goes to landscape and the hour doesn't redisplay.
	@Override
	protected void showDialog(Bundle state)
	{
		super.showDialog(state);
		timePicker_.setCurrentMinute(timePicker_.getCurrentMinute());
		timePicker_.setCurrentHour(timePicker_.getCurrentHour());
	}

// Commented because it could be handy for custom controls someday.
//
//	private static String KEY_INSTANCE_STATE = "INSTANCE_STATE";
//	private static String KEY_STATE_CURRENT_MINUTES = "STATE_CURRENT_MINUTES";
//
//	@Override
//	protected void onRestoreInstanceState(Parcelable state)
//	{
//		Parcelable restoreState = state;
//		if (restoreState instanceof Bundle)
//		{
//			final Bundle bundle = (Bundle) restoreState;
//			currentMinutes_ = bundle.getInt(KEY_STATE_CURRENT_MINUTES);
//			final Parcelable instanceState = bundle.getParcelable(KEY_INSTANCE_STATE);
//			if (instanceState != null)
//			{
//				restoreState = instanceState;
//			}
//		}
//		super.onRestoreInstanceState(restoreState);
//	}
//
//	@Override
//	protected Parcelable onSaveInstanceState()
//	{
//		final Bundle result = new Bundle();
//
//		final Parcelable parcelable = super.onSaveInstanceState();
//
//		result.putParcelable(KEY_INSTANCE_STATE, parcelable);
//		result.putInt(KEY_STATE_CURRENT_MINUTES, currentMinutes_);
//
//		return result;
//	}

	@Override
	public void onTimeChanged(TimePicker view, int hourOfDay, int minute)
	{
		currentMinutes_ = Util.toMinutes(hourOfDay, minute);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult)
	{
		timePicker_.clearFocus();
		super.onDialogClosed(positiveResult);
		if (positiveResult)
		{
			setPersisted(currentMinutes_);
		}
	}

	private int getPersistedMinutes()
	{
		final int result = getPersistedInt(Util.getNowDayMinutes());
		return result;
	}

	private void setPersisted(final int minutes)
	{
		final boolean persist = shouldPersist(); // Always true for now.
		if (persist)
		{
			persistInt(minutes);
			Util.setUpdateRepeat(null);
		}
	}
}
