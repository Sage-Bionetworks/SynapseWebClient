package org.sagebionetworks.web.client.widget.search;

import java.util.ArrayList;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTTimer;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.googlemap.GoogleMap;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.inject.Inject;

public class GooglePlacesSuggestOracle extends SuggestOracle {
	public static final int DELAY = 500; // milliseconds
	public SuggestOracle.Request request;
	public SuggestOracle.Callback callback;
	public int offset;
	public boolean isLoading = false;
	private GWTTimer timer;
	private SynapseJavascriptClient jsClient;
	String searchTerm;
	ArrayList<PlaceSuggestion> suggestions;

	@Inject
	public GooglePlacesSuggestOracle(GWTTimer timer, SynapseJavascriptClient jsClient) {
		this.jsClient = jsClient;
		this.timer = timer;
		GoogleMap.initGoogleLibrary(jsClient, null);
		timer.configure(() -> {
			getSuggestions(offset);
		});
	}

	public SuggestOracle.Request getRequest() {
		return request;
	}

	public SuggestOracle.Callback getCallback() {
		return callback;
	}

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
		var displaySuggestions = function(predictions, status) {
			if (status != google.maps.places.PlacesServiceStatus.OK) {
				console.error("unable to get place suggestions: " + status);
				return;
			}

			predictions
					.forEach(function(prediction) {
						//call addSuggestion with prediction.description
						oracle.@org.sagebionetworks.web.client.widget.search.GooglePlacesSuggestOracle::addSuggestion(Ljava/lang/String;)(prediction.description);
					});
			// call on suggestions ready
			oracle.@org.sagebionetworks.web.client.widget.search.GooglePlacesSuggestOracle::onSuggestionsReady()();
		};
		var service = new google.maps.places.AutocompleteService();
		service.getQueryPredictions({
			input : searchTerm
		}, displaySuggestions);
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
