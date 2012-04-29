/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

/**
 * @author bessermt
 *
 */
public class WebViewActivity extends Activity implements OnClickListener
{
	public static final String URL = "url";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);

		final WebView webview = (WebView)findViewById(R.id.webview);

		final Intent intent = getIntent();

		final String url = intent.getStringExtra(URL);

		webview.loadUrl(url);

		final Button buttonClose = (Button) findViewById(R.id.buttonClose);
		buttonClose.setOnClickListener(this);
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
