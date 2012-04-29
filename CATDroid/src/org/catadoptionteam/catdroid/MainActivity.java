package org.catadoptionteam.catdroid;

import java.util.Random;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ListActivity implements OnClickListener
{
	private interface IDrawableProvider
	{
		Drawable getDrawable();
	}

	private static class CachedIconProvider implements IDrawableProvider
	{
		private Drawable drawable_;

		private CachedIconProvider(final Context context, final int drawableResID)
		{
			drawable_ = context.getResources().getDrawable(drawableResID);
		}

		@Override
		public Drawable getDrawable()
		{
			return drawable_;
		}
	}

	private static final class ActionItem
	{
		private boolean enabled_;
		private final OnClickListener iconClickListener_;
		private final IDrawableProvider drawableProvider_;
		private final OnClickListener buttonClickListener_;
		private final int textResID_;

		public ActionItem(final OnClickListener iconClickListener, final IDrawableProvider drawableProvider, final OnClickListener buttonClickListener, final int textResID)
		{
			enabled_ = true;
			iconClickListener_ = iconClickListener;
			drawableProvider_ = drawableProvider;
			buttonClickListener_ = buttonClickListener;
			textResID_ = textResID;
		}

//		public void setEnabled(final boolean enabled)
//		{
//			enabled_ = enabled;
//		}
	}

	private static final class ViewHolder // TODO: Make more local?
	{
		public ImageButton icon_;
		// public TextView void_;
		public Button title_;
	}

	private static class ActionAdapter extends ArrayAdapter<ActionItem>
	{
		private final Context context_;
		private final LayoutInflater inflater_;
		private final ActionItem[] action_;

		// TODO: AreAllItemsEnabled() return false, and define isEnabled(int position)

		public ActionAdapter(Context context, int resource, ActionItem[] action)
		{
			super(context, resource, R.id.textVoid, action);

			context_ = Util.getSafeContext(context);

			action_ = action;

			inflater_ = LayoutInflater.from(context);
		}

		@Override
		public boolean areAllItemsEnabled()
		{
			super.areAllItemsEnabled();
			return false;
		}

		@Override
		public boolean isEnabled(int position)
		{
			super.isEnabled(position);
			final boolean result = action_[position].enabled_;
			return result;
		}

		@Override
		public boolean hasStableIds()
		{
			super.hasStableIds();
			return true;
		}

		@Override
		public int getViewTypeCount()
		{
			super.getViewTypeCount();
			return 1;
		}

		@Override
		public int getCount()
		{
			super.getCount();
			return action_.length;
		}

		@Override
		public ActionItem getItem(int position)
		{
			super.getItem(position);
			return action_[position];
		}

		@Override
		public long getItemId(int position)
		{
			super.getItemId(position);
			return position;
		}

		@Override
		public int getItemViewType(int position)
		{
			super.getItemViewType(position);
			return 1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			// return super.getView(position, convertView, parent); // Intentionally not calling super method. 

			ViewHolder viewHolder;

			if (convertView == null)
			{
				convertView = inflater_.inflate(R.layout.main_list_item, parent, false);

				viewHolder = new ViewHolder();
				viewHolder.icon_ = (ImageButton) convertView.findViewById(R.id.imageIcon);
				// viewHolder.void_ = (TextView) convertView.findViewById(R.id.textVoid);
				viewHolder.title_ = (Button) convertView.findViewById(R.id.buttonTitle);

				convertView.setTag(viewHolder);
			}
			else
			{
				viewHolder = (ViewHolder) convertView.getTag();
			}

			final ActionItem action = action_[position];

			convertView.setFocusable(false);
			convertView.setOnClickListener(action.buttonClickListener_);

			final IDrawableProvider drawableProvider = action.drawableProvider_;
			final Drawable drawable = drawableProvider.getDrawable();

			final String buttonText = context_.getString(action.textResID_);

			final ImageButton icon = viewHolder.icon_;
			final Button button = viewHolder.title_;

			icon.setOnClickListener(action.iconClickListener_);
			icon.setImageDrawable(drawable);

			button.setOnClickListener(action.buttonClickListener_);
			button.setText(buttonText);

			return convertView;
		}
	}

	private static class LogoGestureListener extends SimpleOnGestureListener
	{
		private static final int MAX_VELOCITY = 500;
		MainActivity mainActivity_;

		public LogoGestureListener(final MainActivity mainActivity)
		{
			mainActivity_ = mainActivity;
		}

//		@Override
//		public boolean onDown(MotionEvent e) {
//			// TODO Auto-generated method stub
//			return super.onDown(e);
//		}"
//
//		@Override
//		public void onShowPress(MotionEvent e) {
//			// TODO Auto-generated method stub
//			super.onShowPress(e);
//		}
//
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e)
		{
			final boolean result = super.onSingleTapConfirmed(e);
			mainActivity_.meow();
			return result;
		}
//
//		@Override
//		public boolean onSingleTapUp(MotionEvent e)
//		{
//			final boolean result = super.onSingleTapUp(e);
//			meow();
//			return result;
//		}
//

		@Override
		public boolean onDoubleTap(MotionEvent e)
		{
			final boolean result = super.onDoubleTap(e);

			final Intent couponIntent = new Intent(mainActivity_, CouponActivity.class);
			Util.startActivity(mainActivity_, couponIntent);

			return result;
		}

		@Override
		public void onLongPress(MotionEvent e)
		{
			super.onLongPress(e);
			mainActivity_.cheshire();
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			boolean result = false;

			final float dy = e2.getY() - e1.getY();
			final float dx = e2.getX() - e1.getX();

			try
			{
				if 
				(
					Math.abs(dx) > Math.abs(dy) && 
					dx > mainActivity_.frameLogo_.getWidth()/4 && 
					velocityX < MAX_VELOCITY
				)
				{
					mainActivity_.purr();
					result = true;
				}
			}
			catch (Exception e)
			{
				// intentionally empty
			}

//			if (result == false)
//			{
//				result = super.onFling(e1, e2, velocityX, velocityY);
//			}

			return result;
		}
	}

	private static final int DEFAULT_TIMEOUT_MIN = 2;

	private static final int MPH = Util.MIN_PER_HR;

	private static final int[] openMin =  {10*MPH, 11*MPH, 11*MPH, 11*MPH, 11*MPH, 11*MPH, 10*MPH};
	private static final int[] closeMin = {18*MPH, 19*MPH, 19*MPH, 19*MPH, 19*MPH, 19*MPH, 18*MPH};

	private static final int[] openClockResID   = {R.drawable.clock10, R.drawable.clock11, R.drawable.clock11, R.drawable.clock11, R.drawable.clock11, R.drawable.clock11, R.drawable.clock10};
	private static final int[] closeClockResID  = {R.drawable.clock18, R.drawable.clock19, R.drawable.clock19, R.drawable.clock19, R.drawable.clock19, R.drawable.clock19, R.drawable.clock18};

	private static final int DIALOG_INFO_ID = 1;
	private static final int DIALOG_REFRESH_ID = 2;

