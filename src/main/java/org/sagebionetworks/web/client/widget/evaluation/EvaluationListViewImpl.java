package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.AvailableEvaluationQueueListProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class EvaluationListViewImpl implements EvaluationListView {

  SynapseReactClientFullContextPropsProvider propsProvider;
  ReactComponent reactContainer;
  Presenter presenter;

  @Inject
  public EvaluationListViewImpl(
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    this.propsProvider = propsProvider;
    reactContainer = new ReactComponent();
  }

  @Override
  public void configure(List<Evaluation> list, boolean isSelectable) {
    ReactNode element = React.createElementWithSynapseContext(
      SRC.SynapseComponents.AvailableEvaluationQueueList,
      AvailableEvaluationQueueListProps.create(
        list,
        isSelectable,
        evaluation -> {
          presenter.onChangeSelectedEvaluation(evaluation);
        }
      ),
      propsProvider.getJsInteropContextProps()
    );
    reactContainer.render(element);
  }

  @Override
  public Widget asWidget() {
    return reactContainer;
  }

  @Override
  public void showLoading() {
    // TODO Auto-generated method stub

  }

  @Override
  public void clear() {
    reactContainer.clear();
  }

  @Override
  public void showInfo(String message) {
    DisplayUtils.showInfo(message);
  }

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }
}
