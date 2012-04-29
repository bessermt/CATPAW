/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;

public class SearchActivity extends TabActivity implements OnClickListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		final Button buttonClose = (Button) findViewById(R.id.buttonClose);
		buttonClose.setOnClickListener(this);

		final Resources res = getResources();
		final TabHost tabHost = getTabHost();
		Intent intent;
		String label;
		TabHost.TabSpec spec;

		intent = new Intent().setClass(this, SearchAllActivity.class);
		label = getString(R.string.all);
		spec = tabHost.newTabSpec("all").setIndicator(label, res.getDrawable(R.drawable.ic_tab_search_all)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, SearchFilterActivity.class);
		label = getString(R.string.filter);
		spec = tabHost.newTabSpec("filter").setIndicator(label, res.getDrawable(R.drawable.ic_tab_search_filter)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, SearchMatchActivity.class);
		label = getString(R.string.matches);
		spec = tabHost.newTabSpec("matches").setIndicator(label, res.getDrawable(R.drawable.ic_tab_search_match)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, SearchFavoriteActivity.class);
		label = getString(R.string.favorites);
		spec = tabHost.newTabSpec("favorites").setIndicator(label, res.getDrawable(R.drawable.ic_tab_search_favorite)).setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(2);
		setDefaultTab(2);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.buttonClose:
			{
				close();
			}
			break;

			default:
			{
				// TODO: Deal with diagnostics...
			}
			break;
		}
	}

	private void close()
	{
		finish();
	}
}
