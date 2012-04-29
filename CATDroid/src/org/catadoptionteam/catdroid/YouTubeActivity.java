/**
 * 
 */
package org.catadoptionteam.catdroid;

import java.util.Date;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;

/**
 * @author bessermt
 * see: 
 * http://code.google.com/p/google-api-java-client/
 * http://code.google.com/p/google-api-java-client/downloads/list
 * http://code.google.com/p/google-api-java-client/source/browse/youtube-jsonc-sample/src/main/java/com/google/api/client/sample/youtube/?repo=samples
 */

// TODO: Create a SyncAdapter and have it update my VideoProvider

public class YouTubeActivity extends ListActivity implements OnClickListener
{
	private static class VideoListAdapter extends SimpleCursorAdapter
	{
		private Context context_;
//		private int layout_;
//		private LayoutInflater inflater_;

//		private int colId_;
//		private int colData_;
		private int colVideoId_;
		private int colUri_;
//		private int colTitle_;
		private int colDescription_;
		private int colUploaded_;

		private java.text.DateFormat dateFormat_;

		private final OnClickListener showVideoListener_ = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final String videoUri = (String) v.getTag(R.id.youtube_uri);
				showVideo(videoUri);
			}

			private void showVideo(final String videoUri)
			{
//				final String tmp = "rtsp://v8.cache7.c.youtube.com/CiILENy73wIaGQmd_LgV_yw6ABMYDSANFEgGUgZ2aWRlb3MM/0/0/0/video.3gp"; // TODO: Delete
//				final Uri videoUri = Uri.parse(tmp);
//				final Intent intent = new Intent(Intent.ACTION_VIEW);
//				intent.setDataAndType(videoUri, "video/*");
//
//				Util.startActivity(context_, intent);

				final Intent intent = new Intent(context_, YouTubePlayerActivity.class);
				intent.putExtra(YouTubePlayerActivity.YOUTUBE_URI, videoUri);
				Util.startActivity(context_, intent);
			}
		};

		public VideoListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags)
		{
			super(context, layout, c, from, to); // TODO: Use flag if API Level 11 or greater

			context_ = context;
//			layout_ = layout;
			dateFormat_ = DateFormat.getDateFormat(context);
//			inflater_ = LayoutInflater.from(context);

//			colId_ = c.getColumnIndexOrThrow(VideoProvider._ID);
//			colData_ = c.getColumnIndexOrThrow(VideoProvider._DATA);
			colVideoId_ = c.getColumnIndexOrThrow(VideoProvider.FIELD_VIDEO_ID);
			colUri_ = c.getColumnIndexOrThrow(VideoProvider.FIELD_VIDEO_URI);
//			colTitle_ = c.getColumnIndexOrThrow(VideoProvider.FIELD_VIDEO_TITLE);
			colDescription_ = c.getColumnIndexOrThrow(VideoProvider.FIELD_VIDEO_DESCRIPTION);
			colUploaded_ = c.getColumnIndexOrThrow(VideoProvider.FIELD_VIDEO_UPLOADED);
		}

		/* (non-Javadoc)
		 * @see android.widget.SimpleCursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
		 */
		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			super.bindView(view, context, cursor);

//			final long id = cursor.getLong(colId_);
//			final String thumbnailFilename = cursor.getString(colData_);
			final String videoId = cursor.getString(colVideoId_);
			final String uri = cursor.getString(colUri_);
//			final String title = cursor.getString(colTitle_);
			final String description = cursor.getString(colDescription_); // TODO: Show in dialog when title is clicked?  Dialog will need a Play button.
			final long uploaded = cursor.getLong(colUploaded_);

			view.setTag(R.id.youtube_video_id, videoId);
			view.setTag(R.id.youtube_uri, uri);
			view.setTag(R.id.youtube_description, description);
			view.setTag(R.id.youtube_uploaded, Long.valueOf(uploaded));

//			view.setFocusable(false);
			view.setOnClickListener(showVideoListener_);
			view.setOnLongClickListener
			(
				new OnLongClickListener()
				{
					@Override
					public boolean onLongClick(View v)
					{
						final boolean result = true;

						final String description = (String) v.getTag(R.id.youtube_description);

						final Long uploaded = (Long) v.getTag(R.id.youtube_uploaded);
						final Time uploadedTime = new Time();
						uploadedTime.set(uploaded.longValue());

						display(uploadedTime, description);

						return result;
					}

					private AlertDialog createOKDialog(final String uploaded, final String text)
					{
						final AlertDialog.Builder builder = new AlertDialog.Builder(context_);
						builder.setTitle(R.string.description);
						builder.setMessage("Uploaded: " + uploaded + "\n\n" + text);
						android.content.DialogInterface.OnClickListener okListener = null;
						builder.setPositiveButton(R.string.ok, okListener);
						final AlertDialog result = builder.create();
						return result;
					}

					private String format(final Time time)
					{
						final long millisec = time.toMillis(false);
						final Date date = new Date(millisec);
						final String result = dateFormat_.format(date);
						return result;
					}

					private void display(final Time uploadedTime, final String description)
					{
						final String uploadedDateText = format(uploadedTime);
						final AlertDialog dialog = createOKDialog(uploadedDateText, description);
						dialog.show();
					}
				}
			);

			final ImageButton imageThumbnail = (ImageButton) view.findViewById(R.id.imageThumbnail);
			imageThumbnail.setTag(R.id.youtube_uri, uri);
			imageThumbnail.setOnClickListener(showVideoListener_);
		}
	}

	private Cursor cursor_;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.youtube);

		final Button buttonClose = (Button) findViewById(R.id.buttonClose);
		buttonClose.setOnClickListener(this);

		final String[] projection = new String[]
			{
				VideoProvider._ID, 
				VideoProvider._DATA, 
				VideoProvider.FIELD_VIDEO_ID, 
				VideoProvider.FIELD_VIDEO_URI, 
				VideoProvider.FIELD_VIDEO_TITLE, 
				VideoProvider.FIELD_VIDEO_DESCRIPTION, 
				VideoProvider.FIELD_VIDEO_UPLOADED
			};

		final ContentResolver contentResolver = getContentResolver();
		cursor_ = contentResolver.query(VideoProvider.CONTENT_URI, projection, null, null, null);

		if (cursor_ != null)
		{
			startManagingCursor(cursor_);

			final String[] cols = new String[]{VideoProvider._DATA, VideoProvider.FIELD_VIDEO_TITLE};
			final int[] names = new int[]{R.id.imageThumbnail, R.id.textTitle};

			final VideoListAdapter adapter = new VideoListAdapter(this, R.layout.youtube_list_item, cursor_, cols, names, 0);
			setListAdapter(adapter); // listVideo_.setAdapter(adapter); // TODO: Find out if these are equivalent. Should this variable be global to avoid garbage collection? 
		}
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.buttonClose:
			{
				close();
			}
			break;

			default:
			{
				// TODO: Deal with diagnostics...
			}
			break;
		}
	}

	private void close()
	{
		finish();
	}
}
