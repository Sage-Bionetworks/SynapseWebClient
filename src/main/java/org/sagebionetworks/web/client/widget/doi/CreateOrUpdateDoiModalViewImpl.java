package org.sagebionetworks.web.client.widget.doi;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityTypeUtils;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.doi.v2.DoiResourceTypeGeneral;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.web.client.widget.HelpWidget;

import java.util.List;
import java.util.Optional;

public class CreateOrUpdateDoiModalViewImpl implements CreateOrUpdateDoiModalView {

	public interface CreateOrUpdateDoiModalViewImplUiBinder extends UiBinder<Widget, CreateOrUpdateDoiModalViewImpl> {
	}

	private CreateOrUpdateDoiModalView.Presenter presenter;
	private Widget widget;
	private Entity entity;

//	private final String VERSION_SELECT_HELP_TEXT = ;

	@UiField
	Button mintDoiButton;
	@UiField
	Div doiEditorDiv;
	@UiField
	TextArea creatorsField;
	@UiField
	TextArea titlesField;
	@UiField
	ListBox resourceTypeGeneralSelect;
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
	@UiField
	FormGroup versionForm;
	@UiField
	ListBox versionSelection;
	@UiField
	HelpWidget versionHelpBox;


	@Inject
	public CreateOrUpdateDoiModalViewImpl(CreateOrUpdateDoiModalViewImplUiBinder binder) {
		this.widget = binder.createAndBindUi(this);
		jobTrackingWidget.setVisible(false);
		mintDoiButton.addClickHandler(event -> presenter.onSaveDoi());
		mintDoiButton.setEnabled(true);
		cancelButton.addClickHandler(event -> doiModal.hide());
		versionSelection.addChangeHandler(event -> {
			Long version = Long.valueOf(versionSelection.getSelectedValue());
			if (version == 0L) { // We let '0' represent no version in the select box.
				presenter.onVersionChange(Optional.empty());
			} else {
				presenter.onVersionChange(Optional.of(version));
			}
		});

		initializeResourceTypeGeneralSelect();
	}

	private void initializeResourceTypeGeneralSelect() {
		// SWC-4445
		// initialize the resource type general select field by adding typical types to the top and adding a
		// separator.
		resourceTypeGeneralSelect.addItem(DoiResourceTypeGeneral.Dataset.name());
		resourceTypeGeneralSelect.addItem(DoiResourceTypeGeneral.Collection.name());
		resourceTypeGeneralSelect.addItem("────────────────────");
		resourceTypeGeneralSelect.getElement().getElementsByTagName("option").getItem(2).setAttribute("disabled", "disabled");
		for (DoiResourceTypeGeneral rtg : DoiResourceTypeGeneral.values()) {
			if (!rtg.equals(DoiResourceTypeGeneral.Dataset) && !rtg.equals(DoiResourceTypeGeneral.Collection)) {
				resourceTypeGeneralSelect.addItem(rtg.name());
			}
		}
	}

	@Override
	public void reset() {
		this.setIsLoading(true);
		creatorsField.clear();
		titlesField.clear();
		resourceTypeGeneralSelect.setTitle(DoiResourceTypeGeneral.Dataset.name());
		versionForm.setVisible(this.entity instanceof Versionable);
		String entityTypeDisplay = EntityTypeUtils.getDisplayName(EntityTypeUtils.getEntityTypeForClass(this.entity.getClass()));
		String helpMarkdown = "The version of the " + entityTypeDisplay +" for which the DOI should be minted.\n\n" +
				"Versioned DOIs will resolve to the specified version of the " + entityTypeDisplay + ".\n\n" +
				"Unversioned DOIs will always resolve to the newest version of this " + entityTypeDisplay + ", so the data in the table may change over time.";
		if (entity instanceof Table) {
			helpMarkdown += "\n\nTo create a DOI that resolves to the current set of data in the " + entityTypeDisplay +
					", create a new version and mint a DOI for that version.";
		}
		versionHelpBox.setHelpMarkdown(helpMarkdown);
		publicationYearField.reset();
		mintDoiButton.setEnabled(true);
	}

	@Override
	public void setIsLoading(boolean isLoading) {
		jobTrackingWidget.setVisible(isLoading);
		mintDoiButton.setEnabled(!isLoading);
		versionSelection.setEnabled(!isLoading);
		creatorsField.setEnabled(!isLoading);
		resourceTypeGeneralSelect.setEnabled(!isLoading);
		titlesField.setEnabled(!isLoading);
		publicationYearField.setEnabled(!isLoading);
	}

	@Override
	public void setVersions(List<VersionInfo> versions, Optional<Long> selectedVersion) {
		versionSelection.clear();
		versionSelection.addItem("Unversioned", "0"); // We let '0' represent no version in the select box.
		int selectedIndex = 0;
		for (int i = 0; i < versions.size(); i++) {
			VersionInfo version = versions.get(i);
			String label = "Version " + version.getVersionNumber();
			if (!version.getVersionLabel().isEmpty()) {
				label += " / " + version.getVersionLabel();
			}
			versionSelection.addItem(label, String.valueOf(version.getVersionNumber()));
			if (selectedVersion.isPresent() && selectedVersion.get().equals(version.getVersionNumber())) {
				selectedIndex = i + 1; // add one to offset the "Unversioned" entry before we iterate
			}
		}
		versionSelection.setSelectedIndex(selectedIndex);
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
		return resourceTypeGeneralSelect.getSelectedValue();
	}

	@Override
	public void setResourceTypeGeneral(String resourceTypeGeneral) {
		for (int i = 0; i < resourceTypeGeneralSelect.getItemCount(); i++) {
			if (resourceTypeGeneral.equals(resourceTypeGeneralSelect.getValue(i))) {
				resourceTypeGeneralSelect.setSelectedIndex(i);
				break;
			}
		}
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
	public void setEntity(Entity entity) {
		this.entity = entity;
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
