/**
 * 
 */
package org.catadoptionteam.catdroid;

import java.io.File;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.Time;

/**
 * @author bessermt
 *
 */
public class PetfinderProvider extends SuperContentProvider
{
	public static class PetRecord
	{
		public static final int AGE_UNKNOWN = -1;
		public static final int AGE_BABY = 0;
		public static final int AGE_YOUNG = 1;
		public static final int AGE_ADULT = 2;
		public static final int AGE_SENIOR = 3;

		public static final int SIZE_UNKNOWN = -1;
		public static final int SIZE_SMALL = 0;
		public static final int SIZE_MEDIUM = 1;
		public static final int SIZE_LARGE = 2;
		public static final int SIZE_XLARGE = 3;

		public static final int SEX_UNKNOWN = AppDBAdapter.SEX_UNKNOWN;
		public static final int SEX_MALE = AppDBAdapter.SEX_MALE;
		public static final int SEX_FEMALE = AppDBAdapter.SEX_FEMALE;

		// See: http://www.petfinder.com/developers/api-docs/faq.html

		private static final int MAX_PHOTOS = 3;

		// private static final int PHOTO_SIZE_UNKNOWN = -1;
		public static final int PHOTO_SIZE_X = 0;
		private static final int PHOTO_SIZE_PN = 1;
		private static final int PHOTO_SIZE_FPM = 2;
		private static final int PHOTO_SIZE_PNT = 3;
		private static final int PHOTO_SIZE_T = 4;
		private static final int PHOTO_SIZE_SIZE = 5;

		private static final int[] AGE = {R.string.baby, R.string.young, R.string.adult, R.string.senior};
		private static final int[] SIZE = {R.string.small_text, R.string.medium_text, R.string.large_text, R.string.xlarge_text};

		private long dbId_;

		private long pfId_;
		private String shelterPetId_;
		private String name_;
		private Time lastUpdate_;
		private String species_;
		private boolean mix_;
		private int age_;
		private int sex_;
		private int size_;
		private boolean altered_;
		private boolean hasShots_;
		private boolean housebroken_;
		private boolean noCats_;
		private boolean noDogs_;
		private boolean noKids_;
		private boolean specialNeeds_;
		private boolean noClaws_;
		private char status_;
		private int photoCount_;
		private String photoBaseUri_;
		private String description_;
		private Time created_;
		private Time modified_;
		private boolean favorite_;
		private boolean viewed_;
		private BitSet breed_;
		private int sponsor_;
		private int donate_;

		private Context context_;

		public PetRecord(final Context context)
		{
			context_ = Util.getSafeContext(context);
			mix_ = true;
			age_ = AGE_UNKNOWN;
			sex_ = AppDBAdapter.SEX_UNKNOWN;
			size_ = SIZE_UNKNOWN;
			setStatus(PetfinderProvider.VALUE_FIELD_PET_PF_STATUS_REMOVAL_PENDING);
			breed_ = new BitSet();
		}

		private static boolean isPhotoUrlValid(final String baseUrl, final int i)
		{
			final boolean result =!TextUtils.isEmpty(baseUrl) && i>=0 && i<MAX_PHOTOS;
			return result;
		}

		private static boolean isPhotoUrlValid(final String baseUrl, final int i, final int photoSize)
		{
			final boolean result =isPhotoUrlValid(baseUrl, i) && photoSize>=0 && photoSize<PHOTO_SIZE_SIZE;
			return result;
		}

		private boolean isPhotoUrlValid()
		{
			final boolean result = isPhotoUrlValid(photoBaseUri_, photoCount_-1);
			return result;
		}

		public boolean isValid()
		{
			final boolean result = isPhotoUrlValid() && status_==VALUE_FIELD_PET_PF_STATUS_ADOPTABLE;
			return result;
		}

		public void setPFId(final long pfId)
		{
			pfId_ = pfId;
		}

		public void setSheterPetId(final String shelterPetId)
		{
			shelterPetId_ = shelterPetId;
		}

		public void setName(final String name)
		{
			name_ = name;
		}

		public String getName()
		{
			String result = context_.getString(R.string.unknown);

			if (!TextUtils.isEmpty(name_))
			{
				result = name_;
			}

			return result;
		}

		private Time parseTime(final String timeStr)
		{
			final Time result = new Time();
			try
			{
				result.parse3339(timeStr);
			}
			catch (Throwable t)
			{
//				final String msg = t.getMessage();
//				Log.e("PetfinderProvider.PetRecord.parseTime(String)", msg);
				result.setToNow();
			}
			return result;
		}

		public void setLastUpdate(final String lastUpdate)
		{
			lastUpdate_ = parseTime(lastUpdate);
		}

		public void setSpecies(final String species)
		{
			species_ = species;
		}

