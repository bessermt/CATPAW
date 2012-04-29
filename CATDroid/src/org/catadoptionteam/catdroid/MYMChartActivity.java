/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * @author bessermt
 *
 */
public class MYMChartActivity extends Activity implements OnClickListener, SeekBar.OnSeekBarChangeListener
{
	private static final int INVALID_FELINEALITY = -1;

	public static final String FELINEALITY = "felineality"; // TODO: Clean this by consolidating all uses of "felineality" to the MYMSurvey class.
	public static final String RETURN_RESULT = "return result";

	public static final int FELINEALITY_REQUEST = 0;

	private static final int[] ALPHA = {85, 85, 255};
	private static final int[] PADDING = {4, 4, 0};

	private static final int[] FELINEALITY_BUTTON_RES_ID = 
	{
		R.id.buttonv1s1, 
		R.id.buttonv1s2, 
		R.id.buttonv1s3, 
		R.id.buttonv2s1, 
		R.id.buttonv2s2, 
		R.id.buttonv2s3, 
		R.id.buttonv3s1, 
		R.id.buttonv3s2, 
		R.id.buttonv3s3
	};

	private static final int[] SOCIABILITY_RES_ID = 
	{
		R.id.textIndependent, 
		R.id.textSocial, 
		R.id.textGregarious
	};

	private static final int[] VALIANCE_RES_ID = 
	{
		R.id.textDiscreet, 
		R.id.textSensible, 
		R.id.textValiant
	};

	private int felineality_ = INVALID_FELINEALITY;

	private TextView textFelinealityTitle_;
	private TextView[] textSociability_;
	private TextView[] textValiance_;
	private SeekBar seekSociability_;
	private SeekBar seekValiance_;
	private ImageButton[] buttonFelineality_;

	private boolean returnResult_;
	private int seekSociabilityEnd_;
	private int seekValianceEnd_;
	private float scaleTextSize_;

	public MYMChartActivity()
	{
		textSociability_ = new TextView[SOCIABILITY_RES_ID.length];
		textValiance_ = new TextView[VALIANCE_RES_ID.length];
		buttonFelineality_ = new ImageButton[FELINEALITY_BUTTON_RES_ID.length];
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Test orientation changes. Chart crashes every so often when changing orientation. 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mym_chart);

		final Intent MYMCategoryIntent = getIntent();

		returnResult_ = MYMCategoryIntent.getBooleanExtra(RETURN_RESULT, false);

		init();

		final int intentFelineality = MYMCategoryIntent.getIntExtra(FELINEALITY, INVALID_FELINEALITY);

