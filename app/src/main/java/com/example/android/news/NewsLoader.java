package com.example.android.news;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.List;

/**
 * NewsLoader has the purpose of managing an Asynchronous Task to perform an HTTP query
 * to access a news web API and then gracefully returning the results of the query back
 * to the MainActivity.
 * The asynchronous task is executed in the background thread and returns a list of
 * {@link Article} objects, which is the result of the web API query.
 * NewsLoader returns these results back to MainActivity in the main thread via callback method
 * onLoadFinished()
 */
public class NewsLoader extends AsyncTaskLoader<List<Article>> {

    private static final String LOG_TAG = NewsLoader.class.getSimpleName();
    private String mQuery;

    public NewsLoader(Context context, String query) {
        super(context);
        mQuery = query;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Article> loadInBackground() {
        // Perform the HTTP request for news articles and process the response.
        if (mQuery != null) {

            Log.i(LOG_TAG, "+++ Issuing a new server query... +++");

            return QueryUtils.fetchNewsArticles(mQuery);
        }
        return null;
    }
}
