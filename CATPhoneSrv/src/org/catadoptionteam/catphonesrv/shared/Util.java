/**
 * 
 */
package org.catadoptionteam.catphonesrv.shared;

import java.util.Date;

import com.google.gwt.i18n.shared.DateTimeFormat;

/**
 * @author bessermt
 *
 */
public class Util
{
	private static final int MILSEC_PER_SEC = 1000;
	private static final int SEC_PER_MIN = 60;
	private static final int MIN_PER_HR = 60;
	private static final int HR_PER_DAY =24;

	public static final int MILSEC_PER_DAY = MILSEC_PER_SEC * SEC_PER_MIN * MIN_PER_HR * HR_PER_DAY;

	private static final String ISO8601_DATE_FORMAT = "yyyy-MM-dd";
	//private static final DateTimeFormat fmt = DateTimeFormat.getFormat(ISO8601_DATE_FORMAT);

	public static String toString(final Date date)
	{
		String result = null;
		if (date != null)
		{
			// final SimpleDateFormat fmt = new SimpleDateFormat(ISO8601_DATE_FORMAT);
			final int year = date.getYear() + 1900;  // Deprecated, but I don't see a viable alternative.  
			final String yearStr = Integer.toString(year);

			String zero;

			final int month = date.getMonth() + 1;  // Deprecated, but I don't see a viable alternative.  
			zero = (month < 10 ? "0" : "");
			final String monthStr = zero + Integer.toString(month);

			final int day = date.getDate();  // Deprecated, but I don't see a viable alternative.  
			zero = (day < 10 ? "0" : "");
			final String dayStr = zero + Integer.toString(day);

			result = yearStr + "-" + monthStr + "-" + dayStr;
		}
		return result;
	}

	public static String norm(final String text)
	{
		final String result = text.replace("\r\n", "\n").replace("\r", "\n");
		return result;
	}

	public static Date parseDate(final String dateISO8601)
	{
		final Date result = DateTimeFormat.getFormat(ISO8601_DATE_FORMAT).parse(dateISO8601);
		return result;
	}
}
