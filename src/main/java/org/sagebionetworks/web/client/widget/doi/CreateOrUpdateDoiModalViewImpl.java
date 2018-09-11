package org.sagebionetworks.web.client.widget.doi;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateOrUpdateDoiModalViewImpl implements CreateOrUpdateDoiModalView {

	public interface CreateOrUpdateDoiModalViewImplUiBinder extends UiBinder<Widget, CreateOrUpdateDoiModalViewImpl> {}

	private CreateOrUpdateDoiModalView.Presenter presenter;
	
	private Widget widget;
	@UiField
	Button selectTeamButton;
	@UiField
	Div doiAuthors;
	@UiField
	Div doiPublicationYear;
	@UiField
	Div doiResourceTypeGeneral;
	@UiField
	Div doiTitles;
	@UiField
	Modal doiModal;
	@UiField
	Heading title;
	@UiField
	Button cancelButton;
	@UiField
	Div jobTrackingWidget;
	
	@Inject
	public CreateOrUpdateDoiModalViewImpl(CreateOrUpdateDoiModalViewImplUiBinder binder) {
		this.widget = binder.createAndBindUi(this);
		selectTeamButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doiModal.hide();
				presenter.onCreateDoi();
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doiModal.hide();
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
	public void setWidgets(Widget authors, Widget titles, Widget publicationYear, Widget resourceTypeGeneral) {
		doiAuthors.clear();
		doiTitles.clear();
		doiPublicationYear.clear();
		doiResourceTypeGeneral.clear();
		doiAuthors.add(authors);
		doiTitles.add(titles);
		doiPublicationYear.add(publicationYear);
		doiResourceTypeGeneral.add(resourceTypeGeneral);
	}
	
	@Override
	public void show() {
		doiModal.show();
	}
	
	@Override
	public void hide() {
		doiModal.hide();
	}
	
	@Override
	public void setTitle(String newTitle) {
		title.setText(newTitle);
	}

	@Override
	public void setJobTrackingWidget(IsWidget w) {
		jobTrackingWidget.clear();
		jobTrackingWidget.add(w);
	}

}
