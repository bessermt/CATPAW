/**
 * 
 */
package org.catadoptionteam.catphonesrv.server;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

/**
 * @author bessermt
 * see ISBN 978-1-93435-663-0, p144
 */
public enum Persister
{
	INSTANCE;

	private static final PersistenceManagerFactory pmfInstance = 
		JDOHelper.getPersistenceManagerFactory("transactions-optional");

	public static PersistenceManager getPersistenceManager()
	{
		final PersistenceManager result = pmfInstance.getPersistenceManager();
		return result;
	}
}
