/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

/**
 * @author bessermt
 *
 */
public class SearchFilterActivity extends Activity implements OnClickListener
{
	private static final int DIALOG_BREEDS_COLORS_ID = 1;

	private static SearchFilter searchFilter_;

	// private static final int SORT[] = new int[] {R.id.radioButtonSortArrival, R.id.radioButtonSortName, R.id.radioButtonSortRecent};
	private RadioGroup radioGroupSort_;
	// private RadioButton radioButtonSortArrival_;
	// private RadioButton radioButtonSortName_;
	// private RadioButton radioButtonSortRecent_;

	private TextView textAge_;
	private CheckBox checkBoxAgeBaby_;
	private CheckBox checkBoxAgeYoung_;
	private CheckBox checkBoxAgeAdult_;
	private CheckBox checkBoxAgeSenior_;

	// private static final int SEX[] = new int[] {R.id.radioButtonSexBoth, R.id.radioButtonSexMale, R.id.radioButtonSexFemale};
	private RadioGroup radioGroupSex_;
	// private RadioButton radioButtonSexBoth_;
	// private RadioButton radioButtonSexMale_;
	// private RadioButton radioButtonSexFemale_;

	private TextView textSize_;
	private CheckBox checkBoxSizeSmall_;
	private CheckBox checkBoxSizeMedium_;
	private CheckBox checkBoxSizeLarge_;
	private CheckBox checkBoxSizeXLarge_;

	private boolean[] catBreedsColorsChecked_ = null;
	private Button buttonBreedsColors_;

	private TextView textIHave_;
	private CheckBox checkBoxIHaveCats_;
	private CheckBox checkBoxIHaveDogs_;
	private CheckBox checkBoxIHaveKids_;

	// private static final int SPECIAL_NEEDS[] = new int[] {R.id.radioButtonSpecialNeedsOnly, R.id.radioButtonSpecialNeedsNo, R.id.radioButtonSpecialNeedsYes};
	private RadioGroup radioGroupSpecialNeeds_;
	// private RadioButton radioButtonSpecialNeedsYes_;
	// private RadioButton radioButtonSpecialNeedsNo_;
	// private RadioButton radioButtonSpecialNeedsOnly_;

	// private static final int DECLAWED[] = new int[] {R.id.radioButtonDeclawedNo, R.id.radioButtonDeclawedYes, R.id.radioButtonDeclawedAll};
	private RadioGroup radioGroupDeclawed_;
	// private RadioButton radioButtonDeclawedAll_;
	// private RadioButton radioButtonDeclawedYes_;
	// private RadioButton radioButtonDeclawedNo_;

	private CheckBox checkBoxNotify_;

	private TabHost tabHost_ = null;

	private Button buttonMyMatches_;

