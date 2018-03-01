package org.sagebionetworks.web.client.widget.search;

import static org.sagebionetworks.web.client.widget.googlemap.GoogleMap.GOOGLE_MAP_URL;

import java.util.ArrayList;

import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GWTTimer;
import org.sagebionetworks.web.client.SynapseJavascriptClient;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.inject.Inject;

public class GooglePlacesSuggestOracle extends SuggestOracle {

	public SuggestOracle.Request request;
	public SuggestOracle.Callback callback;
	public int offset;
	public boolean isLoading = false;
	private GWTTimer timer;
	private SynapseJavascriptClient jsClient;
	private JSONObjectAdapter jsonObjectAdapter;
	private static String key;
	@Inject
	public GooglePlacesSuggestOracle(
			GWTTimer timer,
			SynapseJavascriptClient jsClient,
			JSONObjectAdapter jsonObjectAdapter) {
		this.jsClient = jsClient;
		this.timer = timer;
		this.jsonObjectAdapter = jsonObjectAdapter;
		init();
	}
	
	private void init() {
		if (key == null) {
			jsClient.doGetString(GOOGLE_MAP_URL, new AsyncCallback<String>() {
				@Override
				public void onFailure(Throwable ex) {
				}
				@Override
				public void onSuccess(String result) {
					key = result;
				}
			});
		}
	}
 	
	public SuggestOracle.Request getRequest()	{	return request;		}
	public SuggestOracle.Callback getCallback()	{	return callback;	}

	public void getSuggestions(final int offset) {
		if (!isLoading) {
			isLoading = true;
			String searchTerm = request.getQuery();
			String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input="+searchTerm+"&types=(cities)&key="+key;
			jsClient.doGetString(url, new AsyncCallback<String>() {
				@Override
				public void onFailure(Throwable ex) {
					isLoading = false;
				}
				@Override
				public void onSuccess(String json) {
					try {
						JSONObjectAdapter jsonObject = jsonObjectAdapter.createNew(json);
						JSONArrayAdapter jsonArray = jsonObject.getJSONArray("predictions");
						SuggestOracle.Response response = new SuggestOracle.Response(getSuggestions(jsonArray));
						callback.onSuggestionsReady(request, response);
						isLoading = false;
					} catch (Exception e) {
						onFailure(e);
					}
				}
			});
		}
	}
	
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
	
	public ArrayList<PlaceSuggestion> getSuggestions(JSONArrayAdapter predictionsArray) throws JSONObjectAdapterException {
		ArrayList<PlaceSuggestion> suggestions = new ArrayList<>(predictionsArray.length());
		for (int i = 0; i < predictionsArray.length(); i++) {
			JSONObjectAdapter prediction = predictionsArray.getJSONObject(i);
			suggestions.add(new PlaceSuggestion(prediction.getString("description")));
		}
		return suggestions;
	}
	
	@Override
	public void requestSuggestions(SuggestOracle.Request request, SuggestOracle.Callback callback) {
		this.request = request;
		this.callback = callback;
		timer.cancel();
		timer.schedule(SynapseSuggestBox.DELAY);
	}	
}