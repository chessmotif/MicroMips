package ui.panel;

import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import cpu.PipelinedCPU;

public class DataPanel extends JPanel {
	JTable dataTable;
	public DataPanel(int xpos, int ypos, int xlen, int ylen) {
		super();
		
		this.setLayout(null);

		JTextField search = new JTextField();
		search.setBounds(0, 0, xlen, 20);


		search.addActionListener(new java.awt.event.ActionListener() {
		    public void actionPerformed(java.awt.event.ActionEvent e) {
		    	String query = search.getText();
		    	
		    	try {
		    		int s = 0;
		    		String hex = "0123456789abcdef";
		    		String out = (query+"").replace(" ", "").toLowerCase();
		    		int size = out.length();
		    		
		    		for (int i = 0; i < size - 1; i++) {
		    			s |= hex.indexOf(out.charAt(i));
		    			s <<= 4;
		    		}
		    		
		    		s |= hex.indexOf(out.charAt(size - 1));
		    		s -= 0x2000;
		    		s >>= 2;
		    		
		    		dataTable.setRowSelectionInterval(s, s);
		    		dataTable.scrollRectToVisible(new Rectangle(dataTable.getCellRect(s, 0, true)));
		    	} catch (Exception ex) {
		    		return;
		    	}
		    	
		    }
		});
		
		Object[][] data = PipelinedCPU.getMemoryData();
		Object[] cols = {"Address", "Data"};
		
		dataTable = new JTable(new DefaultTableModel(data, cols));
		dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		dataTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		dataTable.getColumnModel().getColumn(1).setPreferredWidth(xlen-68);
		
		JScrollPane container = new JScrollPane(dataTable);
		container.setBounds(0, 20, xlen, ylen - 20);
		dataTable.setFillsViewportHeight(true);
		dataTable.getModel().addTableModelListener(new DataTableListener(this));
		
		this.add(search);
		this.add(container);
		this.setBounds(xpos, ypos, xlen, ylen);
		this.setVisible(true);
	}

	public void updateUITable() {
		TableModel tb = dataTable.getModel();
		Object[][] data = PipelinedCPU.getMemoryData();
		
		for (int i = 0; i < data.length; i++) {
			String currValue = tb.getValueAt(i, 1) + "";
			
			if (!currValue.equals(data[i][1] + ""))
				tb.setValueAt(data[i][1], i, 1);
		}
	}
}

class DataTableListener implements TableModelListener {
	DataPanel parent;
	
	public DataTableListener(DataPanel p) {
		parent = p;
	}
	
	@Override
	public void tableChanged(TableModelEvent e) {
		// TODO Auto-generated method stub
        int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel)e.getSource();
        Object data = model.getValueAt(row, column);
		Object add = model.getValueAt(row, 0);
		long dataConverted = 0;
        
		if (data.toString().contains(" ")) {
			data = data.toString().replace(" ", "");
		}
		
		
		if (data.toString().length() == 8) { // hex number
    		String hex = "0123456789abcdef";
    		String out = (data+"").replace(" ", "").toLowerCase();
    		int size = out.length();
    		
    		for (int i = 0; i < size - 1; i++) {
    			dataConverted |= hex.indexOf(out.charAt(i));
    			dataConverted <<= 4;
    		}
    		
    		dataConverted |= hex.indexOf(out.charAt(size - 1));
		}
		else if (data.toString().contains(".")) { // float
			dataConverted = (long)Float.floatToIntBits(Float.parseFloat(data+""));
		}
		else { // decimal number
			dataConverted = Long.parseLong(data+"");
		}
		

		int addConverted = 0;
		String hex = "0123456789abcdef";
		String out = (add+"").replace(" ", "").toLowerCase();
		int size = out.length();
		
		for (int i = 0; i < size - 1; i++) {
			addConverted |= hex.indexOf(out.charAt(i));
			addConverted <<= 4;
		}
		
		addConverted |= hex.indexOf(out.charAt(size - 1));
		
		PipelinedCPU.dataStack.writeData(dataConverted, addConverted);
		
        parent.updateUITable();
	}
	
}