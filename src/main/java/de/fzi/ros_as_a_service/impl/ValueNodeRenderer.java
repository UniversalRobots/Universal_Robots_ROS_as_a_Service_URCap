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
import java.awt.Dimension;
import java.awt.Font;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

enum LeafDataDirection { INPUT, OUTPUT }

class ValueNodeRenderer implements TreeCellRenderer {
  private JPanel leafRender = new JPanel();
  private JLabel label = new JLabel();
  private JTextField textfield = null;
  private JCheckBox variableCheckbox = null;
  private JComboBox<String> variableCombobox = new JComboBox<String>();
  private Collection<Variable> variableCollection = null;
  private DefaultTreeCellRenderer nonLeafRender = new DefaultTreeCellRenderer();
  private LeafDataDirection datadirection;

  public ValueNodeRenderer(Collection<Variable> varCollection, LeafDataDirection direction) {
    this.variableCollection = varCollection;
    Iterator<Variable> variableIterator = variableCollection.iterator();
    this.datadirection = direction;

    switch (datadirection) {
      case INPUT:
        label.setLabelFor(variableCombobox);
        variableCombobox.setPreferredSize(new Dimension(100, 30));
        variableCombobox.addItem("");
        while (variableIterator.hasNext()) {
          variableCombobox.addItem(variableIterator.next().getDisplayName());
        }
        leafRender.add(label);
        leafRender.add(variableCombobox);
        break;
      case OUTPUT:
      default:
        textfield = new JTextField();
        textfield.setPreferredSize(new Dimension(150, 30));
        textfield.setMaximumSize(textfield.getPreferredSize());
        label.setLabelFor(textfield);
        variableCheckbox = new JCheckBox();
        variableCheckbox.setText("Use variable");
        variableCombobox.setPreferredSize(new Dimension(100, 30));
        variableCombobox.setVisible(variableCheckbox.isSelected());
        variableCombobox.addItem("");
        while (variableIterator.hasNext()) {
          variableCombobox.addItem(variableIterator.next().getDisplayName());
        }
        leafRender.add(label);
        leafRender.add(textfield);
        leafRender.add(variableCheckbox);
        leafRender.add(variableCombobox);
    }
    Font fontValue;
    fontValue = UIManager.getFont("Tree.font");
    if (fontValue != null) {
      leafRender.setFont(fontValue);
    }
  }

  protected int getComboIndex(JComboBox<String> box, String descr) {
    if (box == null || descr == null) {
      return 0;
    }
    String item;
    for (int cnt = 0; cnt < box.getItemCount(); ++cnt) {
      item = (String) box.getItemAt(cnt);
      System.out.println("Combo Content: " + item.toString());
      if (item.equals(descr)) {
        return cnt;
      }
    }
    return 0;
  }

  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
      boolean expanded, boolean leaf, int row, boolean hasFocus) {
    if (leaf) {
      leafRender.setEnabled(tree.isEnabled());
      if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
        Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
        if (userObject instanceof ValueOutputNode) {
          ValueOutputNode node = (ValueOutputNode) userObject;
          textfield.setText(node.getValue());
          label.setText(node.getLabelText());
          boolean var_used = node.getUseVariable();
          variableCheckbox.setSelected(var_used);
          if (var_used) {
            variableCombobox.setSelectedItem(node.getValue());
          }
        } else if (userObject instanceof ValueInputNode) {
          ValueInputNode node = (ValueInputNode) userObject;
          label.setText(node.getLabelText());
          System.out.println("try to select item " + node.getValue());
          variableCombobox.setSelectedIndex(getComboIndex(variableCombobox, node.getValue()));
        }
      }
      return leafRender;
    }
    return nonLeafRender.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, hasFocus);
  }

  public JPanel getLeafRender() {
    return leafRender;
  }
  public JLabel getLabel() {
    return label;
  }
  public JTextField getTextField() {
    return textfield;
  }
  public JCheckBox getCheckbox() {
    return variableCheckbox;
  }
  public JComboBox<String> getComboBox() {
    return variableCombobox;
  }
}
