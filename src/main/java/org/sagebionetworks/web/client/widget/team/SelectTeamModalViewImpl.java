package org.sagebionetworks.web.client.widget.team;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SelectTeamModalViewImpl implements SelectTeamModalView {

	public interface SelectTeamModalViewImplUiBinder extends UiBinder<Widget, SelectTeamModalViewImpl> {
	}

	private SelectTeamModalView.Presenter presenter;

	private Widget widget;
	@UiField
	Button selectTeamButton;
	@UiField
	Div teamSelectBoxContainer;
	@UiField
	Modal teamModal;
	@UiField
	Heading title;
	@UiField
	Button cancelButton;

	@Inject
	public SelectTeamModalViewImpl(SelectTeamModalViewImplUiBinder binder) {
		this.widget = binder.createAndBindUi(this);
		selectTeamButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				teamModal.hide();
				presenter.onSelectTeam();
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				teamModal.hide();
			}
		});
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
	public void setSuggestWidget(Widget w) {
		teamSelectBoxContainer.clear();
		teamSelectBoxContainer.add(w);
	}

	@Override
	public void show() {
		teamModal.show();
	}

	@Override
	public void hide() {
		teamModal.hide();
	}

	@Override
	public void setTitle(String newTitle) {
		title.setText(newTitle);
	}
}
