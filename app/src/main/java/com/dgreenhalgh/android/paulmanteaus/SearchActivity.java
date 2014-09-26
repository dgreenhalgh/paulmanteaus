package com.dgreenhalgh.android.paulmanteaus;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import rx.Observable;

public class SearchActivity extends Activity {
    private static final String TAG = "SearchActivity";
    private static final String RHYME_BRAIN_URL = "http://rhymebrain.com/talk";

    private EditText mSearchQueryEditText;
    private Button mSearchButton;
    private TextView mSearchResultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchQueryEditText = (EditText) findViewById(R.id.portmanteau_edit_text);

        mSearchResultTextView = (TextView) findViewById(R.id.result_text_view);

        mSearchButton = (Button) findViewById(R.id.portmanteau_search_button);
        mSearchButton.setOnClickListener(view -> {
            DownloadTask portmanteauDownloadTask = new DownloadTask();
            portmanteauDownloadTask.execute();
        });
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
                .url(constructRequestUrl(mSearchQueryEditText.getText().toString()))
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

    private class DownloadTask extends AsyncTask<String, Void, List<String>> {

        @Override
        protected List<String> doInBackground(String... params) {
            String json = "";
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            try {
                json = downloadUrl();
                Log.d(TAG, json);
                jsonArray = new JSONArray(json);
                return grabPortmanteau(jsonArray);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        }

        @Override
        protected void onPostExecute(List<String> portmanteaus) {
            super.onPostExecute(portmanteaus);

            Log.d("woo", "woo");

            String portmanteau = grabRandomPortmanteauFromList(portmanteaus);
            mSearchResultTextView.setText(portmanteau);
        }

        /**
         * Filters out "interesting" portmanteaus from the JSONArray using
         * their length as a heuristic (longer portmanteaus are more
         * interesting).
         *
         * @param jsonArray The JSON pulled from the RhymeBrain portmanteau API
         */
        private List<String> grabPortmanteau(JSONArray jsonArray) {
            List<String> portmanteaus = new ArrayList<>();

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
                            .map(jsonObject -> {
                                try {
                                    return jsonObject.get("combined").toString();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    return null;
                                }
                            })
                            .map(combinedString -> combinedString.split(",")[0])
                            .subscribe(portmanteau -> portmanteaus.add(portmanteau));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return portmanteaus;
        }

        /**
         * Used to grab a psuedorandom list element
         *
         * @param portmanteaus A list of portmanteaus
         * @return A pseudorandom portmanteau from the list of portmanteaus
         */
        private String grabRandomPortmanteauFromList(List<String> portmanteaus) {
            Random randomNumberGenerator = new Random();
            int portmanteauIndex = randomNumberGenerator.nextInt(portmanteaus.size());

            return portmanteaus.get(portmanteauIndex);
        }
    }
}
