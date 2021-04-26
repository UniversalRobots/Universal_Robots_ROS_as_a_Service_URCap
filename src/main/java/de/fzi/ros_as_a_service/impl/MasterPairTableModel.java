package de.fzi.ros_as_a_service.impl;

import javax.swing.table.AbstractTableModel;

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
        value = "define name";
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
        //            data[rowIndex].setName(aValue);
        break;
      case 1:
        data[rowIndex].set((String) aValue, data[rowIndex].getPort());
        break;
      case 2:
        data[rowIndex].set(data[rowIndex].getIp(), (String) aValue);
        break;
    }
  }

  @Override
  public boolean isCellEditable(int row, int col) {
    return true;
  }

  public MasterPair[] getData() {
    return data;
  }
}