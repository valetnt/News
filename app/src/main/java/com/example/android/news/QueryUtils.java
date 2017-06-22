package com.example.android.news;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
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
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


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

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createURL(String stringURL) {
        URL url = null;
        try {
            url = new URL(stringURL);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error creating URL. ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a JSON response.
     */
    private static String makeHTTPRequest(URL url) throws IOException {

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        String JSONResponse = "";

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
                // If the request was successful (response code 200), then read the
                // input stream and store its whole content into a String.
                inputStream = urlConnection.getInputStream();
                JSONResponse = readFromStream(inputStream);

            } else {
                // If the request was not successful, print the error code in the Logcat
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
                // the makeHttpRequest(URL url) method signature specifies that an IOException
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

        /** Create a new instance of {@link StringBuilder}*/
        StringBuilder output = new StringBuilder();

        if (inputStream != null) {
            /** Create a new instance of {@link InputStreamReader} */
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
                    Charset.forName("UTF-8"));
            /** Create a new instance of {@link BufferedReader}*/
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            // Read the first line and store it into a string
            String line = bufferedReader.readLine();
            // As long as the next line is non-null, append the next line to the output string
            while (line != null) {
                output.append(line);
                // Read next line
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
        ArrayList<Article> articlesList = new ArrayList<>();

        // Try to parse the JSONResponse. If there's a problem with the way the JSON string
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            JSONObject jsonObject = new JSONObject(JSONResponse);
            JSONObject response = jsonObject.getJSONObject("response");
            JSONArray results = response.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.getJSONObject(i);
                if (item.has("type")) {
                    // Filter out anything that is not an article
                    if (item.getString("type").equals("article")) {

                        // Get section name
                        String section = "";
                        if (item.has("sectionName")) {
                            section = item.getString("sectionName");
                        }

                        /*
                         * Get date of publication. Requires conversion from
                         * Datetime format (combined date and time in UTC
                         * according to ISO-8601) to a more readable date format.
                         */
                        String formattedDate = "";

                        if (item.has("webPublicationDate")) {
                            // Get datetime string
                            String datetime = item.getString("webPublicationDate");
                            DateFormat dfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            // Parse string into a date
                            Date date = null;
                            try {
                                date = dfInput.parse(datetime);
                            } catch (ParseException e) {
                                Log.e(LOG_TAG, "Problem parsing datetime. ", e);
                            }
                            // Represent the date in a convenient format
                            if (date != null) {
                                StringBuffer sb = new StringBuffer();
                                if (Locale.getDefault() == Locale.US) {
                                    DateFormat dfOutput = new SimpleDateFormat("MM/dd/yy", Locale.US);
                                    dfOutput.format(date, sb, // appendable
                                            new FieldPosition(SimpleDateFormat.DATE_FIELD));
                                } else {
                                    DateFormat dfOutput = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                                    dfOutput.format(date, sb, // appendable
                                            new FieldPosition(SimpleDateFormat.DATE_FIELD));
                                }
                                formattedDate = sb.toString();
                            }
                        }

                        // Get title
                        String title = "";
                        if (item.has("webTitle")) {
                            title = item.getString("webTitle");
                        }

                        // Get link to article
                        String link = "";
                        if (item.has("webUrl")) {
                            link = item.getString("webUrl");
                        }

                        // Get authors
                        String authors = "";
                        JSONArray tags = item.getJSONArray("tags");
                        if(tags.length() > 0) {
                            JSONObject firstAuthor = tags.getJSONObject(0);
                            if (firstAuthor.has("webTitle")) {
                                if (tags.length() > 1) {
                                    authors = firstAuthor.getString("webTitle") + ", ...";
                                } else if (tags.length() == 1) {
                                    authors = firstAuthor.getString("webTitle");
                                }
                            }
                        }

                        // Get thumbnail resource URL
                        Bitmap thumbnail = null;
                        JSONObject fields = item.getJSONObject("fields");
                        String thumbnailURLString = fields.getString("thumbnail");
                        URL thumbnailURL = createURL(thumbnailURLString);
                        try {
                            thumbnail = downloadBitmapFromURL(thumbnailURL);
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Problem retrieving the bitmap image. ", e);
                        }

                        /** Create a new {@link Article} object */
                        Article article = new Article(title, authors, formattedDate, section, link);

                        if (thumbnail != null) {
                            article.setThumbnail(thumbnail);
                        }

                        articlesList.add(article);
                    }
                }
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(LOG_TAG, "Problem parsing the JSON results", e);
        }

        return articlesList;
    }

    /*
     * Download bitmap image from specified URL
     */
    private static Bitmap downloadBitmapFromURL(URL sourceURL) throws IOException {
        Bitmap bitmap = null;

        if (sourceURL == null) {
            return null;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) sourceURL.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                // If the request was successful (response code 200), then read the
                // input stream and download the bitmap image.
                inputStream = urlConnection.getInputStream();
                if (inputStream != null) {
                    bitmap = BitmapFactory.decodeStream(inputStream);
                }
            } else {
                // If the request was not successful, print the error code in the Logcat
                Log.e(LOG_TAG, "Could not retrieve bitmap image. " +
                        "Connection to server has failed with response code: "
                        + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the bitmap image. ", e);

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies that an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return bitmap;
    }

}
