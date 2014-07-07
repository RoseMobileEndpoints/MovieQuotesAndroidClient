package edu.rosehulman.moviequotes;

import java.util.ArrayList;

import com.appspot.boutell_movie_quotes.moviequotes.model.MovieQuote;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ListActivity {

	private static final String MQ = "MQ";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		getListView().setMultiChoiceModeListener(new MyMultiClickListener());

		ArrayList<MovieQuote> testQuotes = new ArrayList<MovieQuote>();
		MovieQuote quote1 = new MovieQuote();
		quote1.setMovie("Title1");
		quote1.setQuote("Quote1");
		testQuotes.add(quote1);
		MovieQuote quote2 = new MovieQuote();
		quote2.setMovie("Title2");
		quote2.setQuote("Quote2");
		testQuotes.add(quote2);
		MovieQuote quote3 = new MovieQuote();
		quote3.setMovie("Title3");
		quote3.setQuote("Quote3");
		testQuotes.add(quote3);

		MovieQuoteArrayAdapter adapter = new MovieQuoteArrayAdapter(this,
				android.R.layout.simple_expandable_list_item_2, android.R.id.text1, testQuotes);
		setListAdapter(adapter);

	}

	private class MyMultiClickListener implements MultiChoiceModeListener {

		private ArrayList<MovieQuote> mQuotesToDelete = new ArrayList<MovieQuote>();

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.getMenuInflater().inflate(R.menu.context, menu);
			mode.setTitle(R.string.context_delete_title);
			return true; // gives tactile feedback
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.context_delete:
				deleteSelectedItems();
				mode.finish();
				return true;
			}
			return false;
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
			MovieQuote item = (MovieQuote) getListAdapter().getItem(position);
			if (checked) {
				mQuotesToDelete.add(item);
			} else {
				mQuotesToDelete.remove(item);
			}
			mode.setTitle("Selected " + mQuotesToDelete.size() + " quotes");
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// purposefully empty
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			mQuotesToDelete = new ArrayList<MovieQuote>();
			return true;
		}

		private void deleteSelectedItems() {
			for (MovieQuote quote : mQuotesToDelete) {
				((MovieQuoteArrayAdapter) getListAdapter()).remove(quote);
				// TODO: Implement deletion on server.

			}
			((MovieQuoteArrayAdapter) getListAdapter()).notifyDataSetChanged();
			updateQuotes();

		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		final MovieQuote currentQuote = (MovieQuote) getListAdapter().getItem(position);

		DialogFragment df = new DialogFragment() {
			@Override
			public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
				View view = inflater.inflate(R.layout.dialog_add, container);
				getDialog().setTitle(getString(R.string.edit_dialog_title));
				final Button confirmButton = (Button) view.findViewById(R.id.add_dialog_ok);
				final Button cancelButton = (Button) view.findViewById(R.id.add_dialog_cancel);
				final EditText movieTitleEditText = (EditText) view.findViewById(R.id.add_dialog_movie_title);
				final EditText movieQuoteEditText = (EditText) view.findViewById(R.id.add_dialog_movie_quote);

				// pre-populate
				movieTitleEditText.setText(currentQuote.getMovie());
				movieQuoteEditText.setText(currentQuote.getQuote());

				confirmButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String movieTitleText = movieTitleEditText.getText().toString();
						String movieQuoteText = movieQuoteEditText.getText().toString();
						Toast.makeText(MainActivity.this,
								"Got the title " + movieTitleText + " and quote " + movieQuoteText, Toast.LENGTH_LONG)
								.show();
						// add the data and send to server
						currentQuote.setMovie(movieTitleText);
						currentQuote.setQuote(movieQuoteText);
						// CONSIDER: Appears at the bottom initially, but
						// inserts to the top on the backend. Could store
						// ArrayList separately and make adapter a field.
						((MovieQuoteArrayAdapter) getListAdapter()).notifyDataSetChanged();
						// TODO: Kick off an AsychTask to insert a quote

						dismiss();

					}
				});

				cancelButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
					}
				});
				return view;
			}
		};
		df.show(getFragmentManager(), "");

		super.onListItemClick(l, v, position, id);
	}

	private void updateQuotes() {
		// TODO: Kick off an AsynchTask to do this.
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add:
			// add
			addItem();

			return true;
		case R.id.sync:
			// sync
			updateQuotes();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void addItem() {
		DialogFragment df = new DialogFragment() {
			@Override
			public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
				View view = inflater.inflate(R.layout.dialog_add, container);
				getDialog().setTitle("Add a movie and quote");
				final Button confirmButton = (Button) view.findViewById(R.id.add_dialog_ok);
				final Button cancelButton = (Button) view.findViewById(R.id.add_dialog_cancel);
				final EditText movieTitleEditText = (EditText) view.findViewById(R.id.add_dialog_movie_title);
				final EditText movieQuoteEditText = (EditText) view.findViewById(R.id.add_dialog_movie_quote);

				confirmButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String movieTitleText = movieTitleEditText.getText().toString();
						String movieQuoteText = movieQuoteEditText.getText().toString();
						Toast.makeText(MainActivity.this,
								"Got the title " + movieTitleText + " and quote " + movieQuoteText, Toast.LENGTH_LONG)
								.show();
						// add the data and send to server
						MovieQuote movieQuote = new MovieQuote();
						movieQuote.setMovie(movieTitleText);
						movieQuote.setQuote(movieQuoteText);
						((MovieQuoteArrayAdapter) getListAdapter()).add(movieQuote);
						((MovieQuoteArrayAdapter) getListAdapter()).notifyDataSetChanged();
						// CONSIDER: Appears at the bottom initially, but
						// inserts to
						// the top on the backend. Could store ArrayList
						// separately and make adapter a field.
						// TODO: insert a movie on the server

						dismiss();
					}
				});

				cancelButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
					}
				});
				return view;
			}
		};
		df.show(getFragmentManager(), "");

	}

	// TODO: Backend communication

}
