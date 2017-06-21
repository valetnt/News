package com.example.android.news;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static com.example.android.news.MainActivity.SECTION_NAME1;
import static com.example.android.news.MainActivity.SECTION_NAME2;
import static com.example.android.news.MainActivity.SECTION_NAME3;
import static com.example.android.news.MainActivity.SECTION_NAME4;


public class CustomAdapter extends ArrayAdapter<Article> {

    public CustomAdapter(@NonNull Context context, @NonNull List<Article> objects) {
        /*
         * Superclass constructor is invoked with LayoutResId=0 because we are going to
         * inflate a customized list item layout from file
         */
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // If a view is already being used, recycle it.
        // Else, inflate it from a layout file.
        View rootView = convertView;
        if (convertView == null) {
            rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent,
                    false);
        }

        /*
         * Fill the list item at the current adapter position
         */
        Article item = getItem(position);

        ImageView thumbnail = (ImageView) rootView.findViewById(R.id.thumbnail);
        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView author = (TextView) rootView.findViewById(R.id.author);
        TextView section = (TextView) rootView.findViewById(R.id.section);
        TextView date = (TextView) rootView.findViewById(R.id.date);

        if (item.hasThumbnail()) {
            thumbnail.setImageBitmap(item.getThumbnail());
        } else {
            // If there is no thumbnail available, set a default one
            thumbnail.setImageResource(R.drawable.guardian_logo);
        }

        title.setText(item.getTitle());
        author.setText(item.getAuthor());
        section.setText(item.getSection());
        date.setText(item.getDate());

        if (item.getSection().equals(SECTION_NAME1)) {
            section.setBackgroundColor(ContextCompat.getColor(rootView.getContext(),
                    R.color.colorSectionTechnology));
        } else if (item.getSection().equals(SECTION_NAME2)) {
            section.setBackgroundColor(ContextCompat.getColor(rootView.getContext(),
                    R.color.colorSectionScience));
        } else if (item.getSection().equals(SECTION_NAME3)) {
            section.setBackgroundColor(ContextCompat.getColor(rootView.getContext(),
                    R.color.colorSectionEducation));
        } else if (item.getSection().equals(SECTION_NAME4)) {
            section.setBackgroundColor(ContextCompat.getColor(rootView.getContext(),
                    R.color.colorSectionEnvironment));
        }

        return rootView;
    }
}
