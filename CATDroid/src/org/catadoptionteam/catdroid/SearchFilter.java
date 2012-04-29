/**
 * 
 */
package org.catadoptionteam.catdroid;

import org.json.JSONArray;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.Time;

/**
 * @author bessermt
 *
 */
public class SearchFilter
{
	private boolean dirty_;

	private static final String KEY_SEARCH_FILTER_SORT = "SEARCH_FILTER_SORT";
	public static final int SORT_ARRIVAL = 0;
	public static final int SORT_NAME = 1;
	public static final int SORT_RECENT = 2;
	private int sort_;

	private static final String KEY_SEARCH_FILTER_AGE = "SEARCH_FILTER_AGE";
	private static final int AGE_BABY_BIT   = 0x0001<<0;
	private static final int AGE_YOUNG_BIT  = 0x0001<<1;
	private static final int AGE_ADULT_BIT  = 0x0001<<2;
	private static final int AGE_SENIOR_BIT = 0x0001<<3;
	private boolean ageBaby_;
	private boolean ageYoung_;
	private boolean ageAdult_;
	private boolean ageSenior_;

	private static final String KEY_SEARCH_FILTER_SEX = "SEARCH_FILTER_SEX";
	public static final int SEX_MALE   = AppDBAdapter.SEX_MALE;
	public static final int SEX_FEMALE = AppDBAdapter.SEX_FEMALE;
	public static final int SEX_BOTH   = SEX_MALE | SEX_FEMALE;
	private int sex_;

	private static final String KEY_SEARCH_FILTER_SIZE = "SEARCH_FILTER_SIZE";
	private static final int SIZE_SMALL_BIT  = 0x0001<<0;
	private static final int SIZE_MEDIUM_BIT = 0x0001<<1;
	private static final int SIZE_LARGE_BIT  = 0x0001<<2;
	private static final int SIZE_XLARGE_BIT = 0x0001<<3;
	private boolean sizeSmall_;
	private boolean sizeMedium_;
	private boolean sizeLarge_;
	private boolean sizeXLarge_;

	private static final String KEY_SEARCH_FILTER_CAT_BREEDS_COLORS = "SEARCH_FILTER_CAT_BREEDS_COLORS";
	private boolean[] catBreedsColors_;

	private static final String KEY_SEARCH_FILTER_HAS = "SEARCH_FILTER_HAS";
	private static final int HAS_DOGS_BIT = 0x0001<<0;
	private static final int HAS_CATS_BIT = 0x0001<<1;
	private static final int HAS_KIDS_BIT = 0x0001<<2;
	private boolean hasCats_;
	private boolean hasDogs_;
	private boolean hasKids_;

	private static final String KEY_SEARCH_FILTER_SPECIAL_NEEDS = "SEARCH_FILTER_SPECIAL_NEEDS";
	public static final int SPECIAL_NEEDS_ONLY = 1;
	public static final int SPECIAL_NEEDS_NO   = 2;
	public static final int SPECIAL_NEEDS_YES  = 3;
	private int specialNeeds_;

	private static final String KEY_SEARCH_FILTER_DECLAWED = "SEARCH_FILTER_DECLAWED";
	public static final int DECLAWED_NO  = 1;
	public static final int DECLAWED_YES = 2;
	public static final int DECLAWED_ALL = 3;
	private int declawed_;

	private static final String KEY_SEARCH_FILTER_NOTIFY = "SEARCH_FILTER_NOTIFY";
	public static final int NOTIFY_NEW_MATCH = 0x0001<<0;
	private boolean notifyNewMatch_;

	private Context ctx_;

	private SharedPreferences preferences_;
	private SharedPreferences.Editor editor_;

	public SearchFilter(final Context context)
	{
		ctx_ = Util.getSafeContext(context);
		final Resources resources = ctx_.getResources();
		final int length = resources.getStringArray(R.array.pf_breed_list_cat).length;
		catBreedsColors_ = new boolean[length];

		preferences_ = PreferenceManager.getDefaultSharedPreferences(ctx_);
		load();
		editor_ = preferences_.edit();
	}

