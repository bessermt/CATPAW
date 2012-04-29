/**
 * 
 */

package org.catadoptionteam.catdroid;

import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;

/**
 * @author bessermt
 *
 */

public class MYMSurvey
{
	public static final int INVALID_ID = View.NO_ID;

	public static final String FELINEALITY = "felineality";

	private static final String EMPTY_STRING = Util.EMPTY_STRING;

	private static final int TITLE = 0;
	private static final int ICON = 1;
	private static final int COLOR = 2;
	private static final int VALIANCE = 3;
	private static final int SOCIABILITY = 4;
	private static final int DESCRIPTION = 5;

	private static final int[][] FELINEALITY_ID = 
	{
		{R.string.v1_s1_pi_title,        R.drawable.v1_s1_pi,        R.color.MYM_purple, R.string.independent, R.string.discreet,  R.string.v1_s1_pi_desc       }, 
		{R.string.v1_s2_admirer_title,   R.drawable.v1_s2_admirer,   R.color.MYM_purple, R.string.social,      R.string.discreet,  R.string.v1_s2_admirer_desc  }, 
		{R.string.v1_s3_lovebug_title,   R.drawable.v1_s3_lovebug,   R.color.MYM_purple, R.string.gregarious,  R.string.discreet,  R.string.v1_s3_lovebug_desc  }, 
		{R.string.v2_s1_executive_title, R.drawable.v2_s1_executive, R.color.MYM_orange, R.string.independent, R.string.sensible,  R.string.v2_s1_executive_desc}, 
		{R.string.v2_s2_sidekick_title,  R.drawable.v2_s2_sidekick,  R.color.MYM_orange, R.string.social,      R.string.sensible,  R.string.v2_s2_sidekick_desc }, 
		{R.string.v2_s3_assistant_title, R.drawable.v2_s3_assistant, R.color.MYM_orange, R.string.gregarious,  R.string.sensible,  R.string.v2_s3_assistant_desc}, 
		{R.string.v3_s1_mvp_title,       R.drawable.v3_s1_mvp,       R.color.MYM_green,  R.string.independent, R.string.valiant,   R.string.v3_s1_mvp_desc      }, 
		{R.string.v3_s2_party_title,     R.drawable.v3_s2_party,     R.color.MYM_green,  R.string.social,      R.string.valiant,   R.string.v3_s2_party_desc    }, 
		{R.string.v3_s3_leader_title,    R.drawable.v3_s3_leader,    R.color.MYM_green,  R.string.gregarious,  R.string.valiant,   R.string.v3_s3_leader_desc   }
	};

	public static final int FELINEALITY_CAPACITY = FELINEALITY_ID.length;

	private static final int[] FELINEALITY_UNDECIDED_ID = 
	{
		R.string.undecided_title, 
		R.drawable.unknown_ality, 
		R.color.black, 
		INVALID_ID, 
		INVALID_ID, 
		R.string.undecided_desc
	};

	private static final String KEY_ANSWER = "MYM_ARRAY_ANSWER";
	private static final String KEY_ANSWER_9_OTHER = "MYM_ANSWER_9_OTHER";
	private static final String KEY_ANSWER_15_IMPORTANT = "MYM_ANSWER_15_IMPORTANT";

	private static final String KEY_FELINEALITY = "MYM_FELINEALITY";

	private static final int UNANSWERED_RADIOBUTTON = -1;
	private static final int UNANSWERED_CHECKBOX = 0;

	private static final int[] DEFAULT_ANSWER = 
	{
		UNANSWERED_RADIOBUTTON, 
		UNANSWERED_RADIOBUTTON, 
		UNANSWERED_RADIOBUTTON, 
		UNANSWERED_RADIOBUTTON, 
		UNANSWERED_RADIOBUTTON, 
		UNANSWERED_RADIOBUTTON, 
		UNANSWERED_RADIOBUTTON, 
		UNANSWERED_RADIOBUTTON, 
		UNANSWERED_RADIOBUTTON, 
		UNANSWERED_CHECKBOX, 
		UNANSWERED_RADIOBUTTON, 
		UNANSWERED_RADIOBUTTON, 
		UNANSWERED_RADIOBUTTON, 
		UNANSWERED_RADIOBUTTON, 
		UNANSWERED_RADIOBUTTON
	};