	OnClickListener ageClickListener_ = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			setDirty();
			setTextAgeEnabled();
		}
	};

	OnClickListener sizeClickListener_ = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			setDirty();
			setTextSizeEnabled();
		}
	};

	OnClickListener iHaveClickListener_ = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			setDirty();
			setTextIHaveEnabled();
		}
	};

	OnClickListener notifyClickListener_ = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			setDirty();
		}
	};

	OnCheckedChangeListener radioGroupChangeListener_ = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId)
		{
			setDirty();
		}
	};

	private static void initSearchFilter(final Context context)
	{
		if (searchFilter_ == null)
		{
			final Context ctx = Util.getSafeContext(context);
			searchFilter_ = new SearchFilter(ctx);
		}
	}

	public static SearchFilter getSearchFilter(final Context context)
	{
		initSearchFilter(context);
		return searchFilter_;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_filter);

		initSearchFilter(this);

		radioGroupSort_ = (RadioGroup)findViewById(R.id.radioGroupSort);
		// radioButtonSortArrival_ = (RadioButton)findViewById(R.id.radioButtonSortArrival);
		// radioButtonSortName_ = (RadioButton)findViewById(R.id.radioButtonSortName);
		// radioButtonSortRecent_ = (RadioButton)findViewById(R.id.radioButtonSortRecent);
		radioGroupSort_.setOnCheckedChangeListener(radioGroupChangeListener_);

		textAge_ = (TextView)findViewById(R.id.textAge);
		checkBoxAgeBaby_ = (CheckBox)findViewById(R.id.checkBoxAgeBaby);
		checkBoxAgeYoung_ = (CheckBox)findViewById(R.id.checkBoxAgeYoung);
		checkBoxAgeAdult_ = (CheckBox)findViewById(R.id.checkBoxAgeAdult);
		checkBoxAgeSenior_ = (CheckBox)findViewById(R.id.checkBoxAgeSenior);
		checkBoxAgeBaby_.setOnClickListener(ageClickListener_);
		checkBoxAgeYoung_.setOnClickListener(ageClickListener_);
		checkBoxAgeAdult_.setOnClickListener(ageClickListener_);
		checkBoxAgeSenior_.setOnClickListener(ageClickListener_);

		radioGroupSex_ = (RadioGroup)findViewById(R.id.radioGroupSex);
		// radioButtonSexBoth_ = (RadioButton)findViewById(R.id.radioButtonSexBoth);
		// radioButtonSexMale_ = (RadioButton)findViewById(R.id.radioButtonSexMale);
		// radioButtonSexFemale_ = (RadioButton)findViewById(R.id.radioButtonSexFemale);
		radioGroupSex_.setOnCheckedChangeListener(radioGroupChangeListener_);

		textSize_ = (TextView)findViewById(R.id.textSize);
		checkBoxSizeSmall_ = (CheckBox)findViewById(R.id.checkBoxSizeSmall);
		checkBoxSizeMedium_ = (CheckBox)findViewById(R.id.checkBoxSizeMedium);
		checkBoxSizeLarge_ = (CheckBox)findViewById(R.id.checkBoxSizeLarge);
		checkBoxSizeXLarge_ = (CheckBox)findViewById(R.id.checkBoxSizeXLarge);
		checkBoxSizeSmall_.setOnClickListener(sizeClickListener_);
		checkBoxSizeMedium_.setOnClickListener(sizeClickListener_);
		checkBoxSizeLarge_.setOnClickListener(sizeClickListener_);
		checkBoxSizeXLarge_.setOnClickListener(sizeClickListener_);

		final Resources resources = getResources();

		final int catBreedsColorsLength = resources.getStringArray(R.array.pf_breed_list_cat).length;
		catBreedsColorsChecked_ = new boolean[catBreedsColorsLength];
		buttonBreedsColors_ = (Button) findViewById(R.id.buttonBreedsColors);
		buttonBreedsColors_.setOnClickListener(this);

		textIHave_ = (TextView)findViewById(R.id.textIHave);
		checkBoxIHaveCats_ = (CheckBox)findViewById(R.id.checkBoxIHaveCats);
		checkBoxIHaveDogs_ = (CheckBox)findViewById(R.id.checkBoxIHaveDogs);
		checkBoxIHaveKids_ = (CheckBox)findViewById(R.id.checkBoxIHaveKids);
		checkBoxIHaveCats_.setOnClickListener(iHaveClickListener_);
		checkBoxIHaveDogs_.setOnClickListener(iHaveClickListener_);
		checkBoxIHaveKids_.setOnClickListener(iHaveClickListener_);

		radioGroupSpecialNeeds_ = (RadioGroup)findViewById(R.id.radioGroupSpecialNeeds);
		// radioButtonSpecialNeedsYes_ = (RadioButton)findViewById(R.id.radioButtonSpecialNeedsYes);
		// radioButtonSpecialNeedsNo_ = (RadioButton)findViewById(R.id.radioButtonSpecialNeedsNo);
		// radioButtonSpecialNeedsOnly_ = (RadioButton)findViewById(R.id.radioButtonSpecialNeedsOnly);
		radioGroupSpecialNeeds_.setOnCheckedChangeListener(radioGroupChangeListener_);

		radioGroupDeclawed_ = (RadioGroup)findViewById(R.id.radioGroupDeclawed);
		// radioButtonDeclawedAll_ = (RadioButton)findViewById(R.id.radioButtonDeclawedAll);
		// radioButtonDeclawedYes_ = (RadioButton)findViewById(R.id.radioButtonDeclawedYes);
		// radioButtonDeclawedNo_ = (RadioButton)findViewById(R.id.radioButtonDeclawedNo);
		radioGroupDeclawed_.setOnCheckedChangeListener(radioGroupChangeListener_);

		checkBoxNotify_ = (CheckBox)findViewById(R.id.checkBoxNotify);
		checkBoxNotify_.setOnClickListener(notifyClickListener_);

		buttonMyMatches_ = (Button)findViewById(R.id.buttonMyMatches);
		buttonMyMatches_.setVisibility(View.GONE);
		buttonMyMatches_.setOnClickListener(this);
		final TabActivity tabActivity = (TabActivity) getParent();
		if (tabActivity != null)
		{
			tabHost_ =  (TabHost) tabActivity.findViewById(android.R.id.tabhost);
			if (tabHost_ != null)
			{
				buttonMyMatches_.setVisibility(View.VISIBLE);
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		initDisplay();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
		saveDisplay();
		if (searchFilter_ != null)
		{
			searchFilter_.save();
		}
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.buttonBreedsColors:
			{
				setDirty();
				breedsColors();
			}
			break;

			case R.id.buttonMyMatches:
			{
				tabHost_.setCurrentTab(2);
			}
			break;

			default:
			{
				// TODO: Deal with diagnostics...
			}
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		Dialog result;

		switch(id)
		{
		case DIALOG_BREEDS_COLORS_ID:
			result = createBreedsColorsDialog();
			break;

		default:
			result = super.onCreateDialog(id);
		}

		return result;
	}

	private void setDirty()
	{
		searchFilter_.setDirty(true);
	}

	private void initDisplay()
	{
		initSort();
		initAge();
		initSex();
		initSize();
		initBreedsColors();
		initIHave();
		initSpecialNeeds();
		initDeclawed();
		initNotify();
	}

	private void saveDisplay()
	{
		saveSort();
		saveAge();
		saveSex();
		saveSize();
		saveBreedsColors();
		saveIHave();
		saveSpecialNeeds();
		saveDeclawed();
		saveNotify();
	}

	private void initSort()
	{
		int id = R.id.radioButtonSortArrival;

		final int sort = searchFilter_.getSort();
		switch (sort)
		{
		case SearchFilter.SORT_ARRIVAL:
			id = R.id.radioButtonSortArrival;
			break;

		case SearchFilter.SORT_NAME:
			id = R.id.radioButtonSortName;
			break;

		case SearchFilter.SORT_RECENT:
			id = R.id.radioButtonSortRecent;
			break;

		default:
			break;
		}

		radioGroupSort_.check(id);
	}

	private void saveSort()
	{
		int sort = SearchFilter.SORT_ARRIVAL;

		final int id = radioGroupSort_.getCheckedRadioButtonId();
		switch (id)
		{
		case R.id.radioButtonSortArrival:
			sort = SearchFilter.SORT_ARRIVAL;
			break;

		case R.id.radioButtonSortName:
			sort = SearchFilter.SORT_NAME;
			break;

		case R.id.radioButtonSortRecent:
			sort = SearchFilter.SORT_RECENT;
			break;

		default:
			break;
		}

		searchFilter_.setSort(sort);
	}

	private boolean isAgeFilterEnabled()
	{
		final boolean babyChecked = checkBoxAgeBaby_.isChecked();
		final boolean youngChecked = checkBoxAgeYoung_.isChecked();
		final boolean adultChecked = checkBoxAgeAdult_.isChecked();
		final boolean seniorChecked = checkBoxAgeSenior_.isChecked();
		final boolean result = babyChecked || youngChecked || adultChecked || seniorChecked;
		return result;
	}

	private void setTextAgeEnabled()
	{
		final boolean enabled = isAgeFilterEnabled();
		textAge_.setEnabled(enabled);
	}

	private void initAge()
	{
		final boolean baby = searchFilter_.getAgeBaby();
		final boolean young = searchFilter_.getAgeYoung();
		final boolean adult = searchFilter_.getAgeAdult();
		final boolean senior = searchFilter_.getAgeSenior();

		checkBoxAgeBaby_.setChecked(baby);
		checkBoxAgeYoung_.setChecked(young);
		checkBoxAgeAdult_.setChecked(adult);
		checkBoxAgeSenior_.setChecked(senior);

		setTextAgeEnabled();
	}

	private void saveAge()
	{
		final boolean baby = checkBoxAgeBaby_.isChecked();
		final boolean young = checkBoxAgeYoung_.isChecked();
		final boolean adult = checkBoxAgeAdult_.isChecked();
		final boolean senior = checkBoxAgeSenior_.isChecked();

		searchFilter_.setAge(baby, young, adult, senior);
	}

	private void initSex()
	{
		int id = R.id.radioButtonSexBoth;

		final int sex = searchFilter_.getSex();
		switch (sex)
		{
		case SearchFilter.SEX_BOTH:
			id = R.id.radioButtonSexBoth;
			break;

		case SearchFilter.SEX_MALE:
			id = R.id.radioButtonSexMale;
			break;

		case SearchFilter.SEX_FEMALE:
			id = R.id.radioButtonSexFemale;
			break;

		default:
			break;
		}

		radioGroupSex_.check(id);
	}

	private void saveSex()
	{
		int sex = SearchFilter.SEX_BOTH;

		final int id = radioGroupSex_.getCheckedRadioButtonId();
		switch (id)
		{
		case R.id.radioButtonSexBoth:
			sex = SearchFilter.SEX_BOTH;
			break;

		case R.id.radioButtonSexMale:
			sex = SearchFilter.SEX_MALE;
			break;

		case R.id.radioButtonSexFemale:
			sex = SearchFilter.SEX_FEMALE;
			break;

		default:
			break;
		}

		searchFilter_.setSex(sex);
	}

	private boolean isSizeFilterEnabled()
	{
		final boolean small = checkBoxSizeSmall_.isChecked();
		final boolean medium = checkBoxSizeMedium_.isChecked();
		final boolean large = checkBoxSizeLarge_.isChecked();
		final boolean xlarge = checkBoxSizeXLarge_.isChecked();
		final boolean result = small || medium || large || xlarge;
		return result;
	}

	private void setTextSizeEnabled()
	{
		final boolean enabled = isSizeFilterEnabled();
		textSize_.setEnabled(enabled);
	}

	private void initSize()
	{
		final boolean small = searchFilter_.getSizeSmall();
		final boolean medium = searchFilter_.getSizeMedium();
		final boolean large = searchFilter_.getSizeLarge();
		final boolean xlarge = searchFilter_.getSizeXLarge();

		checkBoxSizeSmall_.setChecked(small);
		checkBoxSizeMedium_.setChecked(medium);
		checkBoxSizeLarge_.setChecked(large);
		checkBoxSizeXLarge_.setChecked(xlarge);

		setTextSizeEnabled();
	}

	private void saveSize()
	{
		final boolean small = checkBoxSizeSmall_.isChecked();
		final boolean medium = checkBoxSizeMedium_.isChecked();
		final boolean large = checkBoxSizeLarge_.isChecked();
		final boolean xlarge = checkBoxSizeXLarge_.isChecked();

		searchFilter_.setSize(small, medium, large, xlarge);
	}

	private AlertDialog createBreedsColorsDialog()
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.breeds_colors);
		builder.setPositiveButton
		(
			R.string.ok, 
			new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					dialog.dismiss();
				}
			}
		);
		builder.setNeutralButton
		(
			R.string.clear, 
			new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					final AlertDialog alertDialog = (AlertDialog) dialog;
					if (alertDialog != null)
					{
						final ListView listView = alertDialog.getListView();
						if (listView != null)
						{
							int i = catBreedsColorsChecked_.length;
							while (i != 0)
							{
								--i;
								catBreedsColorsChecked_[i] = false;
								listView.setItemChecked(i, false);
							}
						}
					}
					dialog.dismiss();
				}
			}
		);

		final DialogInterface.OnMultiChoiceClickListener clickListener = 
			new DialogInterface.OnMultiChoiceClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked)
				{
					catBreedsColorsChecked_[which] = isChecked;
				}
			};
		builder.setMultiChoiceItems(R.array.pf_breed_list_text_cat, catBreedsColorsChecked_, clickListener);

		final DialogInterface.OnDismissListener dismissListener =
			new DialogInterface.OnDismissListener()
			{
				@Override
				public void onDismiss(DialogInterface dialog)
				{
					initBreedsColorsCount();
				}
			};

		final DialogInterface.OnCancelListener cancelListener =
			new DialogInterface.OnCancelListener()
			{
				@Override
				public void onCancel(DialogInterface dialog)
				{
					initBreedsColorsCount();
				}
			};

		AlertDialog result = builder.create();

		result.setOnDismissListener(dismissListener);
		result.setOnCancelListener(cancelListener);

		return result;
	}

	private int getBreedsColorsCount()
	{
		int result = 0;
		for (final boolean selected: catBreedsColorsChecked_)
		{
			if (selected)
			{
				++result;
			}
		}
		return result;
	}

	private void initBreedsColorsCount()
	{
		String text;

		final int count = getBreedsColorsCount();

		if (count == 0)
		{
			text = getString(R.string.all);
		}
		else
		{
			text = String.valueOf(count);
		}

		text += " " + getString(R.string.breeds_colors);

		buttonBreedsColors_.setText(text);
	}

	private void breedsColors()
	{
		showDialog(DIALOG_BREEDS_COLORS_ID);
	}

	private void initBreedsColors()
	{
		final boolean catBreedsColors[] = searchFilter_.getCatBreedsColors();
		final int length = catBreedsColorsChecked_.length;
		if (catBreedsColors!=null && catBreedsColors.length == length)
		{
			int i = length;
			while (i != 0)
			{
				--i;
				catBreedsColorsChecked_[i] = catBreedsColors[i];
			}
		}
		initBreedsColorsCount();
	}

	private void saveBreedsColors()
	{
		searchFilter_.setCatBreedsColors(catBreedsColorsChecked_);
	}

	private boolean isIHaveFilterEnabled()
	{
		final boolean cats = checkBoxIHaveCats_.isChecked();
		final boolean dogs = checkBoxIHaveDogs_.isChecked();
		final boolean kids = checkBoxIHaveKids_.isChecked();
		final boolean result = cats || dogs || kids;
		return result;
	}

	private void setTextIHaveEnabled()
	{
		final boolean enabled = isIHaveFilterEnabled();
		textIHave_.setEnabled(enabled);
	}

	private void initIHave()
	{
		final boolean cats = searchFilter_.getHasCats();
		final boolean dogs = searchFilter_.getHasDogs();
		final boolean kids = searchFilter_.getHasKids();

		checkBoxIHaveCats_.setChecked(cats);
		checkBoxIHaveDogs_.setChecked(dogs);
		checkBoxIHaveKids_.setChecked(kids);

		setTextIHaveEnabled();
	}

	private void saveIHave()
	{
		final boolean cats = checkBoxIHaveCats_.isChecked();
		final boolean dogs = checkBoxIHaveDogs_.isChecked();
		final boolean kids = checkBoxIHaveKids_.isChecked();

		searchFilter_.setHas(cats, dogs, kids);
	}

	private void initSpecialNeeds()
	{
		int id = R.id.radioButtonSpecialNeedsYes;

		final int specialNeeds = searchFilter_.getSpecialNeeds();
		switch (specialNeeds)
		{
		case SearchFilter.SPECIAL_NEEDS_YES:
			id = R.id.radioButtonSpecialNeedsYes;
			break;

		case SearchFilter.SPECIAL_NEEDS_NO:
			id = R.id.radioButtonSpecialNeedsNo;
			break;

		case SearchFilter.SPECIAL_NEEDS_ONLY:
			id = R.id.radioButtonSpecialNeedsOnly;
			break;

		default:
			break;
		}

		radioGroupSpecialNeeds_.check(id);
	}

	private void saveSpecialNeeds()
	{
		int specialNeeds = SearchFilter.SPECIAL_NEEDS_YES;

		final int id = radioGroupSpecialNeeds_.getCheckedRadioButtonId();
		switch (id)
		{
		case R.id.radioButtonSpecialNeedsYes:
			specialNeeds = SearchFilter.SPECIAL_NEEDS_YES;
			break;

		case R.id.radioButtonSpecialNeedsNo:
			specialNeeds = SearchFilter.SPECIAL_NEEDS_NO;
			break;

		case R.id.radioButtonSpecialNeedsOnly:
			specialNeeds = SearchFilter.SPECIAL_NEEDS_ONLY;
			break;

		default:
			break;
		}

		searchFilter_.setSpecialNeeds(specialNeeds);
	}

	private void initDeclawed()
	{
		int id = R.id.radioButtonDeclawedAll;

		final int declawed = searchFilter_.getDeclawed();
		switch (declawed)
		{
		case SearchFilter.DECLAWED_ALL:
			id = R.id.radioButtonDeclawedAll;
			break;

		case SearchFilter.DECLAWED_YES:
			id = R.id.radioButtonDeclawedYes;
			break;

		case SearchFilter.DECLAWED_NO:
			id = R.id.radioButtonDeclawedNo;
			break;

		default:
			break;
		}

		radioGroupDeclawed_.check(id);
	}

	private void saveDeclawed()
	{
		int declawed = SearchFilter.DECLAWED_ALL;

		final int id = radioGroupDeclawed_.getCheckedRadioButtonId();
		switch (id)
		{
		case R.id.radioButtonDeclawedAll:
			declawed = SearchFilter.DECLAWED_ALL;
			break;

		case R.id.radioButtonDeclawedYes:
			declawed = SearchFilter.DECLAWED_YES;
			break;

		case R.id.radioButtonDeclawedNo:
			declawed = SearchFilter.DECLAWED_NO;
			break;

		default:
			break;
		}

		searchFilter_.setDeclawed(declawed);
	}

	private void initNotify()
	{
		final boolean newMatch = searchFilter_.getNotifyNewMatch();

		checkBoxNotify_.setChecked(newMatch);
	}

	private void saveNotify()
	{
		final boolean newMatch = checkBoxNotify_.isChecked();

		searchFilter_.setNotify(newMatch);
	}
}
