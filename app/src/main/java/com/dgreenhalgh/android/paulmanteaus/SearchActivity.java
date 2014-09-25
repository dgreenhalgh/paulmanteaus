package com.dgreenhalgh.android.paulmanteaus;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import rx.Observable;

public class SearchActivity extends Activity {

    private static final String RHYME_BRAIN_URL = "http://rhymebrain.com/talk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        DownloadTask portmanteauDownloadTask = new DownloadTask();
        portmanteauDownloadTask.execute();
    }

    /**
     * Hits RhymeBrain's API with the specified search query
     *
     * @return The string of JSON from RhymeBrain
     * @throws IOException
     */
    private String downloadUrl() throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(constructRequestUrl("paul"))
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /**
     * Constructs the URL that should be hit in RhymeBrain's portmanteau API
     * based on the provided search query.
     *
     * @param searchQuery The query to search using
     * @return The URL in RhymeBrain's API to hit
     */
    private String constructRequestUrl(String searchQuery) {
        return RHYME_BRAIN_URL
                + "?function=getPortmanteaus"
                + "&word="
                + searchQuery;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String json = "";
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            try {
                json = downloadUrl();
                Log.d("url", json);
                jsonArray = new JSONArray(json);
                grabPortmanteau(jsonArray);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return json;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            // TODO: setText
        }

        /**
         * Filters out "interesting" portmanteaus from the JSONArray using
         * their length as a heuristic (longer portmanteaus are more
         * interesting).
         *
         * @param jsonArray The JSON pulled from the RhymeBrain portmanteau API
         */
        private void grabPortmanteau(JSONArray jsonArray) {
            for(int i = 0; i < jsonArray.length(); i++) {
                try {
                    Observable.from(jsonArray.getJSONObject(i))
                            .filter(jsonObject -> {
                                try {
                                    return jsonObject.get("source").toString().length() > 12;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    return false;
                                }
                            })
                            .subscribe(object -> Log.d("bleh", object.toString()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
