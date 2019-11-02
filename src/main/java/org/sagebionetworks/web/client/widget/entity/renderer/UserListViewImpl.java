package org.sagebionetworks.web.client.widget.entity.renderer;


import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * View that contains a list of users
 * 
 * @author jayhodgson
 *
 */
public class UserListViewImpl implements UserListView {

	public interface Binder extends UiBinder<Widget, UserListViewImpl> {
	}

	private Presenter presenter;
	PortalGinInjector ginInjector;

	@UiField
	Div paginationWidgetContainer;
	@UiField
	Div participantsContainer;

	@UiField
	LoadingSpinner loadingUI;

	@UiField
	Alert errorUI;
	@UiField
	Text errorText;
	@UiField
	Panel participantsUI;

	Widget widget;

	@Inject
	public UserListViewImpl(Binder binder, PortalGinInjector ginInjector) {
		widget = binder.createAndBindUi(this);
		this.ginInjector = ginInjector;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void clearUsers() {
		participantsContainer.clear();
	}

	@Override
	public void addUser(UserProfile profile) {
		UserBadge userBadge = ginInjector.getUserBadgeWidget();
		userBadge.configure(profile);
		Div userBadgeContainer = new Div();
		userBadgeContainer.addStyleName("margin-bottom-5");
		userBadgeContainer.add(userBadge.asWidget());
		participantsContainer.add(userBadgeContainer);
	}

	@Override
	public void showNoUsers() {
		participantsContainer.add(new Paragraph(DisplayConstants.EMPTY));
	}

	@Override
	public void setPaginationWidget(Widget paginationWidget) {
		paginationWidgetContainer.clear();
		paginationWidgetContainer.add(paginationWidget);
	}

	@Override
	public void clear() {
		clearUsers();
		hideErrors();
	}

	@Override
	public void showErrorMessage(String message) {
		errorText.setText(message);
		errorUI.setVisible(true);
		participantsUI.setVisible(false);
	}

	@Override
	public void hideErrors() {
		errorUI.setVisible(false);
		participantsUI.setVisible(true);
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showLoading() {
		loadingUI.setVisible(true);
	}

	@Override
	public void hideLoading() {
		loadingUI.setVisible(false);
	}
}
