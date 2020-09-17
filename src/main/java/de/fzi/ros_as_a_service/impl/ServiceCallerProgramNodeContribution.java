package de.fzi.ros_as_a_service.impl;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.undoredo.UndoRedoManager;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;

public class ServiceCallerProgramNodeContribution implements ProgramNodeContribution{

  private final ProgramAPIProvider apiProvider;
  private final ServiceCallerProgramNodeView view;
  private final DataModel model;
  private final UndoRedoManager undoRedoManager;

  private static final String MASTER_KEY = "master";
  private static final String DEFAULT_MASTER = "";
  private static final String TOPIC_KEY = "topic";
  private static final String DEFAULT_TOPIC = "";

  public ServiceCallerProgramNodeContribution(ProgramAPIProvider apiProvider, ServiceCallerProgramNodeView view, DataModel model) {
    this.apiProvider = apiProvider;
    this.view = view;
    this.model = model;
    this.undoRedoManager = this.apiProvider.getProgramAPI().getUndoRedoManager();
  }

  public void onMasterSelection(final String master) {
    undoRedoManager.recordChanges(new UndoableChanges() {

      @Override
      public void executeChanges() {
        model.set(MASTER_KEY, master);
      }
    });
  }

  public void onTopicSelection(final String master) {
    undoRedoManager.recordChanges(new UndoableChanges() {

      @Override
      public void executeChanges() {
        model.set(TOPIC_KEY, master);
      }
    });
  }

  private String getMaster() {
    return model.get(MASTER_KEY, DEFAULT_MASTER);
  }

  private String getTopic() {
    return model.get(TOPIC_KEY, DEFAULT_TOPIC);
  }

  private String[] getMasterList() {
    // TODO: Get this from installation
    String[] items = new String[1];
    items[0] = "192.168.56.1";
    return items;
  }

  private String[] getTopicsList() {
    // TODO: Get this dynamically
    String[] items = new String[2];
    items[0] = "";
    items[1] = "/test_service";
    return items;
  }

  @Override
  public void openView() {
    view.setMasterComboBoxItems(getMasterList());
    view.setTopicComboBoxItems(getTopicsList());
    // view.setMasterComboBoxSelection(getMaster());
  }

  @Override
  public void closeView() {
  }

  @Override
  public String getTitle() {
    // TODO: Put service name here
    return "Call " + getTopic();
  }

  @Override
  public boolean isDefined() {
    return getTopic() != "";
  }

  @Override
  public void generateScript(ScriptWriter writer) {
    writer.appendLine("sendToSocket('test')");
  }

}
