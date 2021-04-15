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

import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputFactory;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;
import com.ur.urcap.api.domain.variable.Variable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.EventObject;
import java.util.Iterator;
import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
class ValueNodeEditor extends AbstractCellEditor implements TreeCellEditor {
  private LeafDataDirection datadirection;
  private DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
  private JTree tree;
  private JPanel p = new JPanel();
  private JLabel label = new JLabel();
  private JTextField textfield = null;
  private JCheckBox variableCheckbox = null;
  private JComboBox<String> variableCombobox = new JComboBox<String>();
  private final KeyboardInputFactory keyboardFactory;

  public ValueNodeEditor(JTree tree, Collection<Variable> variableCollection,
      LeafDataDirection direction, KeyboardInputFactory keyboard_factory) {
    this.tree = tree;
    this.datadirection = direction;
    this.keyboardFactory = keyboard_factory;

    Iterator<Variable> variableIterator = variableCollection.iterator();
    textfield = new JTextField();
    textfield.setPreferredSize(new Dimension(150, 30));
    textfield.setMaximumSize(textfield.getPreferredSize());
    label.setLabelFor(textfield);
    variableCheckbox = new JCheckBox();
    variableCheckbox.setText("Use variable");
    variableCombobox.setPreferredSize(new Dimension(100, 30));
    variableCombobox.addItem("");
    while (variableIterator.hasNext()) {
      variableCombobox.addItem(variableIterator.next().getDisplayName());
    }

    textfield.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        stopCellEditing();
      }
    });
    textfield.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (!textfield.isEnabled()) {
          // Editing can be disabled when use_var is checked. This doesn't disable the mouse events,
          // though.
          return;
        }
        KeyboardTextInput keyboardInput = keyboardFactory.createStringKeyboardInput();
        keyboardInput.setInitialValue(textfield.getText());
        keyboardInput.show(textfield, new KeyboardInputCallback<String>() {
          @Override
          public void onOk(String value) {
            textfield.setText(value);
            stopCellEditing();
          }
        });
      }
    });
    variableCombobox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (textfield != null) {
          textfield.setText(variableCombobox.getSelectedItem().toString());
        }
        stopCellEditing();
      }
    });
  }

  @Override
  public Object getCellEditorValue() {
    String info = label.getText(); // format: "description (type)"
    String[] descr = info.split("[\\s\\(\\)]+");
    if (datadirection == LeafDataDirection.INPUT) {
      return new ValueInputNode(descr[0], descr[1], variableCombobox.getSelectedItem().toString());
    }
    // otherwise it's an Output...
    return new ValueOutputNode(
        descr[0], descr[1], variableCheckbox.isSelected(), textfield.getText());
  }

  @Override
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
    System.out.println("--- getTreeCellEditorComponent: " + value);
    JLabel l = (JLabel) renderer.getTreeCellRendererComponent(
        tree, value, true, expanded, leaf, row, true);

    if (value instanceof DefaultMutableTreeNode) {
      Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
      if (userObject instanceof ValueInputNode) {
        final ValueInputNode node = (ValueInputNode) userObject;
        System.out.println(node.getLabelText() + " is an InputNode");
        l.setText(node.getLabelText());
        label.setText(node.getLabelText());
        p.add(label);
      }
      if (userObject instanceof ValueOutputNode) {
        final ValueOutputNode node = (ValueOutputNode) userObject;
        System.out.println(node.getLabelText() + " is an OutputNode");
        l.setText(node.getLabelText());
        textfield.setText(node.getValue());
        label.setText(node.getLabelText());
        boolean var_used = node.getUseVariable();
        variableCheckbox.setSelected(var_used);
        variableCombobox.setVisible(var_used);
        System.out.println("Node: " + node);
        if (var_used) {
          variableCombobox.setSelectedItem(node.getValue());
        }
        variableCheckbox.addItemListener(new ItemListener() {
          @Override
          public void itemStateChanged(ItemEvent e) {
            variableCombobox.setVisible(e.getStateChange() == ItemEvent.SELECTED);
            textfield.setEnabled(e.getStateChange() != ItemEvent.SELECTED);
            if (e.getStateChange() == ItemEvent.SELECTED) {
              textfield.setText(variableCombobox.getSelectedItem().toString());
            } else {
              textfield.setText(node.getDefaultValue());
            }
            stopCellEditing();
          }
        });
        p.add(label);
        p.add(textfield);
        p.add(variableCheckbox);
      }
      p.add(variableCombobox);
      return p;
    }
    return l;
  }
}