//	private static final int COTD_ACTION_POSITION = 0;
//	private static final int M9L_ACTION_POSITION = 1;

	private static String KEY_FIRST_TIME = "MAIN_FIRST_TIME";

	private SharedPreferences preferences_;

	private static boolean updateInProgress_ = false;
	private static Handler updateCompleteHandler_ = null;

	private ActionAdapter actionAdapter_;

	private FrameLayout frameLogo_;
//	private ImageView logoText_;
	private ImageView logoCat_;

	private GestureDetector logoGestureDetector_;

	private MediaPlayer mediaPlayer_;

	private Vibrator vibrator_;

// TODO:	private AnimationDrawable logoAnimation_;

	private Random random_;

	private android.content.DialogInterface.OnClickListener refreshDialogClickListener = 
		new android.content.DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				switch (which)
				{
				case DialogInterface.BUTTON_POSITIVE:
					refreshApp();
				break;

				case DialogInterface.BUTTON_NEGATIVE:
				break;

				default:
					// TODO:  Diagnose?
				break;
				}
			}
		};

	private MediaPlayer.OnCompletionListener mediaCompletionListener = 
		new MediaPlayer.OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mp)
			{
				releaseMediaPlayer();
			}
		};

	public MainActivity()
	{
		final long seed = System.currentTimeMillis();
		random_ = new Random();
		random_.setSeed(seed);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		preferences_ = PreferenceManager.getDefaultSharedPreferences(this);

		final OnClickListener cotd = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				cotd();
			}
		};
		final CachedIconProvider cotdDrawableProvider = new CachedIconProvider(this, R.drawable.cotd);
		final ActionItem cotdAction = new ActionItem(cotd, cotdDrawableProvider, cotd, R.string.cotd);

		final OnClickListener m9l = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				m9l();
			}
		};
		final CachedIconProvider m9lDrawableProvider = new CachedIconProvider(this, R.drawable.m9l);
		final ActionItem m9lAction = new ActionItem(m9l, m9lDrawableProvider, m9l, R.string.m9l);

		final OnClickListener mymToast = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final int felineality = getFelineality();
				if (MYMSurvey.isFelineality(felineality))
				{
					final int felinealityTitleResID = MYMSurvey.getFelinealityTitleId(felineality);
					displayToast(felinealityTitleResID);
				}
				else
				{
					mym();
				}
			}
		};
		final OnClickListener mym = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mym();
			}
		};
		final IDrawableProvider mymDrawableProvider = new IDrawableProvider()
		{
			@Override
			public Drawable getDrawable()
			{
				final int felinealityResID = getFelinealityIconResID();
				final Drawable result = getResources().getDrawable(felinealityResID);
				return result;
			}
		};
		final ActionItem mymAction = new ActionItem(mymToast, mymDrawableProvider, mym, R.string.mym);

		final OnClickListener search = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				search();
			}
		};
		final CachedIconProvider searchDrawableProvider = new CachedIconProvider(this, R.drawable.search);
		final ActionItem searchAction = new ActionItem(search, searchDrawableProvider, search, R.string.search);

		final OnClickListener catTube = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				catTube();
			}
		};
		final CachedIconProvider catTubeDrawableProvider = new CachedIconProvider(this, R.drawable.cattube);
		final ActionItem catTubeAction = new ActionItem(catTube, catTubeDrawableProvider, catTube, R.string.cattube);

		final OnClickListener hrsLocsToast = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final Time now = new Time();
				now.setToNow();

				final int weekDay = now.weekDay;
				final int hour = now.hour;
				final int min = hour*MPH;

				int stringResID;

				if (min < openMin[weekDay] || min >= closeMin[weekDay] || isHoliday(now))
				{
					stringResID = R.string.closed;
				}
				else
				{
					stringResID = R.string.open;
				}

				final String openStatus = getString(R.string.shelter_is) + " " + getString(stringResID);

				displayToast(openStatus);

				dataListChanged();
			}
		};
		final OnClickListener hrsLocs = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				hrsLocs();
			}
		};
		final IDrawableProvider hrsLocsDrawableProvider = new IDrawableProvider()
		{
			@Override
			public Drawable getDrawable()
			{
				final int clockResID = getClockResID();
				final Drawable result = getResources().getDrawable(clockResID);
				return result;
			}
		};
		final ActionItem hrsLocsAction = new ActionItem(hrsLocsToast, hrsLocsDrawableProvider, hrsLocs, R.string.hrs_locs);

		final ActionItem[] action = 
		{
			cotdAction, 
			m9lAction, 
			mymAction, 
			searchAction, 
			catTubeAction, 
			hrsLocsAction
		};

		actionAdapter_ = new ActionAdapter(this, R.layout.main_list_item, action);

		final ListView listView = getListView();

		listView.setAdapter(actionAdapter_); // setListAdapter(actionAdapter_); // TODO: Are these equivalent? 
		listView.setItemsCanFocus(true);


		frameLogo_ = (FrameLayout) findViewById(R.id.frameLogo);
