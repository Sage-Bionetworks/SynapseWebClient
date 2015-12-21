package org.sagebionetworks.web.server.servlet;

import java.net.URL;

import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.oauth.OAuthProvider;
import org.sagebionetworks.repo.model.verification.VerificationPagedResults;
import org.sagebionetworks.repo.model.verification.VerificationState;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.web.client.UserProfileClient;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

@SuppressWarnings("serial")
public class UserProfileClientImpl extends SynapseClientBase implements
		UserProfileClient {
	
	@Override
	public VerificationSubmission createVerificationSubmission(VerificationSubmission verificationSubmission, String hostPageBaseURL) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			String notificationEndpoint = SynapseClientImpl.getNotificationEndpoint(NotificationTokenType.Settings, hostPageBaseURL);
			return synapseClient.createVerificationSubmission(verificationSubmission, notificationEndpoint);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public VerificationPagedResults listVerificationSubmissions(VerificationStateEnum currentState, Long submitterId, Long limit, Long offset) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.listVerificationSubmissions(currentState, submitterId, limit, offset);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public void unbindOAuthProvidersUserId(OAuthProvider provider, String alias) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.unbindOAuthProvidersUserId(provider, alias);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	
	@Override
	public void updateVerificationState(long verificationId, VerificationState verificationState, String hostPageBaseURL) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			String notificationEndpoint = SynapseClientImpl.getNotificationEndpoint(NotificationTokenType.Settings, hostPageBaseURL);
			synapseClient.updateVerificationState(verificationId, verificationState, notificationEndpoint);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public void deleteVerificationSubmission(long verificationId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.deleteVerificationSubmission(verificationId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public UserBundle getMyOwnUserBundle(int mask) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getMyOwnUserBundle(mask);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public UserBundle getUserBundle(Long principalId, int mask) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getUserBundle(principalId, mask);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
}
