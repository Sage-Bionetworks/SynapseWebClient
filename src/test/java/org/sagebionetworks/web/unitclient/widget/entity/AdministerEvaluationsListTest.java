package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.widget.entity.AdministerEvaluationsList;
import org.sagebionetworks.web.client.widget.entity.AdministerEvaluationsListView;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class AdministerEvaluationsListTest {
	
	AdministerEvaluationsList evalList;
	AdministerEvaluationsListView mockView;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	ChallengeClientAsync mockChallengeClient;
	
	@Before
	public void setup() throws Exception{
		mockView = mock(AdministerEvaluationsListView.class);
		mockChallengeClient = mock(ChallengeClientAsync.class);
		evalList = new AdministerEvaluationsList(mockView, mockChallengeClient, adapterFactory);
		
		ArrayList<String> evaluationResults = new ArrayList<String>();
		
		Evaluation e1 = new Evaluation();
		e1.setId("101");
		evaluationResults.add(e1.writeToJSONObject(adapterFactory.createNew()).toJSONString());
		
		Evaluation e2 = new Evaluation();
		e1.setId("102");
		evaluationResults.add(e2.writeToJSONObject(adapterFactory.createNew()).toJSONString());
		AsyncMockStubber.callSuccessWith(evaluationResults).when(mockChallengeClient).getSharableEvaluations(anyString(), any(AsyncCallback.class));
	}	
	
	@Test
	public void testConfigure() {
		evalList.configure("syn100", null);
		verify(mockView).configure(any(List.class));
	}
	
	@Test
	public void testConfigureFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new BadRequestException()).when(mockChallengeClient).getSharableEvaluations(anyString(), any(AsyncCallback.class));
		evalList.configure("syn100", null);
		verify(mockView).showErrorMessage(anyString());
	}

}
