package com.tech7.nationalgeographicfrance.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.tech7.nationalgeographicfrance.R;
import com.tech7.nationalgeographicfrance.adapter.VideoPostAdapter;
import com.tech7.nationalgeographicfrance.interfaces.OnItemClickListener;
import com.tech7.nationalgeographicfrance.models.YoutubeDataModel;
import com.tech7.nationalgeographicfrance.utils.Tools;

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

public class PlayListActivity extends AppCompatActivity {

    public static final String EXTRA_PLAYLISTID = "com.tech7.nationalgeographicfrance.EXTRA_PLAYLISTID";  //extra string of playlist id
    public static final String EXTRA_TITLEBAR = "com.tech7.nationalgeographicfrance.EXTRA_TITLEBAR";  //extra string of titlebar

    private static String GOOGLE_YOUTUBE_API_KEY = "AIzaSyDyW7B--90khlyosHdfz9U6bF9giXn2SY8";//here you should use your api key for testing purpose you can use this api also
    private static String PLAYLIST_ID;//here you should use your playlist id for testing purpose you can use this api also
    private static String CHANNEL_GET_URL;

    private RecyclerView rcvPlayVideos = null;
    private VideoPostAdapter adapter = null;
    private ArrayList<YoutubeDataModel> mListData = new ArrayList<>();

    private Boolean wifiConnected = false;
    private Boolean mobileConnected = false;
    private Button btnTryAgain;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private String titleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        //check internet connection
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

                // Display the progress bar.
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                Intent intent = getIntent();

                if (intent.hasExtra(EXTRA_PLAYLISTID)) {
                    Log.d("EXTRA_PLAYLISTID::", intent.getStringExtra(EXTRA_PLAYLISTID));
                    titleBar = intent.getStringExtra(EXTRA_TITLEBAR);
                    initToolbar();
                    PLAYLIST_ID = intent.getStringExtra(EXTRA_PLAYLISTID); //parameter
                    CHANNEL_GET_URL = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=" + PLAYLIST_ID + "&maxResults=30&key=" + GOOGLE_YOUTUBE_API_KEY + "";

                    rcvPlayVideos = (RecyclerView) findViewById(R.id.rcvPlayVideos);
                    initList(mListData);
                    new RequestYoutubeAPI().execute();

                }
            }
        } else {
            //not connected
            // Display the panel
            findViewById(R.id.networkPanel).setVisibility(View.VISIBLE);
            btnTryAgain = findViewById(R.id.btnTryAgain);
            btnTryAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Display the panel
                    findViewById(R.id.networkPanel).setVisibility(View.INVISIBLE);
                    checkNetworkConnection();
                }
            });
        }
    }

    private void initToolbar() {
        //setting title bar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(titleBar);
        Tools.setSystemBarColor(this, R.color.colorPrimary);
    }

    private void initList(ArrayList<YoutubeDataModel> mListData) {
        rcvPlayVideos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoPostAdapter(this, mListData, new OnItemClickListener() {
            @Override
            public void onItemClick(YoutubeDataModel item) {
                YoutubeDataModel youtubeDataModel = item;
                Intent intent = new Intent(PlayListActivity.this, VideoShowActivity.class);
                intent.putExtra(YoutubeDataModel.class.toString(), youtubeDataModel);
                startActivity(intent);
            }
        });
        rcvPlayVideos.setAdapter(adapter);

        // hide the progress bar.
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

    }


    //create an asynctask to get all the data from youtube
    private class RequestYoutubeAPI extends AsyncTask<Void, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(CHANNEL_GET_URL);
            Log.e("URL", CHANNEL_GET_URL);
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
                    mListData = parseVideoListFromResponse(jsonObject);
                    initList(mListData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ArrayList<YoutubeDataModel> parseVideoListFromResponse(JSONObject jsonObject) {
        ArrayList<YoutubeDataModel> mList = new ArrayList<>();

        if (jsonObject.has("items")) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    if (json.has("kind")) {
                        if (json.getString("kind").equals("youtube#playlistItem")) {
                            YoutubeDataModel youtubeObject = new YoutubeDataModel();
                            JSONObject jsonSnippet = json.getJSONObject("snippet");
                            String vedio_id = "";
                            if (jsonSnippet.has("resourceId")) {
                                JSONObject jsonResource = jsonSnippet.getJSONObject("resourceId");
                                vedio_id = jsonResource.getString("videoId");

                            }
                            String title = jsonSnippet.getString("title");
                            String description = jsonSnippet.getString("description");
                            String publishedAt = jsonSnippet.getString("publishedAt");
                            String thumbnail = jsonSnippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");

                            title = title.replaceAll("&#39;","'");
                            description = description.replaceAll("&#39;","'");
                            publishedAt = publishedAt.replaceAll("T"," ");
                            publishedAt = publishedAt.replaceAll(":00.000Z"," ");

                            youtubeObject.setTitle(title);
                            youtubeObject.setDescription(description);
                            youtubeObject.setPublishedAt(publishedAt);
                            youtubeObject.setThumbnail(thumbnail);
                            youtubeObject.setVideo_id(vedio_id);
                            mList.add(youtubeObject);

                        }
                    }


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mList;

    }
}
