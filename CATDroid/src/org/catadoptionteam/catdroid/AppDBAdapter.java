/**
 * 
 */
package org.catadoptionteam.catdroid;

import java.io.File;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.text.format.Time;

/**
 * @author bessermt
 *
 */
public class AppDBAdapter
{
	public static final int INVALID_KEY_ID = 0;

	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_NAME = "app.db";

	public static final String TABLE_ANIMAL = "animal";

	public static final int KEY_ID_COL = 0;

	public static final String KEY_ID = BaseColumns._ID;

	public static final String PET_ID = "petID"; // Unique Pet Identifier

	public static final String EXPIRE_DATE = "expDate"; // "YYYY-MM-DD", See ISO-8601, CATEGORY_ANIMAL_OF_THE_DAY's Date or CATEGORY_M9L's Expiration
	public static final String NAME = "name"; // Name
	public static final String STATUS = "status"; // STATUS_AVAILABLE, STATUS_ADOPTED
	public static final String SPECIES = "species"; // "Cat", ...
	public static final String CATEGORY = "category"; // CATEGORY_ANIMAL_OF_THE_DAY, CATEGORY_M9L
	public static final String DOB = "dob"; // "YYYY-MM-DD", See ISO-8601
	public static final String SEX = "sex"; // 1=Male, 2=Female.  See ISO 5218:2004
	public static final String ASPCA_ALITY = "aspcaAlity"; // ASPCA Feline-ality, Puppy-ality, Canine-ality
	public static final String BIOGRAPHY = "biography"; // Lengthy description of the animal
	public static final String LG_PHOTO = "lgPhoto"; // Large Photo base filename without path or extension, Expected size around 500x600.
	public static final String SM_PHOTO = "smPhoto"; // Small Photo base filename without path or extension, Expected size around 200x200, square.
	public static final String EXTRA_ID = "extraID"; // External ID, useful for getting data from additional servers (ex. Petfinder)
	public static final String EXTRA_URL = "extraURL"; // Web EXTRA_URL with additional info
// TODO: Consider using _data for storing files as described in Ch 3 of the book "Pro Android" ISBN-10: 1430215968

	public static final int STATUS_AVAILABLE = 0;
	public static final int STATUS_ADOPTED = 1;

	public static final int SEX_UNKNOWN = 0;
	public static final int SEX_MALE = 1;
	public static final int SEX_FEMALE = 2;

	public static final int CATEGORY_ANIMAL_OF_THE_DAY = 0;
	public static final int CATEGORY_M9L = 1;

	public class DBOpenHelper extends SQLiteOpenHelper
	{
		private static final String SQL_CREATE_TABLE_ANIMAL = 
			"create table " + TABLE_ANIMAL + 
			" (" + 
				KEY_ID + " integer primary key autoincrement, " + 
				PET_ID + " text unique on conflict replace, " + 
				EXPIRE_DATE + " text not null, " + 
				NAME + " text not null, " + 
				STATUS + " integer not null, " + 
				CATEGORY + " integer not null, " + 
				SPECIES + " text not null, " + 
				DOB + " text not null, " + 
				SEX + " integer not null, " + 
				ASPCA_ALITY + " integer not null, " + 
				BIOGRAPHY + " text not null, " + 
				LG_PHOTO + " text not null, " + 
				SM_PHOTO + " text not null, " + 
				EXTRA_ID + " text, " + 
				EXTRA_URL + " text" + 
			");";

		private static final String SQL_DROP_TABLE_ANIMAL = 
			"drop table if exists " + TABLE_ANIMAL + ";";

		private SQLiteDatabase db_;

		public DBOpenHelper(final Context context, final String name, final CursorFactory factory, final int version)
		{
			super(context, name, factory, version);
		}

		private void create(final SQLiteDatabase db)
		{
			db.execSQL(SQL_CREATE_TABLE_ANIMAL);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			// super.onCreate(db);
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
		}

