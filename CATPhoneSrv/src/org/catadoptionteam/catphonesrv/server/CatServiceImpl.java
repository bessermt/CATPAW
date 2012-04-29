/**
 * 
 */
package org.catadoptionteam.catphonesrv.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.catadoptionteam.catphonesrv.client.CatService;
import org.catadoptionteam.catphonesrv.shared.Cat;
import org.catadoptionteam.catphonesrv.shared.Util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author bessermt
 *
 */

public class CatServiceImpl extends RemoteServiceServlet implements CatService
{
	private static final String QUOTE = "\"";
	private static final String COLON = ":";
	private static final int M9L_EXP_DATE_DAYS_DELTA = 7;
	private static final long M9L_EXP_DATE_MILSEC_DELTA = M9L_EXP_DATE_DAYS_DELTA * Util.MILSEC_PER_DAY;

	private class CatSexSerializer implements JsonSerializer<Cat.Sex>
	{
		@Override
		public JsonElement serialize(Cat.Sex sex, Type arg1, JsonSerializationContext arg2)
		{
			final int value = sex.valueOf();
			return new JsonPrimitive(value);
		}
	};

	private class CatFelinealitySerializer implements JsonSerializer<Cat.Felineality>
	{
		@Override
		public JsonElement serialize(Cat.Felineality felineality, Type arg1, JsonSerializationContext arg2)
		{
			final int value = felineality.valueOf();
			return new JsonPrimitive(value);
		}
	};

	/**
	 * 
	 */
	public CatServiceImpl()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param delegate
	 */
	public CatServiceImpl(Object delegate)
	{
		super(delegate);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException
	{
		final String uri = req.getRequestURI();
		final boolean dailyClean = uri.equals("/dailyclean");
		if (dailyClean)
		{
			super.doGet(req, resp); // TODO: or Not TODO:?  At beginning or ending?  That is the question.

			final String cronRequstStr = req.getHeader("X-AppEngine-Cron");
			final boolean cronRequest = Boolean.parseBoolean(cronRequstStr);
			if (cronRequest)
			{
				deleteOldCOTDCats();
			}
		}
		else
		{
			Cat[] cats = null;
			String jsonName = null;

			final boolean m9l = uri.equals("/catphonesrv/m9l");
			final boolean cotd = uri.equals("/catphonesrv/cotd");

			if (m9l)
			{
				jsonName = "M9L";
				cats = getM9LCats();
			}
			else if (cotd)
			{
				jsonName = "COTD";
				cats = getCOTDCats();
			}
			if (cats != null)
			{
				final GsonBuilder gsonBuilder = new GsonBuilder();
				gsonBuilder.registerTypeAdapter(Cat.Sex.class, new CatSexSerializer());
				gsonBuilder.registerTypeAdapter(Cat.Felineality.class, new CatFelinealitySerializer());
				final Gson gson = gsonBuilder.create();

				resp.setContentType("application/json");
				final PrintWriter out = resp.getWriter();

				final String jsonCOTD = gson.toJson(cats);
				final String catString = "{"+ QUOTE + jsonName + QUOTE + COLON + jsonCOTD + "}";
				out.println(catString);
			}
			else
			{
				super.doGet(req, resp); // TODO: or Not TODO:?  At beginning or ending?  That is the question.
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.catadoptionteam.catphonesrv.client.CatService#getCOTDCats()
	 */
	@Override
	public Cat[] getCOTDCats()
	{
		final Cat[] result = getCats(Cat.CATEGORY_ANIMAL_OF_THE_DAY, "expDate");
		return result;
	}

	/* (non-Javadoc)
	 * @see org.catadoptionteam.catserve.client.AnimalDataService#setCOTDCats(org.catadoptionteam.catserve.shared.Cat[])
	 */
	@Override
	public Long setCOTDCat(Cat cat)
	{
		cat.setCategory(Cat.CATEGORY_ANIMAL_OF_THE_DAY);
		final Long id = setCat(cat);
		return id;
	}

	@Override
	public void deleteOldCOTDCats()
	{
//		pm.deletePersistentAll(); // TODO:  Find a way to delete the old records and reset the database.
		final PersistenceManager pm = getPersistenceManager();
		try
		{
			final Query q = pm.newQuery(CatRecord.class);

			final Date now = new Date();
			final String nowStr = Util.toString(now);
			final String filter = 
				"category == " + Integer.toString(Cat.CATEGORY_ANIMAL_OF_THE_DAY) + " && " + 
				"expDate < " + QUOTE + nowStr + QUOTE;
			q.setFilter(filter);

			// q.declareParameters("String aDateVariable");  // TODO: Delete
			q.deletePersistentAll();
		}
		catch (Throwable t)
		{
			final String message = t.getMessage();
			Window.alert(message);
		}
		finally
		{
			pm.close();
		}
	}

	@Override
	public Cat[] getM9LCats()
	{
		final Date now = new Date();
		final long expMilsec = now.getTime() + M9L_EXP_DATE_MILSEC_DELTA;
		final Date expDate = new Date(expMilsec);
		final String expDateStr = Util.toString(expDate);

		final Cat[] result = getCats(Cat.CATEGORY_M9L, "felineality");
		for (Cat cat: result)
		{
			cat.setExpDate(expDateStr);
		}

		return result;
	}

	@Override
	public Long setM9LCat(Cat cat)
	{
		cat.setCategory(Cat.CATEGORY_M9L);
		final Long id = setCat(cat);
		return id;
	}

	private static PersistenceManager getPersistenceManager()
	{
		final PersistenceManager result = Persister.getPersistenceManager();
		return result;
	}

	public Cat[] getCats(final int category, final String ordering)
	{
		final List<Cat> catList = new ArrayList<Cat>();
		final PersistenceManager pm = getPersistenceManager();
		try
		{
			final Query q = pm.newQuery(CatRecord.class);
			q.setOrdering(ordering);

			final String filter = "category == " + Integer.toString(category);
			q.setFilter(filter);

			final List<CatRecord> catRecords = (List<CatRecord>) q.execute();
			for (CatRecord catRecord: catRecords)
			{
				final Cat cat = catRecord.getCat();
				catList.add(cat);
			}
		}
		catch (Throwable t)
		{
			final String message = t.getMessage();
			Window.alert(message);
		}
		finally
		{
			pm.close();
		}
		final Cat[] result = catList.toArray(new Cat[0]);
		return result;
	}

	private Long setCat(Cat cat)
	{
		Long id = null;
		final CatRecord catRecord = new CatRecord(cat);
		final PersistenceManager pm = getPersistenceManager();
		try
		{
			// TODO: Test this cat is not in both a COTD and M9L cat.
			pm.makePersistent(catRecord);
			id = catRecord.getID();
		}
		catch (Throwable t)
		{
			final String message = t.getMessage();
			Window.alert(message);
		}
		finally
		{
			pm.close();
		}
		return id;
	}
}
