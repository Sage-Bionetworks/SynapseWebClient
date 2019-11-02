package org.sagebionetworks.web.client.widget.sharing;

import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PublicPrivateBadgeViewImpl implements PublicPrivateBadgeView, IsWidget {
	public interface PublicPrivateBadgeViewImplUiBinder extends UiBinder<Widget, PublicPrivateBadgeViewImpl> {
	};

	Widget widget;
	@UiField
	Span publicSpan;
	@UiField
	Span privateSpan;

	@Inject
	public PublicPrivateBadgeViewImpl(PublicPrivateBadgeViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);

	}

	@Override
	public void setIsPublic(boolean isPublic) {
		publicSpan.setVisible(isPublic);
		privateSpan.setVisible(!isPublic);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showLoading() {}

	@Override
	public void clear() {
		privateSpan.setVisible(false);
		publicSpan.setVisible(false);
	}
}
