package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoDialog;

import com.google.inject.Inject;

public class CertifiedUserControllerImpl implements CertifiedUserController {
	
	QuizInfoDialog quizInfoDialog;
	
	@Inject
	public CertifiedUserControllerImpl(QuizInfoDialog quizInfoDialog) {
		super();
		this.quizInfoDialog = quizInfoDialog;
	}

	@Override
	public void checkUploadToEntity(EntityBundle toUpdate, Callback callback) {
		// Only certified users can upload data
		if(toUpdate.getPermissions().getIsCertifiedUser()){
			callback.invoke();
		}else{
			quizInfoDialog.show();
		}
	}

	@Override
	public void checkCreateEntity(EntityBundle bundle, String entityClassName,
			Callback callback) {
		// Anyone can create a project
		if(Project.class.getName().equals(entityClassName)){
			callback.invoke();
		}else{
			// Only certified users can create non-projects
			if(bundle.getPermissions().getIsCertifiedUser()){
				callback.invoke();
			}else{
				quizInfoDialog.show();
			}
		}
	}

	@Override
	public void checkDeleteEntity(EntityBundle toDelete, Callback callback) {
		// Currently both certified users and non-certified users can delete entities.
		callback.invoke();
	}

	@Override
	public void checkUpdateEntity(EntityBundle toUpdate, Callback callback) {
		// Anyone can update an entity
		callback.invoke();
	}

}
