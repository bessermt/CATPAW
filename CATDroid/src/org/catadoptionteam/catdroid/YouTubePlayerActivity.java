/**
 * 
 */
package org.catadoptionteam.catdroid;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * @author bessermt
 *
 */
public class YouTubePlayerActivity extends Activity
{
	public static final String YOUTUBE_URI = "YouTube URI";

	private MediaPlayer mediaPlayer_;

//	private MediaPlayer.OnCompletionListener mediaCompletionListener = 
//		new MediaPlayer.OnCompletionListener()
//		{
//			@Override
//			public void onCompletion(MediaPlayer mp)
//			{
//				releaseMediaPlayer();
//			}
//		};

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.youtube_player);

		final VideoView videoView = (VideoView) findViewById(R.id.videoView);

		final Intent intent = getIntent();
		final String youTubeURIString = intent.getStringExtra(YOUTUBE_URI);
		// TODO: Which one?
		// http://m.youtube.com/details?v=9Dk0ixrhi34
		// rtsp://v1.cache3.c.youtube.com/CiILENy73wIaGQl-i-EaizQ59BMYDSANFEgGUgZ2aWRlb3MM/0/0/0/video.3gp

		final Uri uri = Uri.parse(youTubeURIString);

		videoView.setVideoURI(uri);
		videoView.setMediaController(new MediaController(this));
		videoView.requestFocus();
		videoView.start();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		releaseMediaPlayer();
	}

	private void releaseMediaPlayer()
	{
		if (mediaPlayer_ != null)
		{
			mediaPlayer_.release();
			mediaPlayer_ = null;
		}
	}
}
