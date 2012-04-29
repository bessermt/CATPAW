/**
 * 
 */
package org.catadoptionteam.catphonesrv.client;

import org.catadoptionteam.catphonesrv.shared.Cat;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @author bessermt
 *
 */
@RemoteServiceRelativePath("cotd")
public interface CatService extends RemoteService
{
	Cat[] getCOTDCats();
	Long setCOTDCat(Cat cat);
	void deleteOldCOTDCats();

	Cat[] getM9LCats();
	Long setM9LCat(Cat cat);
}
