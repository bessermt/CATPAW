/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.os.Bundle;
import android.widget.TextView;


public class SearchFavoriteActivity extends SuperSearchList
{
	/* (non-Javadoc)
	 * @see org.catadoptionteam.catdroid.SuperSearchList#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		final TextView textViewEmpty = (TextView)findViewById(android.R.id.empty);
		textViewEmpty.setText(R.string.no_pets_favorite);
	}

	@Override
	protected String getWhere()
	{
		final String result = PetfinderProvider.FIELD_PET_FAVORITE + "!=0";
		return result;
	}

	@Override
	protected String getSortOrderBy()
	{
		final String result = PetfinderProvider.ORDER_BY_NAME;
		return result;
	}
}
