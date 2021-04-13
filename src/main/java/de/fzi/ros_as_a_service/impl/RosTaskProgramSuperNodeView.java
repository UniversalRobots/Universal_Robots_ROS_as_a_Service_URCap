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
 * \date    2021-01-28
 *
 */
//----------------------------------------------------------------------
package de.fzi.ros_as_a_service.impl;
import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;
import com.ur.urcap.api.domain.variable.Variable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class RosTaskProgramSuperNodeView<C extends RosTaskProgramSuperNodeContribution>
    implements SwingProgramNodeView<C> {
  protected final ViewAPIProvider apiProvider;
  protected String description = "";
  Vector<JTree> trees;

  public RosTaskProgramSuperNodeView(ViewAPIProvider apiProvider) {
    this.apiProvider = apiProvider;
    this.trees = new Vector<JTree>();
  }

  protected JComboBox<String> masterComboBox = new JComboBox<String>();
  protected JComboBox<String> topicComboBox = new JComboBox<String>();
  protected JPanel msg_panel = new JPanel();

  @Override
  public void buildUI(JPanel panel, ContributionProvider<C> provider) {
    System.out.println("---buildUI SuperNode---");
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(createDescription(description));
    panel.add(createMasterComboBox(masterComboBox, provider));
    panel.add(createTopicComboBox(topicComboBox, provider));
    panel.add(createVertSeparator(50));

    panel.add(createMsgPanel());
    panel.add(createVertSeparator(20));
    System.out.println("---end buildUI SuperNode---");
  }

  public void updateView(C contribution) {
    setMasterComboBoxItems(contribution.getMastersList());
    setTopicComboBoxItems(contribution.queryMsgList());
    cleanPanel();

    setTopicComboBoxSelection(contribution.getMsg());
    setMasterComboBoxSelection(contribution.getMaster().toString());
  }

  private Box createMasterComboBox(
      final JComboBox<String> combo, final ContributionProvider<C> provider) {
    Box box = Box.createHorizontalBox();
    box.setAlignmentX(Component.LEFT_ALIGNMENT);
    JLabel label = new JLabel("Remote master");

    combo.setPreferredSize(new Dimension(200, 30));
    combo.setMaximumSize(combo.getPreferredSize());

    combo.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          provider.get().onMasterSelection((String) e.getItem());
          updateMsgList(provider);
        }
      }
    });

    box.add(label);
    box.add(createHorSpacer(10));
    box.add(combo);

    return box;
  }

  public void updateMsgList(final ContributionProvider<C> provider) {
    setTopicComboBoxItems(provider.get().queryMsgList());
  }

  private Box createTopicComboBox(
      final JComboBox<String> combo, final ContributionProvider<C> provider) {
    Box box = Box.createHorizontalBox();
    box.setAlignmentX(Component.LEFT_ALIGNMENT);
    JLabel label = new JLabel("Topic");

    combo.setPreferredSize(new Dimension(400, 30));
    combo.setMaximumSize(combo.getPreferredSize());

    combo.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          provider.get().onMsgSelection(getTopicComboBoxSelectedItem());
          JSONArray typedefs = provider.get().getTopicStructure();
          cleanPanel();
          createTreeView(typedefs, provider);
        }
      }
    });

    box.add(label);
    box.add(createHorSpacer(10));
    box.add(combo);

    return box;
  }

  protected void createTreeView(JSONArray structure, final ContributionProvider<C> provider) {
    if (structure == null) {
      return;
    };
    System.out.println("#createTreeView");
    for (int i = 0; i < structure.length(); i++) {
      JSONObject current = (JSONObject) structure.get(i);
      JSONArray layout = current.getJSONArray("layout");
      System.out.println("Layout: " + layout);
      final String name = current.getString("name");
      JPanel panel = createMsgPanel(name);
      final JSONObject values = current.getJSONObject("values");
      LeafDataDirection direction;
      switch (current.getString("direction")) {
        case "out":
          direction = LeafDataDirection.OUTPUT;
          break;
        case "in":
          direction = LeafDataDirection.INPUT;
          break;
        default:
          throw new IllegalArgumentException(
              "Unknown direction: " + current.getString("direction"));
      };
      JTree tree = createMsgTreeLayout(layout, direction, provider.get().getVarCollection());
      trees.add(tree);

      final TreeModel treeModel = tree.getModel();

      treeModel.addTreeModelListener(new TreeModelListener() {
        @Override
        public void treeStructureChanged(TreeModelEvent e) {
          // TODO Auto-generated method stub
        }

        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
          // TODO Auto-generated method stub
        }

        @Override
        public void treeNodesInserted(TreeModelEvent e) {
          // TODO Auto-generated method stub
        }

        @Override
        public void treeNodesChanged(TreeModelEvent e) {
          System.out.println("Tree node changed: " + e.toString());

          Object tmRoot = treeModel.getRoot();
          JSONObject root = getJsonLevel(treeModel, tmRoot, false);
          provider.get().updateModel(name, root);
        }
      });

      addTreePanel(tree, panel);
      LoadValueNode base_node = null;
      if (values == null || values.names() == null || values.names().isEmpty()) {
        Object tmRoot = treeModel.getRoot();
        JSONObject root = getJsonLevel(treeModel, tmRoot, false);
        base_node = loadValuesToTree(null, root, "msg_base");
      } else {
        base_node = loadValuesToTree(null, values, "msg_base");
      }
      try {
        System.out.println("detected values: " + base_node.toString());
        setTreeValues(base_node, tree);
      } catch (Exception e) {
        System.err.println("Error: " + e);
      }
    }
  }

  public void addTreePanel(JTree tree, JPanel panel) {
    JScrollPane treeView = new JScrollPane(tree);
    panel.add(treeView);
  }

  public void addLabel(String text, int n, JPanel panel) {
    JLabel label = new JLabel(text);
    label.setFont(bold(n));
    panel.add(label);
  }

  public void cleanPanel() {
    trees.clear();
    msg_panel.removeAll();
  }
  public JPanel createMsgPanel() {
    return createMsgPanel("Data");
  }
  public JPanel createMsgPanel(final String titel) {
    msg_panel.setLayout(new BoxLayout(msg_panel, BoxLayout.Y_AXIS));
    addLabel(titel, 20, msg_panel);
    return msg_panel;
  }

  public void addMsgField(String field, String type, JPanel panel) {
    panel.add(createTextfieldBox(field, type, 300, ""));
  }

  public Box createTextfieldBox(String field, String type, int width, String text) {
    Box box = Box.createHorizontalBox();
    box.setAlignmentX(Component.LEFT_ALIGNMENT);
    JTextField textfield = createTextfield(width, text);
    JLabel field_label = new JLabel(field + " (" + type + ")");
    field_label.setLabelFor(textfield);
    box.add(field_label);
    box.add(createHorSpacer(10));
    box.add(textfield);
    return box;
  }
  public JTextField createTextfield(int width, String text) {
    JTextField textfield = new JTextField(text);
    textfield.setPreferredSize(new Dimension(width, 30));
    textfield.setMaximumSize(textfield.getPreferredSize());
    return textfield;
  }

  public void setMasterComboBoxItems(String[] items) {
    masterComboBox.removeAllItems();
    masterComboBox.setModel(new DefaultComboBoxModel<String>(items));
  }

  public String getMasterComboBoxSelectedItem() {
    return (String) masterComboBox.getSelectedItem();
  }

  public void setTopicComboBoxItems(String[] items) {
    topicComboBox.removeAllItems();
    topicComboBox.setModel(new DefaultComboBoxModel<String>(items));
  }

  public String getTopicComboBoxSelectedItem() {
    return (String) topicComboBox.getSelectedItem();
  }

  public void setMasterComboBoxSelection(String item) {
    masterComboBox.setSelectedItem(item);
  }

  public void setTopicComboBoxSelection(String item) {
    topicComboBox.setSelectedItem(item);
  }

  protected Box createDescription(String desc) {
    Box box = Box.createHorizontalBox();
    box.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel label = new JLabel(desc);
    box.add(label);

    return box;
  }

  protected Font bold(int n) {
    return new Font("Serif", Font.BOLD, n);
  }

  protected String getDefault(String type) {
    if (type.equals("float64")) {
      return "0.0";
    } else if (type.equals("int32") | type.equals("int64") | type.equals("uint32")
        | type.equals("uint64")) {
      return "0";
    } else {
      return "";
    }
  }

  protected Component createHorSpacer(int width) {
    return Box.createRigidArea(new Dimension(width, 0));
  }

  protected Component createVertSeparator(int height) {
    return Box.createRigidArea(new Dimension(0, height));
  }

  public JTree createMsgTreeLayout(
      JSONArray msg_layout, LeafDataDirection direction, Collection<Variable> varCollection) {
    System.out.println("### createMsgTreeLayout");
    JTree tree = null;
    try {
      Objects.requireNonNull(msg_layout, "msg layout undefined");
      JSONObject obj = (JSONObject) msg_layout.get(0);
      TreeNodeVector<Object> rootVector = getRoot(msg_layout, obj, direction);
      tree = new JTree(rootVector);
      ValueNodeRenderer renderer = new ValueNodeRenderer(varCollection, direction);
      tree.setCellRenderer(renderer);
      // tree.setCellEditor(new TextFieldNodeEditor(tree, renderer));
      tree.setCellEditor(new ValueNodeEditor(tree, varCollection, direction));
      tree.setEditable(true);
    } catch (org.json.JSONException e) {
      System.err.println("createMsgTreeLayout: JSON-Error: " + e);
    } catch (Exception e) {
      System.err.println("createMsgTreeLayout: Error: " + e);
    }
    return tree;
  }

  public TreeNodeVector<Object> getRoot(
      JSONArray typedefs, JSONObject obj, LeafDataDirection direction) {
    JSONObject obj_j = (JSONObject) typedefs.get(0);
    String type = obj_j.get("type").toString();
    TreeNodeVector<Object> root = new TreeNodeVector<Object>(type);
    getNextLevel(typedefs, obj, type, root, direction);
    return root;
  }

  public void getNextLevel(JSONArray typedefs, JSONObject obj, String supertype,
      TreeNodeVector<Object> parentVector, LeafDataDirection direction) {
    JSONArray fieldtypes = obj.getJSONArray("fieldtypes");
    JSONArray fieldnames = obj.getJSONArray("fieldnames");

    for (int j = 0; j < typedefs.length(); j++) {
      Map<String, String> subTypes = new HashMap<String, String>();
      JSONObject obj_j = (JSONObject) typedefs.get(j);
      String type = obj_j.get("type").toString();

      for (int i = 0; i < fieldtypes.length(); i++) {
        String ft = fieldtypes.get(i).toString();
        String fn = fieldnames.get(i).toString();
        TreeNodeVector<Object> branchVector = new TreeNodeVector<Object>(fn + " (" + ft + ")");
        String[] superSplit = supertype.split("-");
        if (!isSimpleType(ft)) {
          if (ft.equals(type)) {
            parentVector.addElement(branchVector);
            if (!supertype.equals("")) {
              ft = supertype + "-" + ft;
            }
            getNextLevel(typedefs, obj_j, ft, branchVector, direction);
          }
        } else if (type.equals(superSplit[superSplit.length - 1])) {
          subTypes.put(fieldnames.getString(i), fieldtypes.getString(i));
        }
      }
      if (!subTypes.isEmpty()) {
        if (direction == LeafDataDirection.INPUT) {
          ValueInputNode[] nodes = new ValueInputNode[subTypes.size()];
          int cnt = 0;
          for (Entry<String, String> st : subTypes.entrySet()) {
            ValueInputNode leafNode = new ValueInputNode(st.getKey(), st.getValue(), " ");
            nodes[cnt] = leafNode;
            ++cnt;
          }
          parentVector.addElements(nodes);
        } else if (direction == LeafDataDirection.OUTPUT) {
          ValueOutputNode[] nodes = new ValueOutputNode[subTypes.size()];
          int cnt = 0;
          for (Entry<String, String> st : subTypes.entrySet()) {
            ValueOutputNode leafNode = new ValueOutputNode(
                st.getKey(), st.getValue(), false, getDefaultType(st.getValue()));
            nodes[cnt] = leafNode;
            ++cnt;
          }
          parentVector.addElements(nodes);
        }
      }
    }
  }

  protected boolean isSimpleType(String type) {
    if (type.equals("string") | type.equals("int32") | type.equals("int64") | type.equals("uint32")
        | type.equals("uint64") | type.equals("float64")) {
      return true;
    }
    return false;
  }

  private String getDefaultType(String type) {
    if (type.equals("int32") | type.equals("int64") | type.equals("uint32")
        | type.equals("uint64")) {
      return "0";
    } else if (type.equals("float64") | type.equals("float32")) {
      return "0.0";
    }
    // default for String
    return "default";
  }

  public JSONObject getJsonLevel(TreeModel treeModel, Object parent, boolean sendable_vars) {
    JSONObject jObj = new JSONObject(); // create JSON Object
    int childCount = treeModel.getChildCount(parent); // get number of Children for Parent TreeNode
    for (int i = 0; i < childCount; i++) { // for each Child
      Object child = treeModel.getChild(parent, i); // get Object of Child
      String name = getChildName(child.toString()); // get Name of this Child
      String type = getChildType(child.toString()); // get Type of this Child
      if (treeModel.isLeaf(child)) { // if Child is Leaf
        String val = " ";
        boolean usevar = false;
        DefaultMutableTreeNode treenode = (DefaultMutableTreeNode) child;
        Object node = treenode.getUserObject();
        if (node instanceof ValueInputNode) {
          val = ((ValueInputNode) node).getJson();
          usevar = ((ValueInputNode) node).getUseVariable();
        }
        try {
          if (usevar) {
            if (sendable_vars) {
              jObj.put(name,
                  "-+useVar+- + to_str(" + ((ValueInputNode) node).getValue() + ") + -+useVar+-");
            } else {
              jObj.put(name, new JSONObject(val));
            }
          } else if (!(node instanceof ValueInputNode)) { // ValueInputNode not using a variable
            jObj.put(name, "");
          } else if (type.equals("string")) {
            jObj.put(name, val);
          } else if (type.equals("int32") | type.equals("int64") | type.equals("uint32")
              | type.equals("uint64")) {
            jObj.put(name, Integer.parseInt(val));
          } else if (type.equals("float64")) {
            jObj.put(name, Double.parseDouble(val));
          }
        } catch (Exception e) {
          System.err.println("getJsonLevel: Error: " + usevar + " " + e);
          jObj.put(name, "");
        }

      } else { // If Child is not a Leaf
        jObj.put(name,
            getJsonLevel(treeModel, child,
                sendable_vars)); // put recursive call with child as parent
      }
    }
    return jObj; // return JSON Object
  }

  protected String getChildName(String input) {
    String[] typeSplit = input.split("\\[");
    String[] typeSplit_2 = typeSplit[1].split("\\s+");
    return typeSplit_2[0];
  }

  protected String getChildType(String input) {
    String[] typeSplit = input.split("\\(");
    String[] typeSplit_2 = typeSplit[1].split("\\)");
    return typeSplit_2[0];
  }

  protected String getChildVal(String input) {
    String[] typeSplit = input.split("\\s+");
    String[] typeSplit_2 = typeSplit[3].split("\\]");
    return typeSplit_2[0];
  }

  // TODO rename e.g. getLoadValueNodeTreeFromJSON
  protected LoadValueNode loadValuesToTree(LoadValueNode parent, JSONObject values, String name) {
    JSONArray keys = null;
    try {
      keys = values.names();
    } catch (Exception nptr) {
      System.out.println("loadValuesToTree: Error: " + nptr);
      return null;
    }

    LoadValueNode child = new LoadValueNode(parent, new ValueNode(name, ""));
    if (parent != null) {
      parent.addChild(child);
    }

    if (keys != null && !keys.isEmpty()) {
      for (int i = 0; i < keys.length(); ++i) {
        try {
          JSONObject obj = values.getJSONObject(keys.get(i).toString());
          if (obj.names().get(0).toString().equals("-+useVar+-")) {
            System.out.println("detected use of variable in " + keys.get(i).toString());
            LoadValueNode new_child = new LoadValueNode(
                child, new ValueNode(keys.getString(i), getJsonObjectValue(obj, 0)));
            new_child.setVariableUsed(true);
            child.addChild(new_child);
            continue;
          } else if (obj.names().get(0).toString().equals("-+useVarNum+-")) {
            System.out.println("detected use of numeric variable in " + keys.get(i).toString());
            LoadValueNode new_child = new LoadValueNode(
                child, new ValueNode(keys.getString(i), getJsonObjectValue(obj, 0)));
            new_child.setVariableUsed(true);
            new_child.setNumericType(true);
            child.addChild(new_child);
            continue;
          }
          loadValuesToTree(child, obj, keys.getString(i));
          continue;
        } catch (Exception e) {
          System.err.println("Error: " + e);
        }
        try {
          LoadValueNode new_child = new LoadValueNode(
              child, new ValueNode(keys.getString(i), getJsonObjectValue(values, i)));
          child.addChild(new_child);
        } catch (Exception ex) {
          System.err.println("Error: " + ex);
        }
      }
    }
    return child;
  }

  String getJsonObjectValue(JSONObject obj, int index) {
    JSONArray names = obj.names();
    // try parse element as number
    try {
      return obj.getNumber(names.getString(index)).toString();
    } catch (Exception ex) {
      System.err.println("Error: " + ex);
    }
    // try parse element as string
    try {
      return obj.getString(names.getString(index));
    } catch (Exception ex) {
      System.err.println("Error: " + ex);
    }
    return null;
  }

  protected void setTreeValues(LoadValueNode base_node, JTree tree) {
    System.out.println("### setTreeValues");
    TreeModel treeModel = tree.getModel();
    Object tmRoot = treeModel.getRoot();
    setNodeValueLevel(base_node, treeModel, tmRoot);
  }

  protected void setNodeValueLevel(LoadValueNode node, TreeModel treeModel, Object parent) {
    int child_count = treeModel.getChildCount(parent);
    if (node.getChildren().size() != child_count) {
      System.err.println("Different ChildCount of Tree and Values");
      return;
    }
    for (int i = 0; i < child_count; ++i) { // for each child of parent in treeModel
      Object child = treeModel.getChild(parent, i); // get Object of child
      String name = getChildName(child.toString()); // get name of child
      for (int j = 0; j < node.getChildren().size(); j++) { // for each child of LoadValueNode
        if (node.getChildren().get(j).getName().equals(name)) { // if names match
          if (treeModel.isLeaf(child)) { // and child is leaf
            DefaultMutableTreeNode treenode = (DefaultMutableTreeNode) child;
            Object obj = treenode.getUserObject();
            if (obj instanceof ValueInputNode) {
              ((ValueInputNode) obj).setValue(node.getChildren().get(j).getValue());
              node.getChildren().get(j).setNumericType(((ValueInputNode) obj).isNumericType());
              if (obj instanceof ValueOutputNode) {
                ((ValueOutputNode) obj)
                    .setVariableUsed(node.getChildren().get(j).getVariableUsed());
              }
            }
          } else {
            setNodeValueLevel(node.getChildren().get(j), treeModel, child);
          }
          break;
        }
      }
    }
  }
}
