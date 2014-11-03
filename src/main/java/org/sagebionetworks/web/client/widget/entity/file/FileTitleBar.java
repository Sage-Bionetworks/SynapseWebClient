package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityEditor;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileTitleBar implements FileTitleBarView.Presenter, SynapseWidgetPresenter {
	
	private FileTitleBarView view;
	private AuthenticationController authenticationController;
	private EntityUpdatedHandler entityUpdatedHandler;
	private EntityBundle entityBundle;
	private EntityTypeProvider entityTypeProvider;
	private SynapseClientAsync synapseClient;
	private EntityEditor entityEditor;
	
	@Inject
	public FileTitleBar(FileTitleBarView view, AuthenticationController authenticationController, EntityTypeProvider entityTypeProvider, SynapseClientAsync synapseClient, EntityEditor entityEditor) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.entityTypeProvider = entityTypeProvider;
		this.synapseClient = synapseClient;
		this.entityEditor = entityEditor;
		view.setPresenter(this);
	}	
	
	public Widget asWidget(EntityBundle bundle) {		
		view.setPresenter(this);
		this.entityBundle = bundle; 		
		
		// Get EntityType
		EntityType entityType = entityTypeProvider.getEntityTypeForEntity(bundle.getEntity());
		
		view.createTitlebar(bundle, entityType, authenticationController);
		return view.asWidget();
	}
	
	/**
	 * For unit testing. call asWidget with the new Entity for the view to be in sync.
	 * @param bundle
	 */
	public void setEntityBundle(EntityBundle bundle) {
		this.entityBundle = bundle;
	}
	
	public void clearState() {
		view.clear();
		// remove handlers
		entityUpdatedHandler = null;		
		this.entityBundle = null;		
	}

	/**
	 * Does nothing. Use asWidget(Entity)
	 */
	@Override
	public Widget asWidget() {
		return null;
	}
    
	@Override
	public void fireEntityUpdatedEvent() {
		if (entityUpdatedHandler != null)
			entityUpdatedHandler.onPersistSuccess(new EntityUpdatedEvent());
	}
	
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
		entityEditor.setEntityUpdatedHandler(handler);
	}

	@Override
	public boolean isUserLoggedIn() {
		return authenticationController.isLoggedIn();
	}

	@Override
	public void addNewChild(EntityType type, String parentId) {
		entityEditor.addNewEntity(type, parentId);
		
	}
	
	public static boolean isDataPossiblyWithin(FileEntity fileEntity) {
		String dataFileHandleId = fileEntity.getDataFileHandleId();
		return (dataFileHandleId != null && dataFileHandleId.length() > 0);
	}

	public static String getEncodedSftpUrl(String sftpProxy, String proxiedSftpLink) {
		//input params should not be null, and the proxied sftp link should be longer than the sftp proxy url!
		if (proxiedSftpLink == null || sftpProxy == null) {
			return null;
		}
		if (proxiedSftpLink.length() <= sftpProxy.length())
			throw new IllegalArgumentException("Not a valid proxied sftp link: proxiedSftpLink=" + proxiedSftpLink + " sftpProxy="+sftpProxy);
		
		//where is the encoded sftp prefix?
		int sftpPrefixIndex = proxiedSftpLink.toLowerCase().indexOf(WebConstants.SFTP_PREFIX_ENCODED);
		if (sftpPrefixIndex == -1) {
			throw new IllegalArgumentException("Proxied sftp link does not contain an encoded sftp url: proxiedSftpLink=" + proxiedSftpLink);
		}
		return proxiedSftpLink.substring(sftpPrefixIndex);
	}
	
	public static String getSftpDomain(String encodedSftpUrl) {
		if (encodedSftpUrl == null)
			return null;
		if (!encodedSftpUrl.toLowerCase().startsWith(WebConstants.SFTP_PREFIX_ENCODED)) {
			throw new IllegalArgumentException("Not an encoded sftp url: " + encodedSftpUrl);
		}
		String domain = encodedSftpUrl.substring(WebConstants.SFTP_PREFIX_ENCODED.length());
		//if a port is specified, then find the (encoded) colon
		int colonIndex = domain.indexOf("%3A");
		if (colonIndex != -1) {
			domain = domain.substring(0, colonIndex);
		} else {
			//no port, find the (encoded) slash
			int slashIndex = domain.indexOf("%2F");
			if (slashIndex != -1) {
				domain = domain.substring(0, slashIndex);
			}
			
		}
		return domain;

	}
	
	
	/*
	 * Private Methods
	 */
}
