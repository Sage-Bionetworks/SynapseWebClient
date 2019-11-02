package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.search.Hit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle;

public class EntitySearchBoxOracle extends SuggestOracle {
	private SuggestOracle.Request request;
	private SuggestOracle.Callback callback;

	private EntitySearchBox suggestBox;

	public void configure(EntitySearchBox suggestBox) {
		this.suggestBox = suggestBox;
	}

	private Timer timer = new Timer() {

		@Override
		public void run() {

			// If you backspace quickly the contents of the field are emptied but a
			// query for a single character is still executed. Workaround for this
			// is to check for an empty string field here.
			if (!suggestBox.getText().trim().isEmpty()) {
				suggestBox.setOffset(0);
				suggestBox.getSuggestions(request, callback);
			}
		}
	};

	@Override
	public boolean isDisplayStringHTML() {
		return true;
	}

	@Override
	public void requestSuggestions(SuggestOracle.Request request, SuggestOracle.Callback callback) {
		this.request = request;
		this.callback = callback;

		timer.cancel();
		timer.schedule(EntitySearchBox.DELAY);
	}

	public SuggestOracle.Request getRequest() {
		return request;
	}

	public SuggestOracle.Callback getCallback() {
		return callback;
	}

	public EntitySearchBoxSuggestion makeEntitySuggestion(Hit hit, String prefix) {
		return new EntitySearchBoxSuggestion(hit, prefix);
	}


	/*
	 * Suggestion
	 */
	public class EntitySearchBoxSuggestion implements IsSerializable, Suggestion {
		private Hit hit;
		private String prefix;

		public EntitySearchBoxSuggestion(Hit hit, String prefix) {
			this.hit = hit;
			this.prefix = prefix;
		}

		public Hit getHit() {
			return hit;
		}

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public String getDisplayString() {
			return hit.getName() + " | " + hit.getId();
		}

		@Override
		public String getReplacementString() {
			return getDisplayString();
		}

	} // end inner class UserGroupSuggestion
}
