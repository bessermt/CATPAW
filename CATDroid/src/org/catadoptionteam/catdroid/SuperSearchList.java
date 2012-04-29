/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

/**
 * @author bessermt
 *
 */
public class SuperSearchList extends ListActivity implements OnClickListener
{
//	Checkbox in listview rules:
//
//		android:focusable="false"
//		android:focusableInTouchMode="false"
//		// Don't android:inputType="textMultiLine" or android:autoLink
//		// android:minLines and android:maxLines are okay
//
//		If you have a ImageButton in the list, call imageButton.setFocusable(false)
//
//		Set OnItemClickListener before setting Adapter

	class SearchListAdapter extends ResourceCursorAdapter implements Filterable
	{
		protected String getSortOrderBy()
		{
			final String result = PetfinderProvider.ORDER_BY_NAME;
			return result;
		}

		private Context context_;
		private ContentResolver contentResolver_;

		private int layout_;
		private LayoutInflater inflater_;

		private int colId_;
		private int colData_;
		private int colName_;
		private int colCreated_;
		private int colModified_;
		private int colFavorite_;

		private final OnClickListener thumbnailClickListener_ = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final Long petIdLong = (Long) v.getTag(R.id.pet_id);
				final long petId = petIdLong.longValue();
				showPetBio(petId);
			}
		};

		private final OnClickListener favoriteClickListener_ = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final long id = (Long) v.getTag();
				final CheckBox checkboxFavorite = (CheckBox) v;
				final boolean isChecked = checkboxFavorite.isChecked();
				PetfinderProvider.updateFavorite(contentResolver_, id, isChecked);
			}
		};

//		private final OnCheckedChangeListener favoriteChangeListener_ = new OnCheckedChangeListener()
//		{
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
//			{
//				final long dbRecId = (Long) buttonView.getTag();
//				if (dbRecId > 0)
//				{
//					// notifyDataSetChanged(); // TODO: calling this seems to make the checkbox not work. 
//				}
//			}
//			
//		};

		public SearchListAdapter(final Context context, final int layout, final Cursor c, final int flags)
		{
			// super(context, c, false);
			super(context, layout, c);

			context_ = context;
			layout_ = layout;
			inflater_ = LayoutInflater.from(context);
			contentResolver_ = context.getContentResolver();

			colId_ = c.getColumnIndexOrThrow(PetfinderProvider._ID);
			colData_ = c.getColumnIndexOrThrow(PetfinderProvider._DATA);
			colName_ = c.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_PF_NAME);
			colCreated_ = c.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_CREATED_TIME);
			colModified_ = c.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_MODIFIED_TIME);
			colFavorite_ = c.getColumnIndexOrThrow(PetfinderProvider.FIELD_PET_FAVORITE);
		}

		// TODO: http://stackoverflow.com/questions/1737009/how-to-make-a-nice-looking-listview-filter-on-android
//		@Override
//		public Filter getFilter()
//		{
//			final Filter result = super.getFilter();
//			return result;
//		}

		/* (non-Javadoc)
		 * @see android.widget.SimpleCursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
		 */
		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			// super.bindView(view, context, cursor); // Intentionally not calling super method. 

			final Long id = new Long(cursor.getLong(colId_));
			final String thumbnailFilename = cursor.getString(colData_);
			final String name = cursor.getString(colName_);
			final Long created = cursor.getLong(colCreated_);
			final Long modified = cursor.getLong(colModified_);
			final int favoriteValue = cursor.getInt(colFavorite_);
			final boolean favorite = favoriteValue != 0;

			view.setTag(R.id.pet_id, id);
			view.setTag(R.id.pet_created, created);
			view.setTag(R.id.pet_modified, modified);

			final ImageButton thumbnail = (ImageButton) view.findViewById(R.id.imageThumbnail);
			final TextView textName = (TextView) view.findViewById(R.id.textName);
			final CheckBox checkFavorite = (CheckBox) view.findViewById(R.id.checkFavorite);

			thumbnail.setFocusable(false);
			thumbnail.setTag(R.id.pet_id, id);

			final Bitmap thumbnailBitmap = Util.getBitmap(context_, thumbnailFilename);
			thumbnail.setImageBitmap(thumbnailBitmap);
			thumbnail.setOnClickListener(thumbnailClickListener_);

			textName.setText(name);

			checkFavorite.setChecked(favorite);
			checkFavorite.setTag(id);
			checkFavorite.setOnClickListener(favoriteClickListener_);
//			checkFavorite.setOnCheckedChangeListener(favoriteChangeListener_);

//			view.setFocusable(false);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent)
		{
			final View result = inflater_.inflate(layout_, parent, false);

			return result;
		}

