/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.os.Bundle;
import android.widget.TextView;



public class SearchMatchActivity extends SuperSearchList
{
	/* (non-Javadoc)
	 * @see org.catadoptionteam.catdroid.SuperSearchList#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		final TextView textViewEmpty = (TextView)findViewById(android.R.id.empty);
		textViewEmpty.setText(R.string.no_pets_match);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume()
	{
		super.onResume();

		final boolean dirty = searchFilter_.getDirty();
		if (dirty)
		{
			searchFilter_.setDirty(false);
			final String where = getWhere();
			final String sortOrder = getSortOrderBy();
			setAdapter(where, sortOrder);
		}
	}

	@Override
	protected String getWhere()
	{
		final String result = searchFilter_.getWhere();
		return result;
	}

	@Override
	protected String getSortOrderBy()
	{
		final String result = searchFilter_.getSortOrderBy();
		return result;
	}
}
