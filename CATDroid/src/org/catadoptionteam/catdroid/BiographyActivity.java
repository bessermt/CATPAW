/**
 * 
 */
package org.catadoptionteam.catdroid;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author bessermt
 *
 */
public class BiographyActivity extends Activity
{
	ImageButton imagePhoto_;
	TextView textName_;
	TextView textASPCAality_;

	int aspcaAlity_;
	Cat cat_;

	private OnClickListener closeClickListener = 
		new OnClickListener()
		{
			public void onClick(View v)
			{
				close();
			}
		};

	private OnClickListener photoClickListener = 
		new OnClickListener()
		{
			public void onClick(View v)
			{
				displayPhoto();
			}
		};

	private OnClickListener ASPCAalityClickListener = 
		new OnClickListener()
		{
			public void onClick(View v)
			{
				displayASPCAality();
			}
		};

	public BiographyActivity()
	{
		aspcaAlity_ = -1;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.biography);

		imagePhoto_ = (ImageButton)findViewById(R.id.imagePhoto);
		imagePhoto_.setOnClickListener(photoClickListener);

		textASPCAality_ = (TextView)findViewById(R.id.textASPCAality);

		textName_ = (TextView)findViewById(R.id.textName);

		final Button buttonPhoto = (Button)findViewById(R.id.buttonPhoto);
		buttonPhoto.setOnClickListener(photoClickListener);

		final Button buttonFelinealityCategory = (Button)findViewById(R.id.buttonFelinealityCategory);
		buttonFelinealityCategory.setOnClickListener(ASPCAalityClickListener);

		final Button buttonClose = (Button)findViewById(R.id.buttonClose);
		buttonClose.setOnClickListener(closeClickListener);

		final int defaultID = Util.getCOTDID(this);
		final Intent intent = getIntent();
		final int keyID = intent.getIntExtra(AppDBAdapter.KEY_ID, defaultID);
		final AppDBAdapter appDBAdapter = new AppDBAdapter(this);
		cat_ = appDBAdapter.getAnimal(this, keyID);
		appDBAdapter.close();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		initDisplay();
	}

	private void initDisplay()
	{
		aspcaAlity_ = cat_.getASPCAality();

		final int color = cat_.getASPCAalityColor();

		final Bitmap bitmap = cat_.getSmPhoto();
		imagePhoto_.setImageBitmap(bitmap);
		imagePhoto_.setBackgroundColor(color);

		final ImageView imageAdopted = (ImageView)findViewById(R.id.imageAdopted);
		final int status = cat_.getStatus();
		int visibility = View.INVISIBLE;
		if (status == AppDBAdapter.STATUS_ADOPTED)
		{
			visibility = View.VISIBLE;
		}
		imageAdopted.setVisibility(visibility);

		final TextView textAgeSex = (TextView)findViewById(R.id.textAgeSex);
		final String age_sex = cat_.getAge() + " " + cat_.getSex();
		textAgeSex.setText(age_sex);
		textAgeSex.setBackgroundColor(color);

		final int aspcaAlityResId = cat_.getASPCAalityResID();
		textASPCAality_.setText(aspcaAlityResId);
		textASPCAality_.setBackgroundColor(color);

		final String name = cat_.getName();
		textName_.setText(name);
		textName_.setTextColor(color);

		final TextView textBio = (TextView)findViewById(R.id.textBio);

		final int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion > android.os.Build.VERSION_CODES.DONUT)
		{
			textBio.setAutoLinkMask(Linkify.ALL);
		}

		final String bio_en = cat_.getBiography();
		final String bioText = Util.concatNoTranslation(this, bio_en);
		textBio.setText(bioText);

		final TextView textPetID = (TextView)findViewById(R.id.textPetID);
		final String petID = getString(R.string.pet_id) + ": " + cat_.getPetID();
		textPetID.setText(petID);
		textPetID.setBackgroundColor(color);
	}

	private void close()
	{
		finish();
	}

	private void displayPhoto()
	{
		try
		{
			final String lgPhotoPathFilename = cat_.getLgPhotoPathFilename();
			final File lgPhotoFile = new File(lgPhotoPathFilename);
			final Uri uri = Uri.fromFile(lgPhotoFile);
			final Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(uri, "image/*");

			Util.startActivity(this, intent);
		}
		catch (Throwable t)
		{
//			final String message = t.getMessage();
//			Log.e("displayPhoto()", message);
		}
	}

	private void displayASPCAality()
	{
		final Intent mymCategoryIntent = new Intent(this, MYMCategoryActivity.class);
		mymCategoryIntent.putExtra(MYMSurvey.FELINEALITY, aspcaAlity_);
		Util.startActivity(this, mymCategoryIntent);
	}
}