//		/* (non-Javadoc)
//		 * @see android.widget.CursorAdapter#getView(int, android.view.View, android.view.ViewGroup)
//		 */
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent)
//		{
//			// return super.getView(position, convertView, parent); // Intentionally not calling super method. 
//
//			ViewHolder viewHolder;
//
//			if (convertView == null)
//			{
//				convertView = inflater_.inflate(layout_, parent, false); // TODO: why not call newView(context_, null, parent);
//
//				viewHolder = new ViewHolder();
//				viewHolder.thumbnail_ = (ImageButton) convertView.findViewById(R.id.imageThumbnail);
//				viewHolder.name_ = (Button) convertView.findViewById(R.id.textName);
//				viewHolder.favorite_ = (CheckBox) convertView.findViewById(R.id.checkFavorite);
//
//				viewHolder.favorite_.setOnCheckedChangeListener
//				(
//					new OnCheckedChangeListener()
//					{
//						@Override
//						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
//						{
//							onFavoriteChanged(buttonView);
//						}
//					}
//				);
//
//				convertView.setTag(viewHolder);
//			}
//			else
//			{
//				viewHolder = (ViewHolder) convertView.getTag();
//			}
//			holder.text.setText(list.get(position).getName());
//			holder.checkbox.setChecked(list.get(position).isSelected());
//			return convertView;
//		}
//
//		private setView()
//		{
//			
//		}
	}

//	private final OnClickListener showPetListener_ = new OnClickListener()
//	{
//		@Override
//		public void onClick(View v)
//		{
//			final Long petId = (Long) v.getTag(R.id.pet_id);
//			showPetBio(petId.longValue());
//		}
//
//		private void showPetBio(final long petId)
//		{
//			final Intent petBioIntent = new Intent(context_, PetfinderBioActivity.class);
//			petBioIntent.putExtra(PetfinderProvider._ID, petId);
//			Util.startActivity(context_, petBioIntent);
//		}
//	};

	private Cursor cursor_;
	protected SearchFilter searchFilter_;
	protected ListView listView_;
	protected SearchListAdapter adapter_;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_list);

		listView_ = getListView();

		searchFilter_ = SearchFilterActivity.getSearchFilter(this);

		final String where = getWhere();
		final String sortOrder = getSortOrderBy();
		setAdapter(where, sortOrder);
	}

//	/* (non-Javadoc)
//	 * @see android.app.Activity#onDestroy()
//	 */
//	@Override
//	protected void onDestroy()
//	{
//		super.onDestroy();
//		if (cursor_ != null)
//		{
//			cursor_.close();
//			cursor_ = null;
//		}
//	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
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

	protected void setAdapter(final String where, final String sortOrder)
	{
		final String[] projection = new String[]
			{
				PetfinderProvider._ID, 
				PetfinderProvider._DATA, 
				PetfinderProvider.FIELD_PET_PF_NAME, 
				PetfinderProvider.FIELD_PET_CREATED_TIME, 
				PetfinderProvider.FIELD_PET_MODIFIED_TIME, 
				PetfinderProvider.FIELD_PET_FAVORITE
			};

		final ContentResolver contentResolver = getContentResolver();
		cursor_ = contentResolver.query(PetfinderProvider.CONTENT_URI_PET, projection, where, null, sortOrder);

		if (cursor_ != null)
		{
			startManagingCursor(cursor_);

			listView_.setOnItemClickListener // list.setOnItemClickListener() Must be called before calling listView.setAdapter().
			(
				new OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent, View v, int position, long id)
					{
						final Long petIdLong = (Long) v.getTag(R.id.pet_id);
						final long petId = petIdLong.longValue();
						showPetBio(petId);
					}
				}
			);

//			final String[] cols = new String[]{PetfinderProvider._DATA, PetfinderProvider.FIELD_PET_PF_NAME, PetfinderProvider.FIELD_PET_FAVORITE};
//			final int[] names = new int[]{R.id.imageThumbnail, R.id.textName, R.id.checkFavorite};

			adapter_ = new SearchListAdapter(this, R.layout.search_list_item, cursor_, 0);
			listView_.setAdapter(adapter_); // setListAdapter(adapter_);
		}
	}

	private void showPetBio(final long petId)
	{
		final Intent petfinderBioIntent = new Intent(this, PetfinderBioActivity.class);
		petfinderBioIntent.putExtra(PetfinderProvider._ID, petId);
		Util.startActivity(this, petfinderBioIntent);
	}

	// Here for @Override in Sub-classes.
	protected String getWhere()
	{
		return null;
	}

	// Here for @Override in Sub-classes.
	protected String getSortOrderBy()
	{
		return null;
	}

	private void close()
	{
		finish();
	}
}
