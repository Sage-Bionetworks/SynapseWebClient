package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.quiz.MultichoiceAnswer;
import org.sagebionetworks.repo.model.quiz.MultichoiceQuestion;
import org.sagebionetworks.repo.model.quiz.Question;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.presenter.QuestionContainerWidget;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.QuestionContainerWidgetView;

import com.google.gwt.event.dom.client.ClickHandler;

public class QuestionContainerWidgetTest {
	
	QuestionContainerWidgetView mockView;
	AuthenticationController mockAuthenticationController;
	UserAccountServiceAsync mockUserAccountServiceAsync;
	GlobalApplicationState mockGlobalApplicationState;
//	Question mockQuestion;
	MultichoiceQuestion mockMultichoiceQuestion;
	QuestionContainerWidget questionContainerWidget;
	MultichoiceAnswer mockAnswerOne;
	MultichoiceAnswer mockAnswerTwo;
	WikiPageKey mockMoreInfoKey;	
	
	@Before
	public void setup(){
		mockView = mock(QuestionContainerWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockUserAccountServiceAsync = mock(UserAccountServiceAsync.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockMultichoiceQuestion = mock(MultichoiceQuestion.class, withSettings().extraInterfaces(Question.class));
		mockAnswerOne = mock(MultichoiceAnswer.class);
		mockAnswerTwo = mock(MultichoiceAnswer.class);
		mockMoreInfoKey = mock(WikiPageKey.class);		
		
		questionContainerWidget = new QuestionContainerWidget(mockView);
		
		when(mockAnswerOne.getPrompt()).thenReturn("Han");
		when(mockAnswerTwo.getPrompt()).thenReturn("Luke");
		ArrayList<MultichoiceAnswer> answers = new ArrayList<MultichoiceAnswer>();
		answers.add(mockAnswerOne);
		answers.add(mockAnswerTwo);		
		
		when(mockMultichoiceQuestion.getPrompt()).thenReturn("What's your name?");
		when(mockMultichoiceQuestion.getQuestionIndex()).thenReturn(4L);
		//when((MultichoiceQuestion)mockMultichoiceQuestion).thenReturn(mockMultichoiceQuestion);
		when(mockMultichoiceQuestion.getAnswers()).thenReturn(answers);
		when(mockMultichoiceQuestion.getReference()).thenReturn(mockMoreInfoKey);
		when(mockMoreInfoKey.getOwnerObjectId()).thenReturn("Chewie");
		when(mockMoreInfoKey.getOwnerObjectType()).thenReturn(ObjectType.FILE);
		when(mockMoreInfoKey.getWikiPageId()).thenReturn("123");
	}	
	
	private void setExclusive(boolean isExclusive) {
		when(mockMultichoiceQuestion.getExclusive()).thenReturn(isExclusive);
	}
	
	@Test
	public void testConfigureRadioButtons() {
		setExclusive(true);
		questionContainerWidget.configure(1L, mockMultichoiceQuestion, null);
		verify(mockView, times(2)).addRadioButton(eq(mockMultichoiceQuestion.getQuestionIndex()), anyString(), any(ClickHandler.class), eq(false));
		verify(mockView).configureMoreInfo(mockMoreInfoKey.getOwnerObjectId(),
				mockMoreInfoKey.getOwnerObjectType().name(), mockMoreInfoKey.getWikiPageId());
	}
	
	@Test
	public void testConfigureCheckBoxes() {
		setExclusive(false);
		questionContainerWidget.configure(1L, mockMultichoiceQuestion, null);
		verify(mockView, times(2)).addCheckBox(eq(mockMultichoiceQuestion.getQuestionIndex()), anyString(), any(ClickHandler.class), eq(false));
		verify(mockView).configureMoreInfo(mockMoreInfoKey.getOwnerObjectId(),
		mockMoreInfoKey.getOwnerObjectType().name(), mockMoreInfoKey.getWikiPageId());
	}
	
	
}
