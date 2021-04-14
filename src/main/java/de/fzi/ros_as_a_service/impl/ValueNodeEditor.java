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
 * \date    2021-03-23
 *
 */
//----------------------------------------------------------------------

package de.fzi.ros_as_a_service.impl;

import com.ur.urcap.api.domain.variable.Variable;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
class ValueNodeEditor extends AbstractCellEditor implements TreeCellEditor {
  private Collection<Variable> variable_collection;
  private LeafDataDirection datadirection;
  private ValueNodeRenderer renderer;
  private JTree tree;

  public ValueNodeEditor(
      JTree tree, Collection<Variable> variableCollection, LeafDataDirection direction) {
    this.tree = tree;
    this.variable_collection = variableCollection;
    this.datadirection = direction;
    this.renderer = new ValueNodeRenderer(variable_collection, direction);
  }

  @Override
  public Object getCellEditorValue() {
    String info = renderer.getLabel().getText(); // format: "description (type)"
    String[] descr = info.split("[\\s\\(\\)]+");
    if (datadirection == LeafDataDirection.INPUT) {
      return new ValueInputNode(
          descr[0], descr[1], renderer.getComboBox().getSelectedItem().toString());
    }
    // otherwise it's an Output...
    return new ValueOutputNode(
        descr[0], descr[1], renderer.getCheckbox().isSelected(), renderer.getTextField().getText());
  }

  public boolean isCellEditable(EventObject event) {
    if (event instanceof MouseEvent) {
      MouseEvent mouseEvent = (MouseEvent) event;
      TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
      if (path != null) {
        Object node = path.getLastPathComponent();
        if ((node != null) && (node instanceof DefaultMutableTreeNode)) {
          DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
          Object userObject = treeNode.getUserObject();
          return ((treeNode.isLeaf())
              && ((userObject instanceof ValueInputNode)
                  || (userObject instanceof ValueOutputNode)));
        }
      }
    }
    return false;
  }

  @Override
  public Component getTreeCellEditorComponent(
      JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
    Component editor =
        renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
    if (editor instanceof JPanel) {
      final JTextField textfield = renderer.getTextField();
      final JCheckBox checkbox = renderer.getCheckbox();
      final JComboBox<String> combobox = renderer.getComboBox();

      if (textfield != null) {
        textfield.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            stopCellEditing();
          }
        });
      }
      if (combobox != null) {
        combobox.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (textfield != null) {
              textfield.setText(combobox.getSelectedItem().toString());
            }
            stopCellEditing();
          }
        });
      }
      if (checkbox != null) {
        checkbox.addItemListener(new ItemListener() {
          @Override
          public void itemStateChanged(ItemEvent e) {
            // combobox.setVisible(e.getStateChange() == ItemEvent.SELECTED);
            textfield.setEditable(e.getStateChange() != ItemEvent.SELECTED);
            stopCellEditing();
          }
        });
      }
    }
    return editor;
  }
}