		public void setMix(final String mix)
		{
			boolean value = true;
			if (mix.equalsIgnoreCase("no"))
			{
				value = false;
			}
			mix_ = value;
		}

		public void setAge(final String age)
		{
			age_ = AGE_UNKNOWN;

			if (age.equals("Senior"))
			{
				age_ = AGE_SENIOR;
			}
			else if (age.equals("Adult"))
			{
				age_ = AGE_ADULT;
			}
			else if (age.equals("Young"))
			{
				age_ = AGE_YOUNG;
			}
			else if (age.equals("Baby"))
			{
				age_ = AGE_BABY;
			}
		}

		public static String getAgeText(final Context context, final int age)
		{
			int strResId = R.string.unknown_age;

			if (age>=0 && age<AGE.length)
			{
				strResId = AGE[age];
			}

			final Context ctx = Util.getSafeContext(context);

			final String result = ctx.getString(strResId);

			return result;
		}

		public void setSex(final String sex)
		{
			int value = AppDBAdapter.SEX_UNKNOWN;

			if (sex.length()==1)
			{
				switch (sex.charAt(0))
				{
				case 'M':
					value = AppDBAdapter.SEX_MALE;
					break;
	
				case 'F':
					value = AppDBAdapter.SEX_FEMALE;
					break;
	
				default:
					break;
				}
			}

			sex_ = value;
		}

		public static String getSizeText(final Context context, final int size)
		{
			int strResId = R.string.unknown_size;

			if (size>=0 && size<SIZE.length)
			{
				strResId = SIZE[size];
			}

			final Context ctx = Util.getSafeContext(context);

			final String result = ctx.getString(strResId);

			return result;
		}

		public void setSize(final String size)
		{
			int value = SIZE_UNKNOWN;

			switch (size.charAt(0))
			{
			case 'S':
				value = SIZE_SMALL;
				break;

			case 'M':
				value = SIZE_MEDIUM;
				break;

			case 'L':
				value = SIZE_LARGE;
				break;

			case 'X':
				value = SIZE_XLARGE;
				break;

			default:
				break;
			}

			size_ = value;
		}

		public boolean setOption(final String optionStr)
		{
			boolean result = true;

			if (optionStr.equals("specialNeeds"))
			{
				specialNeeds_ = true;
			}
			else if (optionStr.equals("noDogs"))
			{
				noDogs_ = true;
			}
			else if (optionStr.equals("noCats"))
			{
				noCats_ = true;
			}
			else if (optionStr.equals("noKids"))
			{
				noKids_ = true;
			}
			else if (optionStr.equals("noClaws"))
			{
				noClaws_ = true;
			}
			else if (optionStr.equals("hasShots"))
			{
				hasShots_ = true;
			}
			else if (optionStr.equals("housebroken"))
			{
				housebroken_ = true;
			}
			else if (optionStr.equals("altered"))
			{
				altered_ = true;
			}
			else
			{
				result = false;
			}

			return result;
		}

		public void setStatus(final char status)
		{
			status_ = status;
		}

		public int getPhotoCount()
		{
			final int result = photoCount_;
			return result;
		}

		public void setPhotoBaseUri(final String photoUri)
		{
			// ex: http://photos.petfinder.com/photos/US/OR/OR07/21931709/OR07.21931709-3-pnt.jpg

			final int start = photoUri.indexOf('-') + 1;
			final int end = photoUri.indexOf('-', start);
			if (start < end)
			{
				final String countStr = photoUri.substring(start, end);
				final int count = Integer.parseInt(countStr);
				if (count > photoCount_)
				{
					photoCount_ = count;
				}

				final String photoBaseUri = photoUri.substring(0, start);
				if (!TextUtils.isEmpty(photoBaseUri))
				{
					photoBaseUri_ = photoBaseUri;
				}
			}
		}

		public static String getPhotoUri(final String baseUri, final int i, final int photoSize)
		{
			// ex: http://photos.petfinder.com/photos/US/OR/OR07/21931709/OR07.21931709-2-pnt.jpg

			String result = null;

			if (isPhotoUrlValid(baseUri, i, photoSize))
			{
				String uri = baseUri + String.valueOf(i+1) + "-";

				String sizeCode = null;

				switch (photoSize)
				{
				case PHOTO_SIZE_X:
					sizeCode = "x";
					break;

				case PHOTO_SIZE_PN:
					sizeCode = "pn";
					break;

				case PHOTO_SIZE_FPM:
					sizeCode = "fpm";
					break;

				case PHOTO_SIZE_PNT:
					sizeCode = "pnt";
					break;

				case PHOTO_SIZE_T:
					sizeCode = "t";
					break;

				default:
					break;
				}

				if (!TextUtils.isEmpty(sizeCode))
				{
					result = uri + sizeCode + ".jpg";
				}
			}

			return result;
		}

		public void setDescription(final String description)
		{
			description_ = description; // TODO: Strip unusable HTML markup tags?
		}

