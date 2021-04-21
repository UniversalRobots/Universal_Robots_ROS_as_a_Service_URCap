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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

class ValueNode {
  protected String label;
  protected String value;

  public ValueNode() {
    label = new String();
    value = new String();
  }

  public ValueNode(String label, String value) {
    this.label = label;
    this.value = value;
  }

  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }
  public String getLabel() {
    return label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
}

class ValueInputNode extends ValueNode {
  public enum ValueType { UNKNOWN, STRING, INTEGER, UINTEGER, FLOAT }
  ;

  protected ValueType type;
  protected String type_str;
  protected boolean is_array;

  public ValueInputNode(String label, String type, String value, boolean is_array) {
    this.label = label;
    this.type_str = type;
    this.type = getTypeFromString(type);
    this.value = value;
    this.is_array = is_array;
  }

  static public ValueType getTypeFromString(String type_str) {
    switch (type_str) {
      case "string":
        return ValueType.STRING;
      case "int8":
      case "int16":
      case "int32":
      case "int64":
        return ValueType.INTEGER;
      case "uint8":
      case "uint16":
      case "uint32":
      case "uint64":
        return ValueType.UINTEGER;
      case "float32":
      case "float64":
        return ValueType.FLOAT;
      case "bool":
        return ValueType.UINTEGER;
      default:
        return ValueType.UNKNOWN;
    }
  }

  public String getLabelText() {
    String out_label = label + " (" + type_str + ")";
    if (is_array) {
      out_label = label + " (" + type_str + "[])";
    }
    return out_label;
  }
  public boolean getUseVariable() {
    return !value.trim().isEmpty();
  }

  public String getJson() {
    if (ValueInputNode.isNumericType(type)) {
      return "{\"-+useVarNum+-\":\"" + value + "\"}";
    }
    return "{\"-+useVar+-\":\"" + value + "\"}";
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String toString() {
    return getClass().getName() + "[" + getLabelText() + " / " + value + "]";
  }

  public boolean isNumericType() {
    return ValueInputNode.isNumericType(type);
  }

  static public boolean isNumericType(final ValueType type) {
    return (type.equals(ValueType.INTEGER) | type.equals(ValueType.UINTEGER)
        | type.equals(ValueType.FLOAT));
  }

  public boolean isArrayType() {
    return is_array;
  }
}

class ValueOutputNode extends ValueInputNode {
  protected boolean variable_used;

  ValueOutputNode(String label, String type, boolean use_variable, String value, boolean is_array) {
    super(label, type, value, is_array);
    this.variable_used = use_variable;
  }

  public String getDefaultValue() {
    if (type.equals(ValueType.INTEGER) || type.equals(ValueType.UINTEGER)) {
      return "0";
    } else if (type.equals(ValueType.FLOAT)) {
      return "0.0";
    }
    // default for String
    return "default";
  }

  public void setVariableUsed(boolean use_variable) {
    this.variable_used = use_variable;
  }

  @Override
  public String getJson() {
    if (variable_used) {
      return super.getJson();
    }
    return getValue();
  }

  @Override
  public boolean getUseVariable() {
    return variable_used;
  }

  @Override
  public String toString() {
    return getClass().getName() + "[" + getLabelText() + " / " + getJson() + "]";
  }
}

class LoadValueNode {
  private LoadValueNode parent;
  private ValueNode value;
  private List<LoadValueNode> children;
  private boolean use_variable;
  private boolean extraction_added;
  private boolean is_numeric;

  public LoadValueNode(LoadValueNode parentNode, ValueNode value) {
    this.parent = parentNode;
    this.value = value;
    this.use_variable = false;
    this.children = new ArrayList<LoadValueNode>();
    this.extraction_added = false;
    this.is_numeric = false;
  }

  public String getValue() {
    return value.getValue();
  }
  public void setValue(String value) {
    this.value.setValue(value);
  }
  public boolean getVariableUsed() {
    return this.use_variable;
  }
  public void setVariableUsed(boolean use) {
    this.use_variable = use;
  }
  public String getName() {
    return this.value.getLabel();
  }
  public void setName(String name) {
    this.value.setLabel(name);
  }
  public void addChild(LoadValueNode node) {
    this.children.add(node);
  }
  public List<LoadValueNode> getChildren() {
    return this.children;
  }
  public boolean isExtractionAdded() {
    return this.extraction_added;
  }
  public void setExtractionAdded() {
    this.extraction_added = true;
  }
  public LoadValueNode getParent() {
    return this.parent;
  }
  public void setNumericType(boolean isNumeric) {
    this.is_numeric = isNumeric;
  }
  public boolean isNumericType() {
    return this.is_numeric;
  }

  public String getExtractionFunctionName() {
    String name = new String();
    if (parent != null) {
      name = parent.getExtractionFunctionName();
      name += "_";
    }
    name += getName();
    return name;
  }

  public List<LoadValueNode> getParentPath() {
    List<LoadValueNode> path = null;
    if (parent == null) {
      path = new ArrayList<LoadValueNode>();
    } else {
      path = parent.getParentPath();
    }
    path.add(this);
    return path;
  }

  public List<LoadValueNode> getVariableUsingNodes(List<LoadValueNode> list) {
    if (list == null) {
      list = new ArrayList<LoadValueNode>();
    }
    if (children.isEmpty()) {
      if (use_variable) {
        list.add(this);
      }
    } else {
      ListIterator<LoadValueNode> iterator = children.listIterator();
      while (iterator.hasNext()) {
        iterator.next().getVariableUsingNodes(list);
      }
    }
    return list;
  }

  public String toString() {
    String returnvalue = value.getLabel();
    if (children.isEmpty()) {
      returnvalue += "(" + value.getValue() + ")";
    } else {
      returnvalue += "[";
      ListIterator<LoadValueNode> iterator = children.listIterator();
      while (iterator.hasNext()) {
        returnvalue += iterator.next().toString();
      }
      returnvalue += "]";
    }
    return returnvalue;
  }
}

@SuppressWarnings("serial")
class TreeNodeVector<E> extends Vector<E> {
  protected String name;

  public TreeNodeVector(String name) {
    this.name = name;
  }

  public TreeNodeVector(String name, E elements[]) {
    this.name = name;
    for (int i = 0, n = elements.length; i < n; i++) {
      add(elements[i]);
    }
  }

  public void addElements(E elements[]) {
    for (int i = 0, n = elements.length; i < n; i++) {
      add(elements[i]);
    }
  }

  public String toString() {
    return "[" + name + "]";
  }
}
