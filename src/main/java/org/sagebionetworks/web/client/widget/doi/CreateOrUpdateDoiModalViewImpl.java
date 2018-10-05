package org.sagebionetworks.web.client.widget.doi;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Divider;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.IntegerBox;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.doi.v2.DoiResourceTypeGeneral;

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
	Button mintDoiButton;
	@UiField
	Div doiEditorDiv;
	@UiField
	TextArea creatorsField;
	@UiField
	TextArea titlesField;
	@UiField
	DropDownMenu resourceTypeGeneralSelect;
	@UiField
	Button resourceTypeGeneralSelectButton;
	@UiField
	IntegerBox publicationYearField;
	@UiField
	Modal doiModal;
	@UiField
	Heading title;
	@UiField
	Button cancelButton;
	@UiField
	Div jobTrackingWidget;
	@UiField
	Div synAlert;
	@UiField
	Div doiOverwriteWarning;

	
	@Inject
	public CreateOrUpdateDoiModalViewImpl(CreateOrUpdateDoiModalViewImplUiBinder binder) {
		this.widget = binder.createAndBindUi(this);
		jobTrackingWidget.setVisible(false);
		mintDoiButton.addClickHandler(event -> presenter.onSaveDoi());
		mintDoiButton.setEnabled(true);
		cancelButton.addClickHandler(event -> doiModal.hide());

		// SWC-4445
		// initialize the resource type general by adding common types to the top and adding a separator.
		AnchorListItem datasetItem = new AnchorListItem();
		datasetItem.setText(DoiResourceTypeGeneral.Dataset.name());
		datasetItem.addClickHandler(event -> setResourceTypeGeneral(DoiResourceTypeGeneral.Dataset.name()));
		resourceTypeGeneralSelect.add(datasetItem);

		AnchorListItem collectionItem = new AnchorListItem();
		collectionItem.setText(DoiResourceTypeGeneral.Collection.name());
		collectionItem.addClickHandler(event -> setResourceTypeGeneral(DoiResourceTypeGeneral.Collection.name()));
		resourceTypeGeneralSelect.add(collectionItem);

		resourceTypeGeneralSelect.add(new Divider());
		for (DoiResourceTypeGeneral rtg : DoiResourceTypeGeneral.values()) {
			if (!rtg.equals(DoiResourceTypeGeneral.Dataset) && !rtg.equals(DoiResourceTypeGeneral.Collection)) {
				AnchorListItem otherItem = new AnchorListItem();
				otherItem.setText(rtg.name());
				otherItem.addClickHandler(event -> setResourceTypeGeneral(rtg.name()));
				resourceTypeGeneralSelect.add(otherItem);
			}
		}
	}

	@Override
	public void reset() {
		jobTrackingWidget.setVisible(false);
		creatorsField.clear();
		titlesField.clear();
		resourceTypeGeneralSelect.setTitle(DoiResourceTypeGeneral.Dataset.name());
		publicationYearField.reset();
		mintDoiButton.setEnabled(true);
	}

	@Override
	public void setIsLoading(boolean isLoading) {
		jobTrackingWidget.setVisible(isLoading);
		mintDoiButton.setEnabled(!isLoading);
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public String getCreators() {
		return creatorsField.getText();
	}

	@Override
	public void setCreators(String creators) {
		creatorsField.setText(creators);
	}

	@Override
	public String getTitles() {
		return titlesField.getText();
	}

	@Override
	public void setTitles(String titles) {
		titlesField.setText(titles);
	}

	@Override
	public String getResourceTypeGeneral() {
		return resourceTypeGeneralSelect.getTitle();
	}

	@Override
	public void setResourceTypeGeneral(String resourceTypeGeneral) {
		// Since the select elements aren't in the enum order, go through the "option" elements
		resourceTypeGeneralSelectButton.setText(resourceTypeGeneral);
	}

	@Override
	public Long getPublicationYear() {
		return Long.valueOf(publicationYearField.getText());
	}

	@Override
	public void setPublicationYear(Long publicationYear) {
		publicationYearField.setText(publicationYear.toString());
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void showOverwriteWarning(boolean showWarning) {
		doiOverwriteWarning.setVisible(showWarning);
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
	public void setModalTitle(String newTitle) {
		title.setText(newTitle);
	}

	@Override
	public void setJobTrackingWidget(IsWidget w) {
		jobTrackingWidget.clear();
		jobTrackingWidget.add(w);
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlert.clear();
		synAlert.add(w);
	}

}