	private void clearBreedsColors()
	{
		int i = catBreedsColors_.length;
		while (i != 0)
		{
			--i;
			catBreedsColors_[i] = false;
		}
	}

	public void load()
	{
		sort_ = preferences_.getInt(KEY_SEARCH_FILTER_SORT, SORT_ARRIVAL);

		final int age = preferences_.getInt(KEY_SEARCH_FILTER_AGE, 0);
		ageBaby_ =   (age & AGE_BABY_BIT)  !=0;
		ageYoung_ =  (age & AGE_YOUNG_BIT) !=0;
		ageAdult_ =  (age & AGE_ADULT_BIT) !=0;
		ageSenior_ = (age & AGE_SENIOR_BIT)!=0;

		sex_ = preferences_.getInt(KEY_SEARCH_FILTER_SEX, SEX_BOTH);

		final int size = preferences_.getInt(KEY_SEARCH_FILTER_SIZE, 0);
		sizeSmall_  = (size & SIZE_SMALL_BIT) !=0;
		sizeMedium_ = (size & SIZE_MEDIUM_BIT)!=0;
		sizeLarge_  = (size & SIZE_LARGE_BIT) !=0;
		sizeXLarge_ = (size & SIZE_XLARGE_BIT)!=0;

		clearBreedsColors();
		final String catBreedsColorsStr = preferences_.getString(KEY_SEARCH_FILTER_CAT_BREEDS_COLORS, Util.EMPTY_STRING);
		if (!TextUtils.isEmpty(catBreedsColorsStr))
		{
			try
			{
				final JSONArray jsonArray = new JSONArray(catBreedsColorsStr);
				int i = catBreedsColors_.length;
				while (i != 0)
				{
					--i;
					catBreedsColors_[i] = jsonArray.getInt(i)!=0;
				}
			}
			catch (Throwable t)
			{
//				final String msg = t.getMessage();
//				Log.e("load() breeds and colors", msg);
			}
		}

		MYMSurvey mymSurvey_ = new MYMSurvey(ctx_);
		mymSurvey_.load();
		boolean mymSurveyHasCat = mymSurvey_.hasCat();
		boolean mymSurveyHasDog = mymSurvey_.hasDog();
		final int hasDefault = (mymSurveyHasCat?HAS_CATS_BIT:0) | (mymSurveyHasDog?HAS_DOGS_BIT:0);

		final int has = preferences_.getInt(KEY_SEARCH_FILTER_HAS, hasDefault);
		hasCats_ = (has & HAS_CATS_BIT)!=0;
		hasDogs_ = (has & HAS_DOGS_BIT)!=0;
		hasKids_ = (has & HAS_KIDS_BIT)!=0;

		specialNeeds_ = preferences_.getInt(KEY_SEARCH_FILTER_SPECIAL_NEEDS, SPECIAL_NEEDS_YES);

		declawed_ = preferences_.getInt(KEY_SEARCH_FILTER_DECLAWED, DECLAWED_ALL);

		final int notify = preferences_.getInt(KEY_SEARCH_FILTER_NOTIFY, 0);
		notifyNewMatch_ = (notify & NOTIFY_NEW_MATCH)!=0;

		dirty_ = false;
	}

