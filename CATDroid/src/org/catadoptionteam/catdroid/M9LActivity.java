/**
 * 
 */
package org.catadoptionteam.catdroid;
import java.util.Random;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author bessermt
 *
 */

// TODO: Consider http://developer.android.com/resources/samples/XmlAdapters/src/com/example/android/xmladapters/ImageDownloader.html
// TODO: Consider http://code.google.com/p/shelves/source/browse/trunk/Shelves/src/org/curiouscreature/android/shelves/util/ImageUtilities.java

// TODO: Change Match button to "New Game" if maximum time is reached...

public class M9LActivity extends Activity
{
	private static final int ROWS = 2;
	private static final int COLS = MYMSurvey.FELINEALITY_CAPACITY;
	private static final int SIZE = ROWS*COLS;

	private static abstract class ImageProvider
	{
		private Bitmap[] cache_;

		protected ImageProvider(final int cacheSize)
		{
			cache_ = new Bitmap[cacheSize];
		}

		public final void clearCache()
		{
			int i = cache_.length;
			while (i != 0)
			{
				--i;
				final Bitmap bitmap = cache_[i];
				if (bitmap != null)
				{
					bitmap.recycle();
					cache_[i] = null;
				}
			}
		}

		public final Bitmap getBitmap(final int index)
		{
			Bitmap result = null;

			if (cache_[index] == null)
			{
				final Bitmap rawBitmap = createRawBitmap(index);
				if (rawBitmap != null)
				{
					result = createSquareSizeBitmap(rawBitmap);
					rawBitmap.recycle();
					cache_[index] = result;
				}
			}
			else
			{
				result = cache_[index];
			}
			return result;
		}

		private static final Bitmap createSquareSizeBitmap(final Bitmap photo)
		{
			final int SIDE_PIXELS = 256; // 187 // 69;// 190; // TODO:  Calculate height of gallery component's image area.

			Bitmap result;

			final int width = photo.getWidth();
			final int height = photo.getHeight();
			final int minSide = Math.min(width, height);
			final int x = (width-minSide)/2;
			final int y = (height-minSide)/2;

			final Bitmap croppedBitmap = Bitmap.createBitmap(photo, x, y, minSide, minSide);
			if (minSide != SIDE_PIXELS)
			{
				result = Bitmap.createScaledBitmap(croppedBitmap, SIDE_PIXELS, SIDE_PIXELS, true);
				croppedBitmap.recycle();
			}
			else
			{
				result = croppedBitmap;
			}

			return result;
		}

		protected abstract Bitmap createRawBitmap(final int index);
	}

	private static final class IconProvider extends ImageProvider
	{
		private Resources res_;

		public IconProvider(final Context context)
		{
			super(COLS);
			res_ = context.getResources();
		}

		private int getIconId(final int position)
		{
			final int index = position;
			final int result = MYMSurvey.getFelinealityIconId(index);
			return result;
		}

		@Override
		protected Bitmap createRawBitmap(int index)
		{
			final int iconId = getIconId(index);
			final Bitmap result = BitmapFactory.decodeResource(res_, iconId);
			return result;
		}
	}

	private static final class PhotoProvider extends ImageProvider
	{
		private Activity activity_;
		private int[] photoSequence_;
		private String path_;
		private Resources res_;

		public PhotoProvider(final Activity activity, final int[] photoSequence)
		{
			super(SIZE);
			activity_ = activity;
			photoSequence_ = photoSequence;
			path_ = AppDBAdapter.getImagePath(activity);
			res_ = activity.getResources();
		}

		private int getSequencedIndex(final int index)
		{
			int result = photoSequence_[index];
			return result;
		}

		private int getFelineality(final int index)
		{
			final int sequencedIndex = getSequencedIndex(index);
			final int result = sequencedIndex%COLS;
			return result;
		}

