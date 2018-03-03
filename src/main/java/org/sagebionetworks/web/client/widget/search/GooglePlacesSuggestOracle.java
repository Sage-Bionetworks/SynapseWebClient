package org.sagebionetworks.web.client.widget.search;

import java.util.ArrayList;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTTimer;
import org.sagebionetworks.web.client.SynapseJavascriptClient;

import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.inject.Inject;

public class GooglePlacesSuggestOracle extends SuggestOracle {
	public static final int DELAY = 1000;	// milliseconds
	public SuggestOracle.Request request;
	public SuggestOracle.Callback callback;
	public int offset;
	public boolean isLoading = false;
	private GWTTimer timer;
	private SynapseJavascriptClient jsClient;
	private static String key;
	public static final String S3_PREFIX = "https://s3.amazonaws.com/suggest-places.sagebase.org/";
	public static final String GOOGLE_PEOPLE_SUGGESTIONS_URL = S3_PREFIX + "google-place-suggestions.txt";
	String searchTerm;
	ArrayList<PlaceSuggestion> suggestions;
	@Inject
	public GooglePlacesSuggestOracle(
			GWTTimer timer,
			SynapseJavascriptClient jsClient) {
		this.jsClient = jsClient;
		this.timer = timer;
		init();
		timer.configure(() -> {
			getSuggestions(offset);
		});
	}

	private void init() {
		if (key == null) {
			boolean forceAnonymous = true;
			jsClient.doGetString(GOOGLE_PEOPLE_SUGGESTIONS_URL, forceAnonymous, new AsyncCallback<String>() {
				@Override
				public void onFailure(Throwable ex) {
				}
				@Override
				public void onSuccess(String result) {
					key = result;
					ScriptInjector.fromUrl("https://maps.googleapis.com/maps/api/js?key=" + key + "&libraries=places").inject();
				}
			});
		}
	}

	public SuggestOracle.Request getRequest()	{	return request;		}
	public SuggestOracle.Callback getCallback()	{	return callback;	}

	public void getSuggestions(final int offset) {
		if (!isLoading && !request.getQuery().equals(searchTerm) && !request.getQuery().isEmpty()) {
			DisplayUtils.scrollToTop();
			isLoading = true;
			searchTerm = request.getQuery();
			suggestions = new ArrayList<>();
			_getPredictions(this, searchTerm);
		}
	}

	public final static native void _getPredictions(GooglePlacesSuggestOracle oracle, String searchTerm) /*-{
		var displaySuggestions = function (predictions, status) {
		    if (status != google.maps.places.PlacesServiceStatus.OK) {
		        console.error("unable to get place suggestions: " + status);
		        return;
		    }
		
		    predictions.forEach(function (prediction) {
		        //call addSuggestion with prediction.description
		        oracle.@org.sagebionetworks.web.client.widget.search.GooglePlacesSuggestOracle::addSuggestion(Ljava/lang/String;)(prediction.description);
		    });
		    // call on suggestions ready
		    oracle.@org.sagebionetworks.web.client.widget.search.GooglePlacesSuggestOracle::onSuggestionsReady()();
		};
		var service = new google.maps.places.AutocompleteService();
		service.getQueryPredictions({ input: searchTerm }, displaySuggestions);
	}-*/;

	public class PlaceSuggestion implements SuggestOracle.Suggestion {
		String displayString;
		public PlaceSuggestion(String displayString) {
			this.displayString = displayString;
		}
		@Override
		public String getDisplayString() {
			return displayString;
		}
		@Override
		public String getReplacementString() {
			return displayString;
		}
	}
	public void addSuggestion(String description) {
		suggestions.add(new PlaceSuggestion(description));
	}

	public void onSuggestionsReady() {
		SuggestOracle.Response response = new SuggestOracle.Response();
		response.setMoreSuggestions(false);
		response.setMoreSuggestionsCount(0);
		response.setSuggestions(suggestions);
		callback.onSuggestionsReady(request, response);
		isLoading = false;
	}

	@Override
	public void requestSuggestions(SuggestOracle.Request request, SuggestOracle.Callback callback) {
		this.request = request;
		this.callback = callback;
		timer.cancel();
		timer.schedule(DELAY);
	}	
}