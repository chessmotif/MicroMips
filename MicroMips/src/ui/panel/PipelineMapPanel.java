package ui.panel;

import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import cpu.PipelinedCPU;

public class PipelineMapPanel extends JPanel {
	JTable dataTable;
	public PipelineMapPanel(int xpos, int ypos, int xlen, int ylen) {
		super();
		
		this.setLayout(null);

		Object[][] data = PipelinedCPU.map.getMapData();
		Object[] cols = generateCols(data);
		
		dataTable = new JTable(new DefaultTableModel(data, cols));
		dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		dataTable.getColumnModel().getColumn(1).setPreferredWidth(150);
		
		JScrollPane container = new JScrollPane(dataTable);
		container.setBounds(0, 0, xlen, ylen);
		dataTable.setFillsViewportHeight(true);
		
		this.add(container);
		this.setBounds(xpos, ypos, xlen, ylen);
		this.setVisible(true);
	}
	
	private Object[] generateCols(Object[][] data) {
		if (data == null) {
			Object[] out = {"Address", "Inst"};
			return out;
		}
		
		Object[] cols = new Object[data[0].length];
		
		cols[0] = "Address";
		cols[1] = "Inst";
		
		for (int i = 2; i < cols.length; i++)
			cols[i] = (i-1) + "";
		
		return cols;
	}

	public void updateUITable() {
		TableModel tb = dataTable.getModel();
		DefaultTableModel dtm = (DefaultTableModel) tb;
		Object[][] data = PipelinedCPU.map.getMapData();
		Object[] cols = generateCols(data);
	
		dtm.setRowCount(0);
		dtm.setColumnIdentifiers(cols);
		
		for (int i = 0; i < data.length; i++)
			dtm.addRow(data[i]);
		
		dataTable.getColumnModel().getColumn(1).setPreferredWidth(150);
		
		for (int i = 2; i < cols.length; i++)
			dataTable.getColumnModel().getColumn(i).setPreferredWidth(30);

		dataTable.scrollRectToVisible(new Rectangle(dataTable.getCellRect(data.length, cols.length, true)));
	}

}
