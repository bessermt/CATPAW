/**
 * 
 */
package org.catadoptionteam.catdroid;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

/**
 * @author bessermt
 *
 */
public class VideoProvider extends SuperContentProvider
{
	public class DBOpenHelper extends SQLiteOpenHelper
	{
		private static final String SQL_CREATE_TABLE_VIDEO = 
			"CREATE TABLE " + TABLE_NAME_VIDEO + 
			" (" + 
				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				_DATA + " TEXT, " + // The fully qualified path and file on the device for the thumbnail file.
				FIELD_VIDEO_ID + " TEXT NOT NULL UNIQUE ON CONFLICT ABORT, " + // TODO: UNIQUE must be doing a look up.  So,... should it have a index?
				FIELD_VIDEO_URI + " TEXT NOT NULL UNIQUE ON CONFLICT ABORT, " + // TODO: UNIQUE must be doing a look up.  So,... should it have a index?
				FIELD_VIDEO_TITLE + " TEXT NOT NULL, " + 
				FIELD_VIDEO_DESCRIPTION + " TEXT NOT NULL, " + 
				FIELD_VIDEO_UPLOADED + " INTEGER NOT NULL" + 
			");";

		private static final String SQL_CREATE_INDEX_FIELD_VIDEO_UPLOADED = 
			"CREATE INDEX " + INDEX_NAME_UPLOADED + " ON " + TABLE_NAME_VIDEO + 
			"(" + 
				FIELD_VIDEO_UPLOADED + " DESC" + 
			");";

		private static final String SQL_DROP_INDEX_FIELD_VIDEO_UPLOADED = 
			"DROP INDEX IF EXISTS " + INDEX_NAME_UPLOADED + ";";

		private static final String SQL_DROP_TABLE_VIDEO = 
			"DROP TABLE IF EXISTS " + TABLE_NAME_VIDEO + ";";

		private SQLiteDatabase db_;

		public DBOpenHelper(final Context context, final String name, final CursorFactory factory, final int version)
		{
			super(context, name, factory, version);
		}

		private void create(final SQLiteDatabase db)
		{
			db.execSQL(SQL_CREATE_TABLE_VIDEO);
			db.execSQL(SQL_CREATE_INDEX_FIELD_VIDEO_UPLOADED);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			create(db);
		}

		@Override
		public void onOpen(SQLiteDatabase db)
		{
			super.onOpen(db);
			db_ = db;
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			truncate(db);
			create(db);
		}

		private void truncate(SQLiteDatabase db)
		{
			db.execSQL(SQL_DROP_INDEX_FIELD_VIDEO_UPLOADED);
			db.execSQL(SQL_DROP_TABLE_VIDEO);
			create(db);
		}

		@Override
		public synchronized void close()
		{
			super.close();
			if (db_ != null)
			{
				db_.close();
				db_ = null;
			}
		}
	}

	// TODO: Move these to different location that can be easily modified

	private static final String THUMBNAIL_DIR = "/Pictures/thumbnail/";

	public static final int MAX_ROWS = 50;

	private static final String ORG_NAME = "org.catadoptionteam";
	private static final String APP_NAME= "catdroid";
	// private static final String CLASS_NAME = "VideoProvider";

	private static final String DATABASE_NAME = "video";
	private static final int DATABASE_VERSION = 1;

	private static final String TABLE_NAME_VIDEO = "video";
	private static final String INDEX_NAME_UPLOADED = "uploadedIndex";

	public static final String FIELD_VIDEO_ID = "videoId";
	public static final String FIELD_VIDEO_URI = "uri";
	public static final String FIELD_VIDEO_TITLE = "title";
	public static final String FIELD_VIDEO_DESCRIPTION = "description";
	public static final String FIELD_VIDEO_UPLOADED = "uploaded";

//		Instead of storing the thumbnail URI as follows:
//		public static final String FIELD_VIDEO_THUMBNAIL_URI = "thumbnail";
//
//		TODO: Use this code when storing the record:
//		final ContentResolver contentResolver = activity.getContentResolver();
//
//		final ContentValues values = new ContentValues();
//		values.put(VideoProvider.FIELD_VIDEO_ID, YouTubeVideoID);
//		values.put(VideoProvider.FIELD_VIDEO_URI, YouTubeVideoURL);
//		values.put(VideoProvider.FIELD_VIDEO_CREATED, YouTubeVideoCreatedDate);
//		values.put(VideoProvider.FIELD_VIDEO_TITLE, YouTubeVideoTitle);
//
//		final Uri newRecord = contentResolver.insert(VideoProvider.CONTENT_URI, values);
//
//		final OutputStream outStream = contentResolver.openOutputStream(newRecord);
//		myVideoThumbnail.compress(Bitmap.CompressFormat.JPEG, 50, outStream);
//		outStream.close();

