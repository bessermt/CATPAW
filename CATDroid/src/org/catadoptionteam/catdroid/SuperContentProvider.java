/**
 * 
 */
package org.catadoptionteam.catdroid;

import java.util.List;

import android.content.ContentProvider;
import android.net.Uri;
import android.text.TextUtils;

/**
 * @author bessermt
 *
 */
public abstract class SuperContentProvider extends ContentProvider
{
	protected static final String _ID = "_id";
	protected static final String _DATA = "_data";

	protected static String getID(final Uri uri)
	{
		String result = null;

		final List<String> path = uri.getPathSegments();
		final int size = path.size();
		if (size > 1) // Currently expecting exactly 2 path segments, but this may change.  
		{
			result = path.get(size-1); // Last path segment
		}
		return result;
	}

	protected static String makeWhereClause(final Uri uri, final String where)
	{
		String result = null;

		final boolean isWhere = !TextUtils.isEmpty(where);
		String terminator = null;

		final String rowID = getID(uri);
		if (rowID != null)
		{
			result = _ID + "=" + rowID;
			if (isWhere)
			{
				result += " AND (";
				terminator = ")";
			}
		}
		if (isWhere)
		{
			if (result == null)
			{
				result = where;
			}
			else
			{
				result += where;
			}
		}
		if (result!=null && terminator!=null)
		{
			result += terminator;
		}

		return result;
	}
}
