/**
 * 
 */
package org.catadoptionteam.catdroid;

import java.io.File;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author bessermt
 *
 */
public class PetfinderBioActivity extends Activity implements OnClickListener
{
	private class PhotoDownloaderTask extends BitmapDownloaderTask
	{
		public PhotoDownloaderTask(String pathname)
		{
			super(pathname, "photo", 50);
			pathname_ = pathname;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			initPhoto(result);
		}

		public String getPathname()
		{
			return pathname_;
		}
	}

	private static class Pet
	{
		private Context context_;

		private long _id_;
		private String shelterPetId_;
		private String name_;
		private int age_;
		private int sex_;
		private int size_;
		private boolean noCats_;
		private boolean noDogs_;
		private boolean noKids_;
		private boolean specialNeeds_;
		private boolean noClaws_;
		// private Bitmap[] photo_; // TODO: remove this?  Replace with base uri?
		private String description_;
		private int[] breed_;
		private boolean favorite_;

		final String[] breedTextArray_;

		public Pet(final Context context)
		{
			context_ = context;

			age_ = PetfinderProvider.PetRecord.AGE_UNKNOWN;
			sex_ = AppDBAdapter.SEX_UNKNOWN;
			size_ = PetfinderProvider.PetRecord.SIZE_UNKNOWN;

			final Resources resources = context_.getResources();

			breedTextArray_ = resources.getStringArray(R.array.pf_breed_list_text_cat);
		}

		public void set
		(
			final long _id, 
			final String shelterPetId, 
			final String name, 
			final int age, 
			final int sex, 
			final int size, 
			final boolean noCats, 
			final boolean noDogs, 
			final boolean noKids, 
			final boolean specialNeeds, 
			final boolean noClaws, 
			final String description, 
			// final int photoCount, 
			final String photoBaseUri, 
			final boolean favorite
		)
		{
			_id_ = _id;
			shelterPetId_ = shelterPetId;
			name_ = name;
			age_ = age;
			sex_ = sex;
			size_ = size;
			noCats_ = noCats;
			noDogs_ = noDogs;
			noKids_ = noKids;
			specialNeeds_ = specialNeeds;
			noClaws_ = noClaws;
			// photo_ = new Bitmap[photoCount];
			description_ = description;
			favorite_ = favorite;
		}

		private String getShelterPetId()
		{
			final String result = shelterPetId_;
			return result;
		}

		private void setBreedCount(final int breedCount)
		{
			if (breedCount > 0 && breedCount<=breedTextArray_.length)
			{
				breed_ = new int[breedCount];
			}
		}

		private void addBreed(final int breed, final int index)
		{
			if (index>=0 && index<breed_.length && breed>=0 && breed<breedTextArray_.length)
			{
				breed_[index] = breed;
			}
		}

		private String getAge()
		{
			final String result = PetfinderProvider.PetRecord.getAgeText(context_, age_);
			return result;
		}

		private String getSex()
		{
			final String result = Cat.getSexText(context_, sex_);
			return result;
		}

		private String getSize()
		{
			final String result = PetfinderProvider.PetRecord.getSizeText(context_, size_);
			return result;
		}

		private String getName()
		{
			final String result = name_;
			return result;
		}

		private boolean getSpecialNeeds()
		{
			final boolean result = specialNeeds_;
			return result;
		}

		private boolean getDeclawed()
		{
			final boolean result = noClaws_;
			return result;
		}

		private boolean getNoCats()
		{
			final boolean result = noCats_;
			return result;
		}

		private boolean getNoDogs()
		{
			final boolean result = noDogs_;
			return result;
		}

		private boolean getNoKids()
		{
			final boolean result = noKids_;
			return result;
		}

		private boolean getFavorite()
		{
			final boolean result = favorite_;
			return result;
		}

		private Spanned getDescription()
		{
			final String description = Util.concatNoTranslation(context_, description_);
			final Spanned result = Html.fromHtml(description);
			return result;
		}

		private String getBreedText(final int index)
		{
			String result = null;

			if (index>0 && index<breedTextArray_.length)
			{
				result = breedTextArray_[index];
			}

			return result;
		}

		private String getBreedsText()
		{
			String result = context_.getString(R.string.unknown_breed);

			String breedText = Util.EMPTY_STRING;
			if (breed_ != null)
			{
				String nl = Util.EMPTY_STRING;
				for (int breed: breed_)
				{
					final String breedStr = getBreedText(breed);
					if (!TextUtils.isEmpty(breedStr))
					{
						breedText += nl + breedStr; // TODO: Consider using a String.concat method.
						nl = "\n";
					}
				}
			}

			if (!TextUtils.isEmpty(breedText))
			{
				result = breedText;
			}

			return result;
		}
	}

