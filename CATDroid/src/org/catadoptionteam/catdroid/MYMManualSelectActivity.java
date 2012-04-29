/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * @author bessermt
 *
 */
public class MYMManualSelectActivity extends Activity
{
	private static final int FELINEALITY_CAPACITY = MYMSurvey.FELINEALITY_CAPACITY;

	private final int[] felineality_;

	private int felinealitySize_;

	private RadioGroup radioGroupFelineality_;

	private TextView textTitle_;
	private TextView textDesc_;
	private View viewDivider_;
	private TextView textScale_;
	private TextView textIndex_;

	private Button buttonOK_;

	private RadioGroup.OnCheckedChangeListener felinealityChangeListener = 
		new RadioGroup.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				selectFelineality(checkedId);
			}
		};

	private OnClickListener okClickListener = 
		new OnClickListener()
		{
			public void onClick(View v)
			{
				ok();
			}
		};

	/**
	 * 
	 */
	public MYMManualSelectActivity()
	{
		felineality_ = new int[FELINEALITY_CAPACITY];
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mym_manual_select);

		final Intent mymManualSelectIntent = getIntent();

		int score = mymManualSelectIntent.getIntExtra("score", -1);

		if (score < 3) // Should never happen.  
		{
			felinealitySize_ = FELINEALITY_CAPACITY;
			int i = FELINEALITY_CAPACITY;
			while (i != 0)
			{
				--i;
				felineality_[i] = i;
			}
		}
		else // Should always happen.  
		{
			felinealitySize_ = 0;
			int felineality = 0;
			while (score != 0)
			{
				final boolean bit = (score & 0x01) != 0;
				if (bit)
				{
					felineality_[felinealitySize_] = felineality;
					++felinealitySize_;
				}
				score >>= 1;
				++felineality;
			}
		}

		radioGroupFelineality_ = (RadioGroup)findViewById(R.id.radioGroupFelineality);

		for (int i = 0; i != felinealitySize_; ++i)
		{
			final int felineality = felineality_[i];

			final int felinealityTitle = MYMSurvey.getFelinealityTitleId(felineality);

			final RadioButton radioButtonFelineality = new RadioButton(this);

			radioButtonFelineality.setText(felinealityTitle);
			radioButtonFelineality.setId(i);

			radioGroupFelineality_.addView(radioButtonFelineality);
		}

		radioGroupFelineality_.setOnCheckedChangeListener(felinealityChangeListener);

		textTitle_ = (TextView)findViewById(R.id.textTitle);
		textDesc_ = (TextView)findViewById(R.id.textDesc);
		viewDivider_ = findViewById(R.id.viewDivider);
		textScale_ = (TextView)findViewById(R.id.textScale);
		textIndex_ = (TextView)findViewById(R.id.textIndex);

		final int radioButtonId = radioGroupFelineality_.getCheckedRadioButtonId();
		final int visibility = radioButtonId==-1 ? View.INVISIBLE:View.VISIBLE;
		viewDivider_.setVisibility(visibility);

		buttonOK_ = (Button)findViewById(R.id.buttonOK);
		buttonOK_.setEnabled(false);
		buttonOK_.setOnClickListener(okClickListener);
	}

	private void selectFelineality(final int i)
	{
		final int felineality = felineality_[i];

		final String scaleText = MYMSurvey.getScaleText(this, felineality);
		final int felinealityTitle = MYMSurvey.getFelinealityTitleId(felineality);
		final int felinealityDesc = MYMSurvey.getFelinealityDescriptionId(felineality);

		final String indexText = "#" + Integer.toString(i+1) + " of " + Integer.toString(felinealitySize_);

		textTitle_.setText(felinealityTitle);
		textDesc_.setText(felinealityDesc);
		viewDivider_.setVisibility(View.VISIBLE);
		textScale_.setText(scaleText);
		textIndex_.setText(indexText);

		buttonOK_.setEnabled(true);
	}

	private void ok()
	{
		Intent resultIntent = new Intent();

		final int index = radioGroupFelineality_.getCheckedRadioButtonId();
		final int felineality = felineality_[index];

		resultIntent.putExtra("felineality", felineality);

		setResult(RESULT_OK, resultIntent);

		finish();
	}
}
