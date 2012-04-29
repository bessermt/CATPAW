/**
 * 
 */
package org.catadoptionteam.catphonesrv.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author bessermt
 *
 */
public interface LoginServiceAsync
{
	/**
	 * 
	 * @see org.catadoptionteam.catphonesrv.client.LoginService#login(java.lang.String)
	 */
	void login(String requestUri, AsyncCallback<LoginInfo> callback);
}
