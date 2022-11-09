package org.sagebionetworks.web.client.widget.table.modal.fileview;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.web.client.widget.entity.tabs.DatasetsTab;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTab;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.shared.WebConstants;

/**
 * Wizard used to create a new Table or View
 *
 * @author Jay
 *
 */
public class CreateTableViewWizard {

  ModalWizardWidget modalWizardWidget;
  CreateTableViewWizardStep1 step1;
  private String parentId;
  private TableType type;
  public static final String VIEW_HELP =
    "Synapse File Views are views of all files within one or more Projects or Folders.";
  public static final String VIEW_URL =
    WebConstants.DOCS_URL + "Views.2011070739.html";
  public static final String PROJECT_VIEW_HELP =
    "A Synapse Project View represents a logical collection of Projects.";

  public static final String DATASET_COLLECTION_HELP =
    "Dataset Collections allow you to collate Datasets found across one or more Synapse Projects or Folders. In order to add a Dataset to a Dataset Collection, it must be shared with you.";

  @Inject
  public CreateTableViewWizard(
    ModalWizardWidget modalWizardWidget,
    CreateTableViewWizardStep1 step1
  ) {
    this.modalWizardWidget = modalWizardWidget;
    this.modalWizardWidget.setModalSize(ModalSize.LARGE);
    this.step1 = step1;
  }

  public void configure(String parentId, TableType type) {
    this.parentId = parentId;
    this.type = type;
    if (TableType.project_view.equals(type)) {
      this.modalWizardWidget.setTitle("Create Project View");
      this.modalWizardWidget.setHelp(PROJECT_VIEW_HELP, VIEW_URL);
    } else if (TableType.table.equals(type)) {
      this.modalWizardWidget.setTitle("Create Table");
      this.modalWizardWidget.setHelp(
          TablesTab.TABLES_HELP,
          TablesTab.TABLES_HELP_URL
        );
    } else if (TableType.dataset.equals(type)) {
      this.modalWizardWidget.setTitle("Create Dataset");
      this.modalWizardWidget.setHelp(
          DatasetsTab.DATASETS_HELP,
          DatasetsTab.DATASETS_HELP_URL
        );
    } else if (TableType.dataset_collection.equals(type)) {
      this.modalWizardWidget.setTitle("Create Dataset Collection");
      this.modalWizardWidget.setHelp(
          DATASET_COLLECTION_HELP,
          DatasetsTab.DATASETS_HELP_URL
        );
    } else if (TableType.submission_view.equals(type)) {
      this.modalWizardWidget.setTitle("Create Submission View");
      // TODO: send to submission view docs page (https://github.com/Sage-Bionetworks/synapseDocs/issues/787)
      // this.modalWizardWidget.setHelp(TablesTab.SUBMISSION_VIEW_HELP, TablesTab.SUBMISSION_VIEW_HELP_URL);
    } else {
      this.modalWizardWidget.setTitle("Create View");
      this.modalWizardWidget.setHelp(VIEW_HELP, VIEW_URL);
    }
  }

  public Widget asWidget() {
    return modalWizardWidget.asWidget();
  }

  public void showModal(WizardCallback wizardCallback) {
    this.step1.configure(parentId, type);
    this.modalWizardWidget.configure(this.step1);
    this.modalWizardWidget.showModal(wizardCallback);
  }
}
