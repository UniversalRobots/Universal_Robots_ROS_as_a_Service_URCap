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

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
class MasterPairTableModel extends AbstractTableModel {
  private String[] columnNames = {"", "Name", "IP Address", "Port"};
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
        value = "#" + row;
        break;
      case 1:
        value = mp.getName();
        break;
      case 2:
        value = mp.getIp();
        break;
      case 3:
        value = mp.getPort();
        break;
    }
    return value;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 1:
        data[rowIndex].setName((String) aValue);
        break;
      case 2:
        data[rowIndex].setIp((String) aValue);
        break;
      case 3:
        data[rowIndex].setPort((String) aValue);
        break;
    }
    fireTableDataChanged();
  }

  @Override
  public boolean isCellEditable(int row, int col) {
    return col > 0;
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
    if (row > 0) {
      MasterPair[] new_data = new MasterPair[data.length - 1];
      for (int i = 0, j = 0; i < data.length; i++) {
        if (i != row) {
          new_data[j++] = data[i];
        }
      }
      data = new_data;
      //    fireTableDataChanged();
      fireTableRowsDeleted(row, row);
    }
  }
}