//		logoText_ = (ImageView) findViewById(R.id.imageLogoCheshire);
		logoCat_ = (ImageView) findViewById(R.id.imageLogoCat);

//TODO:		logoAnimation_ = (AnimationDrawable) logoImage_.getBackground();

		setListeners();

		updateCompleteHandler_ = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				clearLoading();
				// suggestMYMSurvey();
			}
		};

		vibrator_ = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		setVolumeControlStream(AudioManager.STREAM_MUSIC); // Allow user to control media sounds.
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume()
	{
		super.onResume();

		final boolean firstTime = getFirstTime();
		if (firstTime)
		{
			setFirstTime(false);
			updateData(this);
			final boolean surveyCompleted = isMYMSurveyCompleted();
			if (!surveyCompleted)
			{
				mym();
			}
		}
		suggestMYMSurvey();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();

		dataListChanged();
	}

	@Override
	public void onClick(View v)
	{
		try
		{
			switch (v.getId())
			{
				case R.id.imageEmail:
				{
					email();
				}
				break;

				case R.id.imagePhone:
				{
					phone();
				}
				break;

				case R.id.imageMap:
				{
					try
					{
						map();
					}
					catch (ActivityNotFoundException mapException)
					{
						webmap();
					}
				}
				break;

				case R.id.imageWeb:
				{
					web();
				}
				break;

				default:
				{
					// TODO: Deal with diagnostics...
				}
				break;
			}
		}
		catch (ActivityNotFoundException e)
		{
			displayToast(R.string.feature_unavailable);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean result = true;

		switch (item.getItemId())
		{
		case R.id.info:
			info();
			break;

		case R.id.preferences:
			preferences();
			break;

		case R.id.refresh:
			if (updateInProgress_)
			{
				displayBusy();
			}
			else
			{
				refresh();
			}
			break;

		default:
			result = super.onOptionsItemSelected(item);
			break;
		}

		return result;
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		Dialog result = null;

		switch (id)
		{
		case DIALOG_INFO_ID:
			result = createInfoDialog();
			break;

		case DIALOG_REFRESH_ID:
			result = createRefreshDialog();
			break;

		default:
			result = super.onCreateDialog(id);
			break;
		}

		return result;
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		releaseMediaPlayer();
	}

//	@Override
//	protected void onDestroy()
//	{
//		super.onDestroy();
//	}

	private void cheshire()
	{
		final Animation animation = AnimationUtils.loadAnimation(this, R.anim.cheshire);
		logoCat_.startAnimation(animation);
	}

	private void purr()
	{
		sound(R.raw.purr);
		vibrate();
	}

	private void meow()
	{
		sound(R.raw.meow);
	}

	private void dataListChanged()
	{
		if (actionAdapter_ != null)
		{
			actionAdapter_.notifyDataSetChanged();
		}
	}

	private int getFelineality()
	{
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final int result = MYMSurvey.loadFelineality(preferences);
		return result;
	}

	private int getFelinealityIconResID()
	{
		final int felineality = getFelineality();
		final int felinealityResID = MYMSurvey.getFelinealityIconId(felineality);
		return felinealityResID;
	}

	private int getClockResID()
	{
		final Time now = new Time();
		now.setToNow();

		final int weekDay = now.weekDay;
		final int tomorrowWeekDay = (weekDay+1)%Util.DAY_PER_WK;

		int result = openClockResID[weekDay]; // default to today's opening.

		if (isHoliday(now))
		{
			result = openClockResID[tomorrowWeekDay];
		}
		else
		{
			final int nowMinute = Util.toMinutes(now.hour, now.minute);

			if (nowMinute >= closeMin[weekDay])
			{
				result = openClockResID[tomorrowWeekDay];
			}
			else if (nowMinute >= openMin[weekDay])
			{
				result = closeClockResID[weekDay];
			}
		}

		return result;
	}

	private boolean getFirstTime()
	{
		final boolean result = preferences_.getBoolean(KEY_FIRST_TIME, true);

		return result;
	}

	private void setFirstTime(final boolean value)
	{
		final SharedPreferences.Editor editor = preferences_.edit();

		editor.putBoolean(KEY_FIRST_TIME, value);

		editor.commit();
	}

	private void displayBusy()
	{
		setUpdateTimeout();
		displayToast(R.string.busy_downloading);
	}

	private static void setLoading()
	{
		setUpdateTimeout();
	}

	private void clearLoading()
	{
		updateInProgress_ = false;
	}

	private static boolean isHoliday(final Time time)
	{
		final int month = time.month;
		final int monthDay = time.monthDay;
		final int weekDay = time.weekDay;
		final int hour = time.hour;

		final boolean result = 
			month == Util.JAN && monthDay == 1                                                 || // New Year's Day
			month == Util.MAY && weekDay == Time.MONDAY && monthDay > 31 - 7                   || // US Memorial Day
			month == Util.JUL && monthDay == 4                                                 || // US Independence Day
			month == Util.SEP && weekDay == Time.MONDAY && monthDay <= 7                       || // US Labor Day
			month == Util.NOV && weekDay == Time.THURSDAY && monthDay > 3*7 && monthDay <= 4*7 || // US Thanksgiving
			month == Util.DEC && monthDay == 24 && hour > 15                                   || // Christmas Eve
			month == Util.DEC && monthDay == 25                                                || // Christmas
			month == Util.DEC && monthDay == 31 && hour > 15;                                     // New Years Eve

		return result;
	}

	private void displayToast(final String message)
	{
		Util.displayToast(this, message, Toast.LENGTH_LONG);
	}

	private void displayToast(final int stringResID)
	{
		Util.displayToast(this, stringResID, Toast.LENGTH_LONG);
	}

	private boolean isMYMSurveyCompleted()
	{
		final MYMSurvey mymSurvey = new MYMSurvey(this);
		final int felineality = mymSurvey.loadFelineality();
		final boolean result = MYMSurvey.isFelineality(felineality);
		return result;
	}

//	private void attention(final Button button)
//	{
//		final Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_io);
//		button.startAnimation(animation);
//	}

	private void suggestMYMSurvey()
	{
		final boolean surveyCompleted = isMYMSurveyCompleted();
		if (!surveyCompleted)
		{
			displayToast(R.string.suggest_survey);
		}
	}

	public static void signalUpdateCompleted()
	{
		final Handler handler = updateCompleteHandler_;
		if (handler == null)
		{
			updateInProgress_ = false;
		}
		else
		{
			// final Message message = Message.obtain();
			handler.sendEmptyMessage(0);
			// handler.sendMessage(message);
		}
	}

	private static void setUpdateTimeout()
	{
		if (updateCompleteHandler_ != null)
		{
			updateInProgress_ = true;
			updateCompleteHandler_.sendEmptyMessageDelayed(0, DEFAULT_TIMEOUT_MIN * Util.SEC_PER_MIN * Util.MILSEC_PER_SEC);
		}
	}

	public static void updateData(final Context context)
	{
		if (!updateInProgress_)
		{
			final Context ctx = Util.getSafeContext(context);
			final Intent intent = getUpdateService(ctx);
			ctx.startService(intent);
			setLoading();
		}
	}

	private AlertDialog createOKDialog(final int resIdMessage)
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.info);
		String versionInfo = getString(R.string.unknown);
		try
		{
			final PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
			versionInfo = pInfo.versionName;
		}
		catch (PackageManager.NameNotFoundException e)
		{
		}
		final String message = getString(resIdMessage) + "\n\nVersion: " + versionInfo;
		builder.setMessage(message);
		builder.setPositiveButton(R.string.ok, null);
		final AlertDialog result = builder.create();
		return result;
	}

	private AlertDialog createYesNoDialog(final int resIdMessage, final android.content.DialogInterface.OnClickListener dialogClickListener)
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.refresh);
		final String message = getString(resIdMessage);
		builder.setMessage(message);
		builder.setPositiveButton(R.string.yes, dialogClickListener);
		builder.setNegativeButton(R.string.no, dialogClickListener);
		final AlertDialog result = builder.create();
		return result;
	}

	private AlertDialog createInfoDialog()
	{
		final AlertDialog result = createOKDialog(R.string.info_message);
		return result;
	}

	private AlertDialog createRefreshDialog()
	{
		final AlertDialog result = createYesNoDialog(R.string.refresh_message, refreshDialogClickListener);
		return result;
	}

	private void info()
	{
		showDialog(DIALOG_INFO_ID);
	}

	private void preferences()
	{
		final Intent preferencesIntent = new Intent(this, SharedPreferencesActivity.class);
		Util.startActivity(this, preferencesIntent);
	}

	private void refreshApp()
	{
		UpdateService.clearUpdatedToday(preferences_);
		dataListChanged();
		final AppDBAdapter appDBAdapter = new AppDBAdapter(this);
		appDBAdapter.deleteAll();
		appDBAdapter.close();
		final ContentResolver contentResolver = getContentResolver();
		contentResolver.delete(VideoProvider.CONTENT_URI, null, null);
		contentResolver.delete(PetfinderProvider.CONTENT_URI_PET, null, null);
		updateData(this);
	}

	private void refresh()
	{
		showDialog(DIALOG_REFRESH_ID);
	}

	private static Intent getUpdateService(final Context context)
	{
//		TODO:  This commented out code seems to work too.  What are the advantages between them?  
//		final Intent intent = new Intent(context, UpdateService.class);
//		context.startService(intent);

		Intent result = null;

		final Context ctx = Util.getSafeContext(context);
		if (ctx != null)
		{
			final String updateServiceClassName = UpdateService.class.getName();
			final String contextPackageName = ctx.getPackageName();
			final ComponentName componentName = new ComponentName(contextPackageName, updateServiceClassName);
			result = new Intent();
			result.setComponent(componentName);
		}
		return result;
	}

	private void setListeners()
	{
		logoGestureDetector_ = new GestureDetector(new LogoGestureListener(this));

		View.OnTouchListener gestureListener = 
			new View.OnTouchListener()
			{
				public boolean onTouch(View v, MotionEvent event)
				{
					return logoGestureDetector_.onTouchEvent(event);
				}
			};

		frameLogo_.setOnClickListener(this);
		frameLogo_.setOnTouchListener(gestureListener);

		final ImageButton emailImage = (ImageButton) findViewById(R.id.imageEmail);
		emailImage.setOnClickListener(this);

		final ImageButton phoneImage = (ImageButton) findViewById(R.id.imagePhone);
		phoneImage.setOnClickListener(this);

		final ImageButton mapImage = (ImageButton) findViewById(R.id.imageMap);
		mapImage.setOnClickListener(this);

		final ImageButton webImage = (ImageButton) findViewById(R.id.imageWeb);
		webImage.setOnClickListener(this);
	}

	private void releaseMediaPlayer()
	{
		if (mediaPlayer_ != null)
		{
			mediaPlayer_.release();
			mediaPlayer_ = null;
		}
	}

	private void vibrate()
	{
		if (vibrator_ != null)
		{
			final int purr = 2650;
			final int pause = 500;
			long[] pattern = 
			{
				100, // Start in 1/10 of a second.  
				purr, pause
			};
			vibrator_.vibrate(pattern, -1);
		}
	}

	private void sound(final int mp3)
	{
		if (mediaPlayer_ == null)
		{
			mediaPlayer_ = MediaPlayer.create(this, mp3);
			mediaPlayer_.setOnCompletionListener(mediaCompletionListener);
			mediaPlayer_.start();
		}
	}