		private final Bitmap createRawPhoto(final int row, final int col)
		{
			Bitmap result = null;

			final String[] resultColumns = 
				new String[]
				{
					AppDBAdapter.SM_PHOTO
				};

			final String where = 
				AppDBAdapter.CATEGORY + "=" + AppDBAdapter.CATEGORY_M9L + " and " + 
				AppDBAdapter.ASPCA_ALITY + "=" + col;

			final String order = 
				AppDBAdapter.KEY_ID;

			AppDBAdapter db = new AppDBAdapter(activity_);

			Cursor cursor = null;

			try
			{
				cursor = db.query(activity_, resultColumns, where, order);

				if (cursor != null)
				{
					boolean success;
					if (row == 0)
					{
						success = cursor.moveToFirst();
					}
					else
					{
						success = cursor.moveToLast();
					}

					if (success)
					{
						final String photoSm = cursor.getString(0);

						if (photoSm != null)
						{
							result = Util.getBitmap(activity_, path_, photoSm);
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

		@Override
		protected final Bitmap createRawBitmap(final int index)
		{
			final int row = index/COLS;
			final int col = index%COLS;

			Bitmap result = createRawPhoto(row, col);
			if (result == null)
			{
				final int nullPhotoId = R.drawable.missing_photo;
				result = BitmapFactory.decodeResource(res_, nullPhotoId);
			}

			return result;
		}
	}

	private static abstract class GalleryAdapter extends BaseAdapter
	{
		private final Context context_;
		private final boolean[] matches_;
		private final ImageProvider imageProvider_;
		private final LayoutInflater inflater_;
		private int galleryItemBackground_ = View.NO_ID;

		protected GalleryAdapter(final Context context, final ImageProvider imageProvider, final boolean[] matches)
		{
			context_ = context;
			matches_ = matches;
			imageProvider_ = imageProvider;

			inflater_ = LayoutInflater.from(context);

			final TypedArray a = context.obtainStyledAttributes(R.styleable.M9LGallery);
			galleryItemBackground_ = 
				a.getResourceId
				(
					R.styleable.M9LGallery_android_galleryItemBackground, View.NO_ID
				);
			a.recycle();
		}

		@Override
		public int getCount()
		{
			final int result = COLS;
			return result;
		}

		@Override
		public Object getItem(int position) // TODO: What does this do?
		{
			return null;
		}

		@Override
		public long getItemId(int position) // TODO: What does this do?
		{
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			final class ViewHolder
			{
				public ImageView imageCell_;
				public ImageView imageCheck_;
			}

			View result = convertView;

			ViewHolder viewHolder;

			if (result == null)
			{
				result = (View)inflater_.inflate(R.layout.m9l_cell, parent, false /* required */);

				final int width = Gallery.LayoutParams.WRAP_CONTENT;
				final int height = Gallery.LayoutParams.FILL_PARENT;
				final Gallery.LayoutParams layoutParams = new Gallery.LayoutParams(width, height);
				result.setLayoutParams(layoutParams);

				result.setBackgroundResource(galleryItemBackground_);

				viewHolder = new ViewHolder();

				viewHolder.imageCell_ = (ImageView) result.findViewById(R.id.imageCell);
				viewHolder.imageCheck_ = (ImageView) result.findViewById(R.id.imageCheck);

				result.setTag(viewHolder);
			}
			else
			{
				viewHolder = (ViewHolder) result.getTag();
			}

			setImage(position, viewHolder.imageCell_);

			int visibility = View.VISIBLE;
			final boolean matched = getMatched(position);
			if (!matched)
			{
				visibility = View.INVISIBLE;
			}
			viewHolder.imageCheck_.setVisibility(visibility);

			return result;
		}

		private Bitmap getBitmap(final int position)
		{
			final int index = getIndex(position);
			final Bitmap result = imageProvider_.getBitmap(index);
			return result;
		}

		private void setImage(final int position, final ImageView imageView)
		{
			Bitmap bitmap = getBitmap(position);
			if (bitmap == null)
			{
				final int nullPhotoId = R.drawable.missing_photo;
				final Resources resource = context_.getResources();
				bitmap = BitmapFactory.decodeResource(resource, nullPhotoId);
			}

			imageView.setImageBitmap(bitmap);
		}

		private boolean getMatched(final int position)
		{
			final int index = getIndex(position);
			final int felineality = index%COLS;
			final boolean result = matches_[felineality];
			return result;
		}
		protected abstract int getIndex(final int position);
	}

	private static final class IconAdapter extends GalleryAdapter
	{
		protected IconAdapter(final Context context, final IconProvider iconProvider, final boolean[] matches)
		{
			super(context, iconProvider, matches);
		}

		@Override
		protected int getIndex(final int position)
		{
			final int result = position;
			return result;
		}
	}

	private static class PhotoAdapter extends GalleryAdapter
	{
		private int row_;
		private int[] photoSequence_;

		public PhotoAdapter(final Context context, final PhotoProvider photoProvider, final int[] photoSequence, final boolean[] matches, final int row)
		{
			super(context, photoProvider, matches);
			row_ = row;
			photoSequence_ = photoSequence;
		}

		@Override
		protected int getIndex(final int position)
		{
			final int i = COLS*row_ + position;
			final int result = photoSequence_[i];
			return result;
		}
	}

//	private static final String KEY_FIRST_TIME = "M9L_FIRST_TIME";
	private static final String KEY_MATCH = "M9L_MATCH";
	private static final String KEY_GUESS = "M9L_GUESS";
	private static final String KEY_PHOTO_SEQUENCE = "M9L_PHOTO_SEQUENCE";
	private static final String KEY_HIGH_SCORE_SECONDS = "M9L_HIGH_SCORE_SECONDS";
	private static final String KEY_HIGH_SCORE_GUESS = "M9L_HIGH_SCORE_GUESS";

	private static final int MIN_GUESSES = 9;
	private static final int MAX_GUESSES = 1000;

	private static final int DIALOG_HELP_ID = 1;
	private static final int DIALOG_PAUSED_ID = 2;
	private static final int DIALOG_HIGH_SCORE_ID = 3;

	private static final int TOP = 0;
	private static final int BOTTOM = 1;

	private static PhotoProvider photoProvider_;
	private static IconProvider iconProvider_;

	private Random random_;

	private PhotoAdapter topPhotoAdapter_;
	private IconAdapter iconAdapter_;
	private PhotoAdapter bottomPhotoAdapter_;

	private View matchline_;

	private Gallery galleryTopPhoto_;
	private Gallery galleryIcon_;
	private Gallery galleryBottomPhoto_;
	private TextView textMatchGuess_;
	private TextView textGameClock_;

	private Button buttonMatch_;
	private Button buttonNewGame_;

	private int userFelineality_;
	private boolean[] userFelinealityDetailViewed_;
	
	private int[] photoSequence_;

	private GameClock gameClock_;
	private boolean[] matches_;
	private int guesses_;

	private int highScoreSeconds_;
	private int highScoreGuess_;

	private Animation shake_;

	private SharedPreferences preferences_;

	private android.content.DialogInterface.OnClickListener pauseDialogClickListener = 
		new android.content.DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				gameClock_.start();
			}
		};

	private android.content.DialogInterface.OnClickListener helpDialogMoreClickListener = 
		new android.content.DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				final String uriStr = getString(R.string.m9l_help_url);
				final Uri uri = Uri.parse(uriStr);
				final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		};

	private OnClickListener matchClickListener = 
		new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				match();
			}
		};

