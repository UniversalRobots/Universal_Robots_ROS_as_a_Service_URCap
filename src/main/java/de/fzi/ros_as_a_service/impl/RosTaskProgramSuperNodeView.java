// -- BEGIN LICENSE BLOCK ----------------------------------------------
// Copyright 2021 FZI Forschungszentrum Informatik
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// -- END LICENSE BLOCK ------------------------------------------------

//----------------------------------------------------------------------
/*!\file
 *
 * \author  Carsten Plasberg plasberg@fzi.de
 * \date    2021-01-28
 *
 */
//----------------------------------------------------------------------
package de.fzi.ros_as_a_service.impl;
import com.ur.urcap.api.contribution.ViewAPIProvider;
import de.fzi.ros_as_a_service.impl.RosTaskProgramSuperNodeContribution.TaskType;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;

public abstract class RosTaskProgramSuperNodeView {
  protected final ViewAPIProvider apiProvider;
  protected final TaskType task;

  public RosTaskProgramSuperNodeView(ViewAPIProvider apiProvider, TaskType task) {
    this.apiProvider = apiProvider;
    this.task = task;
  }

  protected JComboBox<String> masterComboBox = new JComboBox<String>();
  protected JComboBox<String> topicComboBox = new JComboBox<String>();
  protected JPanel msg_panel = new JPanel();

  public void addTreePanel(JTree tree, JPanel panel) {
    JScrollPane treeView = new JScrollPane(tree);
    panel.add(treeView);
  }

  public void addLabel(String text, int n, JPanel panel) {
    JLabel label = new JLabel(text);
    label.setFont(bold(n));
    panel.add(label);
  }

  public void cleanPanel() {
    msg_panel.removeAll();
  }
  public JPanel createMsgPanel() {
    return createMsgPanel("Data");
  }
  public JPanel createMsgPanel(final String titel) {
    // msg_panel.removeAll();
    msg_panel.setLayout(new BoxLayout(msg_panel, BoxLayout.Y_AXIS));
    // TODO make Label Task specific
    addLabel(titel, 20, msg_panel);
    return msg_panel;
  }

  public void addMsgField(String field, String type, JPanel panel) {
    panel.add(createTextfieldBox(field, type, 300, ""));
  }

  public Box createTextfieldBox(String field, String type, int width, String text) {
    Box box = Box.createHorizontalBox();
    box.setAlignmentX(Component.LEFT_ALIGNMENT);
    JTextField textfield = createTextfield(width, text);
    JLabel field_label = new JLabel(field + " (" + type + ")");
    field_label.setLabelFor(textfield);
    box.add(field_label);
    box.add(createHorSpacer(10));
    box.add(textfield);
    return box;
  }
  public JTextField createTextfield(int width, String text) {
    JTextField textfield = new JTextField(text);
    textfield.setPreferredSize(new Dimension(width, 30));
    textfield.setMaximumSize(textfield.getPreferredSize());
    return textfield;
  }

  public void setMasterComboBoxItems(String[] items) {
    masterComboBox.removeAllItems();
    masterComboBox.setModel(new DefaultComboBoxModel<String>(items));
  }

  public String getMasterComboBoxSelectedItem() {
    return (String) masterComboBox.getSelectedItem();
  }

  public void setTopicComboBoxItems(String[] items) {
    topicComboBox.removeAllItems();
    topicComboBox.setModel(new DefaultComboBoxModel<String>(items));
  }

  public String getTopicComboBoxSelectedItem() {
    return (String) topicComboBox.getSelectedItem();
  }

  public void setMasterComboBoxSelection(String item) {
    masterComboBox.setSelectedItem(item);
  }

  public void setTopicComboBoxSelection(String item) {
    topicComboBox.setSelectedItem(item);
  }

  protected Box createDescription(String desc) {
    Box box = Box.createHorizontalBox();
    box.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel label = new JLabel(desc);
    box.add(label);

    return box;
  }

  protected Font bold(int n) {
    return new Font("Serif", Font.BOLD, n);
  }

  protected String getDefault(String type) {
    if (type.equals("float64")) {
      return "0.0";
    } else if (type.equals("int32") | type.equals("int64") | type.equals("uint32")
        | type.equals("uint64")) {
      return "0";
    } else {
      return "";
    }
  }

  protected Component createHorSpacer(int width) {
    return Box.createRigidArea(new Dimension(width, 0));
  }

  protected Component createVertSeparator(int height) {
    return Box.createRigidArea(new Dimension(0, height));
  }
}
