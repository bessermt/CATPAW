/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * @author bessermt
 *
 */

public class MYMSurveyActivity extends Activity
{
	private static final int INVALID_ID = MYMSurvey.INVALID_ID;

	private static final int[][] RADIO_ANSWER_ID = 
	{
		{ R.id.q1a1,  R.id.q1a2,  R.id.q1a3, INVALID_ID}, 
		{ R.id.q2a1,  R.id.q2a2,  R.id.q2a3, INVALID_ID}, 
		{ R.id.q3a1,  R.id.q3a2,  R.id.q3a3, INVALID_ID}, 
		{ R.id.q4a1,  R.id.q4a2,  R.id.q4a3, INVALID_ID}, 
		{ R.id.q5a1,  R.id.q5a2,  R.id.q5a3, INVALID_ID}, 
		{ R.id.q6a1,  R.id.q6a2,  R.id.q6a3,  R.id.q6a4}, 

		{ R.id.q7a1,  R.id.q7a2,  R.id.q7a3, INVALID_ID}, 
		{ R.id.q8a1,  R.id.q8a2,  R.id.q8a3, INVALID_ID}, 
		{ R.id.q9a1,  R.id.q9a2,  R.id.q9a3, INVALID_ID}, 

		{INVALID_ID, INVALID_ID, INVALID_ID, INVALID_ID}, 
		{R.id.q11a1, R.id.q11a2, R.id.q11a3, INVALID_ID}, 
		{R.id.q12a1, INVALID_ID, R.id.q12a3, R.id.q12a4}, 
		{R.id.q13a1, INVALID_ID, R.id.q13a3, R.id.q13a4}, 
		{R.id.q14a1, R.id.q14a2, R.id.q14a3, INVALID_ID}, 
		{R.id.q15a1, R.id.q15a2, R.id.q15a3, INVALID_ID}  
//		{INVALID_ID, INVALID_ID, INVALID_ID, INVALID_ID}  
	};

	private static final int[][] VIEW_VISIBLE_ID = 
	{
		{  R.id.q1 ,   R.id.q1a,  R.id.q1div}, 
		{  R.id.q2 ,   R.id.q2a,  R.id.q2div}, 
		{  R.id.q3 ,   R.id.q3a,  R.id.q3div}, 
		{  R.id.q4 ,   R.id.q4a,  R.id.q4div}, 
		{  R.id.q5 ,   R.id.q5a,  R.id.q5div}, 
		{  R.id.q6 ,   R.id.q6a,  R.id.q6div}, 
		{  R.id.q7 ,   R.id.q7a,  R.id.q7div}, 
		{  R.id.q8 ,   R.id.q8a,  R.id.q8div}, 
		{  R.id.q9 ,   R.id.q9a,  R.id.q9div}, 
		{INVALID_ID, INVALID_ID,  INVALID_ID}, 
		{  R.id.q11,  R.id.q11a, R.id.q11div}, 
		{  R.id.q12,  R.id.q12a, R.id.q12div}, 
		{  R.id.q13,  R.id.q13a, R.id.q13div}, 
		{  R.id.q14,  R.id.q14a, R.id.q14div}, 
		{  R.id.q15,  R.id.q15a, R.id.q15div}, 
		{INVALID_ID, INVALID_ID,  INVALID_ID}
	};

	private Animation shake_;

	private MYMSurvey mymSurvey_;
	private TextView textASPCAAck_;

	private OnClickListener scoreClickListener = 
		new OnClickListener()
		{
			public void onClick(View v)
			{
				score();
			}
		};

	public MYMSurveyActivity()
	{
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mym_survey);

		// TODO:  Consider using a ListView instead of a large XML file so the Survey initial load is faster.  

		shake_ = AnimationUtils.loadAnimation(this, R.anim.shake_horiz);

		mymSurvey_ = new MYMSurvey(this);

		Button scoreButton = (Button)findViewById(R.id.score);
		scoreButton.setOnClickListener(scoreClickListener);

