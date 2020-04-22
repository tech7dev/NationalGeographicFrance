package com.tech7.nationalgeographicfrance.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
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

public class MainActivity extends AppCompatActivity {

    private Boolean wifiConnected = false;
    private Boolean mobileConnected = false;
    private Button btnTryAgain;

    private Toolbar toolbar;
    private ActionBar actionBar;
    private String titleBar;
    private int i = 0; //checking internet connection

    private static String GOOGLE_YOUTUBE_API_KEY = "AIzaSyDyW7B--90khlyosHdfz9U6bF9giXn2SY8";//here you should use your api key for testing purpose you can use this api also
    private static String CHANNEL_ID = "UCT60XBtfRQzf5NKFGDNbfCw"; //here you should use your channel id for testing purpose you can use this api also
    private static String CHANNEL_GET_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&order=date&channelId=" + CHANNEL_ID + "&maxResults=30&key=" + GOOGLE_YOUTUBE_API_KEY + "";

    private RecyclerView rcvChannelVideos = null;
    private VideoPostAdapter adapter = null;
    private ArrayList<YoutubeDataModel> mListData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check network
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
                initToolbar();
                initNavigationMenu();

                // Display the progress bar.
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                rcvChannelVideos = findViewById(R.id.rcvChannelVideos);
                initList(mListData);
                new RequestYoutubeAPI().execute();
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

