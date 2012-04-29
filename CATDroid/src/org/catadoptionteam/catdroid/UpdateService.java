/**
 * 
 */
package org.catadoptionteam.catdroid;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

/**
 * @author bessermt
 *
 */
public class UpdateService extends IntentService
{
	public class AnimalUpdateWorker implements Runnable
	{
		private Context context_;
		private PowerManager.WakeLock wakeLock_;

		public AnimalUpdateWorker(final Context context, final PowerManager.WakeLock wakeLock)
		{
			context_ = Util.getSafeContext(context);
			wakeLock_ = wakeLock;
		}

		private void download(final DefaultHttpClient client, final AppDBAdapter db, final String URL, final String jsonKey)
		{
			final HttpGet getRequest = new HttpGet(URL);

			try
			{
				final HttpResponse response = client.execute(getRequest);
				final int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == HttpStatus.SC_OK)
				{
					final HttpEntity entity = response.getEntity();
					if (entity != null)
					{
						InputStream inputStream = null;
						try
						{
							inputStream = entity.getContent();
							if (inputStream != null)
							{
								final String jsonString = Util.toString(inputStream);
								final JSONTokener jsonTokener = new JSONTokener(jsonString);
								final JSONObject object = (JSONObject) jsonTokener.nextValue();
								final JSONArray cotds = object.getJSONArray(jsonKey);
								final int length = cotds.length();
								for (int i=0; i<length; i++)
								{
									final JSONObject cat = cotds.getJSONObject(i);

//									final String recordID = cat.getString("id");

									final String petID = cat.getString("petID") + Cat.PET_ID_DELIMITER + jsonKey; // Add jsonKey to be sure petID is unique even if cat is used in both M9L and COTD.

									final String name = cat.getString("name");

									final int category = cat.getInt("category");

									final Time expDate = new Time();
									expDate.setToNow();
									final String expDateStr = cat.getString("expDate");
									if (!TextUtils.isEmpty(expDateStr))
									{
										expDate.parse3339(expDateStr);
									}

									int status = AppDBAdapter.STATUS_AVAILABLE;
									final boolean adopted = cat.getBoolean("adopted");
									if (adopted)
									{
										status = AppDBAdapter.STATUS_ADOPTED;
									}

									Time dob = null;
									final String dobStr = cat.getString("dob");
									if (!TextUtils.isEmpty(dobStr))
									{
										dob = new Time();
										dob.parse3339(dobStr);
									}

									final String species = "Cat";

									final int sex = cat.getInt("sex");

									final int aspcaAlity = cat.getInt("aspcaAlity");

									final String biography = cat.getString("biography");

									final String lgPhotoURL = cat.getString("lgPhotoURL");

									final String smPhotoURL = cat.getString("smPhotoURL");

									final String extraID = cat.getString("extraID");

									final String extraURL = cat.getString("extraURL");

									db.insert(petID, expDate, name, status, category, species, dob, sex, aspcaAlity, biography, lgPhotoURL, smPhotoURL, extraID, extraURL);
								}
							}
						}
						catch (JSONException e)
						{
							// TODO: Diagnose and recover...
//							final String message = e.getMessage();
//							Log.e("JSON", message);
						}
						catch (Throwable t)
						{
							// TODO: Diagnose and recover...
//							final String message = t.getMessage();
//							Log.e("JSON", message);
						}
						finally
						{
							if (inputStream != null)
							{
								inputStream.close();
							}
							entity.consumeContent();
						}
					}
				}
			}
			catch (Exception e)
			{
				// TODO: provide a more explicit error message for IOException or IllegalStateException
//				final String message = e.getMessage();
//				Log.e("downloadCOTD()", message);
				getRequest.abort();
				// Log.w("downloadCOTD", "Error while retrieving COTD from " + url, e.toString());
			}
		}

		private String getServerURL(final Context context)
		{
			final String result = context.getString(R.string.shelter_server) + "catphonesrv/";
			return result;
		}