	private static final String PET_ID_SELECTION = PetfinderProvider._ID + "=?";
	private static final String BREED_PET_ID_SELECTION = PetfinderProvider.FIELD_BREED_PET_ID + "=?";

	private long _id_;
	private ContentResolver contentResolver_;

	private Pet pet_;

	private PhotoDownloaderTask photoDownloaderTask_;

	private int photoIndex_ = 0;
	private int photoCount_ = 0;

	private ImageButton imageButtonPhoto_;
	private CheckBox checkboxFavorite_;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.petfinder_bio);

		final Intent intent = getIntent();
		_id_ = intent.getLongExtra(PetfinderProvider._ID, 0);

		contentResolver_ = getContentResolver();

		imageButtonPhoto_ = (ImageButton)findViewById(R.id.imagePhoto);
		imageButtonPhoto_.setOnClickListener(this);
		imageButtonPhoto_.setOnLongClickListener
		(
			new OnLongClickListener()
			{
				@Override
				public boolean onLongClick(View v)
				{
					final boolean result = true;

					final String photoPathname = photoDownloaderTask_.getPathname();
					displayLargePhoto(photoPathname);

					return result;
				}
			}
		);

		checkboxFavorite_ = (CheckBox) findViewById(R.id.checkFavorite);

		initDisplay(_id_);

		final Button buttonPhoto = (Button)findViewById(R.id.buttonPhoto);
		buttonPhoto.setOnClickListener(this);

		final Button buttonClose = (Button)findViewById(R.id.buttonClose);
		buttonClose.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		final int id = v.getId();
		switch (id)
		{
		case R.id.buttonPhoto:
			final String photoPathFilename = photoDownloaderTask_.getPathFilename(photoIndex_, photoCount_);
			displayLargePhoto(photoPathFilename);
			break;

		case R.id.imagePhoto:
			nextPhoto();
			break;

		case R.id.buttonClose:
			finish();
			break;

		case R.id.buttonSpecialNeeds:
			Util.displayToast(this, R.string.ask_about_my_special_needs, Toast.LENGTH_LONG);
			break;

		case R.id.buttonDeclawed:
			Util.displayToast(this, R.string.i_am_declawed, Toast.LENGTH_LONG);
			break;

		case R.id.buttonNoCats:
			Util.displayToast(this, R.string.dislike_cats, Toast.LENGTH_LONG);
			break;

		case R.id.buttonNoDogs:
			Util.displayToast(this, R.string.dislike_dogs, Toast.LENGTH_LONG);
			break;

		case R.id.buttonNoKids:
			Util.displayToast(this, R.string.dislike_kids, Toast.LENGTH_LONG);
			break;

		case R.id.checkFavorite:
			checkFavorite();
			break;

		default:
			break;
		}
	}

	private void nextPhoto()
	{
		if (photoCount_ > 1)
		{
			photoIndex_ = (photoIndex_+1)%photoCount_;
			initPhoto(photoIndex_);
		}
		else
		{
			imageButtonPhoto_.invalidate();
		}
	}

	private void checkFavorite()
	{
		final boolean isChecked = checkboxFavorite_.isChecked();
		PetfinderProvider.updateFavorite(contentResolver_, _id_, isChecked);
	}

	private void attention(final View view)
	{
		final Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_io);
		view.startAnimation(animation);
	}

	private void initDisplay(final long _id)
	{
		pet_ = new Pet(this);

		final String[] petProjection = new String[]
			{
				PetfinderProvider.FIELD_PET_PF_SHELTER_PET_ID, 
				PetfinderProvider.FIELD_PET_PF_NAME, 
				PetfinderProvider.FIELD_PET_PF_AGE, 
				PetfinderProvider.FIELD_PET_PF_SEX, 
				PetfinderProvider.FIELD_PET_PF_SIZE, 
				PetfinderProvider.FIELD_PET_PF_NO_CATS, 
				PetfinderProvider.FIELD_PET_PF_NO_DOGS, 
				PetfinderProvider.FIELD_PET_PF_NO_KIDS, 
				PetfinderProvider.FIELD_PET_PF_SPECIAL_NEEDS, 
				PetfinderProvider.FIELD_PET_PF_NO_CLAWS, 
				PetfinderProvider.FIELD_PET_PF_PHOTO_COUNT, 
				PetfinderProvider.FIELD_PET_PF_PHOTO_BASE_URI, 
				PetfinderProvider.FIELD_PET_PF_DESCRIPTION, 
				PetfinderProvider.FIELD_PET_FAVORITE
			};

		final String[] idSelectionArgs = new String[] {String.valueOf(_id)};

		final Cursor petCursor = contentResolver_.query(PetfinderProvider.CONTENT_URI_PET, petProjection, PET_ID_SELECTION, idSelectionArgs, null);

		String shelterPetId = "Unknown";
		String photoBaseUri = null;

		if (petCursor != null)
		{
			// startManagingCursor(petCursor);

			final boolean exists = petCursor.moveToFirst();
			try
			{
				if (exists) // TODO: if !exists respond with total failure.
				{
					final int colShelterPetId = petCursor.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_PF_SHELTER_PET_ID);
					final int colName = petCursor.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_PF_NAME);
					final int colAge = petCursor.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_PF_AGE);
					final int colSex = petCursor.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_PF_SEX);
					final int colSize = petCursor.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_PF_SIZE);
					final int colNoCats = petCursor.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_PF_NO_CATS);
					final int colNoDogs = petCursor.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_PF_NO_DOGS);
					final int colNoKids = petCursor.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_PF_NO_KIDS);
					final int colSpecialNeeds = petCursor.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_PF_SPECIAL_NEEDS);
					final int colNoClaws = petCursor.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_PF_NO_CLAWS);
					final int colPhotoCount = petCursor.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_PF_PHOTO_COUNT);
					final int colPhotoBaseUri = petCursor.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_PF_PHOTO_BASE_URI);
					final int colDescription = petCursor.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_PF_DESCRIPTION);
					final int colFavorite = petCursor.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_FAVORITE);

					shelterPetId = petCursor.getString(colShelterPetId);
					final String name = petCursor.getString(colName);
					final int age = petCursor.getInt(colAge);
					final int sex = petCursor.getInt(colSex);
					final int size = petCursor.getInt(colSize);
					final boolean noCats = petCursor.getShort(colNoCats) != 0;
					final boolean noDogs = petCursor.getShort(colNoDogs) != 0;
					final boolean noKids = petCursor.getShort(colNoKids) != 0;
					final boolean specialNeeds = petCursor.getShort(colSpecialNeeds) != 0;
					final boolean noClaws = petCursor.getShort(colNoClaws) != 0;
					photoCount_ = petCursor.getInt(colPhotoCount);
					photoBaseUri = petCursor.getString(colPhotoBaseUri);
					final String description = petCursor.getString(colDescription);
					final boolean favorite = petCursor.getShort(colFavorite) != 0;

					pet_.set
					(
						_id, 
						shelterPetId, 
						name, 
						age, 
						sex, 
						size, 
						noCats, 
						noDogs, 
						noKids, 
						specialNeeds, 
						noClaws, 
						description, 
						photoBaseUri, 
						favorite
					);

					final String[] breedProjection = new String[]
						{
							PetfinderProvider.FIELD_BREED_PF_BREED_INDEX
						};

					final String[] petIdSelectionArgs = new String[] {String.valueOf(_id)};

					final Cursor breedCursor = contentResolver_.query(PetfinderProvider.CONTENT_URI_BREED, breedProjection, BREED_PET_ID_SELECTION, petIdSelectionArgs, null);

					if (breedCursor != null)
					{
						// startManagingCursor(breedCursor);

						final boolean hasBreedInfo = breedCursor.moveToFirst();
						try
						{
							if (hasBreedInfo)
							{
								final int colBreedIndex = breedCursor.getColumnIndexOrThrow(PetfinderProvider.FIELD_BREED_PF_BREED_INDEX);

								final int breedCount = breedCursor.getCount();
								pet_.setBreedCount(breedCount);
								int i = 0;
								do
								{
									final int breedIndex = breedCursor.getInt(colBreedIndex);
									pet_.addBreed(breedIndex, i);
									++i;
								} while (breedCursor.moveToNext());
							}
						}
						finally
						{
							breedCursor.close();
						}
					}
				}
			}
			finally
			{
				petCursor.close();
			}
		}

		final TextView textAgeSex = (TextView) findViewById(R.id.textAgeSex);
		final String age_sex = pet_.getAge() + " " + pet_.getSex();
		textAgeSex.setText(age_sex);

		final TextView textSize = (TextView) findViewById(R.id.textSize);
		final String size = getString(R.string.size) + ": " + pet_.getSize();
		textSize.setText(size);

		final TextView textName = (TextView) findViewById(R.id.textName);
		final String name = pet_.getName();
		textName.setText(name);

		final String photoPathname = PetfinderProvider.getPhotoCachePath(this, shelterPetId);
		photoDownloaderTask_ = new PhotoDownloaderTask(photoPathname);

		final String photoPathFilename = photoDownloaderTask_.getPathFilename(photoIndex_, photoCount_);
		if (photoPathFilename != null)
		{
			final File photoFile = new File(photoPathFilename);

			if (photoFile.exists() || TextUtils.isEmpty(photoBaseUri))
			{
				initPhoto(photoPathFilename);
			}
			else
			{
				final String[] photoUriList = new String[photoCount_];
				int i = photoUriList.length;
				while (i != 0)
				{
					--i;
					final String photoUri = PetfinderProvider.PetRecord.getPhotoUri(photoBaseUri, i, PetfinderProvider.PetRecord.PHOTO_SIZE_X);
					photoUriList[i] = photoUri;
				}

				photoDownloaderTask_.execute(photoUriList);
			}
		}

		final ImageView imageViewPlus = (ImageView) findViewById(R.id.imageViewPlus);
		int visibility = View.VISIBLE;
		if (photoCount_ <= 1)
		{
			visibility = View.INVISIBLE;
		}
		else
		{
			attention(imageViewPlus);
		}
		imageViewPlus.setVisibility(visibility);

		final ImageButton buttonSpecialNeeds = (ImageButton) findViewById(R.id.buttonSpecialNeeds);
		final boolean specialNeeds = pet_.getSpecialNeeds();
		visibility = specialNeeds?View.VISIBLE:View.INVISIBLE;
		buttonSpecialNeeds.setVisibility(visibility);
		buttonSpecialNeeds.setOnClickListener(this);

		final FrameLayout frameDeclawed = (FrameLayout) findViewById(R.id.frameDeclawed);
		final ImageButton buttonDeclawed = (ImageButton) findViewById(R.id.buttonDeclawed);
		final boolean declawed = pet_.getDeclawed();
		visibility = declawed?View.VISIBLE:View.INVISIBLE;
		frameDeclawed.setVisibility(visibility);
		buttonDeclawed.setOnClickListener(this);

		final FrameLayout frameNoCats = (FrameLayout) findViewById(R.id.frameNoCats);
		final ImageButton buttonNoCats = (ImageButton) findViewById(R.id.buttonNoCats);
		final boolean noCats = pet_.getNoCats();
		visibility = noCats?View.VISIBLE:View.INVISIBLE;
		frameNoCats.setVisibility(visibility);
		buttonNoCats.setOnClickListener(this);

		final FrameLayout frameNoDogs = (FrameLayout) findViewById(R.id.frameNoDogs);
		final ImageButton buttonNoDogs = (ImageButton) findViewById(R.id.buttonNoDogs);
		final boolean noDogs = pet_.getNoDogs();
		visibility = noDogs?View.VISIBLE:View.INVISIBLE;
		frameNoDogs.setVisibility(visibility);
		buttonNoDogs.setOnClickListener(this);

		final FrameLayout frameNoKids = (FrameLayout) findViewById(R.id.frameNoKids);
		final ImageButton buttonNoKids = (ImageButton) findViewById(R.id.buttonNoKids);
		final boolean noKids = pet_.getNoKids();
		visibility = noKids?View.VISIBLE:View.INVISIBLE;
		frameNoKids.setVisibility(visibility);
		buttonNoKids.setOnClickListener(this);

		checkboxFavorite_ = (CheckBox) findViewById(R.id.checkFavorite);
		final boolean favorite = pet_.getFavorite();
		checkboxFavorite_.setChecked(favorite);
		checkboxFavorite_.setOnClickListener(this);

		final TextView textBreed = (TextView) findViewById(R.id.textBreed);
		final String breedText = pet_.getBreedsText();
		textBreed.setText(breedText);

		final TextView textBio = (TextView) findViewById(R.id.textBio);
		final Spanned description = pet_.getDescription();
		textBio.setText(description);

		final TextView textPetID = (TextView) findViewById(R.id.textPetID);
		final String petID = getString(R.string.pet_id) + ": " + pet_.getShelterPetId();
		textPetID.setText(petID);

		imageButtonPhoto_.invalidate();
	}

	private void initPhoto(final String photoPathFilename)
	{
		final Bitmap bitmap = Util.getBitmap(this, photoPathFilename);
		imageButtonPhoto_.setImageBitmap(bitmap);
		imageButtonPhoto_.invalidate();
	}

	private void initPhoto(final int photoIndex)
	{
		final String photoPathFilename = photoDownloaderTask_.getPathFilename(photoIndex, photoCount_);
		initPhoto(photoPathFilename);
	}

	private void displayLargePhoto(final String photoName)
	{
		if (!TextUtils.isEmpty(photoName))
		{
			try
			{
				final File file = new File(photoName);
				if (file.exists())
				{
					final Intent intent = new Intent(Intent.ACTION_VIEW);

					final Uri uri = Uri.fromFile(file);
					intent.setDataAndType(uri, "image/*");

					Util.startActivity(this, intent);
				}
			}
			catch (Throwable t)
			{
	//			final String message = t.getMessage();
	//			Log.e("displayPhoto()", message);
			}
		}
	}
}