		public void setLastUpdated(final String lastUpdated)
		{
			lastUpdate_ = parseTime(lastUpdated);
		}

		public Time getLastUpdated()
		{
			final Time result = lastUpdate_;
			return result;
		}

//		public void setCreated(final String created)
//		{
//			created_ = parseTime(created);
//		}
//
//		public Time getCreated()
//		{
//			final Time result = created_;
//			return result;
//		}
//
//		public void setModified(final String modified)
//		{
//			modified_ = parseTime(modified);
//		}
//
//		public Time getModified()
//		{
//			final Time result = modified_;
//			return result;
//		}

//		private void setFavorite(final boolean favorite)
//		{
//			favorite_ = favorite;
//		}

//		private void setViewed(final boolean viewed)
//		{
//			viewed_ = viewed;
//		}

		public boolean setBreed(final String breed)
		{
			boolean result = false;

			if (!TextUtils.isEmpty(breed))
			{
				final String stripSpec = "\\W";

				final String breedNormalized = breed.replaceAll(stripSpec,"");

				final String[] breedList = context_.getResources().getStringArray(R.array.pf_breed_list_cat);

				int i = breedList.length; // TODO: Consider a binary search since the list is sorted.
				while (i > 0)
				{
					--i;
					final String breedItemNormalized = breedList[i].replaceAll(stripSpec,"");
					if (breedNormalized.equalsIgnoreCase(breedItemNormalized))
					{
						breed_.set(i);
						break;
					}
				}
			}

			return result;
		}

//		private void setSponser(final int sponsor)
//		{
//			sponsor_ = sponsor;
//		}

//		private void setDonate(final int donate)
//		{
//			donate_ = donate;
//		}

		public void initContentValues(final ContentValues contentValues)
		{
			contentValues.put(FIELD_PET_PF_ID, pfId_);
			contentValues.put(FIELD_PET_PF_SHELTER_PET_ID, shelterPetId_);
			contentValues.put(FIELD_PET_PF_NAME, name_);
			contentValues.put(FIELD_PET_PF_LASTUPDATE, lastUpdate_.toMillis(true));
			contentValues.put(FIELD_PET_PF_SPECIES, species_);
			contentValues.put(FIELD_PET_PF_MIX, mix_);
			contentValues.put(FIELD_PET_PF_AGE, age_);
			contentValues.put(FIELD_PET_PF_SEX, sex_);
			contentValues.put(FIELD_PET_PF_SIZE, size_);
			contentValues.put(FIELD_PET_PF_ALTERED, altered_);
			contentValues.put(FIELD_PET_PF_HAS_SHOTS, hasShots_);
			contentValues.put(FIELD_PET_PF_HOUSEBROKEN, housebroken_);
			contentValues.put(FIELD_PET_PF_NO_CATS, noCats_);
			contentValues.put(FIELD_PET_PF_NO_DOGS, noDogs_);
			contentValues.put(FIELD_PET_PF_NO_KIDS, noKids_);
			contentValues.put(FIELD_PET_PF_SPECIAL_NEEDS, specialNeeds_);
			contentValues.put(FIELD_PET_PF_NO_CLAWS, noClaws_);
			contentValues.put(FIELD_PET_PF_STATUS, Character.toString(status_));
			contentValues.put(FIELD_PET_PF_PHOTO_COUNT, photoCount_);
			contentValues.put(FIELD_PET_PF_PHOTO_BASE_URI, photoBaseUri_);
			contentValues.put(FIELD_PET_PF_DESCRIPTION, description_);
			// contentValues.put(FIELD_PET_CREATED_TIME, created_.toMillis(true)); // Always done on insert, never on update.
			// contentValues.put(FIELD_PET_MODIFIED_TIME, modified_.toMillis(true)); // Always done on insert and update.
			// contentValues.put(FIELD_PET_FAVORITE, favorite_);

			contentValues.put(FIELD_PET_VIEWED, viewed_);

			contentValues.put(FIELD_PET_SPONSOR, sponsor_);
			contentValues.put(FIELD_PET_DONATE, donate_);
			contentValues.put(FIELD_PET_FLAG, 0);

			contentValues.put(FIELD_BREED_PF_BREED_INDEX, breed_.toString());
		}
	}

	public class DBOpenHelper extends SQLiteOpenHelper
	{
		private static final String SQL_CREATE_TABLE_PETFINDER_PET = 
			"CREATE TABLE " + TABLE_NAME_PETFINDER_PET + 
			" (" + 
				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				_DATA + " TEXT, " + // The fully qualified path and file on the device for the thumbnail file.

