//-- BEGIN LICENSE BLOCK ----------------------------------------------
// Copyright 2021 FZI Forschungszentrum Informatik
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//-- END LICENSE BLOCK ------------------------------------------------

//----------------------------------------------------------------------
/*!\file
 *
 * \author  Lea Steffen steffen@fzi.de
 * \date    2020-12-09
 *
 */
//----------------------------------------------------------------------

package de.fzi.ros_as_a_service.impl;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeView;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputFactory;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;

public class RosbridgeInstallationNodeView
    implements SwingInstallationNodeView<RosbridgeInstallationNodeContribution> {
  private JPanel mastersPanel;
  private final KeyboardInputFactory keyboardFactory;
  private JTable table;

  public RosbridgeInstallationNodeView(ViewAPIProvider apiProvider) {
    this.keyboardFactory =
        apiProvider.getUserInterfaceAPI().getUserInteraction().getKeyboardInputFactory();
  }

  @Override
  public void buildUI(JPanel panel, RosbridgeInstallationNodeContribution contribution) {
    mastersPanel = createMastersPanel(contribution);

    //    panel.setLayout(new BorderLayout());
    //    panel.add(mastersPanel, BorderLayout.CENTER);
    //    panel.add(createAddButton(contribution), BorderLayout.PAGE_END);
    panel.add(mastersPanel);

    panel.add(new JSeparator());

    panel.add(createAddButton(contribution));
    panel.add(createDeleteButton(contribution));
  }

  private JPanel createMastersPanel(final RosbridgeInstallationNodeContribution contribution) {
    JPanel p = new JPanel();

    final MasterPairTableModel tableModel = new MasterPairTableModel(contribution.getMastersList());
    table = new JTable(tableModel);
    JScrollPane scrollPane = new JScrollPane(table);
    table.setFillsViewportHeight(false);
    table.setRowHeight(40);

    TableColumn nameColumn = table.getColumnModel().getColumn(0);
    nameColumn.setCellEditor(new MasterPairCellEditor(keyboardFactory.createStringKeyboardInput()));
    TableColumn ipColumn = table.getColumnModel().getColumn(1);
    ipColumn.setCellEditor(
        new MasterPairCellEditor(keyboardFactory.createIPAddressKeyboardInput()));
    TableColumn portColumn = table.getColumnModel().getColumn(2);
    portColumn.setCellEditor(new MasterPairCellEditor(keyboardFactory.createStringKeyboardInput()));

    tableModel.addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent e) {
        contribution.setMasterList(tableModel.getData());
      }
    });
    p.add(scrollPane);
    return p;
  }

  private void addNewMaster(final RosbridgeInstallationNodeContribution contribution,
      final JPanel panel, final MasterPair master) {
    panel.add(new JLabel("Name"));
    panel.add(createIPBox(contribution, master.getIp()));
    panel.add(createPortBox(contribution, master.getPort()));
  }

  private JTextField createIPBox(
      final RosbridgeInstallationNodeContribution contribution, final String default_value) {
    JTextField textFieldIP = new JTextField();
    textFieldIP.setText(default_value);
    textFieldIP.setFocusable(false);
    return textFieldIP;
  }

  private JTextField createPortBox(
      final RosbridgeInstallationNodeContribution contribution, final String default_value) {
    JTextField textFieldPort = new JTextField();
    return textFieldPort;
    //    Box box = Box.createVerticalBox();
    //    // create port Label
    //    JLabel label = new JLabel("Please setup the custom port: ");
    //    box.add(label);
    //    // create port Textfield
    //    textFieldPort = new JTextField(15);
    //    textFieldPort.setText(contribution.getCustomPort());
    //    textFieldPort.setFocusable(false);
    //    textFieldPort.addMouseListener(new MouseAdapter() {
    //      @Override
    //      public void mousePressed(MouseEvent e) {
    //        // TODO: Use Integer keyboard here
    //        KeyboardTextInput keyboardInput = contribution.getInputForPortTextField();
    //        keyboardInput.show(textFieldPort, contribution.getCallbackForPortTextField());
    //      }
    //    });
    //    box.add(textFieldPort);
    //    return box;
  }

  private Component createSpacer(int height) {
    return Box.createRigidArea(new Dimension(0, height));
  }

  private JButton createAddButton(final RosbridgeInstallationNodeContribution contribution) {
    JButton button = new JButton("Add new rosbridge remote");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MasterPair new_master = new MasterPair();
        MasterPairTableModel table_model = (MasterPairTableModel) table.getModel();
        table_model.addRow(new_master);
      }
    });

    return button;
  }

  private JButton createDeleteButton(final RosbridgeInstallationNodeContribution contribution) {
    JButton button = new JButton("Delete selected remote");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MasterPairTableModel table_model = (MasterPairTableModel) table.getModel();
        int row = table.getSelectedRow();
        table_model.removeRow(row);
      }
    });

    return button;
  }
}
