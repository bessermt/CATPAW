package org.catadoptionteam.catdroid;

import com.google.api.client.util.Key;

public class Item
{
	@Key
	String id;

	@Key
	String title;

	@Key
	Thumbnail thumbnail; // TODO: Ratio of 16:9?

	@Key
	String uploaded; // TODO: Is this working?
}
