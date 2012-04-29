/**
 * 
 */
package org.catadoptionteam.catdroid;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

/**
 * @author bessermt
 *
 * Some code copied from:
 * Multithreading For Performance
 * by Tim Bray and Gilles Debunne
 * http://android-developers.blogspot.com/2010/07/multithreading-for-performance.html
 */

public class BitmapDownloaderTask extends AsyncTask<String, Void, String>
{
	/**
	 * @author bessermt
	 *
	 */

	static class FlushedInputStream extends FilterInputStream
	{
		public FlushedInputStream(InputStream inputStream)
		{
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException
		{
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n)
			{
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L)
				{
					final int bytes = read();
					if (bytes < 0)
					{
						break; // we reached EOF
					}
					else
					{
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}

	private static final int DEFAULT_QUALITY_PERCENT = 30;

	protected String pathname_;
	private String baseFilename_;
	private int qualityPercent_;

	public BitmapDownloaderTask(final String pathname, final String baseFilename, final int qualityPercent)
	{
		pathname_ = pathname;
		baseFilename_ = baseFilename;
		qualityPercent_ = qualityPercent;

		final File dir = new File(pathname_);
		dir.mkdirs();
	}

	public BitmapDownloaderTask(final String pathname, final String baseFilename)
	{
		this(pathname, baseFilename, DEFAULT_QUALITY_PERCENT);
	}

	public BitmapDownloaderTask(final String pathname)
	{
		this(pathname, "photo");
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(String result) // Seem to need this or the derived version isn't called.
	{
		super.onPostExecute(result);
	}

	public static Bitmap downloadBitmap(final String uri)
	{
		Bitmap result = null;

		final DefaultHttpClient client = new DefaultHttpClient();
		final HttpGet getRequest = new HttpGet(uri);

		try
		{
			final HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK)
			{
				final HttpEntity entity = response.getEntity();
				if (entity != null)
				{
					InputStream inputStream = null;
					try
					{
						inputStream = entity.getContent();
						final FlushedInputStream flushedInputStream = new FlushedInputStream(inputStream);
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inPurgeable = true;
						options.inInputShareable = true;
						result = BitmapFactory.decodeStream(flushedInputStream, null, options);
					}
					finally
					{
						if (inputStream != null)
						{
							inputStream.close();
						}
						entity.consumeContent();
					}
				}
			}
		}
		catch (Exception e)
		{
			// Could provide a more explicit error message for IOException or IllegalStateException
			getRequest.abort();
			// Log.w("downloadBitmap", "Error while retrieving bitmap from " + url, e.toString());
		}

		return result;
	}

	public static String getPathFilename(final String pathname, final String baseFilename)
	{
		final String result = pathname + baseFilename + ".jpg";
		return result;
	}

	public String getPathFilename(final int i, final int length)
	{
		String result = null;

		if (length > 0)
		{
			String postfix = Util.EMPTY_STRING;

			if (i!=0 || length>1)
			{
				postfix = String.valueOf(i);
			}
	
			result = getPathFilename(pathname_, baseFilename_ + postfix);
		}

		return result;
	}

	private boolean save(final String pathFilename, final Bitmap bitmap)
	{
		boolean success = false;

		if (bitmap != null)
		{
			try
			{
				final FileOutputStream fileOutputStream = new FileOutputStream(pathFilename);
				final BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

				bitmap.compress(CompressFormat.JPEG, qualityPercent_, bos);

				bos.close();

				success = true;
			}
			catch (FileNotFoundException e) // TODO: ???
			{
	//			final String tag = e.toString();
	//			final String msg = e.getMessage();
	//			Log.w(tag, msg);
			}
			catch (IOException e) // TODO: ???
			{
	//			final String tag = e.toString();
	//			final String msg = e.getMessage();
	//			Log.w(tag, msg);
			}
		}
		return success;
	}

	public boolean executeSync(final String pathFilename, final String uri)
	{
		boolean result = false;

		final File file = new File(pathFilename);
		if (file.exists())
		{
			result = true;
		}
		else
		{
			final Bitmap bitmap = downloadBitmap(uri);
			if (bitmap != null)
			{
				result = save(pathFilename, bitmap);
			}
		}

		return result;
	}

//	/* (non-Javadoc)
//	 * @see android.os.AsyncTask#onPreExecute()
//	 */
//	@Override
//	protected void onPreExecute()
//	{
//		super.onPreExecute();
//	}

	@Override
	protected String doInBackground(String... photoUris)
	{
		String result = null;

		if (photoUris != null)
		{
			final int length = photoUris.length;
			if (length > 0)
			{
				int i = 0;
				for (String photoUri: photoUris)
				{
					final String pathFilename = getPathFilename(i, length);
					executeSync(pathFilename, photoUri);
					++i;
				}

				result = getPathFilename(0, length);
			}
		}

		return result;
	}
}
