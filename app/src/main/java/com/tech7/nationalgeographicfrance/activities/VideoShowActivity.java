package com.tech7.nationalgeographicfrance.activities;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.tech7.nationalgeographicfrance.R;
import com.tech7.nationalgeographicfrance.adapter.CommentAdapter;
import com.tech7.nationalgeographicfrance.models.YoutubeCommentModel;
import com.tech7.nationalgeographicfrance.models.YoutubeDataModel;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class VideoShowActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private Boolean wifiConnected = false;
    private Boolean mobileConnected = false;
    private Button btnTryAgain;

    private static String GOOGLE_YOUTUBE_API = "AIzaSyDyW7B--90khlyosHdfz9U6bF9giXn2SY8";
    private YoutubeDataModel youtubeDataModel = null;
    TextView textViewName;
    TextView textViewDes;
    TextView textViewDate;

    public static final String VIDEO_ID = "c2UNv38V6y4";
    private YouTubePlayerView mYoutubePlayerView = null;
    private YouTubePlayer mYoutubePlayer = null;
    private ArrayList<YoutubeCommentModel> mListData = new ArrayList<>();
    private CommentAdapter mAdapter = null;
    private RecyclerView mList_videos = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_show);

        //check connection
        checkNetworkConnection();
    }

    private void checkNetworkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        //internet connection is ready
        if (networkInfo != null && networkInfo.isConnected()) {
            wifiConnected = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;

            if (wifiConnected || mobileConnected) {

                youtubeDataModel = getIntent().getParcelableExtra(YoutubeDataModel.class.toString());
                Log.e("", youtubeDataModel.getDescription());

                mYoutubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_player);
                mYoutubePlayerView.initialize(GOOGLE_YOUTUBE_API, this);

                textViewName = (TextView) findViewById(R.id.textViewName);
                textViewDes = (TextView) findViewById(R.id.textViewDes);
                // imageViewIcon = (ImageView) findViewById(R.id.imageView);
                textViewDate = (TextView) findViewById(R.id.textViewDate);

                textViewName.setText(youtubeDataModel.getTitle());
                textViewDes.setText(youtubeDataModel.getDescription());
                textViewDate.setText(youtubeDataModel.getPublishedAt());

                mList_videos = (RecyclerView) findViewById(R.id.mList_videos);
                new RequestYoutubeCommentAPI().execute();
            }
        } else {
            //not connected
            // Display the panel
            findViewById(R.id.networkPanel).setVisibility(View.VISIBLE);
            btnTryAgain = findViewById(R.id.btnTryAgain);
            btnTryAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    checkNetworkConnection();
                }
            });
        }
    }

    public void back_btn_pressed(View view) {
        finish();
    }

//    public void playVideo(View view) {
//        if (mYoutubePlayer != null) {
//            if (mYoutubePlayer.isPlaying())
//                mYoutubePlayer.pause();
//            else
//                mYoutubePlayer.play();
//        }
//    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        youTubePlayer.setPlayerStateChangeListener(playerStateChangeListener);
        youTubePlayer.setPlaybackEventListener(playbackEventListener);
        if (!wasRestored) {
            youTubePlayer.cueVideo(youtubeDataModel.getVideo_id());
        }
        mYoutubePlayer = youTubePlayer;
    }

    private YouTubePlayer.PlaybackEventListener playbackEventListener = new YouTubePlayer.PlaybackEventListener() {
        @Override
        public void onPlaying() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onStopped() {

        }

        @Override
        public void onBuffering(boolean b) {

        }

        @Override
        public void onSeekTo(int i) {

        }
    };

    private YouTubePlayer.PlayerStateChangeListener playerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String s) {

        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {

        }

        @Override
        public void onVideoEnded() {

        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {

        }
    };

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

    public void share_btn_pressed(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        String link = ("https://www.youtube.com/watch?v=" + youtubeDataModel.getVideo_id());
        // this is the text that will be shared
        sendIntent.putExtra(Intent.EXTRA_TEXT, link);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, youtubeDataModel.getTitle()
                + "Share");

        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "share"));
    }

    //
    private ProgressDialog pDialog;

    private class RequestYoutubeCommentAPI extends AsyncTask<Void, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String VIDEO_COMMENT_URL = "https://www.googleapis.com/youtube/v3/commentThreads?part=snippet&maxResults=100&videoId=" + youtubeDataModel.getVideo_id() + "&key=" + GOOGLE_YOUTUBE_API;
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(VIDEO_COMMENT_URL);
            Log.e("url: ", VIDEO_COMMENT_URL);
            try {
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity httpEntity = response.getEntity();
                String json = EntityUtils.toString(httpEntity);
                return json;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("response", jsonObject.toString());
                    mListData = parseJson(jsonObject);
                    initVideoList(mListData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void initVideoList(ArrayList<YoutubeCommentModel> mListData) {
        mList_videos.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CommentAdapter(this, mListData);
        mList_videos.setAdapter(mAdapter);
    }

    public ArrayList<YoutubeCommentModel> parseJson(JSONObject jsonObject) {
        ArrayList<YoutubeCommentModel> mList = new ArrayList<>();

        if (jsonObject.has("items")) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);

                    YoutubeCommentModel youtubeObject = new YoutubeCommentModel();
                    JSONObject jsonTopLevelComment = json.getJSONObject("snippet").getJSONObject("topLevelComment");
                    JSONObject jsonSnippet = jsonTopLevelComment.getJSONObject("snippet");

                    String title = jsonSnippet.getString("authorDisplayName");
                    String thumbnail = jsonSnippet.getString("authorProfileImageUrl");
                    String comment = jsonSnippet.getString("textDisplay");

                    title = title.replaceAll("&#39;","'");
                    comment = comment.replaceAll("&#39;","'");

                    youtubeObject.setTitle(title);
                    youtubeObject.setComment(comment);
                    youtubeObject.setThumbnail(thumbnail);
                    mList.add(youtubeObject);


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mList;

    }

//    public void requestPermissionForReadExtertalStorage() throws Exception {
//        try {
//            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    READ_STORAGE_PERMISSION_REQUEST_CODE);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw e;
//        }
//    }

//    public boolean checkPermissionForReadExtertalStorage() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            int result = this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
//            int result2 = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//            return (result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED);
//        }
//        return false;
//    }
}
