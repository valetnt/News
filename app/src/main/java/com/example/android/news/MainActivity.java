package com.example.android.news;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<Article>> {

    public static final String QUERY = "https://content.guardianapis.com/search?" +
            "q=physics%20AND%20NOT%20obituary" +
            "&tag=science/physics|education/physics" +
            "&section=science|technology|education|environment" +
            "&show-fields=thumbnail&show-tags=contributor&order-by=newest" +
            "&page-size=30&api-key=test";
    public static final String SECTION_NAME1 = "Technology";
    public static final String SECTION_NAME2 = "Science";
    public static final String SECTION_NAME3 = "Education";
    public static final String SECTION_NAME4 = "Environment";

    private TextView mEmptyMessage1;
    private TextView mEmptyMessage2;
    private ProgressBar mProgressBar;
    private View mEmptyView;
    private ListView mListView;
    private CustomAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mListView = (ListView) findViewById(R.id.list);
        // Create a custom adapter with an empty array list
        mAdapter = new CustomAdapter(this, new ArrayList<Article>());
        // Set this adapter to the list view
        mListView.setAdapter(mAdapter);

        mEmptyView = findViewById(R.id.empty_view);
        mEmptyMessage1 = (TextView) findViewById(R.id.empty_view_1);
        mEmptyMessage2 = (TextView) findViewById(R.id.empty_view_2);
        // Set mEmptyView as the view to be displayed when mListView is empty
        mListView.setEmptyView(mEmptyView);

        /**
         * Set an {@link android.widget.AdapterView.OnItemClickListener} on the ListView,
         * so by clicking any article in the list it is possible to
         * read the full article on the related page on the Guardian website.
         * This is achieved by sending an intent to a web browser.
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Article selectedArticle = mAdapter.getItem(position);
                Intent openWebPage = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(selectedArticle.getLink()));
                startActivity(openWebPage);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Initially hide the empty view, so that only the progress bar is showing
        mEmptyView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        /*
         * CHECK INTERNET CONNECTION:
         *
         * Make this check onStart(), rather than onCreate(), since the loader is restarted
         * every time the activity is restarted.
         * In this way, if the user presses the HOME button, does other things, and then, after a
         * while, he relaunches the app from "Recent Tasks" with internet connection off,
         * the empty message that the user is going to receive is "you are offline",
         * rather than "no results found", which is more consistent with what the user expects.
         *
         * The price to pay for making this check onStart(), rather than onCreate(),
         * is that we are going to issue more server queries.
         */
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        boolean isConnected = (networkInfo != null && networkInfo.isConnectedOrConnecting());
        if (isConnected) {
            /**
             * If the device is connected to the network, we can perform an http request.
             * Get a LoaderManager in order to be able to create and manage an instance of
             * {@link android.support.v4.content.AsyncTaskLoader}.
             * First argument of method initLoader is arbitrary
             * (we don't care since we only use one loader).
             */
            LoaderManager loaderManager = getSupportLoaderManager();
            loaderManager.initLoader(0, null, this);
        } else {
            // If the device is offline and a loader already exists,
            // then intercept it before it starts loading data and kill it.
            // Thus the message displayed will be "you are offline",
            // rather than "no results found".
            if (getSupportLoaderManager().getLoader(0) != null) {
                getSupportLoaderManager().destroyLoader(0);
            }
            // Hide the progress bar and display the empty view
            // with a message warning the user that he is offline
            mProgressBar.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            mEmptyMessage1.setText(getString(R.string.no_connection1));
            mEmptyMessage2.setText(getString(R.string.no_connection2));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        if(itemID == R.id.action_reload) {
            /*
             * Reload the news feed.
             * This action is needed when:
             *
             * 1. The user has just switched on Internet connection, and wants to see the news feed
             *    update immediately, without having to exit the app and relaunch it.
             *
             *    OR
             *
             * 2. The user wants to reload the results to see if a new article has just come out.
             *
             * In the first case, it is sufficient to restart the activity
             * to issue a new server query.
             *
             * In the second case, we have to force the loader to be recreated from scratch:
             */
            if (getSupportLoaderManager().getLoader(0) != null) {
                getSupportLoaderManager().destroyLoader(0);
            }
            onStart();
            return true;
        }
        return false;
    }

    @Override
    public Loader<List<Article>> onCreateLoader(int id, Bundle args) {
        /**
         * Return a new instance of {@link NewsLoader}
         */
        return new NewsLoader(this, QUERY);
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> data) {
        /*
         * As soon as the Loader has finished loading the results of the query, hide the
         * progress bar and show the list of articles. If the list is empty, or if the
         * http connection and data download has failed, display the empty view with a message
         * saying that no results were found.
         */
        mProgressBar.setVisibility(View.GONE);
        //Clear the Adapter from previous data download
        mAdapter.clear();

        if (data == null || data.isEmpty()) {
            mEmptyMessage1.setText(getString(R.string.empty_view_message1));
            mEmptyMessage2.setText(getString(R.string.empty_view_message2));

        } else {
            // Update the Adapter. This will trigger the ListView to update too.
            mAdapter.addAll(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {
        // When the Loader is no longer needed, clear data
        mAdapter.clear();
    }

}