		textASPCAAck_ = (TextView) findViewById(R.id.textASPCAAck);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		save();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		init_display();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK)
		{
			final int felineality = data.getIntExtra("felineality", -1);
			close(felineality);
		}
	}

	private void init_display()
	{
// TODO: Consider if moving focus to top works and if it's a good idea.
//		final ScrollView survey = (ScrollView)findViewById(R.id.survey);
//		survey.fullScroll(ScrollView.FOCUS_UP);

		mymSurvey_.load();
		populate_display();

		final MovementMethod movementMethod = LinkMovementMethod.getInstance();

		textASPCAAck_.setMovementMethod(movementMethod);
	}

	private void populate_display()
	{
		for (int row = 0; row != RADIO_ANSWER_ID.length; ++row)
		{
			final int answer = mymSurvey_.getAnswer(row);
			if (answer>=0 && answer<RADIO_ANSWER_ID[row].length)
			{
				final int radioButtonID = RADIO_ANSWER_ID[row][answer];
				if (radioButtonID != INVALID_ID)
				{
					final RadioButton radioButtonAnswer = (RadioButton)findViewById(radioButtonID);
					radioButtonAnswer.setChecked(true);
				}
			}
		}

		final CheckBox dogCheckbox = (CheckBox)findViewById(R.id.q10a1);
		final boolean hasDog = mymSurvey_.hasDog();
		dogCheckbox.setChecked(hasDog);

		final CheckBox catCheckbox = (CheckBox)findViewById(R.id.q10a2);
		final boolean hasCat = mymSurvey_.hasCat();
		catCheckbox.setChecked(hasCat);

		final CheckBox birdCheckbox = (CheckBox)findViewById(R.id.q10a3);
		final boolean hasBird = mymSurvey_.hasBird();
		birdCheckbox.setChecked(hasBird);

		final EditText otherEditText = (EditText)findViewById(R.id.q10a4Edit);
		final String otherAnimal = mymSurvey_.getOtherAnimal();
		otherEditText.setText(otherAnimal);

		final EditText importantEditText = (EditText)findViewById(R.id.q16aEdit);
		final String mostImportant = mymSurvey_.getMostImportant();
		importantEditText.setText(mostImportant);
	}

	private void score()
	{
		read_user_answers(mymSurvey_);

		final boolean isSurveyCompleted = mymSurvey_.isComplete();

		if (isSurveyCompleted)
		{
			final int score = mymSurvey_.score();
			final int felineality = getFelineality(score);
			if (felineality>=0 && felineality<9)
			{
				close(felineality);
			}
			else
			{
				mymUserSelectFelineality(score);
			}
		}
		else
		{
			remove_answered(mymSurvey_);

			shake_answers();
		}
	}

	private static int getFelineality(final int score)
	{
		int result = -1;

		final int matchCount = Util.nBits(score);
		if (matchCount == 1)
		{
			int bit = score;
			while (bit != 0)
			{
				bit >>= 1;
				++result;
			}
		}

		return result;
	}

	private void mymUserSelectFelineality(final int score)
	{
		final Intent mymManualSelectIntent = new Intent(this, MYMManualSelectActivity.class);

		mymManualSelectIntent.putExtra("score", score);

		Util.startActivityForResult(this, mymManualSelectIntent, 0);
	}

	private void close(final int felineality)
	{
		if (felineality>=0 && felineality<9)
		{
			mymSurvey_.saveFelineality(this, felineality);
			finish();
		}
	}

	private void save()
	{
		read_user_answers(mymSurvey_);

		mymSurvey_.save();
	}

	private void read_user_answers(final MYMSurvey mymSurvey)
	{
		for (int row = 0; row != RADIO_ANSWER_ID.length; ++row)
		{
			for (int col = 0; col != RADIO_ANSWER_ID[row].length; ++col)
			{
				final int radioButtonID = RADIO_ANSWER_ID[row][col];
				if (radioButtonID != INVALID_ID)
				{
					final RadioButton radioButtonAnswer = (RadioButton)findViewById(radioButtonID);
					final boolean checked = radioButtonAnswer.isChecked();
					if (checked)
					{
						mymSurvey.setAnswer(row, col);
						break;
					}
				}
			}
		}

		final CheckBox dogCheckbox = (CheckBox)findViewById(R.id.q10a1);
		final boolean hasDog = dogCheckbox.isChecked();

		final CheckBox catCheckbox = (CheckBox)findViewById(R.id.q10a2);
		final boolean hasCat = catCheckbox.isChecked();

		final CheckBox birdCheckbox = (CheckBox)findViewById(R.id.q10a3);
		final boolean hasBird = birdCheckbox.isChecked();

		final EditText otherEditText = (EditText)findViewById(R.id.q10a4Edit);
		final String otherAnimal = otherEditText.getText().toString().trim();

		mymSurvey.setAnimalAnswer(hasDog, hasCat, hasBird, otherAnimal);

		final EditText importantEditText = (EditText)findViewById(R.id.q16aEdit);
		final String mostImportant = importantEditText.getText().toString().trim();
		mymSurvey.setMostImportant(mostImportant);
	}

	private void remove_answered(final MYMSurvey mymSurvey)
	{
		for (int row = 0; row != VIEW_VISIBLE_ID.length; ++row)
		{
			final boolean answered = mymSurvey.isAnswered(row);
			if (answered)
			{
				for (int col = 0; col != VIEW_VISIBLE_ID[row].length; ++col)
				{
					final int viewID = VIEW_VISIBLE_ID[row][col];
					if (viewID != INVALID_ID)
					{
						final View view = findViewById(viewID);
						view.setVisibility(View.GONE);
					}
				}
			}
		}

//		findViewById(R.id.q10aText).setVisibility(View.GONE);
//
//		findViewById(R.id.q16).setVisibility(View.GONE);
//
//		findViewById(R.id.q16aEdit).setVisibility(View.GONE);
		
	}

	private void shake_answers()
	{
		for (int row = 0; row != VIEW_VISIBLE_ID.length; ++row)
		{
			final int viewID = VIEW_VISIBLE_ID[row][1];
			if (viewID != INVALID_ID)
			{
				final View view = findViewById(viewID);
				final int visibility = view.getVisibility();
				if (visibility == View.VISIBLE)
				{
					view.startAnimation(shake_);
				}
			}
		}
	}
}
