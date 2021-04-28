package de.fzi.ros_as_a_service.impl;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
class MasterPairTableModel extends AbstractTableModel {
  private String[] columnNames = {"Name", "IP Address", "Port"};
  private MasterPair[] data;

  public MasterPairTableModel(MasterPair[] data) {
    setData(data);
  }

  public void setData(MasterPair[] data) {
    this.data = data;
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public int getRowCount() {
    return data.length;
  }

  @Override
  public String getColumnName(int col) {
    return columnNames[col];
  }

  @Override
  public Object getValueAt(int row, int col) {
    MasterPair mp = data[row];
    Object value = null;
    switch (col) {
      case 0:
        value = mp.getName();
        break;
      case 1:
        value = mp.getIp();
        break;
      case 2:
        value = mp.getPort();
        break;
    }
    return value;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0:
        data[rowIndex].setName((String) aValue);
        break;
      case 1:
        data[rowIndex].setIp((String) aValue);
        break;
      case 2:
        data[rowIndex].setPort((String) aValue);
        break;
    }
    fireTableDataChanged();
  }

  @Override
  public boolean isCellEditable(int row, int col) {
    return true;
  }

  public void addRow(MasterPair row_data) {
    MasterPair[] new_data = new MasterPair[data.length + 1];
    System.arraycopy(data, 0, new_data, 0, data.length);
    new_data[new_data.length - 1] = row_data;
    data = new_data;
    fireTableDataChanged();
  }

  public MasterPair[] getData() {
    return data;
  }

  public void removeRow(int row) {
    MasterPair[] new_data = new MasterPair[data.length - 1];
    for (int i = 0, j = 0; i < data.length; i++) {
      if (i != row) {
        new_data[j++] = data[i];
      }
    }
    data = new_data;
    fireTableDataChanged();
  }
}
