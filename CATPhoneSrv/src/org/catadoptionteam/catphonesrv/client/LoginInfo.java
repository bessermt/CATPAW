/**
 * 
 */
package org.catadoptionteam.catphonesrv.client;

import java.io.Serializable;

/**
 * @author bessermt
 *
 */
public class LoginInfo implements Serializable
{
	private boolean loggedIn_ = false;
	private String loginUrl_;
	private String logoutUrl_;
	private String emailAddress_;
	private String nickname_;

	public boolean isLoggedIn()
	{
		return loggedIn_;
	}

	public void setLoggedIn(boolean loggedIn)
	{
		loggedIn_ = loggedIn;
	}

	public String getLoginUrl()
	{
		return loginUrl_;
	}

	public void setLoginUrl(String loginUrl)
	{
		loginUrl_ = loginUrl;
	}

	public String getLogoutUrl()
	{
		return logoutUrl_;
	}

	public void setLogoutUrl(String logoutUrl)
	{
		logoutUrl_ = logoutUrl;
	}

	public String getEmailAddress()
	{
		return emailAddress_;
	}

	public void setEmailAddress(String emailAddress)
	{
		emailAddress_ = emailAddress;
	}

	public String getNickname()
	{
		return nickname_;
	}

	public void setNickname(String nickname)
	{
		nickname_ = nickname;
	}
}
