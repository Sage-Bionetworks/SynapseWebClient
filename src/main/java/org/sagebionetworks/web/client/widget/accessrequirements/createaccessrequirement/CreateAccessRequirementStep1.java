package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRenderer;
import org.sagebionetworks.web.client.widget.team.TeamBadge;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * First page of data access wizard.  
 * @author Jay
 *
 */
public class CreateAccessRequirementStep1 implements ModalPage, CreateAccessRequirementStep1View.Presenter {
	CreateAccessRequirementStep1View view;
	List<RestrictableObjectDescriptor> subjects;
	ModalPresenter modalPresenter;
	CreateACTAccessRequirement actStep2;
	CreateToUAccessRequirement touStep2;
	PortalGinInjector ginInjector;
	ACCESS_TYPE currentAccessType;
	AccessRequirement accessRequirement;
	
	@Inject
	public CreateAccessRequirementStep1(
			CreateAccessRequirementStep1View view,
			CreateACTAccessRequirement actStep2,
			CreateToUAccessRequirement touStep2,
			PortalGinInjector ginInjector) {
		super();
		this.view = view;
		this.actStep2 = actStep2;
		this.touStep2 = touStep2;
		this.ginInjector = ginInjector;
		view.setPresenter(this);
	}
	
	@Override
	public void onSetEntities() {
		currentAccessType = ACCESS_TYPE.DOWNLOAD;
		String entityIds = view.getEntityIds();
		String[] entities = entityIds.split("[,\\s]\\s*");
		List<RestrictableObjectDescriptor> newSubjects = new ArrayList<RestrictableObjectDescriptor>();
		for (int i = 0; i < entities.length; i++) {
			RestrictableObjectDescriptor newSubject = new RestrictableObjectDescriptor();
			newSubject.setId(entities[i]);
			newSubject.setType(RestrictableObjectType.ENTITY);
			newSubjects.add(newSubject);
		}
		setSubjects(newSubjects);
	}
	
	@Override
	public void onSetTeams() {
		currentAccessType = ACCESS_TYPE.PARTICIPATE;
		String teamIds = view.getTeamIds();
		String[] teams = teamIds.split("[,\\s]\\s*");
		List<RestrictableObjectDescriptor> newSubjects = new ArrayList<RestrictableObjectDescriptor>();
		for (int i = 0; i < teams.length; i++) {
			RestrictableObjectDescriptor newSubject = new RestrictableObjectDescriptor();
			newSubject.setId(teams[i]);
			newSubject.setType(RestrictableObjectType.TEAM);
			newSubjects.add(newSubject);
		}
		setSubjects(newSubjects);
	}
	
	
	/**
	 * Configure this widget before use.
	 * 
	 */
	public void configure(RestrictableObjectDescriptor initialSubject) {
		accessRequirement = null;
		view.setAccessRequirementTypeSelectionVisible(true);
		List<RestrictableObjectDescriptor> initialSubjects = new ArrayList<RestrictableObjectDescriptor>();
		initialSubjects.add(initialSubject);
		setSubjects(initialSubjects);
	}
	
	public void configure(AccessRequirement ar) {
		accessRequirement = ar;
		view.setAccessRequirementTypeSelectionVisible(false);
		setSubjects(accessRequirement.getSubjectIds());
	}
	
	private void setSubjects(List<RestrictableObjectDescriptor> initialSubjects) {
		view.clearSubjects();
		subjects = initialSubjects;
		
		for (RestrictableObjectDescriptor rod : initialSubjects) {
			if (rod.getType().equals(RestrictableObjectType.ENTITY)) {
				currentAccessType = ACCESS_TYPE.DOWNLOAD;
				EntityIdCellRenderer entityRenderer = ginInjector.createEntityIdCellRenderer();
				entityRenderer.setValue(rod.getId());
				view.addSubject(entityRenderer);
			} else if (rod.getType().equals(RestrictableObjectType.TEAM)) {
				currentAccessType = ACCESS_TYPE.PARTICIPATE;
				TeamBadge teamBadge = ginInjector.getTeamBadgeWidget();
				teamBadge.configure(rod.getId());
				view.addSubject(teamBadge.asWidget());
			}
		}
	}
	
	@Override
	public void onPrimary() {
		if (accessRequirement == null) {
			if (view.isACTAccessRequirementType()) {
				accessRequirement = new ACTAccessRequirement();
			} else {
				accessRequirement = new TermsOfUseAccessRequirement();
			}
		}
		accessRequirement.setAccessType(currentAccessType);
		accessRequirement.setSubjectIds(subjects);
		
		if (accessRequirement instanceof ACTAccessRequirement) {
			actStep2.configure((ACTAccessRequirement)accessRequirement);
			modalPresenter.setNextActivePage(actStep2);
		} else {
			touStep2.configure((TermsOfUseAccessRequirement)accessRequirement);
			modalPresenter.setNextActivePage(touStep2);
		}
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