		private void downloadCOTD(final DefaultHttpClient client, final AppDBAdapter db)
		{
			final String cotdURL = getServerURL(context_) + "cotd";
			download(client, db, cotdURL, "COTD");
		}

		private void downloadM9L(final DefaultHttpClient client, final AppDBAdapter db)
		{
			final String cotdURL = getServerURL(context_) + "m9l";
			download(client, db, cotdURL, "M9L");
		}

		private void updateM9L(final DefaultHttpClient client, final AppDBAdapter db, final Time today)
		{
			final String[] resultSetExpired = 
				new String[]
				{
					AppDBAdapter.KEY_ID, 
					AppDBAdapter.EXPIRE_DATE
				};

			final String order = 
				AppDBAdapter.EXPIRE_DATE + " ASC";

			boolean updated = false;
			boolean download = false;
			int felineality = 9;
			while (felineality != 0)
			{
				--felineality;

				final String where = 
					AppDBAdapter.CATEGORY + "=" + AppDBAdapter.CATEGORY_M9L + " and " + 
					AppDBAdapter.ASPCA_ALITY + "=" + felineality;

				Cursor cursor = null;

				try
				{
					cursor = db.query(null, resultSetExpired, where, order);

					int count = 0;
					if (cursor == null)
					{
						download = true;
					}
					else
					{
						count = cursor.getCount();
						if (count <= 1)
						{
							download = true;
						}
						else
						{
							cursor.moveToFirst();
							final String expirationDate = cursor.getString(1);
							final Time expirationDateTime = new Time();
							expirationDateTime.parse3339(expirationDate);
							if (today.after(expirationDateTime))
							{
								download = true;
							}
						}
					}

					if (!updated && download)
					{
						updated = true;
						downloadM9L(client, db);
					}

					// delete old records

					if (cursor != null)
					{
						boolean good = cursor.moveToLast();
						good = good && cursor.moveToPrevious();
						good = good && cursor.moveToPrevious();

						while (good)
						{
							final int id = cursor.getInt(0);
							db.deleteID(null, id);
							good = cursor.moveToPrevious();
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
			}
		}

		// TODO:  This code is similar to updateM9L(), can it be factored?
		private void updateCOTD(final DefaultHttpClient client, final AppDBAdapter db, final Time today)
		{
			final String[] resultSetExpired = 
				new String[]
				{
					AppDBAdapter.KEY_ID, 
					AppDBAdapter.EXPIRE_DATE
				};

			final String where = 
				AppDBAdapter.CATEGORY + "=" + AppDBAdapter.CATEGORY_ANIMAL_OF_THE_DAY;

			final String order = 
				AppDBAdapter.EXPIRE_DATE + " DESC";

			Cursor cursor = null;

			boolean download = true;

			try
			{
				cursor = db.query(null, resultSetExpired, where, order);

				boolean good = false;
				if (cursor != null)
				{
					good = cursor.moveToFirst();
					if (good)
					{
						final String date = cursor.getString(1);
						final Time time = new Time();
						time.parse3339(date);

						final Time tomorrow = new Time();
						tomorrow.set(today.toMillis(false) + Util.MILSEC_PER_DAY);

						download = time.before(tomorrow); // TODO:  Have a deep thought about if this will give the needed data in all cases.  
					}
				}

				if (download)
				{
					downloadCOTD(client, db);
				}

				// delete old records

				final Time yesterday = new Time();
				yesterday.set(today.toMillis(false) - Util.MILSEC_PER_DAY);

				good = false;
				if (cursor != null)
				{
					good = cursor.moveToLast();
					while (good)
					{
						good = false;

						final int id = cursor.getInt(0);
						final String date = cursor.getString(1);

						final Time time = new Time();
						time.parse3339(date);

						if (time.before(yesterday))
						{
							db.deleteID(null, id);
							good = cursor.moveToPrevious();
						}
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
		}

		@Override
		public void run()
		{
			if (wakeLock_ != null)
			{
				wakeLock_.acquire(); 
			}

			final Time today = new Time();
			today.setToNow();

			AppDBAdapter db = null;

			try
			{
				final DefaultHttpClient client = new DefaultHttpClient();
				db = new AppDBAdapter(context_, true);

				updateM9L(client, db, today);
				updateCOTD(client, db, today);
			}
			finally
			{
				if (db != null)
				{
					db.close();
				}

				if (wakeLock_ != null)
				{
					wakeLock_.release();
				}
			}
		}
	}

	public class VideoUpdateWorker implements Runnable
	{
		private static final int MAX_ROWS = VideoProvider.MAX_ROWS;

//		private static final String AUTHORITY_VIDEO = VideoProvider.PROVIDER_AUTHORITY;
//		private static final String ACCOUNT_NAME = "VideoSyncService";

		private Context context_;
		private PowerManager.WakeLock wakeLock_;

		public VideoUpdateWorker(final Context context, final PowerManager.WakeLock wakeLock)
		{
			context_ = Util.getSafeContext(context);
			wakeLock_ = wakeLock;
		}

		@Override
		public void run()
		{
			if (wakeLock_ != null)
			{
				wakeLock_.acquire(); 
			}
			try
			{
				clearOld();
				update();
			}
			catch (Throwable e)
			{
				// TODO: comment out
				// final String msg = e.getMessage();
				// Log.e(TAG, msg);
			}
			finally
			{
				if (wakeLock_ != null)
				{
					wakeLock_.release();
				}
			}
		}

		// TODO: Something like this?
//		private void updateVideo()
//		{
//			Account account = null;
//			final AccountManager accountManager = AccountManager.get(context_);
//			final Account[] accounts = accountManager.getAccountsByType(accountType_);
//			if (accounts != null && accounts.length > 0)
//			{
//				account = accounts[0]; // TODO: What if there is more than 1?
//			}
//			final Bundle extras =new Bundle(); // TODO: anything go into extras.
//			ContentResolver.requestSync(account, AUTHORITY_VIDEO, extras); // TODO: If API > 8, use ContentResolver.addPeriodicSync();
//		}

		private void clearOld()
		{
			final ContentResolver contentResolver = context_.getContentResolver();

			final String[] projection = new String[]
				{
					VideoProvider._ID, 
					VideoProvider._DATA
				};

			final String sortOrder = VideoProvider.FIELD_VIDEO_UPLOADED + " ASC";

			final Cursor cursor = contentResolver.query(VideoProvider.CONTENT_URI, projection, null, null, sortOrder);

			if (cursor != null)
			{
				try
				{
					if (cursor.moveToFirst())
					{
						int count = cursor.getCount() - MAX_ROWS;
						while (count > 0)
						{
							--count;
							final long id = cursor.getLong(0); // TODO: Change to a calculated column
							final String pathFileName = cursor.getString(1); // TODO: Change to a calculated column
							final Uri obsoleteVideo = ContentUris.withAppendedId(VideoProvider.CONTENT_URI, id);
							final int n = contentResolver.delete(obsoleteVideo, null, null);
							if (n == 1)
							{
								final File file = new File(pathFileName);
								if (file != null)
								{
									file.delete();
								}
							}
							cursor.moveToNext();
						}
					}
				}
				finally
				{
					cursor.close();
				}
			}
		}

		private void update()
		{
			try
			{
				final VideoFeed videoFeed = new VideoFeed();
				videoFeed.update(context_);
			}
			catch (Throwable e)
			{
				// TODO: Comment Out
				final String msg = e.getMessage();
				Log.e(TAG, msg);
				Util.displayToast(context_, msg, Toast.LENGTH_LONG);
			}
		}
	}

	public class PetfinderUpdateWorker implements Runnable
	{
		private static final String SCHEME = "http";
		private static final String DOMAIN = "api.petfinder.com";
		private static final String SHELTER_GET_PETS = "shelter.getPets";
		private static final String PET_GET = "pet.get";

		private static final String JSON_TAG_PLACEHOLDER = Util.JSON_TAG_PLACEHOLDER;

		private static final String DEV_KEY = "deb1a50a01d45c5c20a4c5c93eb75dcd"; // TODO: Move to a static string table along with other language independent strings.

		private static final int EXPIRATION_DAYS = 7;

		private static final String REMOVE_NONADOPTABLE = PetfinderProvider.FIELD_PET_PF_STATUS + "!=" + "\'" + PetfinderProvider.VALUE_FIELD_PET_PF_STATUS_ADOPTABLE + "\'";

		private static final String PF_ID_SELECTION = PetfinderProvider.FIELD_PET_PF_ID + "=?";

		private final String[] pfIdProjection_;

		private final Context context_;
		private final PowerManager.WakeLock wakeLock_;
		private final String shelterPetfinderId_;

		public PetfinderUpdateWorker(final Context context, final PowerManager.WakeLock wakeLock)
		{
			context_ = Util.getSafeContext(context);
			wakeLock_ = wakeLock;
			shelterPetfinderId_ = getString(R.string.shelter_petfinder_id);

			pfIdProjection_ = new String[]{PetfinderProvider.FIELD_PET_MODIFIED_TIME};
		}

		@Override
		public void run()
		{
			if (wakeLock_ != null)
			{
				wakeLock_.acquire(); 
			}
			try
			{
				update();
			}
			catch (Throwable e)
			{
//				final String msg = e.getMessage();
//				Log.e(TAG, msg);
			}
			finally
			{
				if (wakeLock_ != null)
				{
					wakeLock_.release();
				}
			}
		}

		private Uri createGetPetsUri(final int offset, final String output)
		{
			// example: http://api.petfinder.com/shelter.getPets?key=deb1a50a01d45c5c20a4c5c93eb75dcd&id=OR07&output=id&offset=25&format=json
			final Uri.Builder uriBuilder = new Uri.Builder();

			uriBuilder.scheme(SCHEME);
			uriBuilder.authority(DOMAIN);
			uriBuilder.appendPath(SHELTER_GET_PETS);
			uriBuilder.appendQueryParameter("key", DEV_KEY);
			uriBuilder.appendQueryParameter("id", shelterPetfinderId_);
			uriBuilder.appendQueryParameter("output", output);
			uriBuilder.appendQueryParameter("offset", Integer.toString(offset));
			uriBuilder.appendQueryParameter("format", "json");

			final Uri result = uriBuilder.build();

			return result;
		}

		private int downloadPetIds(final ArrayList<Long> resultList, final int nextOffset)
		{
			int lastOffset = 0;

			final Uri uri = createGetPetsUri(nextOffset, "id");
			final String url = uri.toString();

			final DefaultHttpClient client = new DefaultHttpClient();
			final HttpGet getRequest = new HttpGet(url);

			try
			{
				final HttpResponse response = client.execute(getRequest);
				final int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == HttpStatus.SC_OK)
				{
					final HttpEntity entity = response.getEntity();
					if (entity != null)
					{
						InputStream inputStream = null;
						try
						{
							inputStream = entity.getContent();
							if (inputStream != null)
							{
								final String jsonString = Util.toString(inputStream);
								final JSONTokener jsonTokener = new JSONTokener(jsonString);
								final JSONObject object = (JSONObject) jsonTokener.nextValue();
								final JSONObject petfinder = object.getJSONObject("petfinder");
								final JSONObject petIds = petfinder.getJSONObject("petIds");
								final JSONArray idArray = Util.getSafeJSONArray(petIds, "id");
								if (idArray != null)
								{
									final int length = idArray.length();
									for (int i=0; i<length; i++)
									{
										final JSONObject id = idArray.getJSONObject(i);
										final String idStr = id.getString(JSON_TAG_PLACEHOLDER);
										final long idLong = Long.parseLong(idStr);
										resultList.add(idLong);
									}
	
									final JSONObject offset = petfinder.getJSONObject("lastOffset");
									final String offsetStr = offset.getString(JSON_TAG_PLACEHOLDER);
									lastOffset = Integer.parseInt(offsetStr);
								}
							}
						}
						catch (JSONException e)
						{
							// TODO: Diagnose and recover...
//							final String message = e.getMessage();
//							Log.e("JSON", message);
						}
						catch (Throwable t)
						{
							// TODO: Diagnose and recover...
//							final String message = t.getMessage();
//							Log.e("JSON", message);
						}
						finally
						{
							if (inputStream != null)
							{
								inputStream.close();
							}
							entity.consumeContent();
						}
					}
				}
			}
			catch (Exception e)
			{
				// TODO: provide a more explicit error message for IOException or IllegalStateException
//				final String message = e.getMessage();
//				Log.e("downloadPetIds", message);
				getRequest.abort();
			}

			final int result = lastOffset;

			return result;
		}

		private Uri createGetPetUri(final String petId)
		{
			// example: http://api.petfinder.com/pet.get?key=deb1a50a01d45c5c20a4c5c93eb75dcd&id=42&format=json
			final Uri.Builder uriBuilder = new Uri.Builder();

			uriBuilder.scheme(SCHEME);
			uriBuilder.authority(DOMAIN);
			uriBuilder.appendPath(PET_GET);
			uriBuilder.appendQueryParameter("key", DEV_KEY);
			uriBuilder.appendQueryParameter("id", petId);
			uriBuilder.appendQueryParameter("format", "json");

			final Uri result = uriBuilder.build();

			return result;
		}

		private PetfinderProvider.PetRecord downloadPet(final long pfPetId)
		{
			PetfinderProvider.PetRecord result = null;

			final String pfPetIdStr = String.valueOf(pfPetId);
			final Uri uri = createGetPetUri(pfPetIdStr);
			final String uriStr = uri.toString();

			final DefaultHttpClient client = new DefaultHttpClient();
			final HttpGet getRequest = new HttpGet(uriStr);

			try
			{
				final HttpResponse response = client.execute(getRequest);
				final int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == HttpStatus.SC_OK)
				{
					final HttpEntity entity = response.getEntity();
					if (entity != null)
					{
						InputStream inputStream = null;
						try
						{
							inputStream = entity.getContent();
							if (inputStream != null)
							{
								int length;

								final String jsonString = Util.toString(inputStream);
								final JSONTokener jsonTokener = new JSONTokener(jsonString);
								final JSONObject object = (JSONObject) jsonTokener.nextValue();
								final JSONObject petfinder = object.getJSONObject("petfinder");
								final JSONObject pet = petfinder.getJSONObject("pet");

								PetfinderProvider.PetRecord petRecord = new PetfinderProvider.PetRecord(context_);

								final JSONObject pfId = pet.getJSONObject("id");
								final long pfIdLong = pfId.getLong(JSON_TAG_PLACEHOLDER);
								petRecord.setPFId(pfIdLong);

								String shelterPetId = String.valueOf(pfIdLong);
								final String shelterPetIdStr = Util.getSafeJSONString(pet, "shelterPetId");
								if (!TextUtils.isEmpty(shelterPetIdStr))
								{
									shelterPetId = shelterPetIdStr;
								}
								petRecord.setSheterPetId(shelterPetId);

								final JSONObject status = pet.getJSONObject("status");
								final String statusStr = status.getString(JSON_TAG_PLACEHOLDER);
								petRecord.setStatus(statusStr.charAt(0));

								final JSONObject name = pet.getJSONObject("name");
								final String nameStr = name.getString(JSON_TAG_PLACEHOLDER);
								petRecord.setName(nameStr);

								final JSONObject options = pet.getJSONObject("options");
								final JSONArray optionArray = Util.getSafeJSONArray(options, "option");
								if (optionArray != null)
								{
									length = optionArray.length();
									for (int i=0; i<length; i++)
									{
										final JSONObject option = optionArray.getJSONObject(i);
										final String optionStr = option.getString(JSON_TAG_PLACEHOLDER);
										petRecord.setOption(optionStr);
									}
								}

								final JSONObject breeds = pet.getJSONObject("breeds");
								final JSONArray breedArray = Util.getSafeJSONArray(breeds, "breed");
								if (breedArray == null)
								{
									final String breedStr = Util.getSafeJSONString(breeds, "breed");
									petRecord.setBreed(breedStr);
								}
								else
								{
									length = breedArray.length();
									for (int i=0; i<length; i++)
									{
										final JSONObject breed = breedArray.getJSONObject(i);
										final String breedStr = breed.getString(JSON_TAG_PLACEHOLDER);
										petRecord.setBreed(breedStr);
									}
								}

								final String description = Util.getSafeJSONString(pet, "description");
								petRecord.setDescription(description);

								final String sex = Util.getSafeJSONString(pet, "sex");
								petRecord.setSex(sex);

								final String age = Util.getSafeJSONString(pet, "age");
								petRecord.setAge(age);

								final String size = Util.getSafeJSONString(pet, "size");
								petRecord.setSize(size);

								final String mix = Util.getSafeJSONString(pet, "mix");
								petRecord.setMix(mix);

								final String lastUpdate = Util.getSafeJSONString(pet, "lastUpdate");
								petRecord.setLastUpdate(lastUpdate);

								final JSONObject media = pet.getJSONObject("media");
								final JSONObject photos = media.getJSONObject("photos");
								final JSONArray photoArray = photos.getJSONArray("photo");

								length = photoArray.length();
								for (int i=0; i<length; i++)
								{
									final JSONObject photo = photoArray.getJSONObject(i);
									final String photoUriStr = photo.getString(JSON_TAG_PLACEHOLDER);
									petRecord.setPhotoBaseUri(photoUriStr);
								}

								String speciesStr = "animal";
								final JSONObject species = pet.optJSONObject("animal");
								if (species != null)
								{
									speciesStr = species.getString(JSON_TAG_PLACEHOLDER);
								}
								if (TextUtils.isEmpty(speciesStr))
								{
									speciesStr = "animal";
								}
								petRecord.setSpecies(speciesStr);

								if (petRecord.isValid())
								{
									result = petRecord;
								}
							}
						}
						catch (JSONException e)
						{
							// TODO: Diagnose and recover...
//							final String message = e.getMessage();
//							Log.e("ID: " + pfPetIdStr, message);
						}
						catch (Throwable t)
						{
							// TODO: Diagnose and recover...
//							final String message = t.getMessage();
//							Log.e("ID: " + pfPetIdStr, message);
						}
						finally
						{
							if (inputStream != null)
							{
								inputStream.close();
							}
							entity.consumeContent();
						}
					}
				}
			}
			catch (Exception e)
			{
				// TODO: provide a more explicit error message for IOException or IllegalStateException
//				final String message = e.getMessage();
//				Log.e("downloadPet", message);
				getRequest.abort();
			}

			return result;
		}

		private Long[] downloadPetIds()
		{
			Long[] result = null;

			final ArrayList<Long> resultList = new ArrayList<Long>(1024);

			int nextOffset = 0;

			for (;;)
			{
				final int lastOffset = downloadPetIds(resultList, nextOffset);
				if (lastOffset <= nextOffset)
				{
					break;
				}
				nextOffset = lastOffset;
			}

			final int size = resultList.size();
			if (size > 0)
			{
				result = new Long[size];
				resultList.toArray(result);
			}

			return result;
		}

		private void update()
		{
			final Long[] petfinderPetId = downloadPetIds();

			if (petfinderPetId!=null && petfinderPetId.length>0)
			{
				boolean failure = false;

				final Time now = new Time();
				now.setToNow();

				final ContentResolver contentResolver = context_.getContentResolver();

				final ContentValues removePendValues = new ContentValues();

				removePendValues.put(PetfinderProvider.FIELD_PET_PF_STATUS, Character.toString(PetfinderProvider.VALUE_FIELD_PET_PF_STATUS_REMOVAL_PENDING));

				contentResolver.update(PetfinderProvider.CONTENT_URI_PET, removePendValues, null, null);

				for (final Long pfPetId: petfinderPetId)
				{
					try
					{
						final ContentValues pfPetIdValues = new ContentValues();

						pfPetIdValues.put(PetfinderProvider.FIELD_PET_PF_ID, pfPetId);

						final String[] selectionArgs = new String[] {pfPetId.toString()};

						final Cursor cursor = contentResolver.query(PetfinderProvider.CONTENT_URI_PET, pfIdProjection_, PF_ID_SELECTION, selectionArgs, null);

						if (cursor != null)
						{
							try
							{
								final boolean petExists = cursor.moveToFirst();

								if (petExists)
								{
									final int modifiedTimeIndex = cursor.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_MODIFIED_TIME);
									final long modifiedTimeMillisec = cursor.getLong(modifiedTimeIndex);
									final Time expiration = new Time();
									expiration.set(modifiedTimeMillisec + EXPIRATION_DAYS * Util.MILSEC_PER_DAY);

									final boolean obsolete = now.after(expiration);

									final ContentValues updateValues = new ContentValues();

									if (obsolete)
									{
										final PetfinderProvider.PetRecord petRecord = downloadPet(pfPetId);
										if (petRecord!=null && petRecord.isValid())
										{
											petRecord.initContentValues(updateValues);
										}
									}

									updateValues.put(PetfinderProvider.FIELD_PET_PF_STATUS, Character.toString(PetfinderProvider.VALUE_FIELD_PET_PF_STATUS_ADOPTABLE));
									updateValues.remove(PetfinderProvider.FIELD_PET_FAVORITE); // Don't modify the user's favorite.
									contentResolver.update(PetfinderProvider.CONTENT_URI_PET, updateValues, PF_ID_SELECTION, selectionArgs);
								}
								else
								{
									final PetfinderProvider.PetRecord petRecord = downloadPet(pfPetId);
									if (petRecord!=null && petRecord.isValid())
									{
										final ContentValues insertValues = new ContentValues();
										petRecord.initContentValues(insertValues);
										contentResolver.insert(PetfinderProvider.CONTENT_URI_PET, insertValues);
									}
								}
							}
							finally
							{
								cursor.close();
							}
						}
					}
					catch (Throwable e)
					{
						failure = true;
					}
				}

				if (!failure)
				{
					contentResolver.delete(PetfinderProvider.CONTENT_URI_PET, REMOVE_NONADOPTABLE, null);
				}
			}
		}
	}

	private static final String TAG = "UpdateService";

	public static final String ACTION = "org.catadoptionteam.catdroid.intent.action.UPDATE"; // TODO: Where is this used?

	private static final String KEY_PREV_NOTIFY_DAY = "UPDATE_SERVICE_PREV_NOTIFY_DAY";
	private static final String KEY_PREV_UPDATE_DAY = "UPDATE_SERVICE_PREV_UPDATE_DAY";

	private SharedPreferences preferences_;
	private PowerManager.WakeLock wakeLock_;

	private NotificationManager notificationManager_;
	/**
	 * 
	 */
	public UpdateService()
	{
		super("UpdateService");
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		preferences_ = PreferenceManager.getDefaultSharedPreferences(this);
		notificationManager_ = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		final PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE); 
		wakeLock_ = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG); 
		wakeLock_.setReferenceCounted(true); // Not required, reference counted by default.
	}

//	@Override
//	public void onStart(Intent intent, int startId)
//	{
//		super.onStart(intent, startId);
//	}

//	@Override
//	public IBinder onBind(Intent arg0)
//	{
//		return null;
//	}

//	@Override
//	public void onDestroy()
//	{
//		super.onDestroy();
//	}

