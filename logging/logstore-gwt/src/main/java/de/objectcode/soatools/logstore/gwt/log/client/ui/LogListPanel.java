package de.objectcode.soatools.logstore.gwt.log.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListView;
import com.google.gwt.view.client.ListView.Delegate;
import com.google.gwt.view.client.SelectionModel.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel.SelectionChangeHandler;
import com.google.gwt.view.client.SingleSelectionModel;

import de.objectcode.soatools.logstore.gwt.log.client.service.LogMessageFilter;
import de.objectcode.soatools.logstore.gwt.log.client.service.LogMessageService;
import de.objectcode.soatools.logstore.gwt.log.client.service.LogMessageServiceAsync;
import de.objectcode.soatools.logstore.gwt.log.client.service.LogMessageSummary;

public class LogListPanel extends Composite {
	private final LogMessageServiceAsync logMessageService = GWT
			.create(LogMessageService.class);

	private static UI uiBinder = GWT.create(UI.class);

	interface UI extends UiBinder<Widget, LogListPanel> {
	}
	
	@UiField
	CellTable<LogMessageSummary> logMessageTable;
	
	@UiField
	LogFilterPanel logFilterPanel;
	
	public LogListPanel() {
		initWidget(uiBinder.createAndBindUi(this));
		
		logMessageTable.setPageSize(20);

		setColumns(logMessageTable);
		setSelectionModel(logMessageTable);
		logMessageTable.setDelegate(new Delegate<LogMessageSummary>() {
			@Override
			public void onRangeChanged(ListView<LogMessageSummary> view) {
				loadData(view.getRange().getStart(), view.getRange()
						.getLength(), view);
			}
		});

		logMessageTable.refresh();
	}
	
	@UiHandler("logFilterPanel")
	public void handleFilterChange(ValueChangeEvent<LogMessageFilter> event) {
		logMessageTable.refresh();
	}

	private void setColumns(CellTable<LogMessageSummary> logMessageTable) {
		final DateTimeFormat timestampFormat = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
		
		logMessageTable.addColumn(new TextColumn<LogMessageSummary>() {
			@Override
			public String getValue(LogMessageSummary logMessage) {
				return String.valueOf(logMessage.getId());
			}
		}, new TextHeader("Id"));
		logMessageTable.addColumn(new TextColumn<LogMessageSummary>() {
			@Override
			public String getValue(LogMessageSummary logMessage) {
				return logMessage.getServiceCategory() + " : "
						+ logMessage.getServiceName();
			}
		}, new TextHeader("Service"));
		logMessageTable.addColumn(new TextColumn<LogMessageSummary>() {
			@Override
			public String getValue(LogMessageSummary logMessage) {
				return timestampFormat.format(logMessage.getLogEnterTimestamp());
			}			
		}, new TextHeader("Enter"));
		logMessageTable.addColumn(new TextColumn<LogMessageSummary>() {
			@Override
			public String getValue(LogMessageSummary logMessage) {
				return timestampFormat.format(logMessage.getLogLeaveTimestamp());
			}			
		}, new TextHeader("Leave"));
	}

	private void setSelectionModel(CellTable<LogMessageSummary> logMessageTable) {
		final SingleSelectionModel<LogMessageSummary> selectionModel = new SingleSelectionModel<LogMessageSummary>();
		SelectionChangeHandler selectionHandler = new SelectionChangeHandler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				LogMessageSummary logMessage = selectionModel
						.getSelectedObject();
				Window.alert(String.valueOf(logMessage.getId()));
			}
		};
		selectionModel.addSelectionChangeHandler(selectionHandler);
		logMessageTable.setSelectionEnabled(true);
		logMessageTable.setSelectionModel(selectionModel);
	}

	private void loadData(int pageStart, int pageSize,
			final ListView<LogMessageSummary> logMessageTable) {

		logMessageService.getLogMessages(logFilterPanel.getValue(), pageStart, pageSize,
				new AsyncCallback<LogMessageSummary.Page>() {
					@Override
					public void onSuccess(LogMessageSummary.Page logMessagePage) {
						logMessageTable.setDataSize(
								logMessagePage.getTotalNumber(), true);
						logMessageTable.setData(logMessagePage.getPageStart(),
								logMessagePage.getPageSize(),
								logMessagePage.getPageData());
					}

					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Server error: " + caught);
					}
				});
	}

}
