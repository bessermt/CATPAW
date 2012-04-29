/**
 * 
 */
package org.catadoptionteam.catphonesrv.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.catadoptionteam.catphonesrv.shared.Cat;
import org.catadoptionteam.catphonesrv.shared.Cat.Felineality;
import org.catadoptionteam.catphonesrv.shared.Cat.Sex;

import com.google.appengine.api.datastore.Text;

/**
 * @author bessermt
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public final class CatRecord
{
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
//	private Key id; // TODO: ???

	@Persistent
	private String petID; // Unique shelter ID

	@Persistent
	private String name; // Commonly used name

	@Persistent
	private int category; // COTD or M9L

	@Persistent
	private String expDate; // Date for Cat of the Day in ISO 8601 format.  TTL for M9L.

	@Persistent
	private boolean adopted;

	@Persistent
	private String dob; // Date Of Birth in ISO 8601 format

	@Persistent
	private Sex sex;

	@Persistent
	private Felineality felineality;

	@Persistent
	private Text biography; // biographical description

	@Persistent
	private String lgPhotoURL; // Large size photo URL

	@Persistent
	private String smPhotoURL; // Small size photo URL

	@Persistent
	private String extraID; // Additional ID

	@Persistent
	private String extraURL; // Additional info webpage

	public CatRecord(final Cat cat)
	{
		id = cat.getID();
		petID = cat.getPetID();
		name = cat.getName();
		category = cat.getCategory();
		expDate = cat.getExpDate();
		adopted = cat.isAdopted();
		dob = cat.getDOB();
		sex = cat.getSex();
		felineality = cat.getFelineality();
		biography = new Text(cat.getBiography());
		lgPhotoURL = cat.getLgPhotoURL();
		smPhotoURL = cat.getSmPhotoURL();
		extraID = cat.getExtraID();
		extraURL = cat.getExtraURL();
	}

	public Cat getCat()
	{
		String bioString = null;
		if (biography != null)
		{
			bioString = biography.getValue();
		}
		final Cat result = new Cat(id, petID, name, category, expDate, adopted, dob, sex, felineality, bioString, lgPhotoURL, smPhotoURL, extraID, extraURL);
		return result;
	}

	public Long getID()
	{
		final Long result = id;
		return result;
	}
}