	public void save()
	{
		editor_.putInt(KEY_SEARCH_FILTER_SORT, sort_);

		final int age = 
			(ageBaby_  ?AGE_BABY_BIT  :0)| 
			(ageYoung_ ?AGE_YOUNG_BIT :0)| 
			(ageAdult_ ?AGE_ADULT_BIT :0)| 
			(ageSenior_?AGE_SENIOR_BIT:0);
		editor_.putInt(KEY_SEARCH_FILTER_AGE, age);

		editor_.putInt(KEY_SEARCH_FILTER_SEX, sex_);

		final int size = 
			(sizeSmall_ ?SIZE_SMALL_BIT :0)| 
			(sizeMedium_?SIZE_MEDIUM_BIT:0)| 
			(sizeLarge_ ?SIZE_LARGE_BIT :0)| 
			(sizeXLarge_?SIZE_XLARGE_BIT:0);
		editor_.putInt(KEY_SEARCH_FILTER_SIZE, size);

		final JSONArray jsonArray = new JSONArray();
		for (final boolean breedColor: catBreedsColors_)
		{
			final int value = breedColor?1:0;
			jsonArray.put(value);
		}
		final String catBreedsColorsStr = jsonArray.toString();
		editor_.putString(KEY_SEARCH_FILTER_CAT_BREEDS_COLORS, catBreedsColorsStr);

		final int has = 
			(hasCats_?HAS_CATS_BIT:0)| 
			(hasDogs_?HAS_DOGS_BIT:0)| 
			(hasKids_?HAS_KIDS_BIT:0);
		editor_.putInt(KEY_SEARCH_FILTER_HAS, has);

		editor_.putInt(KEY_SEARCH_FILTER_SPECIAL_NEEDS, specialNeeds_);

		editor_.putInt(KEY_SEARCH_FILTER_DECLAWED, declawed_);

		final int notify = notifyNewMatch_?NOTIFY_NEW_MATCH:0;
		editor_.putInt(KEY_SEARCH_FILTER_NOTIFY, notify);

		editor_.commit();
	}