	private static final String ITEM_MIME_TYPE = "vnd.android.cursor.item/vnd." + ORG_NAME + "." + APP_NAME + "." + TABLE_NAME_VIDEO;
	private static final String DIR_MIME_TYPE = "vnd.android.cursor.dir/vnd." + ORG_NAME + "." + APP_NAME + "." + TABLE_NAME_VIDEO;

	private static final int MATCH_ONE_ROW = 1;
	private static final int MATCH_ALL_ROWS = 2;

	private static final String DEFAULT_ORDER_BY = FIELD_VIDEO_UPLOADED + " DESC";

	private static final String CONTENT_PREFIX = "content://";

	public static final String PROVIDER_AUTHORITY = "org.catadoptionteam.provider.catdroid.video";

	public static final Uri CONTENT_URI = Uri.parse(CONTENT_PREFIX + PROVIDER_AUTHORITY + "/" + TABLE_NAME_VIDEO);

	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	static
	{
		sUriMatcher.addURI(PROVIDER_AUTHORITY, TABLE_NAME_VIDEO + "/#", MATCH_ONE_ROW);
		sUriMatcher.addURI(PROVIDER_AUTHORITY, TABLE_NAME_VIDEO, MATCH_ALL_ROWS);
	}

	private SQLiteDatabase db_;

	private DBOpenHelper dbOpenHelper_;

	private Context context_;

	private String path_;

