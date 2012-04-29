package org.catadoptionteam.catdroid;

import java.util.List;


import com.google.api.client.util.Key;

// TODO:  Consider moving this and other related classes to package org.catadoptionteam.catdroid.video;

public class Feed<T extends Item>
{
	@Key
	List<T> items;

	@Key
	int totalItems;

	@Key
	int startIndex;

	@Key
	int itemsPerPage;
}
