package org.catadoptionteam.catdroid;

import java.util.ArrayList;
import java.util.List;


import com.google.api.client.util.Key;

public final class Video extends Item
{
	@Key
	String description;

	@Key
	List<String> tags = new ArrayList<String>();

	@Key
	Player player;

	@Key
	Content content;

}
