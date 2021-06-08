// -- BEGIN LICENSE BLOCK ----------------------------------------------
// Copyright 2021 FZI Forschungszentrum Informatik
// Created on behalf of Universal Robots A/S
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
* \author  Felix Exner <exner@fzi.de>
* \date    2021-04-26
*
*/
//----------------------------------------------------------------------
package de.fzi.ros_as_a_service.impl;

import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class MasterPairCellEditorString extends AbstractCellEditor implements TableCellEditor {
  final private KeyboardTextInput keyboardInput;
  JTextField textField;

  public MasterPairCellEditorString(KeyboardTextInput keyboard) {
    this.keyboardInput = keyboard;
    this.textField = new JTextField();

    this.textField.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        keyboardInput.setInitialValue(textField.getText());
        keyboardInput.show(textField, new KeyboardInputCallback<String>() {
          @Override
          public void onOk(String value) {
            textField.setText(value);
            stopCellEditing();
          }
          @Override
          public void onCancel() {
            stopCellEditing();
          }
        });
      }
    });
  }

  @Override
  public Object getCellEditorValue() {
    return textField.getText();
  }

  @Override
  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column) {
    textField.setText(value.toString());
    return textField;
  }
}
