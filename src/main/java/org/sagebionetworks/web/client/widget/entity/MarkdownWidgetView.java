package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface MarkdownWidgetView extends IsWidget {

	public interface Presenter {

		void configure(String md, WikiPageKey wikiKey, Long wikiVersionInView);

		void clear();

	}

	void setSynAlertWidget(Widget synAlert);

	void setEmptyVisible(boolean b);

	void setMarkdown(String result);

	ElementWrapper getElementById(String string);

	void addWidget(Widget widget, String divID);

	Widget asWidget();

	void callbackWhenAttached(final Callback callback);

	void clearMarkdown();

}
