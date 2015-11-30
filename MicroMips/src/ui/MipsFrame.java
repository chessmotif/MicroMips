package ui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import ui.panel.DataPanel;
import ui.panel.InstructionPanel;
import ui.panel.PipelineMapPanel;
import ui.panel.PipelineRegisterPanel;
import ui.panel.RegisterPanel;
import cpu.Instruction;
import cpu.InstructionParser;
import cpu.PipelinedCPU;

public class MipsFrame extends JFrame {
	PipelineRegisterPanel pipeline;
	RegisterPanel regs;
	InstructionPanel inst;
	DataPanel data;
	PipelineMapPanel map;
	
	public MipsFrame(String title) {
		super(title);
		
		this.setSize(1024, 720);
		this.setVisible(true);
		
		this.createMenu();
		this.setResizable(false);
		
		Container content = this.getContentPane();
		content.setLayout(null);
		
		pipeline = new PipelineRegisterPanel(10, 350, 300, 311);
		regs = new RegisterPanel(760, 10, 250, 650);
		inst = new InstructionPanel(10, 10, 300, 330);
 		data = new DataPanel(320, 10, 430, 180);
 		map = new PipelineMapPanel(320, 200, 430, 200);
		
		content.add(pipeline);
		content.add(regs);
		content.add(inst);
		content.add(data);
		content.add(map);
		
		this.addKeyListener(new FrameKeyListener(this));
	}
	
	public void updateUITable() {
		this.requestFocus();
		pipeline.updateUITable();
		inst.updateUITable();
		regs.updateUITable();
		data.updateUITable();
		map.updateUITable();
	}
	
	private void createMenu() {
		JMenuBar menu = new JMenuBar();
		
		JMenu m = new JMenu("Actions");
		
		JMenuItem load = new JMenuItem("Load");
		load.addActionListener(new LoadFileActionListener(this));
		JMenuItem reset = new JMenuItem("Reset");
		reset.addActionListener(new ResetCPUActionListener(this));
		JMenuItem runOne = new JMenuItem("Run Once");
		runOne.addActionListener(new RunOnceActionListener(this));
		JMenuItem runAll = new JMenuItem("Run All");
		runAll.addActionListener(new RunAllActionListener(this));

		m.add(load);
		m.add(reset);
		m.add(runOne);
		m.add(runAll);
		
		menu.add(m);
		menu.setVisible(true);
		
		this.setJMenuBar(menu);
	}
}

class LoadFileActionListener implements ActionListener {
	public MipsFrame parent;
	
	public LoadFileActionListener(MipsFrame p) {
		parent = p;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		JFileChooser fc = new JFileChooser();
		
		int returnVal = fc.showOpenDialog(parent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();

            Instruction[] instSet;
			try {
				instSet = InstructionParser.parseFile(f);
	    		PipelinedCPU.opcodeStack.setInstructionSet(instSet);
	    		PipelinedCPU.setPipelineMap(instSet);
	    		PipelinedCPU.reset();

	    		parent.updateUITable();
	    		
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				System.err.println("file choose broke");
				e.printStackTrace();
			}
        }
	}
}

class ResetCPUActionListener implements ActionListener {
	public MipsFrame parent;

	public ResetCPUActionListener(MipsFrame p) {
		parent = p;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		PipelinedCPU.reset();
		parent.updateUITable();
	}
}

class RunOnceActionListener implements ActionListener {
	public MipsFrame parent;
	
	public RunOnceActionListener(MipsFrame p) {
		parent = p;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		PipelinedCPU.runOneClockCycle(parent);
	}
}

class RunAllActionListener implements ActionListener {
	public MipsFrame parent;
	
	public RunAllActionListener(MipsFrame p) {
		parent = p;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		PipelinedCPU.runAllInstructions(parent);
	}
}

class FrameKeyListener implements KeyListener {
	MipsFrame parent;
	
	public FrameKeyListener(MipsFrame p) {
		parent = p;
	}
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		switch(arg0.getKeyChar()) {
			case 'c':
				PipelinedCPU.runOneClockCycle(parent);
				return;
			case 'v':
				PipelinedCPU.runAllInstructions(parent);
				return;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}