//	private void swishTail()
//	{
//		logoImage_.setAlpha(0);
//		logoAnimation_.stop();
//		logoAnimation_.start();
//	}
//
//	private void animateLogo()
//	{
//		final int behavior = random_.nextInt(1); // Change to random_.nextInt(N); for additional cases.  
//		switch (behavior)
//		{
//		case 0:
//			sound(R.raw.meow);
//			break;
//
//		case 1:
//			// sound(R.raw.purr);
//			// vibrate();
//			break;
//
//		case 2: // TODO: Implement swishTail animation
//			// swishTail();
//			break;
//
//		case 3: // TODO: Implement Cheshire Cat Animation
//			// cheshire();
//			break;
//
//		default:
//			break;
//		}
//	}

	private void cotd()
	{
		final int keyID = Util.getCOTDID(this);
		if (updateInProgress_)
		{
			displayBusy();
		}
		else if (keyID == AppDBAdapter.INVALID_KEY_ID)
		{
			UpdateService.clearUpdatedToday(preferences_);
			updateData(this);
			displayBusy();
		}
		else
		{
			final Intent intent = new Intent(this, BiographyActivity.class);
			intent.putExtra(AppDBAdapter.KEY_ID, keyID);
			Util.startActivity(this, intent);
		}
	}

	private void m9l()
	{
		if (updateInProgress_)
		{
			displayBusy();
		}
		else
		{
			final Intent intent = new Intent(this, M9LActivity.class);
			Util.startActivity(this, intent);
		}
	}

	private void mym()
	{
		final Intent intent = new Intent(this, MYMCategoryActivity.class);
		Util.startActivity(this, intent);
	}

	private void search()
	{
		final Intent intent = new Intent(this, SearchActivity.class);
		Util.startActivity(this, intent);
	}

	private void catTube()
	{
		final Intent intent = new Intent(this, YouTubeActivity.class);
		Util.startActivity(this, intent);
	}

	private void hrsLocs()
	{
		final Intent intent = new Intent(this, WebViewActivity.class);
		// intent.setData(data); // TODO: Should I use this???
		final String url = getString(R.string.shelter_hours_location_url);
		intent.putExtra(WebViewActivity.URL, url);
		Util.startActivity(this, intent);
//		Util.viewAction(this, R.string.shelter_location_hours_url); // Use this for an Internet located URL.
	}

	private void email()
	{
		final String emailString = "mailto:" + getString(R.string.shelter_email);
		final Uri emailURI = Uri.parse(emailString);
		final Intent intent = new Intent(Intent.ACTION_VIEW, emailURI);
		final String subject = getString(R.string.email_subject);
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		Util.startActivity(this, intent);
	}

	private void phone()
	{
		// TODO: Test and do some research on getting the phone call to automatically return to app with PhoneStateListener
		final String phoneString = "tel:" + getString(R.string.shelter_tel_number);
		final Uri phoneURI = Uri.parse(phoneString);
		final Intent intent = new Intent(Intent.ACTION_DIAL, phoneURI);
		Util.startActivity(this, intent);
	}

	private void map()
	{
		final String geoURIString = getString(R.string.shelter_geo);
		final Uri geoURI = Uri.parse(geoURIString);
		final Intent intent = new Intent(Intent.ACTION_VIEW, geoURI);
		Util.startActivity(this, intent);
	}

	private void webmap()
	{
		final String webMapURLString = getString(R.string.shelter_webmap);
		final Uri webURI = Uri.parse(webMapURLString);
		final Intent intent = new Intent(Intent.ACTION_VIEW, webURI);
		Util.startActivity(this, intent);
	}

	private void web()
	{
		final String webURLString = getString(R.string.shelter_url);
		final Uri webURI = Uri.parse(webURLString);
		final Intent intent = new Intent(Intent.ACTION_VIEW, webURI);
		Util.startActivity(this, intent);
	}
}