	protected String getWhere()
	{
		String result = null;

		final ConjunctionBuilder whereBuilder = new ConjunctionBuilder("AND");


		final ConjunctionBuilder ageBuilder = new ConjunctionBuilder(",");

		final boolean baby = getAgeBaby();
		final boolean young = getAgeYoung();
		final boolean adult = getAgeAdult();
		final boolean senior = getAgeSenior();

		if (baby) ageBuilder.append(PetfinderProvider.PetRecord.AGE_BABY);
		if (young) ageBuilder.append(PetfinderProvider.PetRecord.AGE_YOUNG);
		if (adult) ageBuilder.append(PetfinderProvider.PetRecord.AGE_ADULT);
		if (senior) ageBuilder.append(PetfinderProvider.PetRecord.AGE_SENIOR);

		if (!ageBuilder.isEmpty())
		{
			final String ageClause = PetfinderProvider.FIELD_PET_PF_AGE + " IN " + "("+ ageBuilder.toString() +")";
			whereBuilder.append(ageClause);
		}


		String sexClause = null;

		final int sex = getSex();
		switch (sex)
		{
		case SearchFilter.SEX_BOTH:
			break;

		case SearchFilter.SEX_MALE:
			sexClause = PetfinderProvider.FIELD_PET_PF_SEX + "==" + PetfinderProvider.PetRecord.SEX_MALE;
			break;

		case SearchFilter.SEX_FEMALE:
			sexClause = PetfinderProvider.FIELD_PET_PF_SEX + "==" + PetfinderProvider.PetRecord.SEX_FEMALE;
			break;

		default:
			break;
		}

		whereBuilder.append(sexClause);


		final ConjunctionBuilder sizeBuilder = new ConjunctionBuilder(",");

		final boolean small = getSizeSmall();
		final boolean medium = getSizeMedium();
		final boolean large = getSizeLarge();
		final boolean xlarge = getSizeXLarge();

		if (small) sizeBuilder.append(PetfinderProvider.PetRecord.SIZE_SMALL);
		if (medium) sizeBuilder.append(PetfinderProvider.PetRecord.SIZE_MEDIUM);
		if (large) sizeBuilder.append(PetfinderProvider.PetRecord.SIZE_LARGE);
		if (xlarge) sizeBuilder.append(PetfinderProvider.PetRecord.SIZE_XLARGE);

		if (!sizeBuilder.isEmpty())
		{
			final String sizeClause = PetfinderProvider.FIELD_PET_PF_SIZE + " IN " + "("+ sizeBuilder.toString() +")";
			whereBuilder.append(sizeClause);
		}


		final ConjunctionBuilder breedsColorsBuilder = new ConjunctionBuilder(",");

		final boolean[] breedsColors = getCatBreedsColors();
		int index = breedsColors.length;
		while (index != 0)
		{
			--index;
			if (breedsColors[index])
			{
				breedsColorsBuilder.append(index);
			}
		}

		if (!breedsColorsBuilder.isEmpty())
		{
			final String breedsColorsFilter = breedsColorsBuilder.toString();
			final String breedsColorsClause = 
				"EXISTS" + 
				" (" + 
					"SELECT NULL FROM " + PetfinderProvider.TABLE_NAME_PETFINDER_BREED + " WHERE " + 
					PetfinderProvider.FIELD_BREED_PET_ID + "=" + PetfinderProvider.TABLE_NAME_PETFINDER_PET + "." + PetfinderProvider._ID + " AND " + 
					PetfinderProvider.FIELD_BREED_PF_BREED_INDEX + " IN " + "("+ breedsColorsFilter + ")" + 
				")";

			whereBuilder.append(breedsColorsClause);
		}


		final boolean cats = getHasCats();
		final boolean dogs = getHasDogs();
		final boolean kids = getHasKids();

		if (cats) whereBuilder.append(PetfinderProvider.FIELD_PET_PF_NO_CATS + "==" + 0);
		if (dogs) whereBuilder.append(PetfinderProvider.FIELD_PET_PF_NO_DOGS + "==" + 0);
		if (kids) whereBuilder.append(PetfinderProvider.FIELD_PET_PF_NO_KIDS + "==" + 0);


		String specialNeedsClause = null;

		final int specialNeeds = getSpecialNeeds();
		switch (specialNeeds)
		{
		case SearchFilter.SPECIAL_NEEDS_YES:
			break;

		case SearchFilter.SPECIAL_NEEDS_NO:
			specialNeedsClause = PetfinderProvider.FIELD_PET_PF_SPECIAL_NEEDS + "==" + 0;
			break;

		case SearchFilter.SPECIAL_NEEDS_ONLY:
			specialNeedsClause = PetfinderProvider.FIELD_PET_PF_SPECIAL_NEEDS + "!=" + 0;
			break;

		default:
			break;
		}

		whereBuilder.append(specialNeedsClause);


		String declawedClause = null;

		final int declawed = getDeclawed();
		switch (declawed)
		{
		case SearchFilter.DECLAWED_ALL:
			break;

		case SearchFilter.DECLAWED_YES:
			declawedClause = PetfinderProvider.FIELD_PET_PF_NO_CLAWS + "!=" + 0;
			break;

		case SearchFilter.DECLAWED_NO:
			declawedClause = PetfinderProvider.FIELD_PET_PF_NO_CLAWS + "==" + 0;
			break;

		default:
			break;
		}

		whereBuilder.append(declawedClause);


		if (!whereBuilder.isEmpty())
		{
			result = whereBuilder.toString();
		}

		return result;
	}

