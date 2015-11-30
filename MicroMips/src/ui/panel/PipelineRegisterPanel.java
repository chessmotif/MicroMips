package ui.panel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import cpu.PipelinedCPU;

public class PipelineRegisterPanel extends JPanel {
	JTable dataTable;
	
	public PipelineRegisterPanel(int xpos, int ypos, int xlen, int ylen) {
		super();
		
		this.setLayout(null);
		
		Object[][] data = PipelinedCPU.getInternalRegisterData();
		Object[] cols = {"Registers", "Contents"};
		
		dataTable = new JTable(new DefaultTableModel(data, cols));
//		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//		table.getColumnModel().getColumn(0).setPreferredWidth(50);
//		table.getColumnModel().getColumn(1).setPreferredWidth(xlen-68);

		JScrollPane container = new JScrollPane(dataTable);
		container.setBounds(0, 0, xlen, ylen);
		dataTable.setFillsViewportHeight(true);
		
		this.add(container);
		this.setBounds(xpos, ypos, xlen, ylen);
		this.setVisible(true);
	}
	
	public void updateUITable() {
		TableModel tb = dataTable.getModel();
		Object[][] data = PipelinedCPU.getInternalRegisterData();
		
		for (int i = 0; i < data.length; i++)
			tb.setValueAt(data[i][1], i, 1);
	}
}
