package ui.panel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import cpu.PipelinedCPU;

public class RegisterPanel extends JPanel{
	JTable dataTable;
	
	public RegisterPanel(int xpos, int ypos, int xlen, int ylen) {
		super();
		
		this.setLayout(null);

		Object[][] data = PipelinedCPU.getIntRegisterData();
		Object[] cols = {"Registers", "Contents"};
		
		dataTable = new JTable(new DefaultTableModel(data, cols));
		dataTable.getColumnModel().getColumn(0).setPreferredWidth(10);

		JScrollPane container = new JScrollPane(dataTable);
		container.setBounds(0, 0, xlen, ylen);
		dataTable.setFillsViewportHeight(true);
		dataTable.getModel().addTableModelListener(new RegisterTableListener(this));
		
		this.add(container);
		this.setBounds(xpos, ypos, xlen, ylen);
		this.setVisible(true);
	}

	public void updateUITable() {
		TableModel tb = dataTable.getModel();
		Object[][] data = PipelinedCPU.getIntRegisterData();
		
		for (int i = 0; i < data.length; i++) {
			String currValue = tb.getValueAt(i, 1) + "";
			
			if (!currValue.equals(data[i][1] + ""))
				tb.setValueAt(data[i][1], i, 1);
		}
	}
}

class RegisterTableListener implements TableModelListener {
	RegisterPanel parent;
	
	public RegisterTableListener(RegisterPanel p) {
		parent = p;
	}
	
	@Override
	public void tableChanged(TableModelEvent e) {
		// TODO Auto-generated method stub
        int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel)e.getSource();
        Object data = model.getValueAt(row, column);
		
		System.out.println("change detected at " + row + "," + column);
		System.out.println(data + "");

		if (row < 31) {
        	try {
        		PipelinedCPU.Regs[row] = Long.parseLong(data+"");
        	}
        	catch (Exception ex) {
        		String hex = "0123456789abcdef";
        		String out = (data+"").replace(" ", "").toLowerCase();
        		int size = out.length();
        		long ans = 0;
        		
        		for (int i = 0; i < size - 1; i++) {
        			ans |= hex.indexOf(out.charAt(i));
        			ans <<= 4;
        		}
        		
        		ans |= hex.indexOf(out.charAt(size - 1));
        		
        		PipelinedCPU.Regs[row] = ans;
        	}
        }
        else {
        	try {
        		PipelinedCPU.FRegs[row-31] = (long)Float.floatToIntBits(Float.parseFloat(data+""));
        	}
        	catch (Exception ex) {
        		String hex = "0123456789abcdef";
        		String out = (data+"").replace(" ", "").toLowerCase();
        		System.out.println("formatted - " + out);
        		int size = out.length();
        		long ans = 0;
        		
        		for (int i = 0; i < size - 1; i++) {
        			ans |= hex.indexOf(out.charAt(i));
        			ans <<= 4;
        		}
        		
        		ans |= hex.indexOf(out.charAt(size - 1));
        		
        		PipelinedCPU.FRegs[row-31] = ans;
        	}
        }
        
        parent.updateUITable();
	}
	
}