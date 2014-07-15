package edu.rosehulman.moviequotes;

import java.io.IOException;
import java.util.ArrayList;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

import com.appspot.boutell_movie_quotes.moviequotes.Moviequotes;
import com.appspot.boutell_movie_quotes.moviequotes.Moviequotes.Moviequote;
import com.appspot.boutell_movie_quotes.moviequotes.model.MovieQuote;
import com.appspot.boutell_movie_quotes.moviequotes.model.MovieQuoteCollection;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

public class MainActivity extends ListActivity {

	private static final String MQ = "MQ";
	private Moviequotes mService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Moviequotes.Builder builder = new Moviequotes.Builder(AndroidHttp.newCompatibleTransport(),
				new GsonFactory(), null);
		mService = builder.build();

		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		getListView().setMultiChoiceModeListener(new MyMultiClickListener());

		// ArrayList<MovieQuote> testQuotes = new ArrayList<MovieQuote>();
		// MovieQuote quote1 = new MovieQuote();
		// quote1.setMovie("Title1");
		// quote1.setQuote("Quote1");
		// testQuotes.add(quote1);
		// MovieQuote quote2 = new MovieQuote();
		// quote2.setMovie("Title2");
		// quote2.setQuote("Quote2");
		// testQuotes.add(quote2);
		// MovieQuote quote3 = new MovieQuote();
		// quote3.setMovie("Title3");
		// quote3.setQuote("Quote3");
		// testQuotes.add(quote3);
		//
		// MovieQuoteArrayAdapter adapter = new MovieQuoteArrayAdapter(this,
		// android.R.layout.simple_expandable_list_item_2, android.R.id.text1,
		// testQuotes);
		// setListAdapter(adapter);
		updateQuotes();
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
		public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
				boolean checked) {
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
				new DeleteQuoteTask().execute(quote.getEntityKey());
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
			public View onCreateView(LayoutInflater inflater, ViewGroup container,
					Bundle savedInstanceState) {
				View view = inflater.inflate(R.layout.dialog_add, container);
				getDialog().setTitle(getString(R.string.edit_dialog_title));
				final Button confirmButton = (Button) view.findViewById(R.id.add_dialog_ok);
				final Button cancelButton = (Button) view.findViewById(R.id.add_dialog_cancel);
				final EditText movieTitleEditText = (EditText) view
						.findViewById(R.id.add_dialog_movie_title);
				final EditText movieQuoteEditText = (EditText) view
						.findViewById(R.id.add_dialog_movie_quote);

				// pre-populate
				movieTitleEditText.setText(currentQuote.getMovie());
				movieQuoteEditText.setText(currentQuote.getQuote());

				confirmButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String movieTitleText = movieTitleEditText.getText().toString();
						String movieQuoteText = movieQuoteEditText.getText().toString();
						Toast.makeText(MainActivity.this,
								"Got the title " + movieTitleText + " and quote " + movieQuoteText,
								Toast.LENGTH_LONG).show();
						// add the data and send to server
						currentQuote.setMovie(movieTitleText);
						currentQuote.setQuote(movieQuoteText);
						// CONSIDER: Appears at the bottom initially, but
						// inserts to the top on the backend. Could store
						// ArrayList separately and make adapter a field.
						((MovieQuoteArrayAdapter) getListAdapter()).notifyDataSetChanged();
						// TODO: Kick off an AsychTask to insert a quote
						(new InsertQuoteTask()).execute(currentQuote);

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
		// DONE: Kick off an AsynchTask to do this.
		(new QueryForQuotesTask()).execute();
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
			public View onCreateView(LayoutInflater inflater, ViewGroup container,
					Bundle savedInstanceState) {
				View view = inflater.inflate(R.layout.dialog_add, container);
				getDialog().setTitle("Add a movie and quote");
				final Button confirmButton = (Button) view.findViewById(R.id.add_dialog_ok);
				final Button cancelButton = (Button) view.findViewById(R.id.add_dialog_cancel);
				final EditText movieTitleEditText = (EditText) view
						.findViewById(R.id.add_dialog_movie_title);
				final EditText movieQuoteEditText = (EditText) view
						.findViewById(R.id.add_dialog_movie_quote);

				confirmButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String movieTitleText = movieTitleEditText.getText().toString();
						String movieQuoteText = movieQuoteEditText.getText().toString();
						Toast.makeText(MainActivity.this,
								"Got the title " + movieTitleText + " and quote " + movieQuoteText,
								Toast.LENGTH_LONG).show();
						// add the data and send to server
						MovieQuote currentQuote = new MovieQuote();
						currentQuote.setMovie(movieTitleText);
						currentQuote.setQuote(movieQuoteText);
						((MovieQuoteArrayAdapter) getListAdapter()).add(currentQuote);
						((MovieQuoteArrayAdapter) getListAdapter()).notifyDataSetChanged();
						// CONSIDER: Appears at the bottom initially, but
						// inserts to
						// the top on the backend. Could store ArrayList
						// separately and make adapter a field.
						// TODO: insert a movie on the server
						(new InsertQuoteTask()).execute(currentQuote);
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
	class QueryForQuotesTask extends AsyncTask<Void, Void, MovieQuoteCollection> {

		@Override
		protected MovieQuoteCollection doInBackground(Void... params) {
			MovieQuoteCollection quotes = null;
			try {
				Moviequote.List query = mService.moviequote().list();
				query.setOrder("-last_touch_date_time");
				// For more than 50 quotes, we need to deal with pageTokens.
				query.setLimit(50L);
				quotes = query.execute();
			} catch (IOException e) {
				Log.e(MQ, "Failed loading " + e);
			}
			return quotes;
		}

		@Override
		protected void onPostExecute(MovieQuoteCollection result) {
			if (result == null) {
				Log.e(MQ, "Failed loading, result is null");
				return;
			}

			MovieQuoteArrayAdapter adapter = new MovieQuoteArrayAdapter(MainActivity.this,
					android.R.layout.simple_expandable_list_item_2, android.R.id.text1,
					result.getItems());
			setListAdapter(adapter);
		}
	}

	class InsertQuoteTask extends AsyncTask<MovieQuote, Void, MovieQuote> {

		@Override
		protected MovieQuote doInBackground(MovieQuote... quotes) {
			MovieQuote returnedQuote = null;
			try {
				returnedQuote = mService.moviequote().insert(quotes[0]).execute();
			} catch (IOException e) {
				Log.e(MQ, "Failed inserting " + e);
			}
			return returnedQuote;
		}

		@Override
		protected void onPostExecute(MovieQuote result) {
			super.onPostExecute(result);
			if (result == null) {
				Log.e(MQ, "Failed inserting, result is null");
				return;
			}

			updateQuotes();
		}
	}

	class DeleteQuoteTask extends AsyncTask<String, Void, MovieQuote> {

		@Override
		protected MovieQuote doInBackground(String... entityKeys) {
			MovieQuote returnedQuote = null;
			try {
				returnedQuote = mService.moviequote().delete(entityKeys[0]).execute();
			} catch (IOException e) {
				Log.e(MQ, "Failed deleting " + e);
			}
			return returnedQuote;
		}

		@Override
		protected void onPostExecute(MovieQuote result) {
			super.onPostExecute(result);
			if (result == null) {
				Log.e(MQ, "Failed deleting, result is null");
				return;
			}
			updateQuotes();
		}
	}
}
