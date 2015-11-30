package ui.panel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import cpu.PipelinedCPU;

public class InstructionPanel extends JPanel {
	JTable dataTable;
	public InstructionPanel(int xpos, int ypos, int xlen, int ylen) {
		super();
		
		this.setLayout(null);

		Object[][] data = PipelinedCPU.getInstructionData();
		Object[] cols = {"Address", "Opcode", "Instruction"};
		
		dataTable = new JTable(new DefaultTableModel(data, cols));
		dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		dataTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		dataTable.getColumnModel().getColumn(1).setPreferredWidth(70);
		dataTable.getColumnModel().getColumn(2).setPreferredWidth(xlen-138);

		JScrollPane container = new JScrollPane(dataTable);
		container.setBounds(0, 0, xlen, ylen);
		dataTable.setFillsViewportHeight(true);
		
		this.add(container);
		this.setBounds(xpos, ypos, xlen, ylen);
		this.setVisible(true);
	}

	public void updateUITable() {
		TableModel tb = dataTable.getModel();
		DefaultTableModel dtm = (DefaultTableModel) tb;
		Object[][] data = PipelinedCPU.getInstructionData();
	
		dtm.setRowCount(0);
		
		for (int i = 0; i < data.length; i++)
			dtm.addRow(data[i]);
	}
}