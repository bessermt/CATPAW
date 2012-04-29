/**
 * 
 */
package org.catadoptionteam.catdroid;


//import com.google.gdata.client.youtube.YouTubeQuery;
//import com.google.gdata.client.youtube.YouTubeService;

import java.util.List;

import android.content.Context;

import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.util.Key;

// See: http://code.google.com/p/gdata-java-client/source/detail?r=490

/**
 * @author bessermt
 *
 */
public class VideoClient
{
	static class YouTubeUrl extends GoogleUrl
	{
		/** Whether to pretty print HTTP requests and responses. */
		private static final boolean PRETTY_PRINT = false;

		private static final String ROOT_URL = "http://gdata.youtube.com/feeds/mobile";

		@Key
		String author;

		@Key("max-results")
		Integer maxResults;

		@Key
		String orderby;

		@Key
		String safeSearch;

		@Key("start-index")
		int startIndex;

		public YouTubeUrl(final String encodedUrl)
		{
			super(encodedUrl);
			maxResults = 10;
			final Context context = CATApp.getAppContext();
			author = context.getString(R.string.youtube_author);
			alt = "jsonc";
			prettyprint = PRETTY_PRINT;
			orderby="published";
			safeSearch="strict";
			// v=2;
			// fields = "entry[link/@rel='http://gdata.youtube.com/schemas/2007%23mobile']"; // TODO: Do something like this when JSON-C can handle it. 
		}

		private static YouTubeUrl root()
		{
			return new YouTubeUrl(ROOT_URL);
		}

		public static YouTubeUrl forVideosFeed()
		{
			YouTubeUrl result = root();

			final List<String> pathParts = result.getPathParts();
			pathParts.add("videos");

			return result;
		}
	}
}
