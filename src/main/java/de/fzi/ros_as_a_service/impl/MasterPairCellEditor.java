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

public class MasterPairCellEditor extends AbstractCellEditor implements TableCellEditor {
  final private KeyboardTextInput keyboardInput;
  JTextField textField;

  public MasterPairCellEditor(KeyboardTextInput keyboard) {
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
