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
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

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

    panel.setLayout(new BorderLayout());
    panel.add(mastersPanel, BorderLayout.CENTER);
    panel.add(createAddButton(contribution), BorderLayout.PAGE_END);

    Box button_box = Box.createHorizontalBox();
    button_box.add(createAddButton(contribution));
    button_box.add(createDeleteButton(contribution));
    panel.add(button_box, BorderLayout.PAGE_END);
  }

  private JPanel createMastersPanel(final RosbridgeInstallationNodeContribution contribution) {
    JPanel p = new JPanel();

    final MasterPairTableModel tableModel = new MasterPairTableModel(contribution.getMastersList());
    table = new JTable(tableModel);
    JScrollPane scrollPane = new JScrollPane(table);
    table.setFillsViewportHeight(false);
    table.setRowHeight(40);

    TableColumn nameColumn = table.getColumnModel().getColumn(0);
    nameColumn.setCellEditor(new MasterPairCellEditorString(keyboardFactory.createStringKeyboardInput()));
    TableColumn ipColumn = table.getColumnModel().getColumn(1);
    ipColumn.setCellEditor(
        new MasterPairCellEditorString(keyboardFactory.createIPAddressKeyboardInput()));
    TableColumn portColumn = table.getColumnModel().getColumn(2);
    portColumn.setCellEditor(new MasterPairCellEditorNum(keyboardFactory.createIntegerKeypadInput()));

    tableModel.addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent e) {
        contribution.setMasterList(tableModel.getData());
      }
    });
    p.add(scrollPane);
    return p;
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