	private static final int DOG  = 0x0001<<0;
	private static final int CAT  = 0x0001<<1;
	private static final int BIRD = 0x0001<<2;

	private String answer9Other_;
	private String answer15Important_;
	private int[] answer_;

	private SharedPreferences preferences_;

	public MYMSurvey(final Context context)
	{
		preferences_ = PreferenceManager.getDefaultSharedPreferences(context);

		answer_ = new int[15];
		answer9Other_ = new String();
		answer15Important_ = new String();

		initAnswers();
	}

	private void initAnswers()
	{
		int i = answer_.length;
		while (i != 0)
		{
			--i;
			answer_[i] = DEFAULT_ANSWER[i];
		}

		answer9Other_ = EMPTY_STRING;
		answer15Important_ = EMPTY_STRING;
	}

	private static int getFelinealityId(final int felineality, final int type)
	{
		int result = INVALID_ID;

		if (isFelineality(felineality) && type>=0 && type<FELINEALITY_ID[felineality].length)
		{
			result = FELINEALITY_ID[felineality][type];
		}
		else
		{
			result = FELINEALITY_UNDECIDED_ID[type];
		}
		return result;
	}

	public static int getFelinealityTitleId(final int felineality)
	{
		final int result = getFelinealityId(felineality, TITLE);
		return result;
	}

	public static int getFelinealityIconId(final int felineality)
	{
		final int result = getFelinealityId(felineality, ICON);
		return result;
	}

	public static int getFelinealityColorId(final int felineality)
	{
		final int result = getFelinealityId(felineality, COLOR);
		return result;
	}

	private static int getFelinealityValianceId(final int felineality)
	{
		final int result = getFelinealityId(felineality, VALIANCE);
		return result;
	}

	private static int getFelinealitySociabilityId(final int felineality)
	{
		final int result = getFelinealityId(felineality, SOCIABILITY);
		return result;
	}

	public static String getScaleText(final Context context, final int felineality)
	{
		final Context ctx = Util.getSafeContext(context);
		String result = EMPTY_STRING;
		final int sociability = getFelinealitySociabilityId(felineality);
		final int valiance = getFelinealityValianceId(felineality);
		if (ctx!=null && sociability!=INVALID_ID && valiance!=INVALID_ID)
		{
			final String sociabilityText = ctx.getString(sociability);
			final String valianceText = ctx.getString(valiance);
			final String and = ctx.getString(R.string.and);
			result = valianceText + " " + and +" " + sociabilityText;
		}
		return result;
	}

	public static int getFelinealityDescriptionId(final int felineality)
	{
		final int result = getFelinealityId(felineality, DESCRIPTION);
		return result;
	}

	public int getAnswer(final int index)
	{
		final int result = answer_[index];

		return result;
	}

	private static int toAnimalsValue(final boolean dog, final boolean cat, final boolean bird)
	{
		final int result = (dog?DOG:0) | (cat?CAT:0) | (bird?BIRD:0);

		return result;
	}

	public void setAnimalAnswer(final boolean hasDog, final boolean hasCat, final boolean hasBird, final String otherAnimal)
	{
		final int animals = toAnimalsValue(hasDog, hasCat, hasBird);
		setAnswer(9, animals);
		answer9Other_ = otherAnimal;
	}

	public void setAnswer(final int index, final int value)
	{
		answer_[index] = value;
	}

	public boolean hasDog()
	{
		final boolean result = isAnimal(answer_[9], DOG);

		return result;
	}

	public boolean hasCat()
	{
		final boolean result = isAnimal(answer_[9], CAT);

		return result;
	}

	public boolean hasBird()
	{
		final boolean result = isAnimal(answer_[9], BIRD);

		return result;
	}

	private static boolean isAnimal(final int answer9, final int ANIMAL)
	{
		final boolean result = (answer9 & ANIMAL) != 0;

		return result;
	}

	public boolean hasOtherAnimal()
	{
		final boolean result = !TextUtils.isEmpty(answer9Other_);

		return result;
	}

