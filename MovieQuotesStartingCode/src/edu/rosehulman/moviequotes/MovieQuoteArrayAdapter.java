package edu.rosehulman.moviequotes;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MovieQuoteArrayAdapter extends ArrayAdapter<MovieQuote> {

	public MovieQuoteArrayAdapter(Context context, int resource, int textViewResourceId, List<MovieQuote> quotes) {
		super(context, resource, textViewResourceId, quotes);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		TextView titleTextView = (TextView) view.findViewById(android.R.id.text2);
		titleTextView.setText(getItem(position).getTitle());
		TextView quoteTextView = (TextView) view.findViewById(android.R.id.text1);
		quoteTextView.setText(getItem(position).getQuote());
		return view;
	}

}
