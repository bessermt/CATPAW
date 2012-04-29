/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.text.format.Time;
import android.util.TimeFormatException;

/**
 * @author bessermt
 *
 */
public class Cat
{
	public static final String PET_ID_DELIMITER = "-"; // No need to have a delimiter on the petID if cats can't be both M9L and COTD.  

	private static final int PRIVATE_INVESTIGATOR = 0;
	private static final int SECRET_ADMIRER       = 1;
	private static final int LOVE_BUG             = 2;
	private static final int THE_EXECUTIVE        = 3;
	private static final int SIDEKICK             = 4;
	private static final int PERSONAL_ASSISTANT   = 5;
	private static final int MVP                  = 6;
	private static final int PARTY_ANIMAL         = 7;
	private static final int LEADER_OF_THE_BAND   = 8;

	private Context context_;

	private String petID_;
	private String name_;
	private int status_;
	private String species_;
	private String dob_;
	private int sex_;
	private int ASPCAality_;
	private String biography_;
	private String lgPhotoPathFilename_;
	private String smPhotoPathFilename_;
	private String extraID_;
	private String extraURL_;

	public Cat
	(
		final Context context, 
		final String petID, 
		final String name, 
		final int status, 
		final String species, 
		final String dob, 
		final int sex, 
		final int ASPCAality, 
		final String description, 
		final String lgPhotoBaseFilename, 
		final String smPhotoBaseFilename, 
		final String extraID, 
		final String extraURL
	)
	{
		context_ = context;

		petID_ = petID;
		name_ = name;
		status_ = status;
		species_ = species;
		dob_ = dob;
		sex_ = sex;
		ASPCAality_ = ASPCAality;
		biography_ = description;
		final String path = AppDBAdapter.getImagePath(context);
		lgPhotoPathFilename_ = path + lgPhotoBaseFilename + ".jpg";
		smPhotoPathFilename_ = path + smPhotoBaseFilename + ".jpg";
		extraID_ = extraID;
		extraURL_ = extraURL;
	}

	private static String getPetIDText(final Context context, final String petID)
	{
		String result = context.getString(R.string.unknown);
		if (petID != null)
		{
			final int delimiterIndex = petID.lastIndexOf(Cat.PET_ID_DELIMITER);
			if (delimiterIndex <= 0)
			{
				result = petID;
			}
			else
			{
				result = petID.substring(0, delimiterIndex);
			}
		}
		return result;
	}

	public static String getPhotoBaseFilename(final Context context, final String petID, final String postfix)
	{
		final String petIdText = getPetIDText(context, petID);
		final String result = petIdText + "_" + postfix;
		return result;
	}

	public String getPetID()
	{
		final String result = getPetIDText(context_, petID_);
		return result;
	}

	public String getName()
	{
		return name_;
	}

	public int getStatus()
	{
		return status_;
	}

	public String getSpecies()
	{
		return species_;
	}

	private String getDOB()
	{
		return dob_;
	}

	public String getAge()
	{
		int ageStringID = R.string.unknown_age;

		final Time nowDate = new Time();
		nowDate.setToNow();

		final String dob = getDOB();

		try
		{
			final Time dobDate = new Time();
			dobDate.parse3339(dob);

			final int years = nowDate.year - dobDate.year;
			final int months = nowDate.month - dobDate.month;
			final int days = nowDate.monthDay - dobDate.monthDay;

			final int partialMonth = Integer.signum(days) >= 0 ? 0 : 1;

			final int ageMonth = years * Util.MONTH_PER_YR + months - partialMonth;

			if (ageMonth >= 7*Util.MONTH_PER_YR)
			{
				ageStringID = R.string.senior;
			}
			else if (ageMonth >= Util.MONTH_PER_YR)
			{
				ageStringID = R.string.adult;
			}
			else if (ageMonth >= 9)
			{
				ageStringID = R.string.teenage;
			}
			else if (ageMonth >= 0)
			{
				ageStringID = R.string.kitten;
			}
		}
		catch (TimeFormatException e)
		{
		}

		final String result = context_.getString(ageStringID);

		return result;
	}

	public static String getSexText(final Context context, final int sex)
	{
		int resID = R.string.unknown_sex;

		switch (sex)
		{
		case AppDBAdapter.SEX_MALE:
			resID = R.string.male;
			break;

		case AppDBAdapter.SEX_FEMALE:
			resID = R.string.female;
			break;

		default:
			break;
		}

		final Context ctx = Util.getSafeContext(context);

		final String result = ctx.getString(resID);

		return result;
	}

//	public static String getSexText(final int sex)
//	{
//		final Context context = CATApp.getAppContext();
//		final String result = getSexText(context, sex);
//		return result;
//	}

	public String getSex()
	{
		final String result = getSexText(context_, sex_);
		return result;
	}

	public int getASPCAality()
	{
		final int result = ASPCAality_;
		return result;
	}

	public int getASPCAalityResID()
	{
		int result = R.string.unknown_felineality;

		switch (ASPCAality_)
		{
		case PRIVATE_INVESTIGATOR:
			result = R.string.v1_s1_pi_title;
			break;

		case SECRET_ADMIRER:
			result = R.string.v1_s2_admirer_title;
			break;

		case LOVE_BUG:
			result = R.string.v1_s3_lovebug_title;
			break;

		case THE_EXECUTIVE:
			result = R.string.v2_s1_executive_title;
			break;

		case SIDEKICK:
			result = R.string.v2_s2_sidekick_title;
			break;

		case PERSONAL_ASSISTANT:
			result = R.string.v2_s3_assistant_title;
			break;

		case MVP:
			result = R.string.v3_s1_mvp_title;
			break;

		case PARTY_ANIMAL:
			result = R.string.v3_s2_party_title;
			break;

		case LEADER_OF_THE_BAND:
			result = R.string.v3_s3_leader_title;
			break;

		default:
			break;
		}

		return result;
	}

	public String getASPCAalityString()
	{
		final int resID = getASPCAalityResID();

		final String result = context_.getString(resID);

		return result;
	}

	public int getASPCAalityColor()
	{
		final int colorRes = MYMSurvey.getFelinealityColorId(ASPCAality_);
		final Resources res = context_.getResources();
		final int result = res.getColor(colorRes);
		return result;
	}

	public String getBiography()
	{
		return biography_;
	}

	public String getLgPhotoPathFilename()
	{
		return lgPhotoPathFilename_;
	}

	public String getSmPhotoPathFilename()
	{
		return smPhotoPathFilename_;
	}

	public Bitmap getSmPhoto()
	{
		final String photoPathFilename = getSmPhotoPathFilename();
		final Context ctx = Util.getSafeContext(context_);
		final Bitmap result = Util.getBitmap(ctx, photoPathFilename);
		return result;
	}

	public String getExtraID()
	{
		return extraID_;
	}

	public String getExtraURL()
	{
		return extraURL_;
	}
}