    private void initList(ArrayList<YoutubeDataModel> mListData) {
        rcvChannelVideos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoPostAdapter(this, mListData, new OnItemClickListener() {
            @Override
            public void onItemClick(YoutubeDataModel item) {
                YoutubeDataModel youtubeDataModel = item;
                Intent intent = new Intent(MainActivity.this, VideoShowActivity.class);
                intent.putExtra(YoutubeDataModel.class.toString(), youtubeDataModel);
                startActivity(intent);
            }
        });
        rcvChannelVideos.setAdapter(adapter);
        //Hide progressbar
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
                    if (json.has("id")) {
                        JSONObject jsonID = json.getJSONObject("id");
                        String video_id = "";
                        if (jsonID.has("videoId")) {
                            video_id = jsonID.getString("videoId");
                        }
                        if (jsonID.has("kind")) {
                            if (jsonID.getString("kind").equals("youtube#video")) {
                                YoutubeDataModel youtubeObject = new YoutubeDataModel();
                                JSONObject jsonSnippet = json.getJSONObject("snippet");
                                String title = jsonSnippet.getString("title");
                                String description = jsonSnippet.getString("description");
                                String publishedAt = jsonSnippet.getString("publishedAt");
                                String thumbnail = jsonSnippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");

                                title = title.replaceAll("&#39;","'");
                                description = description.replaceAll("&#39;","'");
                                publishedAt = publishedAt.replaceAll("T"," ");
                                publishedAt = publishedAt.replaceAll(".000Z"," ");

                                youtubeObject.setTitle(title);
                                youtubeObject.setDescription(description);
                                youtubeObject.setPublishedAt(publishedAt);
                                youtubeObject.setThumbnail(thumbnail);
                                youtubeObject.setVideo_id(video_id);
                                mList.add(youtubeObject);

                            }
                        }
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mList;

    }

    private void initToolbar() {
        //setting title bar
        titleBar = "Nat Geo France";
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(titleBar);
        Tools.setSystemBarColor(this, R.color.colorPrimary);
    }

    private void initNavigationMenu() {
        final NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                updateCounter(nav_view);
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem item) {
                //Toast.makeText(getApplicationContext(), item.getTitle() + " Selected", Toast.LENGTH_SHORT).show();

                // Handle navigation view item clicks here.
                int id = item.getItemId();
                if (id == R.id.nav_okovango) {
                    // Handle the camera action
                    actionBar.setTitle(item.getTitle());
                    Intent intent = new Intent(MainActivity.this, PlayListActivity.class);
                    intent.putExtra(PlayListActivity.EXTRA_PLAYLISTID, "PLwcVxN-yepawe4xhmbvgm-SfMhyrdivcB");
                    intent.putExtra(PlayListActivity.EXTRA_TITLEBAR, item.getTitle());
                    startActivity(intent);
                } else if (id == R.id.nav_arraigne) {
                    // Handle the camera action
                    actionBar.setTitle(item.getTitle());
                    Intent intent = new Intent(MainActivity.this, PlayListActivity.class);
                    intent.putExtra(PlayListActivity.EXTRA_PLAYLISTID, "PLwcVxN-yepazOyRQHR49Q5Ej3ZOTnMhCm");
                    intent.putExtra(PlayListActivity.EXTRA_TITLEBAR, item.getTitle());
                    startActivity(intent);
                } else if (id == R.id.nav_snake) {
                    // Handle the camera action
                    actionBar.setTitle(item.getTitle());
                    Intent intent = new Intent(MainActivity.this, PlayListActivity.class);
                    intent.putExtra(PlayListActivity.EXTRA_PLAYLISTID, "PLwcVxN-yepawr8JYeHB0vUOp-FM_5sXLe");
                    intent.putExtra(PlayListActivity.EXTRA_TITLEBAR, item.getTitle());
                    startActivity(intent);
                } else if (id == R.id.nav_reptiles) {
                    // Handle the camera action
                    actionBar.setTitle(item.getTitle());
                    Intent intent = new Intent(MainActivity.this, PlayListActivity.class);
                    intent.putExtra(PlayListActivity.EXTRA_PLAYLISTID, "PLwcVxN-yepawvNi9AnK5kfnnnMPeOkUqe");
                    intent.putExtra(PlayListActivity.EXTRA_TITLEBAR, item.getTitle());
                    startActivity(intent);
                } else if (id == R.id.nav_primate) {
                    // Handle the camera action
                    actionBar.setTitle(item.getTitle());
                    Intent intent = new Intent(MainActivity.this, PlayListActivity.class);
                    intent.putExtra(PlayListActivity.EXTRA_PLAYLISTID, "PLwcVxN-yepawSsxWwG_53GLXf2zMtgDX1");
                    intent.putExtra(PlayListActivity.EXTRA_TITLEBAR, item.getTitle());
                    startActivity(intent);
                } else if (id == R.id.nav_felin) {
                    // Handle the camera action
                    actionBar.setTitle(item.getTitle());
                    Intent intent = new Intent(MainActivity.this, PlayListActivity.class);
                    intent.putExtra(PlayListActivity.EXTRA_PLAYLISTID, "PLwcVxN-yepaz9RY_tW0pUsTSzn5FgkWRc");
                    intent.putExtra(PlayListActivity.EXTRA_TITLEBAR, item.getTitle());
                    startActivity(intent);
                } else if (id == R.id.nav_elephant) {
                    // Handle the camera action
                    actionBar.setTitle(item.getTitle());
                    Intent intent = new Intent(MainActivity.this, PlayListActivity.class);
                    intent.putExtra(PlayListActivity.EXTRA_PLAYLISTID, "PLwcVxN-yepayI_hHnRjR8iG-pn1pj6puK");
                    intent.putExtra(PlayListActivity.EXTRA_TITLEBAR, item.getTitle());
                    startActivity(intent);
                } else if (id == R.id.nav_croco) {
                    // Handle the camera action
                    actionBar.setTitle(item.getTitle());
                    Intent intent = new Intent(MainActivity.this, PlayListActivity.class);
                    intent.putExtra(PlayListActivity.EXTRA_PLAYLISTID, "PLwcVxN-yepaw6HdBuwuFpIpv8004hVa4_");
                    intent.putExtra(PlayListActivity.EXTRA_TITLEBAR, item.getTitle());
                    startActivity(intent);
                } else if (id == R.id.nav_requin) {
                    // Handle the camera action
                    actionBar.setTitle(item.getTitle());
                    Intent intent = new Intent(MainActivity.this, PlayListActivity.class);
                    intent.putExtra(PlayListActivity.EXTRA_PLAYLISTID, "PLwcVxN-yepaw4Lb_gRQZ6PBOTJH9Vz3cl");
                    intent.putExtra(PlayListActivity.EXTRA_TITLEBAR, item.getTitle());
                    startActivity(intent);
                } else if (id == R.id.nav_chine) {
                    // Handle the camera action
                    actionBar.setTitle(item.getTitle());
                    Intent intent = new Intent(MainActivity.this, PlayListActivity.class);
                    intent.putExtra(PlayListActivity.EXTRA_PLAYLISTID, "PLwcVxN-yepaxpsluHivPShNoQVO3V67J5");
                    intent.putExtra(PlayListActivity.EXTRA_TITLEBAR, item.getTitle());
                    startActivity(intent);
                } else if (id == R.id.nav_destinationWild) {
                    // Handle the camera action
                    actionBar.setTitle(item.getTitle());
                    Intent intent = new Intent(MainActivity.this, PlayListActivity.class);
                    intent.putExtra(PlayListActivity.EXTRA_PLAYLISTID, "PLwcVxN-yepaypTGYEw7EFjyAHUiNFOPlq");
                    intent.putExtra(PlayListActivity.EXTRA_TITLEBAR, item.getTitle());
                    startActivity(intent);
                } else if (id == R.id.nav_share) {
                    // Handle the camera action
                    actionBar.setTitle(item.getTitle());
//                    Intent intent = new Intent(MainActivity.this, ShareActivity.class);
//                    startActivity(intent);
                } else if (id == R.id.nav_contact) {
                    // Handle the camera action
                    actionBar.setTitle(item.getTitle());
                    Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                    startActivity(intent);
                }

                drawer.closeDrawers();
                return true;
            }
        });

        // open drawer at start
        drawer.openDrawer(GravityCompat.START);
        updateCounter(nav_view);
    }

    private void updateCounter(NavigationView nav) {
        Menu m = nav.getMenu();
        ((TextView) m.findItem(R.id.nav_okovango).getActionView().findViewById(R.id.text)).setText("14");
        ((TextView) m.findItem(R.id.nav_snake).getActionView().findViewById(R.id.text)).setText("21");
        ((TextView) m.findItem(R.id.nav_reptiles).getActionView().findViewById(R.id.text)).setText("106");
        ((TextView) m.findItem(R.id.nav_felin).getActionView().findViewById(R.id.text)).setText("109");
        ((TextView) m.findItem(R.id.nav_requin).getActionView().findViewById(R.id.text)).setText("93");
        ((TextView) m.findItem(R.id.nav_primate).getActionView().findViewById(R.id.text)).setText("33");
        ((TextView) m.findItem(R.id.nav_arraigne).getActionView().findViewById(R.id.text)).setText("5");
        ((TextView) m.findItem(R.id.nav_elephant).getActionView().findViewById(R.id.text)).setText("32");
        ((TextView) m.findItem(R.id.nav_chine).getActionView().findViewById(R.id.text)).setText("10");
        ((TextView) m.findItem(R.id.nav_destinationWild).getActionView().findViewById(R.id.text)).setText("1");
        ((TextView) m.findItem(R.id.nav_croco).getActionView().findViewById(R.id.text)).setText("10");
    }
}
