package org.catadoptionteam.catphonesrv.shared;

/**
 * <p>
 * FieldVerifier validates that the name the user enters is valid.
 * </p>
 * <p>
 * This class is in the <code>shared</code> package because we use it in both
 * the client code and on the server. On the client, we verify that the name is
 * valid before sending an RPC request so the user doesn't have to wait for a
 * network round trip to get feedback. On the server, we verify that the name is
 * correct to ensure that the input is correct regardless of where the RPC
 * originates.
 * </p>
 * <p>
 * When creating a class that is used on both the client and the server, be sure
 * that all code is translatable and does not use native JavaScript. Code that
 * is not translatable (such as code that interacts with a database or the file
 * system) cannot be compiled into client side JavaScript. Code that uses native
 * JavaScript (such as Widgets) cannot be run on the server.
 * </p>
 */
public class FieldVerifier {

	/**
	 * Verifies that the specified name is valid for our service.
	 * 
	 * In this example, we only require that the name is at least four
	 * characters. In your application, you can use more complex checks to ensure
	 * that usernames, passwords, email addresses, URLs, and other fields have the
	 * proper syntax.
	 * 
	 * @param name the name to validate
	 * @return true if valid, false if invalid
	 */
	public static boolean isValidName(String name) {
		if (name == null) {
			return false;
		}
		return name.length() > 3;
	}

	public static boolean isRequired(final String field)
	{
		boolean result = false;
		if (field != null)
		{
			result = !field.isEmpty();
		}
		return result;
	}

	public static boolean isVisible(final String field)
	{
		boolean result = false;
		final byte[] bytes = field.getBytes(); // The behavior of getBytes() when field cannot be encoded in the default charset is unspecified.
		if (bytes.length == field.length()) // Check for a 1:1 encoded mapping
		{
			for (byte b: bytes)
			{
				result = (b >= 0x20 && b < 0x7f || b == '\n');
				if (!result)
				{
					break;
				}
			}
		}
		return result;
	}

	public static boolean isText(final String field)
	{
		boolean result = false;
		if (isRequired(field))
		{
			result = isVisible(field);
		}
		return result;
	}

	public static boolean isDate(final String field)
	{
		boolean result = false;
		if (isText(field))
		{
			final String value = field.trim();
			result = 
				value.length()==10 && 
				(
					value.charAt(0)=='1' && value.charAt(1)=='9' || 
					value.charAt(0)=='2' && value.charAt(1)=='0'
				) && 
				Character.isDigit(value.charAt(2)) && 
				Character.isDigit(value.charAt(3)) && 
				value.charAt(4)=='-' && 
				Character.isDigit(value.charAt(5)) && 
				Character.isDigit(value.charAt(6)) && 
				value.charAt(7)=='-' && 
				Character.isDigit(value.charAt(8)) && 
				Character.isDigit(value.charAt(9));
		}
		return result;
	}

	public static boolean isPhotoURL(final String field)
	{
		boolean result = false;
		if (isText(field))
		{
			result = field.startsWith("http://") && field.endsWith(".jpg");
		}
		return result;
	}
}
