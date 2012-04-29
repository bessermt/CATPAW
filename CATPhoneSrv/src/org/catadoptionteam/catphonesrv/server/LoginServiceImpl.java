/**
 * 
 */
package org.catadoptionteam.catphonesrv.server;

import org.catadoptionteam.catphonesrv.client.LoginInfo;
import org.catadoptionteam.catphonesrv.client.LoginService;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author bessermt
 *
 */
public class LoginServiceImpl extends RemoteServiceServlet implements LoginService
{
	private static final String AUTH_EMAIL = "catnipcat2011@gmail.com";

	public LoginInfo login(String requestUri)
	{
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		LoginInfo loginInfo = new LoginInfo();

		String email = null;

		boolean authorized = false;

		if (user != null)
		{
			email = user.getEmail();
			if (email.equals(AUTH_EMAIL))
			{
				authorized = true;
			}
		}

		if (authorized)
		{
			loginInfo.setLoggedIn(true);
			loginInfo.setEmailAddress(email);
			loginInfo.setNickname(user.getNickname());
			loginInfo.setLogoutUrl(userService.createLogoutURL(requestUri));
		}
		else
		{
			loginInfo.setLoggedIn(false);
			loginInfo.setLoginUrl(userService.createLoginURL(requestUri));
		}
		return loginInfo;
	}
}