		public void truncate(final SQLiteDatabase db)
		{
			db.execSQL(SQL_DROP_TABLE_ANIMAL);
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

	private static final String PHOTO_DIR = "Pictures/photos/";

	private SQLiteDatabase db_;
	private DBOpenHelper dbOpenHelper_;
	private Context context_;

	public AppDBAdapter(final Context context, final boolean rw)
	{
		if (context == null)
		{
			context_ = CATApp.getAppContext();
		}
		else
		{
			context_ = context;
		}
		dbOpenHelper_ = new DBOpenHelper(context_, DATABASE_NAME, null, DATABASE_VERSION);
		if (rw)
		{
			openRW();
		}
		else
		{
			openRO();
		}
	}

	public AppDBAdapter(final Context context)
	{
		this(context, false);
	}

	public static String getImagePath(final Context context)
	{
		String result = null;

		final Context ctx = Util.getSafeContext(context);
		final String filepath = Util.getAppStorageDirectory(ctx);
		if (!TextUtils.isEmpty(filepath))
		{
			result = filepath + PHOTO_DIR;
		}

		return result;
	}

	private void openRW()
	{
		db_ = dbOpenHelper_.getWritableDatabase();
	}

	private void openRO()
	{
		db_ = dbOpenHelper_.getReadableDatabase();
	}

	public void close()
	{
		if (db_ != null)
		{
			db_.close();
			db_ = null;
		}
		if (dbOpenHelper_ != null)
		{
			dbOpenHelper_.close();
			dbOpenHelper_ = null;
		}
	}

	public Cursor query(final Activity activity, final String[] cols, final String where, final String order)
	{
		Cursor result = null;
		try
		{
			Cursor cursor;
			cursor = db_.query(TABLE_ANIMAL, cols, where, null, null, null, order);
			if (cursor != null)
			{
				if (activity != null)
				{
					activity.startManagingCursor(cursor);
				}
				result = cursor;
			}
		}
		catch (Throwable t)
		{
//			String message = "null";
//			if (t != null)
//			{
//				message = t.getMessage();
//			}
//			Log.e("Query", message);
		}
		return result;
	}

	private Cursor query(final Activity activity, final String[] cols, final String where)
	{
		final Cursor result = query(activity, cols, where, null);

		return result;
	}

	public final Cat getAnimal(final Activity activity, final int keyID)
	{
		Cat result = null;

		final String[] resultColumns = 
			new String[]
			{
				PET_ID, 
				NAME, 
				STATUS, 
				DOB, 
				SEX, 
				ASPCA_ALITY, 
				BIOGRAPHY, 
				LG_PHOTO, 
				SM_PHOTO, 
				EXTRA_ID, 
				EXTRA_URL
			};

		final String where = 
			AppDBAdapter.KEY_ID + "=" + keyID;

		Cursor cursor = null;

		try
		{
			cursor = query(activity, resultColumns, where);
	
			if (cursor != null)
			{
				final boolean success = cursor.moveToFirst();
	
				if (success)
				{
					final String petID = cursor.getString(0);
					final String name = cursor.getString(1);
					final int status = cursor.getInt(2);
					final String dob = cursor.getString(3);
					final int sex = cursor.getInt(4);
					final int ASPCAality = cursor.getInt(5);
					final String biography = cursor.getString(6);
					final String lgPhotoBaseFilename = cursor.getString(7);
					final String smPhotoBaseFilename = cursor.getString(8);
					final String extraID = cursor.getString(9);
					final String extraURL = cursor.getString(10);
	
					result = new Cat(context_, petID, name, status, "Cat", dob, sex, ASPCAality, biography, lgPhotoBaseFilename, smPhotoBaseFilename, extraID, extraURL);
				}
			}
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}

		return result;
	}

	public long insert
	(
		final String petID, 
		final Time expDate, 
		final String name, 
		final int status, 
		final int category, 
		final String species, 
		final Time dob, 
		final int sex, 
		final int felineality, 
		final String biography, 
		final String lgPhotoURL, 
		final String smPhotoURL, 
		final String extraID, 
		final String extraURL
	)
	{
		boolean success = true;

		final ContentValues contentValues = new ContentValues();

		contentValues.put(PET_ID, petID);
		final String expDateStr = expDate.format3339(true);
		contentValues.put(EXPIRE_DATE, expDateStr);
		contentValues.put(NAME, name);
		contentValues.put(STATUS, status);
		contentValues.put(CATEGORY, category);
		contentValues.put(SPECIES, species);
		final String dobStr = dob.format3339(true);
		contentValues.put(DOB, dobStr);
		contentValues.put(SEX, sex);
		contentValues.put(ASPCA_ALITY, felineality);
		contentValues.put(BIOGRAPHY, biography);

		final String imagePath = getImagePath(context_);
		final File imageDir = new File(imagePath);
		imageDir.mkdirs();

		final String lgPhotoBaseFilename = Cat.getPhotoBaseFilename(context_, petID, "lg");
		final String lgPhotoPathFilename = BitmapDownloaderTask.getPathFilename(imagePath, lgPhotoBaseFilename);
		final BitmapDownloaderTask bitmapDownloaderTaskLg = new BitmapDownloaderTask(imagePath);

		success = success && bitmapDownloaderTaskLg.executeSync(lgPhotoPathFilename, lgPhotoURL);

		contentValues.put(LG_PHOTO, lgPhotoBaseFilename);

		final String smPhotoBaseFilename = Cat.getPhotoBaseFilename(context_, petID, "sm");
		final String smPhotoPathFilename = BitmapDownloaderTask.getPathFilename(imagePath, smPhotoBaseFilename);
		final BitmapDownloaderTask bitmapDownloaderTaskSm = new BitmapDownloaderTask(imagePath);

		success = success && bitmapDownloaderTaskSm.executeSync(smPhotoPathFilename, smPhotoURL);

		contentValues.put(SM_PHOTO, smPhotoBaseFilename);

		contentValues.put(EXTRA_ID, extraID);

		contentValues.put(EXTRA_URL, extraURL);

		long result = -1;

		if (success)
		{
			result = db_.insert(TABLE_ANIMAL, null, contentValues);
		}

//		if (result == -1)
//		{
//			Log.e("DB Insert", "Unable to insert the data"); // TODO:  What should be done under failure?
//		}

		return result;
	}

	public void deleteID(final Activity activity, final int id)
	{
		final String[] resultColumns = 
			new String[]
			{
				LG_PHOTO, 
				SM_PHOTO
			};

		final String where = KEY_ID + "=" + id;

		Cursor cursor = null;

		try
		{
			cursor = query(activity, resultColumns, where);
			if (cursor != null)
			{
				if (cursor.moveToFirst())
				{
					final String path = getImagePath(context_);
					final String photoLg = path + cursor.getString(0);
					final String photoSm = path + cursor.getString(1);

					if (photoLg != null)
					{
						final File file = new File(photoLg);
						file.delete();
					}
					if (photoSm != null)
					{
						final File file = new File(photoSm);
						file.delete();
					}

					db_.delete(TABLE_ANIMAL, where, null);
				}
			}
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
	}

	public void deleteAll()
	{
		dbOpenHelper_.truncate(db_);
		final String path = getImagePath(context_);
		final File dir = new File(path);
		Util.delDirs(dir);
	}
}