	public VideoProvider()
	{
		super();
		// TODO: consider saving video content to MediaStore.Video.Media.EXTERNAL_CONTENT_URI
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#openFile(android.net.Uri, java.lang.String)
	 */
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException
	{
		// final ParcelFileDescriptor result = super.openFile(uri, mode); // super throws FileNotFoundException, so don't call it.
		ParcelFileDescriptor result = null;

		if (sUriMatcher.match(uri) != MATCH_ONE_ROW)
		{
			throw new IllegalArgumentException("URI must refer to a specific record.");
		}
		else
		{
			result = openFileHelper(uri, mode);
		}
		return result;
	}

	@Override
	public boolean onCreate()
	{
		context_ = getContext();

		dbOpenHelper_ = new DBOpenHelper(context_, DATABASE_NAME + ".db", null, DATABASE_VERSION);
		db_ = openRW(dbOpenHelper_);
		path_ = getThumbnailPathname(context_);

		final File dir = new File(path_);
		dir.mkdirs();

		final boolean result = (db_!=null);

		return result;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri)
	{
		String result = null;
		final int match = sUriMatcher.match(uri);

		switch (match)
		{
		case MATCH_ONE_ROW:
			result = ITEM_MIME_TYPE;
			break;

		case MATCH_ALL_ROWS:
			result = DIR_MIME_TYPE;
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
			// break;
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		Uri result = null;

		final int match = sUriMatcher.match(uri);
		if (match != MATCH_ALL_ROWS)
		{
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		else
		{
			// TODO: Test values fields and/or set to default?

			final long rowId = db_.insertOrThrow(TABLE_NAME_VIDEO, null, values);
			if (rowId > 0)
			{
				// TODO: Read:
				// http://developer.android.com/reference/android/content/Context.html#getExternalFilesDir%28java.lang.String%29

				final String filename = dataPathFileName(rowId);

				values.put(_DATA, filename);

				final int rows = db_.update(TABLE_NAME_VIDEO, values, _ID + "=" + rowId, null);

				if (rows == 1)
				{
					result = ContentUris.withAppendedId(CONTENT_URI, rowId);

					final ContentResolver contentResolver = context_.getContentResolver();
					contentResolver.notifyChange(result, null);
				}
				else
				{
					throw new SQLException("Failed to update " + uri);
				}
			}
			else
			{
				throw new SQLException("Failed to insert row into " + uri);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		queryBuilder.setTables(TABLE_NAME_VIDEO);
		// queryBuilder.setStrict(true);
		// queryBuilder.setProjectionMap(sColumnMap);

		final int match = sUriMatcher.match(uri);

		switch (match)
		{
		case MATCH_ONE_ROW:
			final String id = getID(uri);
			if (id != null)
			{
				final CharSequence where = _ID + "=" + id;
				queryBuilder.appendWhere(where);
			}
			else
			{
				throw new IllegalArgumentException("Invalid URI " + uri);
			}
			break;

		case MATCH_ALL_ROWS:
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
			// break;
		}

		String orderBy;
		if (TextUtils.isEmpty(sortOrder))
		{
			orderBy = DEFAULT_ORDER_BY;
		}
		else
		{
			orderBy = sortOrder;
		}

		Cursor result = null;
		try
		{
			result = queryBuilder.query(db_, projection, selection, selectionArgs, null, null, orderBy);
		}
		catch (Throwable e)
		{
//			final String msg = e.getMessage();
//			Log.e("VideoProvider", msg);
		}

		if (result != null)
		{
			final ContentResolver contentResolver = context_.getContentResolver();
			result.setNotificationUri(contentResolver, uri);
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
	{
		int result = 0;

		String where;

		final int match = sUriMatcher.match(uri);
		switch (match)
		{
		case MATCH_ONE_ROW:
			where = makeWhereClause(uri, selection);
			break;

		case MATCH_ALL_ROWS:
			where = selection;
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
			// break;
		}

		result = db_.update(TABLE_NAME_VIDEO, values, where, selectionArgs);

		final ContentResolver contentResolver = context_.getContentResolver();
		contentResolver.notifyChange(uri, null);

		return result;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		int result = 0;

		String where;

		final int match = sUriMatcher.match(uri);
		switch (match)
		{
		case MATCH_ONE_ROW:
			where = makeWhereClause(uri, selection);
			break;

		case MATCH_ALL_ROWS:
			where = selection;
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
			// break;
		}

// This should delete the _DATA file, but I don't need it as I'm deleting the _DATA in the caller. 
//		final String[] columns = new String[] {_DATA};
//
//		final Cursor cursor = db_.query(TABLE_NAME_VIDEO, columns, selection, selectionArgs, null, null, null);
//
//		if (cursor != null)
//		{
//			try
//			{
//				if (cursor.moveToFirst())
//				{
//					do
//					{
//						final String pathFileName = cursor.getString(0);
//						final File file = new File(pathFileName);
//						if (file != null)
//						{
//							file.delete();
//						}
//					} while (cursor.moveToNext());
//				}
//			}
//			finally
//			{
//				cursor.close();
//			}
//		}

		if (match==MATCH_ALL_ROWS && where==null && selectionArgs==null)
		{
			deleteAll();
		}
		else
		{
			result = db_.delete(TABLE_NAME_VIDEO, where, selectionArgs);
		}

		final ContentResolver contentResolver = context_.getContentResolver();
		contentResolver.notifyChange(uri, null);

		return result;
	}

	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
		close();
	}

	public void close()
	{
		if (dbOpenHelper_ != null)
		{
			dbOpenHelper_.close();
			dbOpenHelper_ = null;
		}
		if (db_ != null)
		{
			db_.close();
			db_ = null;
		}
	}

	private static SQLiteDatabase openRW(final DBOpenHelper dbOpenHelper)
	{
		final SQLiteDatabase result = dbOpenHelper.getWritableDatabase();
		return result;
	}

//	private static SQLiteDatabase openRO(final DBOpenHelper dbOpenHelper)
//	{
//		final SQLiteDatabase result = dbOpenHelper.getReadableDatabase();
//		return result;
//	}

	private String dataPathFileName(final long rowId)
	{
		final String result = path_ + "vt" + rowId + ".jpg"; // vt: Video Thumbnail
		return result;
	}

	private static String getThumbnailPathname(final Context context)
	{
		String result = null;

		final Context ctx = Util.getSafeContext(context);
		final String filepath = Util.getAppStorageDirectory(ctx);
		if (!TextUtils.isEmpty(filepath))
		{
			result = filepath + DATABASE_NAME + THUMBNAIL_DIR;
		}
		return result;
	}

	private void deleteAll()
	{
		dbOpenHelper_.truncate(db_);
		final File dir = new File(path_);
		Util.delDirs(dir);
		dir.mkdirs();
	}
}