				// Data from Petfinder (PF) html get api.petfinder.com/pet.get?key=deb1a50a01d45c5c20a4c5c93eb75dcd&id=42&format=json
				FIELD_PET_PF_ID + " INTEGER NOT NULL UNIQUE ON CONFLICT ABORT, " + 
				FIELD_PET_PF_SHELTER_PET_ID + " TEXT NOT NULL UNIQUE ON CONFLICT ABORT, " + 
				FIELD_PET_PF_NAME + " TEXT NOT NULL, " + 
				FIELD_PET_PF_LASTUPDATE + " INTEGER NOT NULL, " + 
				FIELD_PET_PF_SPECIES + " TEXT NOT NULL, " + 
				FIELD_PET_PF_MIX + " INTEGER NOT NULL, " + 
				FIELD_PET_PF_AGE + " INTEGER NOT NULL, " + 
				FIELD_PET_PF_SEX + " INTEGER NOT NULL, " + 
				FIELD_PET_PF_SIZE + " INTEGER NOT NULL, " + 
				FIELD_PET_PF_ALTERED + " INTEGER NOT NULL, " + 
				FIELD_PET_PF_HAS_SHOTS + " INTEGER NOT NULL, " + 
				FIELD_PET_PF_HOUSEBROKEN + " INTEGER NOT NULL, " + 
				FIELD_PET_PF_NO_CATS + " INTEGER NOT NULL, " + 
				FIELD_PET_PF_NO_DOGS + " INTEGER NOT NULL, " + 
				FIELD_PET_PF_NO_KIDS + " INTEGER NOT NULL, " + 
				FIELD_PET_PF_SPECIAL_NEEDS + " INTEGER NOT NULL, " + 
				FIELD_PET_PF_NO_CLAWS + " INTEGER NOT NULL, " + 
				FIELD_PET_PF_STATUS + " TEXT NOT NULL, " + // "A"=adoptable, "R"=removal potential
				FIELD_PET_PF_PHOTO_COUNT + " INTEGER NOT NULL, " + 
				FIELD_PET_PF_PHOTO_BASE_URI + " TEXT NOT NULL, " + 
				FIELD_PET_PF_DESCRIPTION + " TEXT NOT NULL, " + 

				// Calculated
				FIELD_PET_CREATED_TIME  + " INTEGER NOT NULL, " + // Assigned to now when record is created.
				FIELD_PET_MODIFIED_TIME + " INTEGER NOT NULL, " + // Assigned to now with every record update.
				FIELD_PET_FAVORITE + " INTEGER NOT NULL, " + // User selected animal as a favorite.
				FIELD_PET_VIEWED + " INTEGER NOT NULL, " + // TODO: Assigned true iff user views animal's bio.

				// Reserved for future use
				FIELD_PET_SPONSOR + " INTEGER NOT NULL, " + // TODO: Allow NULL?
				FIELD_PET_DONATE + " INTEGER NOT NULL, " + // TODO: Allow NULL?
				FIELD_PET_FLAG + " INTEGER NOT NULL, " +  // TODO: Allow NULL?
				FIELD_PET_EXTRA_INTEGER + " INTEGER, " + 
				FIELD_PET_EXTRA_TEXT + " TEXT" + 
			");";

		private static final String SQL_CREATE_TABLE_PETFINDER_BREED = 
			"CREATE TABLE " + TABLE_NAME_PETFINDER_BREED + 
			" (" + 
				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				FIELD_BREED_PET_ID + " INTEGER NOT NULL, " + 
				FIELD_BREED_PF_BREED_INDEX + " INTEGER NOT NULL, " + 
				"FOREIGN KEY(" + FIELD_BREED_PET_ID + ") REFERENCES " + TABLE_NAME_PETFINDER_PET + "(" + _ID + ")" + 
			");";

		private static final String SQL_CREATE_INDEX_FIELD_PET = 
			"CREATE INDEX " + INDEX_NAME_PET + " ON " + TABLE_NAME_PETFINDER_PET + 
			"(" + 
				FIELD_PET_CREATED_TIME + " DESC, " + 
				FIELD_PET_PF_NAME + " ASC, " + 
				FIELD_PET_MODIFIED_TIME + " ASC, " + 
				FIELD_PET_FAVORITE + 
			");";

		private static final String SQL_CREATE_INDEX_FIELD_BREED = 
			"CREATE INDEX " + INDEX_NAME_BREED + " ON " + TABLE_NAME_PETFINDER_BREED + 
			"(" + 
				FIELD_BREED_PET_ID + // TODO: Already a FOREIGN KEY, so is an INDEX actually helpful?
			");";

		private static final String SQL_DROP_INDEX_PETFINDER_PET = 
				"DROP INDEX IF EXISTS " + INDEX_NAME_PET + ";";

		private static final String SQL_DROP_INDEX_PETFINDER_BREED = 
				"DROP INDEX IF EXISTS " + INDEX_NAME_BREED + ";";

		private static final String SQL_DROP_TABLE_PETFINDER_PET = 
				"DROP TABLE IF EXISTS " + TABLE_NAME_PETFINDER_PET + ";";

