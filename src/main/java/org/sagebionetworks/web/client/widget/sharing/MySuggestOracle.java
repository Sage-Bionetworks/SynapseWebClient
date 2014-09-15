package org.sagebionetworks.web.client.widget.sharing;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

//TODO: PUT THIS ELSEWHERE
public class MySuggestOracle extends SuggestOracle {
	public static final int DELAY = 750;
	
	private SuggestOracle.Request request;
	private SuggestOracle.Callback callback;
	private SuggestBox suggestBox;
	private Timer timer = new Timer() {

		@Override
		public void run() {
			
			/* If you backspace quickly the contents of the field are emptied but a
			 * query for a single character is still executed. Workaround for this
			 * is to check for an empty string field here.
             */
			if (suggestBox != null && !suggestBox.getText().trim().isEmpty()) {
				getSuggestions();
			}
		}
		
	};
	
	public void setSuggestBox(SuggestBox suggestBox) {
		this.suggestBox = suggestBox;
	}
	
	public boolean isDisplayStringHTML() {
           return true;
       }
	
	public void requestSuggestions(SuggestOracle.Request request, SuggestOracle.Callback callback) {
		this.request = request;
		this.callback = callback;
		
		timer.cancel();
		timer.schedule(DELAY);
	}
	
	public void getSuggestions() {
		String query = request.getQuery();
		Set<Suggestion> suggestions = new HashSet<Suggestion>();
		suggestions.add(new ItemSuggestion("Dog"));
		suggestions.add(new ItemSuggestion("Cat"));
		suggestions.add(new ItemSuggestion("Doge"));
		suggestions.add(new ItemSuggestion("Catamaran"));
		suggestions.add(new ItemSuggestion(query));
		
		// Set up response
		SuggestOracle.Response response = new SuggestOracle.Response(suggestions);
		    	   
		callback.onSuggestionsReady(request, response);
	}

//	class ItemSuggestCallback implements AsyncCallback {
//		private SuggestOracle.Request request;
//		private SuggestOracle.Callback callback;
//
//		public ItemSuggestCallback(SuggestOracle.Request request,
//					SuggestOracle.Callback callback) {
//			this.request = request;
//			this.callback = callback;
//		}
//
//		public void onFailure(Throwable error) {
//			callback.onSuggestionsReady(request, new SuggestOracle.Response());
//		}
//
//		public void onSuccess(Object retValue) {
//			callback.onSuggestionsReady(request,
//					(SuggestOracle.Response) retValue);
//		}
//	}
	
	public class ItemSuggestion implements IsSerializable, Suggestion {

		private String s;
		// Required for IsSerializable to work
		//public ItemSuggestion() {}	// TODO: this?

		// Convenience method for creation of a suggestion
		public ItemSuggestion(String s) {
			this.s = s;
		}

		public String getDisplayString() {
			return s;
		}

		public String getReplacementString() {
			return s;
		}
	} // end inner class ItemSuggestion
}
