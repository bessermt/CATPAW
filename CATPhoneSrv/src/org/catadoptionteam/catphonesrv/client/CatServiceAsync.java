/**
 * 
 */
package org.catadoptionteam.catphonesrv.client;

import org.catadoptionteam.catphonesrv.shared.Cat;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author bessermt
 *
 */
public interface CatServiceAsync
{
	void deleteOldCOTDCats(AsyncCallback<Void> callback);

	void getCOTDCats(AsyncCallback<Cat[]> callback);

	void setCOTDCat(Cat cat, AsyncCallback<Long> callback);

	void getM9LCats(AsyncCallback<Cat[]> callback);

	void setM9LCat(Cat cat, AsyncCallback<Long> callback);
}