		private static final String SQL_DROP_TABLE_PETFINDER_BREED = 
				"DROP TABLE IF EXISTS " + TABLE_NAME_PETFINDER_BREED + ";";

		private SQLiteDatabase db_;

		public DBOpenHelper(final Context context, final String name, final CursorFactory factory, final int version)
		{
			super(context, name, factory, version);
		}

		private void create(final SQLiteDatabase db)
		{
			db.execSQL(SQL_CREATE_TABLE_PETFINDER_PET);
			db.execSQL(SQL_CREATE_INDEX_FIELD_PET);
			db.execSQL(SQL_CREATE_TABLE_PETFINDER_BREED);
			db.execSQL(SQL_CREATE_INDEX_FIELD_BREED);
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
			db.execSQL(SQL_DROP_INDEX_PETFINDER_BREED);
			db.execSQL(SQL_DROP_TABLE_PETFINDER_BREED);
			db.execSQL(SQL_DROP_INDEX_PETFINDER_PET);
			db.execSQL(SQL_DROP_TABLE_PETFINDER_PET);
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

	private static final String ORG_NAME = "org.catadoptionteam";
	private static final String APP_NAME= "catdroid";
	// private static final String CLASS_NAME = "PetfinderProvider";

	private static final String DATABASE_NAME = "petfinder";
	private static final int DATABASE_VERSION = 1;

	// TODO: Make private and build to see if used externally.
	public static final String TABLE_NAME_PETFINDER_PET = "pet";
	public static final String TABLE_NAME_PETFINDER_BREED = "breed";

	private static final String INDEX_NAME_PET = "petIndex";
	private static final String INDEX_NAME_BREED = "breedIndex";

	public static final String FIELD_PET_PF_ID = "PFid";
	public static final String FIELD_PET_PF_SHELTER_PET_ID = "shelterPetId";
	public static final String FIELD_PET_PF_NAME = "name";
	public static final String FIELD_PET_PF_LASTUPDATE = "lastUpdate";
	private static final String FIELD_PET_PF_SPECIES = "species";
	private static final String FIELD_PET_PF_MIX = "mix";
	public static final String FIELD_PET_PF_AGE = "age";
	public static final String FIELD_PET_PF_SEX = "sex";
	public static final String FIELD_PET_PF_SIZE = "size";
	private static final String FIELD_PET_PF_ALTERED = "altered";
	private static final String FIELD_PET_PF_HAS_SHOTS = "hasShots";
	private static final String FIELD_PET_PF_HOUSEBROKEN = "housebroken";
	public static final String FIELD_PET_PF_NO_CATS = "noCats";
	public static final String FIELD_PET_PF_NO_DOGS = "noDogs";
	public static final String FIELD_PET_PF_NO_KIDS = "noKids";
	public static final String FIELD_PET_PF_SPECIAL_NEEDS = "specialNeeds";
	public static final String FIELD_PET_PF_NO_CLAWS = "noClaws";
	public static final String FIELD_PET_PF_STATUS = "status";
	public static final String FIELD_PET_PF_PHOTO_COUNT = "photoCount";
	public static final String FIELD_PET_PF_PHOTO_BASE_URI = "photoBaseUri";
	public static final String FIELD_PET_PF_DESCRIPTION = "description";

	public static final String FIELD_PET_CREATED_TIME = "createdTime";
	public static final String FIELD_PET_MODIFIED_TIME = "modifiedTime";
	public static final String FIELD_PET_FAVORITE = "favorite";
	private static final String FIELD_PET_VIEWED = "viewed";

	private static final String FIELD_PET_SPONSOR = "sponsor";
	private static final String FIELD_PET_DONATE = "donate";
	private static final String FIELD_PET_FLAG = "flag";
	private static final String FIELD_PET_EXTRA_INTEGER = "extraInteger";
	private static final String FIELD_PET_EXTRA_TEXT = "extraText";

	public static final String FIELD_BREED_PET_ID = "petId";
	public static final String FIELD_BREED_PF_BREED_INDEX = "breedIndex";

	public static final char VALUE_FIELD_PET_PF_STATUS_REMOVAL_PENDING = 'R';
	public static final char VALUE_FIELD_PET_PF_STATUS_ADOPTABLE = 'A';

	private static final String ITEM_PET_MIME_TYPE = "vnd.android.cursor.item/vnd." + ORG_NAME + "." + APP_NAME + "." + TABLE_NAME_PETFINDER_PET;
	private static final String DIR_PET_MIME_TYPE = "vnd.android.cursor.dir/vnd." + ORG_NAME + "." + APP_NAME + "." + TABLE_NAME_PETFINDER_PET;
	private static final String ITEM_BREED_MIME_TYPE = "vnd.android.cursor.item/vnd." + ORG_NAME + "." + APP_NAME + "." + TABLE_NAME_PETFINDER_PET;
	private static final String DIR_BREED_MIME_TYPE = "vnd.android.cursor.dir/vnd." + ORG_NAME + "." + APP_NAME + "." + TABLE_NAME_PETFINDER_PET;

	private static final int MATCH_PET_ONE_ROW = 1;
	private static final int MATCH_PET_ALL_ROWS = 2;
	private static final int MATCH_BREED_ONE_ROW = 3;
	private static final int MATCH_BREED_ALL_ROWS = 4;

	public static final String ORDER_BY_ARRIVAL = FIELD_PET_CREATED_TIME + " ASC";
	public static final String ORDER_BY_NAME = FIELD_PET_PF_NAME;
	public static final String ORDER_BY_RECENT = FIELD_PET_PF_LASTUPDATE + " DESC";
	private static final String DEFAULT_ORDER_BY = ORDER_BY_ARRIVAL;

	private static final String CONTENT_PREFIX = "content://";

	public static final String PROVIDER_AUTHORITY = "org.catadoptionteam.provider.catdroid.petfinder";

	public static final Uri CONTENT_URI_PET = Uri.parse(CONTENT_PREFIX + PROVIDER_AUTHORITY + "/" + TABLE_NAME_PETFINDER_PET);
	public static final Uri CONTENT_URI_BREED = Uri.parse(CONTENT_PREFIX + PROVIDER_AUTHORITY + "/" + TABLE_NAME_PETFINDER_BREED);

	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	static
	{
		sUriMatcher.addURI(PROVIDER_AUTHORITY, TABLE_NAME_PETFINDER_PET + "/#", MATCH_PET_ONE_ROW);
		sUriMatcher.addURI(PROVIDER_AUTHORITY, TABLE_NAME_PETFINDER_PET, MATCH_PET_ALL_ROWS);
		sUriMatcher.addURI(PROVIDER_AUTHORITY, TABLE_NAME_PETFINDER_BREED + "/#", MATCH_BREED_ONE_ROW);
		sUriMatcher.addURI(PROVIDER_AUTHORITY, TABLE_NAME_PETFINDER_BREED, MATCH_BREED_ALL_ROWS);
	}

	private SQLiteDatabase db_;

	private DBOpenHelper dbOpenHelper_;

	private Context context_;

	private String path_;
	private int breedsColorsLength_;

	public PetfinderProvider()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		int result = 0;

		final String where = makeWhereClause(uri, selection);

		final int match = sUriMatcher.match(uri);

		switch (match)
		{
		case MATCH_BREED_ONE_ROW:
		case MATCH_BREED_ALL_ROWS:
			result = db_.delete(TABLE_NAME_PETFINDER_BREED, where, selectionArgs);
			break;

		case MATCH_PET_ONE_ROW:
		case MATCH_PET_ALL_ROWS:
			final String[] columns = new String[] {_ID, _DATA, FIELD_PET_PF_SHELTER_PET_ID};

			Cursor cursor = null;
			try
			{
				cursor = db_.query(TABLE_NAME_PETFINDER_PET, columns, where, selectionArgs, null, null, null);
			}
			catch (Throwable e)
			{
//				final String msg = e.getMessage();
//				Log.e("PetfinderProvider", msg);
			}

			if (cursor != null)
			{
				try
				{
					if (cursor.moveToFirst())
					{
						final int colId = cursor.getColumnIndexOrThrow(_ID);
						final int colData = cursor.getColumnIndexOrThrow(_DATA);
						final int colShelterPetId = cursor.getColumnIndexOrThrow(FIELD_PET_PF_SHELTER_PET_ID);

						db_.beginTransaction();

						do
						{
							final int _id = cursor.getInt(colId);
							final String pathFileName = cursor.getString(colData);
							final String shelterPetId = cursor.getString(colShelterPetId);

							deletePhotoCache(shelterPetId);

							db_.delete(TABLE_NAME_PETFINDER_BREED, FIELD_BREED_PET_ID + "=" + _id, null);

							final File file = new File(pathFileName);
							if (file != null)
							{
								file.delete();
							}
						} while (cursor.moveToNext());

						result = db_.delete(TABLE_NAME_PETFINDER_PET, where, selectionArgs);

						db_.setTransactionSuccessful();
						db_.endTransaction();
					}
				}
				finally
				{
					cursor.close();
				}
			}

			if (match==MATCH_PET_ALL_ROWS && where==null && selectionArgs==null)
			{
				deleteAll();
			}

			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
			// break;
		}

		final ContentResolver contentResolver = context_.getContentResolver();
		contentResolver.notifyChange(uri, null);

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
		case MATCH_PET_ONE_ROW:
			result = ITEM_PET_MIME_TYPE;
			break;

		case MATCH_PET_ALL_ROWS:
			result = DIR_PET_MIME_TYPE;
			break;

		case MATCH_BREED_ONE_ROW:
			result = ITEM_BREED_MIME_TYPE;
			break;

		case MATCH_BREED_ALL_ROWS:
			result = DIR_BREED_MIME_TYPE;
			break;

		default:
			throw new IllegalArgumentException("Unknown URI Type: " + uri);
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

		Uri contentUri = null;

		long rowId = 0;

		final int match = sUriMatcher.match(uri);

		switch (match)
		{
		case MATCH_PET_ALL_ROWS:
			// TODO: Test values fields and/or set to default?

			final String breedIndex = values.getAsString(FIELD_BREED_PF_BREED_INDEX); // Separate breed info.
			values.remove(FIELD_BREED_PF_BREED_INDEX); // Remove breed info before inserting into pet table.

			final Time now = new Time();
			now.setToNow();
			final long nowMillisec = now.toMillis(true);
			values.put(FIELD_PET_CREATED_TIME, nowMillisec);
			values.put(FIELD_PET_MODIFIED_TIME, nowMillisec);
			values.put(FIELD_PET_FAVORITE, false);

			db_.beginTransaction();

			rowId = db_.insertOrThrow(TABLE_NAME_PETFINDER_PET, null, values);
			if (rowId > 0)
			{
				// TODO: Read:
				// http://developer.android.com/reference/android/content/Context.html#getExternalFilesDir%28java.lang.String%29

				final String thumbnailPathname = getThumbnailPathname(context_);
				final String thumbnailBaseFilename = getThumbnailBaseFilename(rowId);
				final String thumbnailPathFilename = BitmapDownloaderTask.getPathFilename(thumbnailPathname, thumbnailBaseFilename);

				values.put(_DATA, thumbnailPathFilename);

				final int rows = db_.update(TABLE_NAME_PETFINDER_PET, values, _ID + "=" + rowId, null);

				if (rows != 1)
				{
					throw new SQLException("Failed to update " + uri);
				}

				final String photoBaseUri = values.getAsString(FIELD_PET_PF_PHOTO_BASE_URI);
				if (!TextUtils.isEmpty(photoBaseUri))
				{
					final String thumbnailUri = PetRecord.getPhotoUri(photoBaseUri, 0, PetRecord.PHOTO_SIZE_FPM);
					if (thumbnailUri != null)
					{
						final BitmapDownloaderTask thumbnailDownloaderTask = new BitmapDownloaderTask(thumbnailPathname, thumbnailBaseFilename, 30);
						thumbnailDownloaderTask.execute(thumbnailUri);
						contentUri = CONTENT_URI_PET;
					}
				}

				final int end = breedIndex.length()-1;
				if (end > 1 && breedIndex.charAt(0)=='{' && breedIndex.charAt(end)=='}')
				{
					final String breeds = breedIndex.substring(1, end);
					if (breeds != null)
					{
						final List<String> breedList = Arrays.asList(breeds.split(","));

						for (final String breed: breedList)
						{
							final int i = Integer.valueOf(breed.trim());

							if (i>=0 && i<breedsColorsLength_)
							{
								final ContentValues breedValues = new ContentValues();

								breedValues.put(FIELD_BREED_PET_ID, rowId);
								breedValues.put(FIELD_BREED_PF_BREED_INDEX, i);

								final long breedRowId = db_.insertOrThrow(TABLE_NAME_PETFINDER_BREED, null, breedValues);
								if (breedRowId <= 0)
								{
									throw new SQLException("Failed to insert breed row " + rowId);
								}
							}
						}
					}
				}

				db_.setTransactionSuccessful();
				db_.endTransaction();
			}
			else
			{
				throw new SQLException("Failed to insert row into " + uri);
			}

			break;

		case MATCH_BREED_ALL_ROWS:
			// TODO: Test values fields and/or set to default?

			rowId = db_.insertOrThrow(TABLE_NAME_PETFINDER_BREED, null, values);

			if (rowId > 0)
			{
				contentUri = CONTENT_URI_BREED;
			}

			break;

		case MATCH_PET_ONE_ROW:
		case MATCH_BREED_ONE_ROW:
			throw new IllegalArgumentException("Invalid URI " + uri);
			// break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
			// break;
		}

		if (rowId > 0 && contentUri != null)
		{
			result = ContentUris.withAppendedId(contentUri, rowId);

			final ContentResolver contentResolver = context_.getContentResolver();
			contentResolver.notifyChange(result, null);
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate()
	{
		context_ = getContext();

		final Resources resources = context_.getResources();
		breedsColorsLength_ = resources.getStringArray(R.array.pf_breed_list_cat).length;

		dbOpenHelper_ = new DBOpenHelper(context_, DATABASE_NAME + ".db", null, DATABASE_VERSION);
		db_ = openRW(dbOpenHelper_);

		path_ = getThumbnailPathname(context_);
		final File dir = new File(path_);
		dir.mkdirs();

		final String photoCachePath = getPhotoCachePath(context_);
		final File filePhotoDir = new File(photoCachePath);
		filePhotoDir.mkdirs();

		final boolean result = (db_!=null);

		return result;
	}

	private void appendWhereId(final SQLiteQueryBuilder queryBuilder, final Uri uri)
	{
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
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		String orderBy = null;
		String tableName;

		final int match = sUriMatcher.match(uri);
		switch (match)
		{
		case MATCH_PET_ONE_ROW:
			appendWhereId(queryBuilder, uri);
		case MATCH_PET_ALL_ROWS:
			tableName = TABLE_NAME_PETFINDER_PET;
			orderBy = DEFAULT_ORDER_BY;
			break;

		case MATCH_BREED_ONE_ROW:
			appendWhereId(queryBuilder, uri);
		case MATCH_BREED_ALL_ROWS:
			tableName = TABLE_NAME_PETFINDER_BREED;
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
			// break;
		}

		queryBuilder.setTables(tableName);
		// queryBuilder.setStrict(true);
		// queryBuilder.setProjectionMap(sColumnMap);

		if (!TextUtils.isEmpty(sortOrder))
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
//			Log.e("PetfinderProvider", msg);
		}

		if (result != null)
		{
			final ContentResolver contentResolver = context_.getContentResolver();
			result.setNotificationUri(contentResolver, uri); // TODO: Why should query() call setNotificationUri()?
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

		String where = null;

		final int match = sUriMatcher.match(uri);
		switch (match)
		{
		case MATCH_PET_ONE_ROW:
		case MATCH_PET_ALL_ROWS:
			where = makeWhereClause(uri, selection);
			break;

		case MATCH_BREED_ONE_ROW:
		case MATCH_BREED_ALL_ROWS:
			throw new IllegalArgumentException("Invalid URI " + uri);
			// break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
			// break;
		}

		final Time now = new Time();
		now.setToNow();
		values.put(FIELD_PET_MODIFIED_TIME, now.toMillis(true));

		result = db_.update(TABLE_NAME_PETFINDER_PET, values, where, selectionArgs);

		if (result != 0)
		{
			final ContentResolver contentResolver = context_.getContentResolver();
			contentResolver.notifyChange(uri, null); // TODO: Why should update() call setNotificationUri()?
		}

		return result;
	}

	public static int updateFavorite(final ContentResolver contentResolver, final long id, final boolean isChecked)
	{
		final String where = PetfinderProvider._ID + "=" + id;
		final ContentValues values = new ContentValues();
		values.put(PetfinderProvider.FIELD_PET_FAVORITE, isChecked);
		final int result = contentResolver.update(PetfinderProvider.CONTENT_URI_PET, values, where, null);
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

	private static String getThumbnailPathname(final Context context)
	{
		String result = null;

		final Context ctx = Util.getSafeContext(context);
		// final File file = ctx.getFilesDir();
		// final String filepath = file.getPath();
		final String filepath = Util.getAppStorageDirectory(ctx);
		if (!TextUtils.isEmpty(filepath))
		{
			result = filepath + DATABASE_NAME + "/Pictures/thumbnail/";
		}
		return result;
	}

	private static String getThumbnailBaseFilename(final long rowId)
	{
		final String result = "pt" + rowId; // pt: Petfinder Thumbnail
		return result;
	}

//	private String dataPathFileName(final long rowId)
//	{
//		final String filename = getThumbnailBaseFilename(rowId);
//		final String result = path_ + filename;
//		return result;
//	}

	public static String getPhotoCachePath(final Context context)
	{
		String result = null;

		final Context ctx = Util.getSafeContext(context);
		final String filepath = Util.getAppStorageDirectory(ctx);
		if (!TextUtils.isEmpty(filepath))
		{
			result = filepath + DATABASE_NAME + "/Pictures/cache/";
		}

		return result;
	}

	public static String getPhotoCachePath(final Context context, final String shelterPetID)
	{
		final String result = PetfinderProvider.getPhotoCachePath(context) + "PetID_" + shelterPetID + "/";
		return result;
	}

	private void deletePhotoCache(final String shelterPetID)
	{
		final String photoPath = getPhotoCachePath(context_, shelterPetID);
		final File dir = new File(photoPath);
		Util.delDirs(dir);
	}

	private void deletePhotoCache()
	{
		final String photoPath = getPhotoCachePath(context_);
		final File dir = new File(photoPath);
		Util.delDirs(dir);
		dir.mkdirs(); // TODO: probably not needed here or in similar location of VideoProvider
	}

	private void deleteAll()
	{
		deletePhotoCache();

		dbOpenHelper_.truncate(db_);
		final File dir = new File(path_);
		Util.delDirs(dir);

		dir.mkdirs();
	}
}
