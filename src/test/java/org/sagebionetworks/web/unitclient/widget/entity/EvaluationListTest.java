package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Mockito.mock;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.widget.entity.EvaluationList;
import org.sagebionetworks.web.client.widget.entity.EvaluationListView;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

public class EvaluationListTest {

	EvaluationListView mockView;
	EvaluationList widget;
	
	@Before
	public void before() {
		mockView = mock(EvaluationListView.class);
		widget = new EvaluationList(mockView);
	}
	
	@Test
	public void testGetSelectedEvaluationsEmpty() throws RestServiceException {
		//configure with no evaluations
		List<Evaluation> emptyList = new ArrayList<Evaluation>();
		widget.configure(emptyList);
	}
	
	@Test
	public void testGetSelectedEvaluationsNoneSelected() throws RestServiceException {
		//user selected none
		List<Evaluation> evaluations = new ArrayList<Evaluation>();
		evaluations.add(new Evaluation());
		evaluations.add(new Evaluation());
		widget.configure(evaluations);
		when(mockView.getSelectedEvaluationIndexes()).thenReturn(new ArrayList<Integer>());
		
		assertTrue(widget.getSelectedEvaluations().isEmpty());
	}
	
	@Test
	public void testGetSelectedEvaluationsSomeSelected() throws RestServiceException {
		//user selected a subset of the evaluations
		List<Evaluation> evaluations = new ArrayList<Evaluation>();
		evaluations.add(new Evaluation());
		Evaluation eval2 = new Evaluation();
		eval2.setId("2");
		evaluations.add(eval2);
		widget.configure(evaluations);
		ArrayList<Integer> selectedIndexes = new ArrayList<Integer>();
		selectedIndexes.add(1);
		when(mockView.getSelectedEvaluationIndexes()).thenReturn(selectedIndexes);
		
		assertEquals(1, widget.getSelectedEvaluations().size());
		assertEquals(eval2, widget.getSelectedEvaluations().get(0));
	}
	
	
}
