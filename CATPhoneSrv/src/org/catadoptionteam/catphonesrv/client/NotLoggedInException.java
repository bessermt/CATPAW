/**
 * 
 */
package org.catadoptionteam.catphonesrv.client;

import java.io.Serializable;

/**
 * @author bessermt
 *
 */
public class NotLoggedInException extends Exception implements Serializable
{
	/**
	 * 
	 */
	public NotLoggedInException()
	{
		super();
	}

	/**
	 * @param message
	 */
	public NotLoggedInException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public NotLoggedInException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NotLoggedInException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