	private OnClickListener newGameClickListener = 
		new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				newGame();
			}
		};

	private OnItemClickListener iconItemClickListener = 
		new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id)
			{
				iconHint(position);
			}
		};

	private OnItemClickListener photoTopItemClickListener = 
		new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id)
			{
				photoHint(TOP, position);
			}
		};

	private OnItemClickListener photoBottomItemClickListener = 
		new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id)
			{
				photoHint(BOTTOM, position);
			}
		};

	private OnItemSelectedListener iconItemSelectListener = 
		new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				updateMatchline();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
				updateMatchline();
			}
		};

	private OnItemLongClickListener iconLongClickListener = 
		new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
			{
				iconDetails(position);
				return false;
			}
		};

	private OnItemLongClickListener photoTopLongClickListener = 
		new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
			{
				photoDetails(TOP, position);
				return false;
			}
		};

	private OnItemLongClickListener photoBottomLongClickListener = 
		new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
			{
				photoDetails(BOTTOM, position);
				return false;
			}
		};

	public M9LActivity()
	{
		final long seed = System.currentTimeMillis();
		random_ = new Random();
		random_.setSeed(seed);

		photoSequence_ = new int[SIZE];

		guesses_ = 0;

		matches_ = new boolean[COLS];

		userFelinealityDetailViewed_ = new boolean[ROWS];
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.m9l);

		try // Center the activity title bar's text
		{
			(
				(TextView)
				(
					(FrameLayout)
					(
						(LinearLayout)
						(
							(ViewGroup)
							getWindow().getDecorView()
						).getChildAt(0)
					).getChildAt(0)
				).getChildAt(0)
			).setGravity(Gravity.CENTER);
		}
		catch (Throwable t)
		{
			// Tried and failed.  Oh well.  
		}

		shake_ = AnimationUtils.loadAnimation(this, R.anim.shake_horiz);

		preferences_ = PreferenceManager.getDefaultSharedPreferences(this);

		userFelineality_ = MYMSurvey.loadFelineality(preferences_);

		photoProvider_ = new PhotoProvider(this, photoSequence_);
		iconProvider_ = new IconProvider(this);

		galleryTopPhoto_ = (Gallery) findViewById(R.id.galleryTopPhoto);
		topPhotoAdapter_ = new PhotoAdapter(this, photoProvider_, photoSequence_, matches_, TOP);
		galleryTopPhoto_.setAdapter(topPhotoAdapter_);

		galleryIcon_ = (Gallery) findViewById(R.id.galleryIcon);
		iconAdapter_ = new IconAdapter(this, iconProvider_, matches_);
		galleryIcon_.setAdapter(iconAdapter_);

		galleryBottomPhoto_ = (Gallery) findViewById(R.id.galleryBottomPhoto);
		bottomPhotoAdapter_ = new PhotoAdapter(this, photoProvider_, photoSequence_, matches_, BOTTOM);
		galleryBottomPhoto_.setAdapter(bottomPhotoAdapter_);

		matchline_ = findViewById(R.id.matchline);

		buttonMatch_ = (Button) findViewById(R.id.buttonMatch);
		buttonMatch_.requestFocus();
		buttonNewGame_ = (Button) findViewById(R.id.buttonNewGame);

		textMatchGuess_ = (TextView) findViewById(R.id.textMatchGuess);
		textGameClock_ = (TextView) findViewById(R.id.textGameClock);
		gameClock_ = new GameClock(this, textGameClock_);

		init();

		setListeners();

		if (firstTime())
		{
			help();
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		load();
		updateDisplay();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		save();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		photoProvider_.clearCache();
		iconProvider_.clearCache();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.m9l_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean result = true;

		switch (item.getItemId())
		{
		case R.id.close:
			finish();
			break;

		case R.id.help:
			help();
			break;

		case R.id.new_game:
			newGame();
			break;

		case R.id.high_score:
			highScore();
			break;

		case R.id.pause:
			pause();
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
		case DIALOG_HELP_ID:
			result = createHelpDialog();
			break;

		case DIALOG_PAUSED_ID:
			result = createPausedDialog();
			break;

		case DIALOG_HIGH_SCORE_ID:
			result = createHighScoreDialog();
			break;

		default:
			result = super.onCreateDialog(id);
			break;
		}

		return result;
	}

	private AlertDialog createOKDialog(final int resMessage, final android.content.DialogInterface.OnClickListener dialogClickListener)
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.m9l);
		builder.setMessage(resMessage);
		android.content.DialogInterface.OnClickListener okListener = dialogClickListener;
		builder.setPositiveButton(R.string.ok, okListener);
		final AlertDialog result = builder.create();
		return result;
	}

	private AlertDialog createOKDialog(final int resMessage)
	{
		final AlertDialog result = createOKDialog(resMessage, null);
		return result;
	}

	private AlertDialog createHelpDialog()
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.help);
		builder.setMessage(R.string.m9l_help_message);
		android.content.DialogInterface.OnClickListener okListener = null;
		builder.setPositiveButton(R.string.ok, okListener);
		builder.setNeutralButton(R.string.more_help, helpDialogMoreClickListener);
		final AlertDialog result = builder.create();
		return result;
	}

	private Dialog createPausedDialog()
	{
		final AlertDialog result = createOKDialog(R.string.game_paused, pauseDialogClickListener);
		return result;
	}

	private Dialog createHighScoreDialog()
	{
		final AlertDialog result = createOKDialog(R.string.high_score);
		return result;
	}

	private void help()
	{
		showDialog(DIALOG_HELP_ID);
	}

	private void pause()
	{
		gameClock_.pause();
		showDialog(DIALOG_PAUSED_ID);
	}

	private void highScore()
	{
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.m9l_high_score_dlg);
		dialog.setTitle(R.string.high_score);
		dialog.setCancelable(true);

		final TextView textBestTime = (TextView) dialog.findViewById(R.id.best_time);
		final TextView textLeastGuesses = (TextView) dialog.findViewById(R.id.least_guesses);
		final Button buttonReset = (Button) dialog.findViewById(R.id.buttonReset);
		final Button buttonOK = (Button) dialog.findViewById(R.id.buttonOK);

		final String none = getString(R.string.none);

		String bestTime = none;
		String leastGuesses = none;

		if (highScoreSeconds_>=GameClock.MIN_SECONDS && highScoreSeconds_<GameClock.MAX_SECONDS)
		{
			bestTime = GameClock.timeToString(highScoreSeconds_);
		}

		if (highScoreGuess_>=MIN_GUESSES && highScoreGuess_<MAX_GUESSES)
		{
			leastGuesses = String.valueOf(highScoreGuess_);
		}

		textBestTime.setText(bestTime);
		textLeastGuesses.setText(leastGuesses);

		buttonReset.setOnClickListener
		(
			new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					saveHighScore(GameClock.MAX_SECONDS, MAX_GUESSES);
					dialog.cancel();
				}
			}
		);

		buttonOK.setOnClickListener
		(
			new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					dialog.cancel();
				}
			}
		);

		dialog.show();
	}

	private void newGame()
	{
		init();
		save();
	}

	private void initPhotoSequence()
	{
		int[][] tmpSequence = new int[ROWS][COLS];
		int[] rowSeq = new int[ROWS];

		int row;

		int col = COLS;
		while (col != 0)
		{
			--col;

			row = ROWS;
			while (row != 0)
			{
				--row;

				rowSeq[row] = row*COLS + col;
			}

			Util.random_shuffle(rowSeq, random_);

			row = ROWS;
			while (row != 0)
			{
				--row;

				tmpSequence[row][col] = rowSeq[row];
			}
		}

		row = ROWS;
		while (row != 0)
		{
			--row;

			Util.random_shuffle(tmpSequence[row], random_);
		}

		int i = photoSequence_.length;
		while (i != 0)
		{
			--i;
			row = i/COLS;
			col = i%COLS;
			photoSequence_[i] = tmpSequence[row][col];
		}
	}

	private void notifyAdapterDataChanged()
	{
		topPhotoAdapter_.notifyDataSetChanged();
		iconAdapter_.notifyDataSetChanged();
		bottomPhotoAdapter_.notifyDataSetChanged();
	}

	private int getIconSelection()
	{
		final int result = galleryIcon_.getSelectedItemPosition();
		return result;
	}

	private void updateMatchline()
	{
		final int iconSelection = getIconSelection();
		final int colorRes = MYMSurvey.getFelinealityColorId(iconSelection);
		final int color = getResources().getColor(colorRes);
		matchline_.setBackgroundColor(color);
	}

	private void updateGameClock(final int matches)
	{
		if (matches == 9)
		{
			gameClock_.stop();
		}

		gameClock_.update();
	}

	private void updateButton(final int matches)
	{
		int buttonTryMatchVisibility = View.VISIBLE;
		int buttonNewGameVisibility = View.VISIBLE;

		if ((matches>=0 && matches<9) && gameClock_.getSeconds()<GameClock.MAX_SECONDS)
		{
			buttonNewGameVisibility = View.GONE;
		}
		else
		{
			buttonTryMatchVisibility = View.GONE;
		}

		buttonMatch_.setVisibility(buttonTryMatchVisibility);
		buttonNewGame_.setVisibility(buttonNewGameVisibility);
	}

	private void updateMatchGuess(final int matches)
	{
		final String matchString = String.valueOf(matches);
		final String guessString = String.valueOf(guesses_);

		final String matchGuessString = matchString + "/" + guessString;
		textMatchGuess_.setText(matchGuessString);
	}

	private void updateDisplay()
	{
		final int matches = getMatchCount();

		updateMatchline();
		updateGameClock(matches);
		updateButton(matches);
		updateMatchGuess(matches);
	}

	private void initMatches()
	{
		int i=matches_.length;
		while (i != 0)
		{
			--i;
			matches_[i] = false;
		}
	}

	private void initUserFelinealityDetailViewed()
	{
		int i=userFelinealityDetailViewed_.length;
		while (i != 0)
		{
			--i;
			userFelinealityDetailViewed_[i] = false;
		}
	}

	private void init()
	{
		initPhotoSequence();

		final int startPosition = MYMSurvey.FELINEALITY_CAPACITY/2;

		galleryTopPhoto_.setSelection(startPosition);
		galleryIcon_.setSelection(startPosition);
		galleryBottomPhoto_.setSelection(startPosition);

		notifyAdapterDataChanged();

		gameClock_.reset();

		guesses_ = 0;

		initMatches();

		initUserFelinealityDetailViewed();

		updateDisplay();
	}

	private void setListeners()
	{
		buttonMatch_.setOnClickListener(matchClickListener);
		buttonNewGame_.setOnClickListener(newGameClickListener);

		galleryIcon_.setOnItemClickListener(iconItemClickListener);
		galleryIcon_.setOnItemLongClickListener(iconLongClickListener);
		galleryIcon_.setOnItemSelectedListener(iconItemSelectListener);

		galleryTopPhoto_.setOnItemLongClickListener(photoTopLongClickListener);
		galleryTopPhoto_.setOnItemClickListener(photoTopItemClickListener);

		galleryBottomPhoto_.setOnItemLongClickListener(photoBottomLongClickListener);
		galleryBottomPhoto_.setOnItemClickListener(photoBottomItemClickListener);
	}

	private int getPhotoFelineality(final int side, final int position)
	{
		final int result = photoProvider_.getFelineality(COLS*side + position);
		return result;
	}

	private int imagesMatched(final int topPhotoSelection, final int iconSelection, final int bottomPhotoSelection)
	{
		final int topPhotoFelineality = getPhotoFelineality(TOP, topPhotoSelection);
		final int bottomPhotoFelineality = getPhotoFelineality(BOTTOM, bottomPhotoSelection);

		int result = 0;

		if (topPhotoFelineality == iconSelection)
		{
			++result;
		}
		if (bottomPhotoFelineality == iconSelection)
		{
			++result;
		}

		return result;
	}

	private void galleryHint(final int topPhotoSelection, final int iconSelection, final int bottomPhotoSelection)
	{
		final int topPhotoFelineality = getPhotoFelineality(TOP, topPhotoSelection);
		final int bottomPhotoFelineality = getPhotoFelineality(BOTTOM, bottomPhotoSelection);

		final boolean topIncorrect = (topPhotoFelineality != iconSelection);
		final boolean bottomIncorrect = (bottomPhotoFelineality != iconSelection);
		final boolean photoMatch = (bottomPhotoFelineality == topPhotoFelineality);

		final boolean iconIncorrect = topIncorrect && bottomIncorrect && photoMatch;

		if (iconIncorrect)
		{
			galleryIcon_.startAnimation(shake_);
		}
		else if (!photoMatch)
		{
			if (topIncorrect)
			{
				galleryTopPhoto_.startAnimation(shake_);
			}
			if (bottomIncorrect)
			{
				galleryBottomPhoto_.startAnimation(shake_);
			}
		}
	}

	private int getMatchCount()
	{
		int result = 0;

		for (final boolean match: matches_)
		{
			if (match)
			{
				++result;
			}
		}

		return result;
	}

	private void incrementGuesses()
	{
		if (guesses_<MAX_GUESSES) // else, user should give up.  
		{
			++guesses_;
		}
	}

	private void displayPhotoNameToast(final int side, final String name) // TODO: The toast code needs to be factored and better organized.  
	{
		int gravity = Gravity.CENTER;
		switch (side)
		{
		case TOP:
			gravity = Gravity.TOP;
			break;

		case BOTTOM:
			gravity = Gravity.BOTTOM;
			break;
		}

		final Toast toast = Toast.makeText(this, name, Toast.LENGTH_SHORT);
		toast.setGravity(gravity, 0, 0);
		toast.show();
	}

	private void displayToast(final int stringResId, final int duration)
	{
		Util.displayToast(this, stringResId, duration);
	}

	private void displayToast(final int stringResId)
	{
		displayToast(stringResId, Toast.LENGTH_SHORT);
	}

	private void match()
	{
		if (!gameClock_.isRunning())
		{
			gameClock_.start();
		}

		incrementGuesses();

		final int topPhotoSelection = galleryTopPhoto_.getSelectedItemPosition();
		final int iconSelection = galleryIcon_.getSelectedItemPosition();
		final int bottomPhotoSelection = galleryBottomPhoto_.getSelectedItemPosition();

		final int match = imagesMatched(topPhotoSelection, iconSelection, bottomPhotoSelection);

		final boolean gameHint = Util.isGameHintEnabled(this);
		if (gameHint)
		{
			galleryHint(topPhotoSelection, iconSelection, bottomPhotoSelection);
		}

		final int matchMessage;

		int duration = Toast.LENGTH_SHORT;

		switch (match)
		{
		case 0:
			matchMessage = R.string.no_match;
			break;

		case 1:
			matchMessage = R.string.one_match;
			break;

		case 2:
			{
				duration = Toast.LENGTH_LONG;

				matches_[iconSelection] = true;

				notifyAdapterDataChanged();

				final int matchCount = getMatchCount();
				if (matchCount == 9)
				{
					final int seconds = gameClock_.getSeconds();
					if (seconds < highScoreSeconds_)
					{
						highScoreSeconds_ = seconds;
						displayToast(R.string.best_time, duration);
					}

					if (guesses_ < highScoreGuess_)
					{
						highScoreGuess_ = guesses_;
						displayToast(R.string.least_guesses, duration);
					}

					matchMessage = R.string.game_over;
				}
				else
				{
					matchMessage = R.string.all_match;
				}
			}
			break;

		default:
			matchMessage = R.string.no_match;
			break;
		}

		if (gameHint)
		{
			displayToast(matchMessage, duration);
		}

		updateDisplay();
	}

	private void iconHint(final int position)
	{
		final int iconSelection = galleryIcon_.getSelectedItemPosition();
		if (position == iconSelection)
		{
			final int felinealityStringResID = MYMSurvey.getFelinealityTitleId(position);
			displayToast(felinealityStringResID);
		}
	}

	// TODO: This is almost identical to Bitmap PhotoProvider.createRawPhoto(final int row, final int col).  Should it be factored?
	private final String getName(final int row, final int aspcaAlity)
	{
		final String[] resultColumns = 
			new String[]
			{
				AppDBAdapter.NAME
			};

		final String where = 
			AppDBAdapter.CATEGORY + "=" + AppDBAdapter.CATEGORY_M9L + " and " + 
			AppDBAdapter.ASPCA_ALITY + "=" + aspcaAlity;

		final String order = 
			AppDBAdapter.KEY_ID;

		String result = getString(R.string.unknown);

		AppDBAdapter db = null;

		Cursor cursor = null;

		try
		{
			db = new AppDBAdapter(this);

			cursor = db.query(this, resultColumns, where, order);

			boolean success;
			if (row == 0)
			{
				success = cursor.moveToFirst();
			}
			else
			{
				success = cursor.moveToLast();
			}

			if (success)
			{
				result = cursor.getString(0);
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

	private int sequenceSide(final int index)
	{
		final int result = 2*photoSequence_[index]/photoSequence_.length;
		return result;
	}

	private void photoHint(final int selectedSide, final int position)
	{
		Gallery gallery;
		if (selectedSide == TOP)
		{
			gallery = galleryTopPhoto_;
		}
		else
		{
			gallery = galleryBottomPhoto_;
		}
		final int photoSelection = gallery.getSelectedItemPosition();

		if (position == photoSelection)
		{
			final int photoFelineality = getPhotoFelineality(selectedSide, position);
			final int index = COLS*selectedSide + position;
			final int side = sequenceSide(index);
			final String name = getName(side, photoFelineality);
			displayPhotoNameToast(selectedSide, name);
		}
	}

	private void iconDetails(final int position)
	{
		final int iconSelection = galleryIcon_.getSelectedItemPosition();
		if (position == iconSelection)
		{
			final Intent mymCategoryIntent = new Intent(this, MYMCategoryActivity.class);
			mymCategoryIntent.putExtra(MYMSurvey.FELINEALITY, position);
			Util.startActivity(this, mymCategoryIntent);
		}
	}

	// TODO: This is almost identical to Bitmap PhotoProvider.createRawPhoto(final int row, final int col).  Should it be factored?
	private final int getKeyID(final int row, final int ASPCAality)
	{
		int result = AppDBAdapter.INVALID_KEY_ID;
		
		final String[] resultColumns = 
			new String[]
			{
				AppDBAdapter.KEY_ID
			};

		final String where = 
			AppDBAdapter.CATEGORY + "=" + AppDBAdapter.CATEGORY_M9L + " and " + 
			AppDBAdapter.ASPCA_ALITY + "=" + ASPCAality;

		final String order = 
			AppDBAdapter.KEY_ID;

		AppDBAdapter db = null;

		Cursor cursor = null;

		try
		{
			db = new AppDBAdapter(this);

			cursor = db.query(this, resultColumns, where, order);

			if (cursor != null)
			{
				boolean success;
				if (row == 0)
				{
					success = cursor.moveToFirst();
				}
				else
				{
					success = cursor.moveToLast();
				}

				if (success)
				{
					result = cursor.getInt(0);
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

	private void photoDetails(final int selectedSide, final int position)
	{
		Gallery gallery;
		if (selectedSide == TOP)
		{
			gallery = galleryTopPhoto_;
		}
		else
		{
			gallery = galleryBottomPhoto_;
		}
		final int photoSelection = gallery.getSelectedItemPosition();
		if (position == photoSelection)
		{
			final int photoFelineality = getPhotoFelineality(selectedSide, position);
			if 
			(
				MYMSurvey.isFelineality(userFelineality_) && 
				matches_[userFelineality_] == true && 
				photoFelineality == userFelineality_
			)
			{
				userFelinealityDetailViewed_[selectedSide] = true;
			}

			final int index = COLS*selectedSide + position;
			final int side = sequenceSide(index);
			final int keyID = getKeyID(side, photoFelineality);
			if (keyID != AppDBAdapter.INVALID_KEY_ID)
			{
				final Intent profileIntent = new Intent(this, BiographyActivity.class);
				profileIntent.putExtra(AppDBAdapter.KEY_ID, keyID);
				Util.startActivity(this, profileIntent);
			}
			else
			{
				final int felinealityStringResID = MYMSurvey.getFelinealityTitleId(photoFelineality);
				displayToast(felinealityStringResID);
			}
		}
	}

	private void saveSequence()
	{
		Util.save(preferences_, KEY_PHOTO_SEQUENCE, photoSequence_);
	}

	private void loadSequence()
	{
		try
		{
			Util.load(preferences_, KEY_PHOTO_SEQUENCE, photoSequence_);
		}
		catch (JSONException e)
		{
			init();
		}
	}

	public void loadMatchGuess()
	{
		try
		{
			Util.load(preferences_, KEY_MATCH, matches_);
		}
		catch (JSONException e)
		{
		}

		guesses_ = preferences_.getInt(KEY_GUESS, 0);
	}

	public void saveMatchGuess()
	{
		Util.save(preferences_, KEY_MATCH, matches_);

		final SharedPreferences.Editor editor = preferences_.edit();

		editor.putInt(KEY_GUESS, guesses_);

		editor.commit(); // TODO: Use apply when API is 9 or greater
	}

	private int getHighScoreSeconds()
	{
		final int result = preferences_.getInt(KEY_HIGH_SCORE_SECONDS, GameClock.MAX_SECONDS);
		return result;
	}

	private int getHighScoreGuess()
	{
		final int result = preferences_.getInt(KEY_HIGH_SCORE_GUESS, MAX_GUESSES);
		return result;
	}

	private boolean firstTime()
	{
		final int highScoreSeconds = getHighScoreSeconds();
		final int highScoreGuess = getHighScoreGuess();
		final boolean result = highScoreSeconds == GameClock.MAX_SECONDS && highScoreGuess == MAX_GUESSES;
		return result;
	}

	private void loadHighScore()
	{
		highScoreSeconds_ = getHighScoreSeconds();
		highScoreGuess_ = getHighScoreGuess();
	}

	private void saveHighScore(final int seconds, final int guesses)
	{
		highScoreSeconds_ = seconds;
		highScoreGuess_ = guesses;

		final SharedPreferences.Editor editor = preferences_.edit();

		editor.putInt(KEY_HIGH_SCORE_SECONDS, seconds);
		editor.putInt(KEY_HIGH_SCORE_GUESS, guesses);

		editor.commit();
	}

	private void load()
	{
		loadSequence();
		gameClock_.load();
		loadMatchGuess();
		loadHighScore();
	}

	private void save()
	{
		saveSequence();
		gameClock_.save();
		saveMatchGuess();
		saveHighScore(highScoreSeconds_, highScoreGuess_);
	}
}
