package com.example.android.news;


import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving news feed from the "Guardian OpenPlatform" API.
 */
public class QueryUtils {
    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    public static List<Article> fetchNewsArticles(String query) {

        if (query == null) {
            return null;
        }

        // Turn String query into a URL
        URL queryURL = createURL(query);

        // Perform HTTP request to the URL and receive a JSON response back
        String JSONResponse = null;
        try {
            JSONResponse = makeHTTPRequest(queryURL);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request. ", e);
        }

        // Extract relevant fields from the JSON response and create a list of articles
        List<Article> newsFeed = extractFeatureFromJson(JSONResponse);

        return newsFeed;
    }

    private static URL createURL(String stringURL) {
        URL url = null;
        try {
            url = new URL(stringURL);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error creating URL. ", e);
        }
        return url;
    }

    private static String makeHTTPRequest(URL url) throws IOException {

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        String JSONResponse = null;

        if (url == null) {
            return null;
        }

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                JSONResponse = readFromStream(inputStream);

            } else {
                Log.e(LOG_TAG, "Connection to server has failed with response code: "
                        + urlConnection.getResponseCode());
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving JSON response. ", e);

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }

        return JSONResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
                    Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            while (line != null) {
                output.append(line);
                line = bufferedReader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link Article} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<Article> extractFeatureFromJson(String JSONResponse) {

        if (TextUtils.isEmpty(JSONResponse)) {
            // If the JSON string is empty or null, then return early.
            return null;
        }

        // Create empty arraylist to be filled with data obtained from parsing JSON response
        ArrayList<Article> articles = new ArrayList<>();

        // Try to parse the JSONResponse. If there's a problem with the way the JSON string
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            JSONObject jsonObject = new JSONObject(JSONResponse);
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(LOG_TAG, "Problem parsing the JSON results", e);
        }

        return articles;
    }

}
