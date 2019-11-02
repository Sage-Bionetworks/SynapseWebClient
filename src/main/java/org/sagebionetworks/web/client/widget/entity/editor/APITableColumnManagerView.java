package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.CheckBoxState;
import com.google.gwt.user.client.ui.IsWidget;

public interface APITableColumnManagerView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void configure(List<APITableColumnConfig> configs);

		void addColumnConfig();

		void onMoveDown();

		void onMoveUp();

		void deleteSelected();

		void selectNone();

		void selectAll();
	}

	void addColumn(IsWidget widget);

	void clearColumns();

	void setHeaderColumnsVisible(boolean visible);

	void setNoColumnsUIVisible(boolean visible);

	// selection toolbar state
	void setCanDelete(boolean canDelete);

	void setCanMoveUp(boolean canMoveUp);

	void setCanMoveDown(boolean canMoveDown);

	void setButtonToolbarVisible(boolean visible);

	void setSelectionState(CheckBoxState selectionState);
}