		setFelineality(intentFelineality);
	}

	@Override
	public void onClick(View v)
	{
		final int id = v.getId();
		switch (id)
		{
		case R.id.buttonOK:
			ok();
			break;

		case R.id.buttonCancel:
			cancel();
			break;

		default:
			{
				int felineality = FELINEALITY_BUTTON_RES_ID.length;
				while (felineality != 0)
				{
					--felineality;
					if (FELINEALITY_BUTTON_RES_ID[felineality] == id)
					{
						setFelineality(felineality);
						break;
					}
				}
			}
			break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		// super.onProgressChanged(seekBar, progress, fromUser);
//		if (fromUser) // TODO: Figure out why this doesn't work for the vertical SeekBar
		{
			final int seekSociabilityProgress = seekSociability_.getProgress();
			final int seekValianceProgress = seekValiance_.getProgress();

			final int sociability = (textSociability_.length*seekSociabilityProgress)/seekSociabilityEnd_;
			final int valiance = (textValiance_.length*seekValianceProgress)/seekValianceEnd_;

			final int felineality = textSociability_.length * valiance + sociability;

			setFelineality(felineality, false);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
		// super.onStartTrackingTouch(seekBar);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		// super.onStopTrackingTouch(seekBar);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

//		TODO: Test for memory leaks when switch orientation.  
// TODO: Test to see if I need to null member data when doing orientation changes.
//		textFelinealityTitle_ = null;
//		textSociability_ = null;
//		textValiance_ = null;
//		seekSociability_ = null;
//		seekValiance_ = null;
//		buttonFelineality_ = null;
		// ... and so on?
	}

	private void init()
	{
		textFelinealityTitle_ = (TextView) findViewById(R.id.textFelinealityTitle);

		int i;

		i = textSociability_.length;
		while (i != 0)
		{
			--i;
			textSociability_[i] = (TextView) findViewById(SOCIABILITY_RES_ID[i]);
		}

		i = textValiance_.length;
		while (i != 0)
		{
			--i;
			textValiance_[i] = (TextView) findViewById(VALIANCE_RES_ID[i]);
		}

		scaleTextSize_ = textSociability_[0].getTextSize();

		i = buttonFelineality_.length;
		while (i != 0)
		{
			--i;
			buttonFelineality_[i] = (ImageButton) findViewById(FELINEALITY_BUTTON_RES_ID[i]);
		}

		seekSociability_ = (SeekBar) findViewById(R.id.seekSociability);
		seekSociability_.setOnSeekBarChangeListener(this);
		seekSociabilityEnd_ = seekSociability_.getMax() + 1;

		seekValiance_ = (SeekBar) findViewById(R.id.seekValiance);
		seekValiance_.setOnSeekBarChangeListener(this);
		seekValianceEnd_ = seekValiance_.getMax() + 1;

		int felineality = FELINEALITY_BUTTON_RES_ID.length;
		while (felineality != 0)
		{
			--felineality;
			final ImageButton imageButton = (ImageButton) findViewById(FELINEALITY_BUTTON_RES_ID[felineality]);
			imageButton.setOnClickListener(this);
		}

		final Button buttonOK = (Button) findViewById(R.id.buttonOK);
		buttonOK.setOnClickListener(this);

		final Button buttonCancel = (Button) findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(this);
		if (!returnResult_)
		{
			buttonCancel.setVisibility(View.GONE);
		}
	}

	private int getSociability(final int felineality)
	{
		int result = 0;
		if (MYMSurvey.isFelineality(felineality))
		{
			result = felineality % textSociability_.length;
		}
		return result;
	}

	private int getValiance(final int felineality)
	{
		int result = 0;
		if (MYMSurvey.isFelineality(felineality))
		{
			result = felineality / textSociability_.length;
		}
		return result;
	}

	private void setFelineality(final int felineality, final boolean updateSeek)
	{
		// TODO: Enable ability to click felinealities if called from MYMCategoryActivity and Disable ability to click felinealities if called from BiographyActivity.
		if (MYMSurvey.isFelineality(felineality) && felineality != felineality_)
		{
			final int title = MYMSurvey.getFelinealityTitleId(felineality);

			final Resources resources = getResources();

			int colorResId;
			int color;

			colorResId = MYMSurvey.getFelinealityColorId(felineality);
			color = resources.getColor(colorResId);

			textFelinealityTitle_.setText(title);
			textFelinealityTitle_.setTextColor(color);

			float scaleTextSize;

			int pixelDelta = 0;
			if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT )
			{
				pixelDelta = 4;
			}

			final float scaleTextSizeSmall = scaleTextSize_ - pixelDelta;

			int i;

			final int sociability = getSociability(felineality);

			i = textSociability_.length;
			while (i != 0)
			{
				--i;
				final TextView textView = textSociability_[i];
				scaleTextSize = scaleTextSizeSmall;
				colorResId = R.color.silver;
				if (i == sociability)
				{
					colorResId = R.color.white;
					scaleTextSize = scaleTextSize_;
				}
				color = resources.getColor(colorResId);
				textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaleTextSize);
				textView.setTextColor(color);
			}

			final int valiance = getValiance(felineality);

			i = textValiance_.length;
			while (i != 0)
			{
				--i;
				final TextView textView = textValiance_[i];
				colorResId = R.color.silver;
				scaleTextSize = scaleTextSizeSmall;
				if (i == valiance)
				{
					colorResId = R.color.white;
					scaleTextSize = scaleTextSize_;
				}
				color = resources.getColor(colorResId);
				textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaleTextSize);
				textView.setTextColor(color);
			}

			final int sociabilityProgress = (sociability*seekSociabilityEnd_)/textSociability_.length + seekSociabilityEnd_/(2*textSociability_.length);
			final int valianceProgress = (valiance*seekValianceEnd_)/textValiance_.length + seekValianceEnd_/(2*textValiance_.length);

			seekSociability_.setSecondaryProgress(sociabilityProgress);
			seekValiance_.setSecondaryProgress(valianceProgress);

			if (updateSeek)
			{
				seekSociability_.setProgress(sociabilityProgress);
				seekValiance_.setProgress(valianceProgress);
			}

			i = buttonFelineality_.length;
			while (i != 0)
			{
				--i;
				final ImageButton buttonFelineality = buttonFelineality_[i];
				int alpha = ALPHA[0];
				int padding = PADDING[0];
				if (i == felineality)
				{
					alpha = ALPHA[2];
					padding = PADDING[2];
				}
				else if (getSociability(i)==getSociability(felineality) || getValiance(i)==getValiance(felineality))
				{
					alpha = ALPHA[1];
					padding = PADDING[1];
				}
				buttonFelineality.setPadding(padding, padding, padding, padding);
				buttonFelineality.setAlpha(alpha);
			}

			felineality_ = felineality;
		}
	}

	private void setFelineality(final int felineality)
	{
		setFelineality(felineality, true);
	}

	private void close(final boolean returnResult)
	{
		if (returnResult)
		{
			final Intent result = new Intent();
			result.putExtra(MYMChartActivity.FELINEALITY, felineality_);
			setResult(RESULT_OK, result);
		}
		finish();
	}

	private void ok()
	{
		close(returnResult_);
	}

	private void cancel()
	{
		close(false);
	}
}
