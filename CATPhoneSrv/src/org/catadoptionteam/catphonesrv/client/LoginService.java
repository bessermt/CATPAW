/**
 * 
 */
package org.catadoptionteam.catphonesrv.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @author bessermt
 *
 */
@RemoteServiceRelativePath("login")
public interface LoginService extends RemoteService
{
	public LoginInfo login(String requestUri);
}
