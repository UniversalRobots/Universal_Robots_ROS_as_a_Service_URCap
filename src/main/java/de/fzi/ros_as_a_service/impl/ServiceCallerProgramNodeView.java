package de.fzi.ros_as_a_service.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;

public class ServiceCallerProgramNodeView implements SwingProgramNodeView<ServiceCallerProgramNodeContribution>{
	
	private final ViewAPIProvider apiProvider;
	
	public ServiceCallerProgramNodeView(ViewAPIProvider apiProvider) {
		this.apiProvider = apiProvider;
	}
	
	private JComboBox<String> masterComboBox = new JComboBox<String>();

	@Override
	public void buildUI(JPanel panel, ContributionProvider<ServiceCallerProgramNodeContribution> provider) {
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		panel.add(createDescription("Configure service caller"));
		panel.add(createMasterComboBox(masterComboBox, provider));
	}
	
	public void setMasterComboBoxItems(String[] items) {
		masterComboBox.removeAllItems();
		masterComboBox.setModel(new DefaultComboBoxModel<String>(items));
	}
	
	public void setMasterComboBoxSelection(String item) {
		masterComboBox.setSelectedItem(item);
	}
	
	private Box createDescription(String desc) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JLabel label = new JLabel(desc);
		box.add(label);
		
		return box;
	}
	
	private Box createMasterComboBox(final JComboBox<String> combo,
			final ContributionProvider<ServiceCallerProgramNodeContribution> provider) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel label = new JLabel("Remote master");
		
		combo.setPreferredSize(new Dimension(104, 30));
		combo.setMaximumSize(combo.getPreferredSize());
		
		combo.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					provider.get().onMasterSelection((String)e.getItem());
				}
			}
		});
		
		box.add(label);
		box.add(combo);
		
		return box;
	}

}
