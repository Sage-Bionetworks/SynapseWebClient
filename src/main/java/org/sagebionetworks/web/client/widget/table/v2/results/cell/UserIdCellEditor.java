package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * UserId cell editor
 * 
 * @author Jay
 *
 */
public class UserIdCellEditor implements CellEditor {
	UserIdCellEditorView view;
	SynapseSuggestBox peopleSuggestWidget;
	UserGroupSuggestionProvider provider;
	UserIdCellRenderer userIdCellRenderer;
	ClickHandler onUserBadgeClick;
	String value;

	@Inject
	public UserIdCellEditor(UserIdCellEditorView view, SynapseSuggestBox peopleSuggestWidget, UserGroupSuggestionProvider provider, UserIdCellRenderer userIdCellRenderer) {
		this.view = view;
		this.peopleSuggestWidget = peopleSuggestWidget;
		this.provider = provider;
		this.userIdCellRenderer = userIdCellRenderer;
		view.setSynapseSuggestBoxWidget(peopleSuggestWidget.asWidget());
		view.setUserIdCellRenderer(userIdCellRenderer.asWidget());
		peopleSuggestWidget.setPlaceholderText("Enter ID or name...");
		peopleSuggestWidget.setSuggestionProvider(provider);
		peopleSuggestWidget.addItemSelectedHandler(new CallbackP<UserGroupSuggestion>() {
			@Override
			public void invoke(UserGroupSuggestion suggestion) {
				onUserSelected(suggestion);
			}
		});
		onUserBadgeClick = event -> {
			view.showEditor(true);
			peopleSuggestWidget.setFocus(true);
			peopleSuggestWidget.selectAll();
		};
		view.setUserIdCellRendererClickHandler(onUserBadgeClick);
	}

	public void onUserSelected(UserGroupSuggestion suggestion) {
		setValue(suggestion.getId());
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getValue() {
		// if empty, then value is null
		String value = peopleSuggestWidget.getText();
		if (value != null && value.trim().isEmpty()) {
			value = null;
		}
		return value;
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return peopleSuggestWidget.addKeyDownHandler(handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		peopleSuggestWidget.fireEvent(event);
	}

	@Override
	public int getTabIndex() {
		return peopleSuggestWidget.getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		peopleSuggestWidget.setAccessKey(key);
	}

	@Override
	public void setFocus(boolean focused) {
		peopleSuggestWidget.setFocus(focused);
	}

	@Override
	public void setTabIndex(int index) {
		peopleSuggestWidget.setTabIndex(index);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setValue(String value) {
		peopleSuggestWidget.clear();
		peopleSuggestWidget.setText(value);
		userIdCellRenderer.setValue(value, onUserBadgeClick);
		boolean showEditor = value == null || value.trim().isEmpty();
		view.showEditor(showEditor);
	}
}