	public boolean todayMatches()
	{
		boolean result = false;

		final String[] projection = new String[]
			{
				PetfinderProvider._ID
			};

		final ConjunctionBuilder whereBuilder = new ConjunctionBuilder("AND");

		final Time today = new Time();
		today.setToNow();
		final long todayMillisecs = today.toMillis(true)- Util.MILSEC_PER_DAY;
		final String updatedToday = PetfinderProvider.FIELD_PET_PF_LASTUPDATE + ">=" + String.valueOf(todayMillisecs);
		whereBuilder.append(updatedToday);

		final String whereSubClause = getWhere();
		whereBuilder.append(whereSubClause);

		final String where = whereBuilder.toString();

		final ContentResolver contentResolver = ctx_.getContentResolver();

		Cursor cursor = null;
		try
		{
			cursor = contentResolver.query(PetfinderProvider.CONTENT_URI_PET, projection, where, null, null);

			if (cursor != null)
			{
				if (cursor.moveToFirst())
				{
					result = cursor.getCount()>0;
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

	// get methods

	public boolean getDirty()
	{
		return dirty_;
	}

	public int getSort()
	{
		return sort_;
	}

	public boolean getAgeBaby()
	{
		return ageBaby_;
	}

	public boolean getAgeYoung()
	{
		return ageYoung_;
	}

	public boolean getAgeAdult()
	{
		return ageAdult_;
	}

	public boolean getAgeSenior()
	{
		return ageSenior_;
	}

	public int getSex()
	{
		return sex_;
	}

	public boolean getSizeSmall()
	{
		return sizeSmall_;
	}

	public boolean getSizeMedium()
	{
		return sizeMedium_;
	}

	public boolean getSizeLarge()
	{
		return sizeLarge_;
	}

	public boolean getSizeXLarge()
	{
		return sizeXLarge_;
	}

	public boolean[] getCatBreedsColors()
	{
		return catBreedsColors_;
	}

	public boolean getHasCats()
	{
		return hasCats_;
	}

	public boolean getHasDogs()
	{
		return hasDogs_;
	}

	public boolean getHasKids()
	{
		return hasKids_;
	}

	public int getSpecialNeeds()
	{
		return specialNeeds_;
	}

	public int getDeclawed()
	{
		return declawed_;
	}

	public boolean getNotifyNewMatch()
	{
		return notifyNewMatch_;
	}

	public String getSortOrderBy()
	{
		String result = PetfinderProvider.ORDER_BY_ARRIVAL;

		final int sort = getSort();
		switch (sort)
		{
		case SORT_ARRIVAL:
			result = PetfinderProvider.ORDER_BY_ARRIVAL;
			break;

		case SORT_NAME:
			result = PetfinderProvider.ORDER_BY_NAME;
			break;

		case SORT_RECENT:
			result = PetfinderProvider.ORDER_BY_RECENT;
			break;

		default:
			break;
		}

		return result;
	}


	// set methods

	public void setDirty(final boolean dirty)
	{
		dirty_ = dirty;
	}

	public void setSort(final int sort)
	{
		sort_ = sort;
	}

	public void setAge(final boolean baby, final boolean young, final boolean adult, final boolean senior)
	{
		ageBaby_ = baby;
		ageYoung_ = young;
		ageAdult_ = adult;
		ageSenior_ = senior;
	}

	public void setSex(final int sex)
	{
		sex_ = sex;
	}

	public void setSize(final boolean small, final boolean medium, final boolean large, final boolean xlarge)
	{
		sizeSmall_ = small;
		sizeMedium_ = medium;
		sizeLarge_ = large;
		sizeXLarge_ = xlarge;
	}

	public void setCatBreedsColors(final boolean[] catBreedsColors)
	{
		final int length = catBreedsColors_.length;
		if (catBreedsColors!=null && catBreedsColors.length == length)
		{
			clearBreedsColors();
			int i = length;
			while (i != 0)
			{
				--i;
				catBreedsColors_[i] = catBreedsColors[i];
			}
		}
	}

	public void setHas(final boolean cats, final boolean dogs, final boolean kids)
	{
		hasCats_ = cats;
		hasDogs_ = dogs;
		hasKids_ = kids;
	}

	public void setSpecialNeeds(final int specialNeeds)
	{
		specialNeeds_ = specialNeeds;
	}

	public void setDeclawed(final int declawed)
	{
		declawed_ = declawed;
	}

	public void setNotify(final boolean newMatch)
	{
		notifyNewMatch_ = newMatch;
	}
}
