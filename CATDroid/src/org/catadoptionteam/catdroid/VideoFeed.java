package org.catadoptionteam.catdroid;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.catadoptionteam.catdroid.VideoClient.YouTubeUrl;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.TimeFormatException;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.json.JsonCParser;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

public class VideoFeed extends Feed<Video>
{
	// TODO: delete this: private static final String GET = "http://gdata.youtube.com/feeds/mobile/videos?author=catadoptionteam&orderby=published&start-index=1&max-results=10&v=2&alt=jsonc";
	private static final int MAX_START_INDEX = 1000;
	private static final int MAX_ROWS = VideoProvider.MAX_ROWS;

	private final HttpTransport transport = AndroidHttp.newCompatibleTransport();
	private final JsonFactory jsonFactory = CATApp.JSONFactory.getJsonFactory();
	private final JsonCParser parser = new JsonCParser(jsonFactory);

	private final HttpRequestFactory requestFactory = 
		transport.createRequestFactory
		(
			new HttpRequestInitializer()
			{
				@Override
				public void initialize(final HttpRequest request)
				{
					final GoogleHeaders headers = new GoogleHeaders();
					headers.setApplicationName("CatAdoptionTeam-CATDroid/1.1");
					headers.gdataVersion = "2";
					request.setHeaders(headers);
					request.addParser(parser);
					request.setEnableGZipContent(true);
					// TODO:??? request.setConnectTimeout(60000);
					// TODO:??? request.setReadTimeout(30000);
				}
			}
		);

//	public VideoFeed()
//	{
//	}

	private <F extends Feed<? extends Item> > F executeGetFeed(YouTubeUrl youTubeUrl, Class<F> feedClass) throws IOException
	{
		final HttpRequest request = requestFactory.buildGetRequest(youTubeUrl);
		// TODO:??? request.setConnectTimeout(60000);
		// TODO:??? request.setReadTimeout(30000);
		final HttpResponse response = request.execute();
		return response.parseAs(feedClass);
	}

	private VideoFeed executeGetVideoFeed(YouTubeUrl youTubeUrl) throws IOException
	{
		final VideoFeed videoFeed = executeGetFeed(youTubeUrl, VideoFeed.class);
		return videoFeed;
	}

	public void update(final Context context)
	{
		final Context ctx = Util.getSafeContext(context);

		final YouTubeUrl youTubeUrl = YouTubeUrl.forVideosFeed();
		youTubeUrl.startIndex = 1;

		for (;;)
		{
			// TODO: Example that may be helpful: http://www.javacodegeeks.com/2010/05/getting-started-with-youtube-java-api.html
			VideoFeed videoFeed = null;

			try
			{
				videoFeed = executeGetVideoFeed(youTubeUrl);
			}
			catch (IOException e)
			{
				// TODO: comment out
				// final String msg = e.getMessage();
				// Log.e("VideoFeed.update", msg);
			}

			if (videoFeed == null)
			{
				break;
			}

			final List<Video> videoList = videoFeed.items;
			if (videoList != null)
			{
				final ContentResolver contentResolver = ctx.getContentResolver();
				for (final Video video: videoList)
				{
					final Player player = video.player;
					if (player == null)
					{
						continue;
					}

					final Content content = video.content;
					if (content == null)
					{
						continue;
					}

					String url = content.mpeg4;
					if (TextUtils.isEmpty(url))
					{
						url = content.h263;
					}
//					String url = player.mobile;
//					if (url == null)
//					{
//						url = player.defaultUrl;
//					}
					if (url != null)
					{
						final Time uploaded = new Time();
						final String uploadedDateStr = video.uploaded;
						try
						{
							uploaded.parse3339(uploadedDateStr);
						}
						catch (TimeFormatException e)
						{
						}
						final Thumbnail thumbnail = video.thumbnail;
						final String thumbnailUrl = thumbnail.hqDefault;

						final Bitmap thumbnailBitmap = BitmapDownloaderTask.downloadBitmap(thumbnailUrl);
						if (thumbnailBitmap != null)
						{
							final ContentValues contentValues = new ContentValues();

							final String videoId = video.id;

							contentValues.put(VideoProvider.FIELD_VIDEO_ID, videoId);

							contentValues.put(VideoProvider.FIELD_VIDEO_URI, url);

							final String unknown = ctx.getString(R.string.unknown);

							String title = video.title;
							if (TextUtils.isEmpty(title))
							{
								title = unknown;
							}
							contentValues.put(VideoProvider.FIELD_VIDEO_TITLE, title);

							String description = video.description;
							if (TextUtils.isEmpty(description))
							{
								description = unknown;
							}
							contentValues.put(VideoProvider.FIELD_VIDEO_DESCRIPTION, description);

							long uploadedMillisec = uploaded.toMillis(false);
							if (uploadedMillisec < 0)
							{
								uploadedMillisec = 0;
							}
							contentValues.put(VideoProvider.FIELD_VIDEO_UPLOADED, uploadedMillisec);

							try
							{
								final Uri uri = contentResolver.insert(VideoProvider.CONTENT_URI, contentValues);
								if (uri != null)
								{
									try
									{
										final OutputStream outStream = contentResolver.openOutputStream(uri);
										thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 10, outStream);
										outStream.close();
									}
									catch (Exception e)
									{
//										Log.e("VideoFeed", "Exception writing thumbnail.", e);
									}
								}
							}
							catch (SQLException e)
							{
								videoFeed.startIndex = MAX_START_INDEX + 1;
								break;
							}
						}
					}
				}
			}

			final int totalItems = videoFeed.totalItems;
			final int startIndex = videoFeed.startIndex;
			final int itemsPerPage = videoFeed.itemsPerPage;
			final int nextStartIndex = startIndex + itemsPerPage;
			final int maxResults = totalItems+1 - nextStartIndex;

			if (maxResults <= 0 || startIndex >= MAX_ROWS || nextStartIndex > MAX_START_INDEX)
			{
				break;
			}

			youTubeUrl.startIndex = nextStartIndex;

			if (maxResults < youTubeUrl.maxResults)
			{
				youTubeUrl.maxResults = maxResults;
			}
		}
	}
}
