/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.text.TextUtils;

/**
 * @author bessermt
 *
 */
public class ConjunctionBuilder
{
	private String conjunction_;
	private String buffer_ = null;

	public ConjunctionBuilder(final String conjunction)
	{
		conjunction_ = conjunction;
	}

	public void append(final String subclause)
	{
		if (!TextUtils.isEmpty(subclause))
		{
			if (buffer_ == null)
			{
				buffer_ = subclause;
			}
			else
			{
				buffer_ += " " + conjunction_ + " " + subclause;
			}
		}
	}

	public void append(final int value)
	{
		final String subclause = String.valueOf(value);
		append(subclause);
	}

	public boolean isEmpty()
	{
		final boolean result = TextUtils.isEmpty(buffer_);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return buffer_;
	}
}
