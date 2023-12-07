package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.SelfSignAccessRequirement;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.accessrequirements.EntitySubjectsWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.TeamSubjectsWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;

/**
 * First page of creating an access requirement
 *
 * @author Jay
 *
 */
public class CreateAccessRequirementStep1
  implements ModalPage, CreateAccessRequirementStep1View.Presenter {

  public static final String EMPTY_SUBJECT_LIST_ERROR_MESSAGE =
    "Please select at least one resource for this Access Requirement to be associated with.";
  CreateAccessRequirementStep1View view;
  List<RestrictableObjectDescriptor> subjects;
  ModalPresenter modalPresenter;
  CreateManagedACTAccessRequirementStep2 actStep2;
  CreateBasicAccessRequirementStep2 basicStep2;
  ACCESS_TYPE currentAccessType;
  AccessRequirement accessRequirement;
  SynapseClientAsync synapseClient;
  TeamSubjectsWidget teamSubjectsWidget;
  EntitySubjectsWidget entitySubjectsWidget;

  @Inject
  public CreateAccessRequirementStep1(
    CreateAccessRequirementStep1View view,
    CreateManagedACTAccessRequirementStep2 actStep2,
    CreateBasicAccessRequirementStep2 touStep2,
    SynapseClientAsync synapseClient,
    TeamSubjectsWidget teamSubjectsWidget,
    EntitySubjectsWidget entitySubjectsWidget
  ) {
    super();
    this.view = view;
    this.actStep2 = actStep2;
    this.basicStep2 = touStep2;
    this.teamSubjectsWidget = teamSubjectsWidget;
    this.entitySubjectsWidget = entitySubjectsWidget;
    this.synapseClient = synapseClient;
    fixServiceEntryPoint(synapseClient);
    view.setTeamSubjects(teamSubjectsWidget);
    view.setEntitySubjects(entitySubjectsWidget);
    view.setPresenter(this);
    CallbackP<RestrictableObjectDescriptor> deleteSubjectCallback =
      new CallbackP<RestrictableObjectDescriptor>() {
        @Override
        public void invoke(RestrictableObjectDescriptor subject) {
          subjects.remove(subject);
          refreshSubjects();
        }
      };
    teamSubjectsWidget.setDeleteCallback(deleteSubjectCallback);
  }

  @Override
  public void onAddTeams() {
    currentAccessType = ACCESS_TYPE.PARTICIPATE;
    String teamIds = view.getTeamIds();
    String[] teams = teamIds.split("[,\\s]\\s*");
    for (int i = 0; i < teams.length; i++) {
      if (teams[i].trim().length() > 0) {
        RestrictableObjectDescriptor newSubject =
          new RestrictableObjectDescriptor();
        newSubject.setId(teams[i]);
        newSubject.setType(RestrictableObjectType.TEAM);
        if (!subjects.contains(newSubject)) {
          subjects.add(newSubject);
        }
      }
    }
    refreshSubjects();
  }

  /**
   * Configure this widget before use.
   *
   */
  public void configure(RestrictableObjectDescriptor initialSubject) {
    accessRequirement = null;
    view.setAccessRequirementTypeSelectionVisible(true);
    List<RestrictableObjectDescriptor> initialSubjects =
      new ArrayList<RestrictableObjectDescriptor>();
    initialSubjects.add(initialSubject);
    setSubjects(initialSubjects);
  }

  public void configure(AccessRequirement ar) {
    accessRequirement = ar;
    view.setAccessRequirementTypeSelectionVisible(false);
    view.setName(accessRequirement.getName());
    setSubjects(accessRequirement.getSubjectIds());
  }

  private void refreshSubjects() {
    setSubjects(subjects);
  }

  private void setSubjects(List<RestrictableObjectDescriptor> initialSubjects) {
    subjects = initialSubjects;
    teamSubjectsWidget.configure(subjects);
    entitySubjectsWidget.configure(
      subjects,
      true,
      newSubjects -> {
        subjects = newSubjects;
      }
    );

    if (subjects.size() > 0) {
      if (subjects.get(0).getType().equals(RestrictableObjectType.ENTITY)) {
        currentAccessType = ACCESS_TYPE.DOWNLOAD;
        view.showEntityUI();
      } else {
        currentAccessType = ACCESS_TYPE.PARTICIPATE;
        view.setTeamIdsString("");
      }
    }
  }

  @Override
  public void onPrimary() {
    // SWC-3700: validate that subjects have been set
    if (subjects.size() == 0) {
      modalPresenter.setErrorMessage(EMPTY_SUBJECT_LIST_ERROR_MESSAGE);
      return;
    }

    if (accessRequirement == null) {
      if (view.isACTAccessRequirementType()) {
        accessRequirement = new ACTAccessRequirement();
        ((ACTAccessRequirement) accessRequirement).setOpenJiraIssue(true);
      } else if (view.isManagedACTAccessRequirementType()) {
        accessRequirement = new ManagedACTAccessRequirement();
      } else {
        accessRequirement = new SelfSignAccessRequirement();
      }
    }
    accessRequirement.setAccessType(currentAccessType);
    accessRequirement.setSubjectIds(subjects);
    accessRequirement.setName(view.getName());

    modalPresenter.setLoading(true);
    synapseClient.createOrUpdateAccessRequirement(
      accessRequirement,
      new AsyncCallback<AccessRequirement>() {
        @Override
        public void onFailure(Throwable caught) {
          modalPresenter.setLoading(false);
          modalPresenter.setErrorMessage(caught.getMessage());
        }

        @Override
        public void onSuccess(AccessRequirement accessRequirement) {
          modalPresenter.setLoading(false);
          if (accessRequirement instanceof ManagedACTAccessRequirement) {
            actStep2.configure((ManagedACTAccessRequirement) accessRequirement);
            modalPresenter.setNextActivePage(actStep2);
          } else {
            basicStep2.configure(accessRequirement);
            modalPresenter.setNextActivePage(basicStep2);
          }
        }
      }
    );
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public void setModalPresenter(ModalPresenter modalPresenter) {
    this.modalPresenter = modalPresenter;
    modalPresenter.setPrimaryButtonText(DisplayConstants.NEXT);
  }
}