	public String getOtherAnimal()
	{
		final String result = answer9Other_;

		return result;
	}

	public String getMostImportant()
	{
		final String result = answer15Important_;

		return result;
	}

	public void setMostImportant(final String importantValue)
	{
		answer15Important_ = importantValue;
	}

	public boolean isComplete()
	{
		boolean result = true;

		int i = 9;
		while (i != 0)
		{
			--i;

			final int answer = answer_[i];
			if (answer == UNANSWERED_RADIOBUTTON)
			{
				result = false;
				break;
			}
		}

		return result;
	}

	public boolean isAnswered(final int question)
	{
		boolean result = false;
		if (question>=0 && question<9 || question>=10 && question<15)
		{
			final int answer = answer_[question];
			result = answer!=UNANSWERED_RADIOBUTTON;
		}
		return result;
	}

	public void load()
	{
		initAnswers();

		try
		{
			Util.load(preferences_, KEY_ANSWER, answer_);
		}
		catch (JSONException e)
		{
		}

		answer9Other_ = preferences_.getString(KEY_ANSWER_9_OTHER, EMPTY_STRING);
		answer15Important_ = preferences_.getString(KEY_ANSWER_15_IMPORTANT, EMPTY_STRING);
	}

	public void save()
	{
		Util.save(preferences_, KEY_ANSWER, answer_);

		final SharedPreferences.Editor editor = preferences_.edit();

		editor.putString(KEY_ANSWER_9_OTHER, answer9Other_);
		editor.putString(KEY_ANSWER_15_IMPORTANT, answer15Important_);

		editor.commit();
	}

	public static int loadFelineality(final SharedPreferences preferences)
	{
		int result = -1;

		final int felineality = preferences.getInt(KEY_FELINEALITY, -1);
		if (isFelineality(felineality))
		{
			result = felineality;
		}
		return result;
	}

	public int loadFelineality()
	{
		int result = loadFelineality(preferences_);
		return result;
	}

	public static void saveFelineality(final Context context, final SharedPreferences preferences, final int felineality)
	{
		if (isFelineality(felineality))
		{
			final SharedPreferences.Editor editor = preferences.edit();
	
			editor.putInt(KEY_FELINEALITY, felineality);
	
			editor.commit();

			// Notify User
			final Context ctx = Util.getSafeContext(context);

			MainActivity.updateData(ctx);
		}
	}

	public void saveFelineality(final Context context, final int felineality)
	{
		saveFelineality(context, preferences_, felineality);
	}

	public static boolean isFelineality(final int felineality)
	{
		final boolean result = felineality>=0 && felineality<FELINEALITY_CAPACITY;
		return result;
	}

	public int score()
	{
		int result = 0;

		final int[] valiance = new int[3];
		final int valianceLength = valiance.length;

		for (int row = 0; row != 6; ++row)
		{
			final int answer = answer_[row];
			if (answer>=0 && answer<valianceLength)
			{
				++valiance[answer];
			}
		}

		final int[] sociability = new int[3];
		final int sociabilityLength = sociability.length;

		for (int row = 5; row != 9; ++row)
		{
			final int answer = answer_[row];
			if (answer>=0 && answer<sociabilityLength)
			{
				++sociability[answer];
			}
		}

		final int valiant = max_indicies(valiance);
		final int sociable = max_indicies(sociability);

		int v = valianceLength;
		while (v != 0)
		{
			--v;

			final boolean isValiant = (valiant & 1<<v) != 0;

			if (isValiant)
			{
				final int offset = v*sociabilityLength;

				int s = sociabilityLength;
				while (s != 0)
				{
					--s;

					final boolean isSociable = (sociable & 1<<s) != 0;

					if (isSociable)
					{
						final int i = offset+s;

						result |= 1<<i;
					}
				}
			}
		}

		return result;
	}

	private static int max_indicies(final int[] array)
	{
		int result = 0;

		int max = Integer.MIN_VALUE;
		int index = array.length;
		while (index != 0)
		{
			--index;
			final int value = array[index];
			final int bit = 1 << index;

			if (value > max)
			{
				max = value;
				result = bit;
			}
			else if (value == max)
			{
				result |= bit;
			}
		}

		return result;
	}
}
