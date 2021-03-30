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
  public enum ValueType { STRING, INTEGER, UINTEGER, FLOAT }
  ;

  protected ValueType type;
  protected String type_str;

  public ValueInputNode(String label, String type, String value) {
    this.label = label;
    this.type_str = type;
    this.type = getTypeFromString(type);
    this.value = value;
  }

  protected ValueType getTypeFromString(String type) {
    if (type.equals("string")) {
      return ValueType.STRING;
    }
    if (type.equals("int32") | type.equals("int64")) {
      return ValueType.INTEGER;
    }
    if (type.equals("uint32") | type.equals("uint64")) {
      return ValueType.UINTEGER;
    }
    if (type.equals("float64")) {
      return ValueType.FLOAT;
    }
    // default
    return ValueType.STRING;
  }

  public String getLabelText() {
    return label + " (" + type_str + ")";
  }
  public boolean getUseVariable() {
    return !value.equals("");
  }

  public String getJson() {
    return "{\"-+useVar+-\":\"" + value + "\"}";
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String toString() {
    return getClass().getName() + "[" + getLabelText() + " / " + value + "]";
  }
}

class ValueOutputNode extends ValueInputNode {
  protected boolean variable_used;

  ValueOutputNode(String label, String type, boolean use_variable, String value) {
    super(label, type, value);
    this.variable_used = use_variable;
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

  public LoadValueNode(LoadValueNode parentNode, ValueNode value) {
    this.parent = parentNode;
    this.value = value;
    this.use_variable = false;
    this.children = new ArrayList<LoadValueNode>();
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

  public List<String> getParentPath() {
    List<String> keys = null;
    if (parent == null) {
      keys = new ArrayList<String>();
    } else {
      keys = parent.getParentPath();
    }
    keys.add(value.getLabel());
    return keys;
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