	@Override
	protected void onHandleIntent(Intent arg0)
	{
		update();
		notifyUser();
	}

	private int getDayOfYear()
	{
		final int result = Util.getDayOfYear(this);
		return result;
	}

	private boolean getNotifiedToday()
	{
		final int prevNotifyDay = preferences_.getInt(KEY_PREV_NOTIFY_DAY, -1);
		final int dayOfYear = getDayOfYear();
		final boolean result = (dayOfYear == prevNotifyDay);
		return result;
	}

	private void setNotifiedToday()
	{
		final SharedPreferences.Editor editor = preferences_.edit();
		final int dayOfYear = getDayOfYear();
		editor.putInt(KEY_PREV_NOTIFY_DAY, dayOfYear);
		editor.commit();
	}

	private boolean getUpdatedToday()
	{
		final int prevDayofYearUpdate = preferences_.getInt(KEY_PREV_UPDATE_DAY, -1);
		final int dayOfYear = getDayOfYear();
		final boolean result = (dayOfYear == prevDayofYearUpdate);
		return result;
	}

	private void setUpdatedToday()
	{
		final SharedPreferences.Editor editor = preferences_.edit();
		final int dayOfYear = getDayOfYear();
		editor.putInt(KEY_PREV_UPDATE_DAY, dayOfYear);
		editor.commit();
	}

