package org.sagebionetworks.web.client.view;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import java.util.Set;
import org.sagebionetworks.repo.model.quiz.MultichoiceResponse;
import org.sagebionetworks.repo.model.quiz.Question;

public interface QuestionContainerWidgetView extends IsWidget {
  public Widget asWidget();

  public void addStyleName(String style);

  /**
   * Presenter interface
   */
  public interface Presenter {
    Set<Long> getAnswers();

    void addCorrectnessStyle(boolean isCorrect);

    Long getQuestionIndex();

    String getQuestionPrompt();

    void setEnabled(boolean enabled);

    void configure(
      Long questionNumber,
      Question question,
      MultichoiceResponse response
    );
  }

  void addAnswer(Widget answerContainer);

  void showSuccess(boolean isShown);

  void showFailure(boolean isShown);

  void addCheckBox(
    Long questionIndex,
    String questionPrompt,
    Long answerIndex,
    boolean isSelected
  );

  void addRadioButton(
    Long long1,
    String string,
    Long answerIndex,
    boolean isSelected
  );

  public void configure(Long questionNumber, String prompt);

  void setIsEnabled(boolean isEnabled);

  void configureMoreInfo(String helpUrl, String helpText);

  void setMoreInfoVisible(boolean isVisible);

  Set<Long> getAnswers();
}
