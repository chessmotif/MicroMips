package cpu;

public class PipelineMap {
	public History[] hist;
	
	public PipelineMap() {
		
	}
	
	public void setInstructionSet(Instruction[] set) {
		hist = new History[set.length];
		
		for (int i = 0; i < hist.length; i++)
			hist[i] = new History(i, set[i].opcode);
	}
	
	public void clear() {
		History[] clearedHist = new History[hist.length];
		
		for (int i = 0; i < hist.length; i++)
			clearedHist[i] = new History(i, hist[i].opcode);
		
		hist = clearedHist;
	}
	
	public void signCycle(int add, int cycle, String phase) {
		if (add >> 2 >= hist.length)
			return;
		
		switch(phase) {
			case "IF":
				hist[add >> 2].IF_cycle = cycle;
				return;
			case "ID":
				hist[add >> 2].ID_cycle = cycle;
				return;
			case "EX":
				hist[add >> 2].EX_cycle = cycle;
				return;
			case "A1":
				hist[add >> 2].Ax_cycle[0] = cycle;
				return;
			case "A2":
				hist[add >> 2].Ax_cycle[1] = cycle;
				return;
			case "A3":
				hist[add >> 2].Ax_cycle[2] = cycle;
				return;
			case "A4":
				hist[add >> 2].Ax_cycle[3] = cycle;
				return;
			case "M1":
				hist[add >> 2].Mx_cycle[0] = cycle;
				return;
			case "M2":
				hist[add >> 2].Mx_cycle[1] = cycle;
				return;
			case "M3":
				hist[add >> 2].Mx_cycle[2] = cycle;
				return;
			case "M4":
				hist[add >> 2].Mx_cycle[3] = cycle;
				return;
			case "M5":
				hist[add >> 2].Mx_cycle[4] = cycle;
				return;
			case "M6":
				hist[add >> 2].Mx_cycle[5] = cycle;
				return;
			case "MEM":
				hist[add >> 2].MEM_cycle = cycle;
				return;
			case "WB":
				hist[add >> 2].WB_cycle = cycle;
				return;
		}
	}
	
	public Object[][] getMapData() {
		if (hist == null)
			return null;
		
		int maxCycle = 0;
		for (int i = 0; i < hist.length; i++) {
			if (hist[i].IF_cycle > maxCycle) 
				maxCycle = hist[i].IF_cycle;
			if (hist[i].ID_cycle > maxCycle) 
				maxCycle = hist[i].ID_cycle;
			if (hist[i].EX_cycle > maxCycle) 
				maxCycle = hist[i].EX_cycle;
			if (hist[i].Ax_cycle[0] > maxCycle) 
				maxCycle = hist[i].Ax_cycle[0];
			if (hist[i].Ax_cycle[1] > maxCycle) 
				maxCycle = hist[i].Ax_cycle[1];
			if (hist[i].Ax_cycle[2] > maxCycle) 
				maxCycle = hist[i].Ax_cycle[2];
			if (hist[i].Ax_cycle[3] > maxCycle) 
				maxCycle = hist[i].Ax_cycle[3];

			if (hist[i].Mx_cycle[0] > maxCycle) 
				maxCycle = hist[i].Mx_cycle[0];
			if (hist[i].Mx_cycle[1] > maxCycle) 
				maxCycle = hist[i].Mx_cycle[1];
			if (hist[i].Mx_cycle[2] > maxCycle) 
				maxCycle = hist[i].Mx_cycle[2];
			if (hist[i].Mx_cycle[3] > maxCycle) 
				maxCycle = hist[i].Mx_cycle[3];
			if (hist[i].Mx_cycle[4] > maxCycle) 
				maxCycle = hist[i].Mx_cycle[4];
			if (hist[i].Mx_cycle[5] > maxCycle) 
				maxCycle = hist[i].Mx_cycle[5];

			if (hist[i].MEM_cycle > maxCycle) 
				maxCycle = hist[i].MEM_cycle;
			if (hist[i].WB_cycle > maxCycle) 
				maxCycle = hist[i].WB_cycle;
		}
		
//		System.out.println(maxCycle);
		String[][] out = new String[hist.length][maxCycle + 2];

		
		for (int i = 0; i < hist.length; i++) {
			out[i][0] = PipelinedCPU.formatString(hist[i].address);
			out[i][1] = OpcodeDecoder.generateInstruction(hist[i].opcode);
			
			if (hist[i].IF_cycle != -1)
				out[i][hist[i].IF_cycle+1] = "IF";
			if (hist[i].ID_cycle != -1)
				out[i][hist[i].ID_cycle+1] = "ID";
			if (hist[i].EX_cycle != -1)
				out[i][hist[i].EX_cycle+1] = "EX";
			
			if (hist[i].Ax_cycle[0] != -1)
				out[i][hist[i].Ax_cycle[0]+1] = "A" + 1;
			if (hist[i].Ax_cycle[1] != -1)
				out[i][hist[i].Ax_cycle[1]+1] = "A" + 2;
			if (hist[i].Ax_cycle[2] != -1)
				out[i][hist[i].Ax_cycle[2]+1] = "A" + 3;
			if (hist[i].Ax_cycle[3] != -1)
				out[i][hist[i].Ax_cycle[3]+1] = "A" + 4;
			
			if (hist[i].Mx_cycle[0] != -1)
				out[i][hist[i].Mx_cycle[0]+1] = "M" + 1;
			if (hist[i].Mx_cycle[1] != -1)
				out[i][hist[i].Mx_cycle[1]+1] = "M" + 2;
			if (hist[i].Mx_cycle[2] != -1)
				out[i][hist[i].Mx_cycle[2]+1] = "M" + 3;
			if (hist[i].Mx_cycle[3] != -1)
				out[i][hist[i].Mx_cycle[3]+1] = "M" + 4;
			if (hist[i].Mx_cycle[4] != -1)
				out[i][hist[i].Mx_cycle[4]+1] = "M" + 5;
			if (hist[i].Mx_cycle[5] != -1)
				out[i][hist[i].Mx_cycle[5]+1] = "M" + 6;
			
			if (hist[i].MEM_cycle != -1)
				out[i][hist[i].MEM_cycle+1] = "MEM";
			if (hist[i].WB_cycle != -1)
				out[i][hist[i].WB_cycle+1] = "WB";
		}
		
		return out;
	}
}

class History {
	int address;
	int opcode;
	int IF_cycle = -1;
	int ID_cycle = -1;
	int EX_cycle = -1;
	int Ax_cycle[] = {-1,-1,-1,-1};
	int Mx_cycle[] = {-1,-1,-1,-1,-1,-1};
	int MEM_cycle = -1;
	int WB_cycle = -1;
	
	public History(int add, int op) {
		address = add << 2;
		opcode = op;
	}
}