	public static void clearUpdatedToday(final SharedPreferences preferences)
	{
		final SharedPreferences.Editor editor = preferences.edit();
		editor.remove(KEY_PREV_UPDATE_DAY);
		editor.commit();
	}

	private void displayNotificationMessage(final int titleResId, final int messageResId, Class<?> cls)
	{
		setNotifiedToday();

		final String title = getString(titleResId);
		final String message = getString(messageResId);

		final Notification notification = 
			new Notification(R.drawable.ic_notify, message, System.currentTimeMillis());

		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		final Intent intent = new Intent(this, cls);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(this, title, message, contentIntent);
		notificationManager_.notify(titleResId, notification); // Using the title resource id as the notification id.
	}

	private void notifyUser()
	{
		final boolean notify = Util.isNotifyEnabled(this) && !getNotifiedToday();
		if (notify)
		{
			final Context context = CATApp.getAppContext();
			final SearchFilter searchFilter = new SearchFilter(context);
			final boolean notifyNewMatch = searchFilter.getNotifyNewMatch();
			if (notifyNewMatch)
			{
				final boolean newSearchMatched = searchFilter.todayMatches();
				if (newSearchMatched)
				{
					displayNotificationMessage(R.string.pet_search, R.string.new_pet_match_found, SearchActivity.class);
				}
			}

			final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			final int userFelineality = MYMSurvey.loadFelineality(preferences);
			final boolean isFelineality = MYMSurvey.isFelineality(userFelineality);
			if (isFelineality)
			{
				final int keyID = Util.getCOTDID(null);
				if (keyID > AppDBAdapter.INVALID_KEY_ID)
				{
					final AppDBAdapter appDBAdapter = new AppDBAdapter(this);
					final Cat cat = appDBAdapter.getAnimal(null, keyID);
					if (cat != null)
					{
						final int cotdFelineality = cat.getASPCAality();
						if (cotdFelineality == userFelineality)
						{
							displayNotificationMessage(R.string.cotd, R.string.mym_perfect, BiographyActivity.class);
						}
					}
					appDBAdapter.close();
				}
			}
		}
	}

	private void updateData()
	{
		final ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		if (cm.getBackgroundDataSetting())
		{
			final AnimalUpdateWorker animalUpdater = new AnimalUpdateWorker(this, wakeLock_);
			animalUpdater.run();
			MainActivity.signalUpdateCompleted();

			final PetfinderUpdateWorker petfinderUpdater = new PetfinderUpdateWorker(this, wakeLock_);
			petfinderUpdater.run();

			final VideoUpdateWorker videoUpdater = new VideoUpdateWorker(this, wakeLock_);
			videoUpdater.run();
		}
		MainActivity.signalUpdateCompleted();
	}

	private void update()
	{
		final boolean updatedToday = getUpdatedToday();

		if (!updatedToday)
		{
			updateData();
			setUpdatedToday();
		}
	}
}
