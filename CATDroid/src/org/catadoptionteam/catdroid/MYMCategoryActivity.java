/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * @author bessermt
 *
 */
public class MYMCategoryActivity extends Activity implements OnClickListener
{
	private static final int INVALID_FELINEALITY = -1;

	private int felineality_ = INVALID_FELINEALITY;

	private int titleBarResId_;

	private ImageButton buttonFelinealityIcon_;
	private TextView textFelinealityTitle_;
	private TextView textScale_;
	private TextView textFelinealityDesc_;
	private View viewDivider_;
	private TextView textASPCAAck_;
	private Button buttonMYMSurvey_;
	private Button buttonMYMChart_;
	private Button buttonClose_;

	/**
	 * 
	 */
	public MYMCategoryActivity()
	{
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mym_category);

		buttonFelinealityIcon_ = (ImageButton)findViewById(R.id.buttonFelinealityIcon);
		textFelinealityTitle_ = (TextView)findViewById(R.id.textFelinealityTitle);
		textScale_ = (TextView)findViewById(R.id.textScale);
		textFelinealityDesc_ = (TextView)findViewById(R.id.textFelinealityDesc);
		viewDivider_ = findViewById(R.id.viewDivider);
		textASPCAAck_ = (TextView) findViewById(R.id.textASPCAAck);
		buttonMYMSurvey_ = (Button) findViewById(R.id.buttonMYMSurvey);
		buttonMYMChart_ = (Button) findViewById(R.id.buttonMYMChart);
		buttonClose_ = (Button) findViewById(R.id.buttonClose);

		buttonMYMSurvey_.requestFocus();

		setListeners();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		initDisplay();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.buttonFelinealityIcon:
			{
				chart();
			}
			break;

			case R.id.buttonMYMSurvey:
			{
				survey();
			}
			break;

			case R.id.buttonMYMChart:
			{
				chart();
			}
			break;

			case R.id.buttonClose:
			{
				close();
			}
			break;

			default:
			{
				// TODO: Deal with diagnostics...
			}
			break;
		}
	}

	private void setListeners()
	{
		buttonFelinealityIcon_.setOnClickListener(this);
		buttonMYMSurvey_.setOnClickListener(this);
		buttonMYMChart_.setOnClickListener(this);
		buttonClose_.setOnClickListener(this);
	}

	private void updateFelineality(final int felineality)
	{
		if (felineality!=felineality_ && MYMSurvey.isFelineality(felineality))
		{
			final int icon = MYMSurvey.getFelinealityIconId(felineality);
			final int title = MYMSurvey.getFelinealityTitleId(felineality);
			final int colorRes = MYMSurvey.getFelinealityColorId(felineality);
			final int color = getResources().getColor(colorRes);
			final int description = MYMSurvey.getFelinealityDescriptionId(felineality);

			final String scaleText = MYMSurvey.getScaleText(this, felineality);

			viewDivider_.setBackgroundColor(color);

			buttonFelinealityIcon_.setImageResource(icon); // TODO:  Read documentation about this call.  It may not be a good idea.  

			textFelinealityTitle_.setText(title);
			textFelinealityTitle_.setTextColor(color);

			textScale_.setText(scaleText);
			textScale_.setBackgroundColor(color);

			textFelinealityDesc_.setText(description);

			felineality_ = felineality;
		}
	}

	private void initDisplay()
	{
		int surveyVisible = View.GONE;

		final Intent MYMCategoryIntent = getIntent();

		int felineality = MYMCategoryIntent.getIntExtra(MYMSurvey.FELINEALITY, INVALID_FELINEALITY);

		if (MYMSurvey.isFelineality(felineality))
		{
			titleBarResId_ = R.string.felineality_category;
		}
		else
		{
			titleBarResId_ = R.string.your_felineality;

			surveyVisible = View.VISIBLE;

			final MYMSurvey mymSurvey = new MYMSurvey(this);

			felineality = mymSurvey.loadFelineality();
		}

		setTitle(titleBarResId_);

		updateFelineality(felineality);

		buttonMYMSurvey_.setVisibility(surveyVisible);

		final MovementMethod movementMethod = LinkMovementMethod.getInstance();
		textFelinealityDesc_.setMovementMethod(movementMethod);

		textASPCAAck_.setMovementMethod(movementMethod);
	}

	private void close()
	{
		finish();
	}

	private void survey()
	{
//		Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show(); // TODO: Something like this might be nice.  
		final Intent mymSurveyIntent = new Intent(this, MYMSurveyActivity.class);
		Util.startActivity(this, mymSurveyIntent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO: Test why the image button is highlighted when chart returns with different selection from start.
		super.onActivityResult(requestCode, resultCode, data);
		if (titleBarResId_==R.string.your_felineality && resultCode == RESULT_OK && requestCode==MYMChartActivity.FELINEALITY_REQUEST)
		{
			final int felineality = data.getIntExtra(MYMChartActivity.FELINEALITY, INVALID_FELINEALITY);
			if (MYMSurvey.isFelineality(felineality) && felineality!=felineality_)
			{
				final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

				MYMSurvey.saveFelineality(this, preferences, felineality);
				updateFelineality(felineality);
			}
		}
	}

	private void chart()
	{
		final boolean returnResult = titleBarResId_==R.string.your_felineality;
		final Intent mymChartIntent = new Intent(this, MYMChartActivity.class);
		mymChartIntent.putExtra(MYMChartActivity.RETURN_RESULT, returnResult);
		mymChartIntent.putExtra(MYMChartActivity.FELINEALITY, felineality_);
		Util.startActivityForResult(this, mymChartIntent, MYMChartActivity.FELINEALITY_REQUEST);
	}
}
