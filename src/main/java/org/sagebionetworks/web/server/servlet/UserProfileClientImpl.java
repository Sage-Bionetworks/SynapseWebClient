package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.verification.VerificationPagedResults;
import org.sagebionetworks.repo.model.verification.VerificationState;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.web.client.UserProfileClient;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

@SuppressWarnings("serial")
public class UserProfileClientImpl extends SynapseClientBase implements UserProfileClient {

	@Override
	public VerificationSubmission createVerificationSubmission(VerificationSubmission verificationSubmission, String hostPageBaseURL) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			// update user profile
			UserProfile myProfile = synapseClient.getMyProfile();
			myProfile.setFirstName(verificationSubmission.getFirstName());
			myProfile.setLastName(verificationSubmission.getLastName());
			myProfile.setLocation(verificationSubmission.getLocation());
			myProfile.setCompany(verificationSubmission.getCompany());
			synapseClient.updateMyProfile(myProfile);

			String signedTokenEndpoint = SynapseClientImpl.getSignedTokenEndpoint(hostPageBaseURL);
			return synapseClient.createVerificationSubmission(verificationSubmission, signedTokenEndpoint);
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
	public void updateVerificationState(long verificationId, VerificationState verificationState, String hostPageBaseURL) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			String signedTokenEndpoint = SynapseClientImpl.getSignedTokenEndpoint(hostPageBaseURL);
			synapseClient.updateVerificationState(verificationId, verificationState, signedTokenEndpoint);
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
}
