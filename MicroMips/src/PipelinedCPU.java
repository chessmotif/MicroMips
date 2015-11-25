public class PipelinedCPU {
	// registers
	public static int[] Regs = new int[32];
	public static int[] FRegs = new int[12];
	
	// mem
	public static int[] opcodeStack = new int[800];
	public static int[] dataStack = new int[800];
	
	public static long HI = 0, LO = 0;

	// board state
	public static int PC = 0;
	public static State prev = new State();
	public static State curr = new State();
	
	public static void loadOpcodes(int[] instructions) {
		for (int i = 0; i < instructions.length; i++)
			opcodeStack[i] = instructions[i];
	}
	
	public static void runOneClockCycle() {
		runWB();
		runMEM();
		runEX();
		runID();
		runIF();
		
		prev = curr;
	}
	
	private static void runWB() {
		if (prev.MEM_WB.IR == 0)
			return;
		
		switch(OpcodeDecoder.getType(prev.MEM_WB.IR)) {
			case "R-type":
			case "Shift-type":
				Regs[OpcodeDecoder.getRC(prev.MEM_WB.IR)] = prev.MEM_WB.ALUOutput;
				break;
			case "Extended R-type":
				FRegs[OpcodeDecoder.getRD(prev.MEM_WB.IR)] = prev.MEM_WB.ALUOutput;
				break;
			case "Mem-type":
				switch (OpcodeDecoder.getOp(prev.MEM_WB.IR)) {
					case 35:	case 39:
						Regs[OpcodeDecoder.getRB(prev.MEM_WB.IR)] = prev.MEM_WB.ALUOutput;
						break;
					case 49:
						FRegs[OpcodeDecoder.getRB(prev.MEM_WB.IR)] = prev.MEM_WB.ALUOutput;
						break;
				}
				break;
			case "I-type": // care for BEQ
			case "J-type": // empty for now
				break;
		}
		
		
	}
	
	private static void runMEM() {
		if (prev.EX_MEM.IR == 0)
			return;

		curr.MEM_WB.IR = prev.EX_MEM.IR;
		switch(OpcodeDecoder.getType(prev.EX_MEM.IR)) {
			case "R-type":
			case "Extended R-type":
			case "Shift-type":
			case "I-type": // care for BEQ
				curr.MEM_WB.ALUOutput = prev.EX_MEM.ALUOutput;
				break;
			case "Mem-type":
				switch (OpcodeDecoder.getOp(prev.EX_MEM.IR)) {
				// BE VERY CAREFUL WITH THE DATA STACK SHI-
					case 35:	case 39:	case 49: // loads
						curr.MEM_WB.LMD = dataStack[prev.EX_MEM.ALUOutput];
						break;
					case 43:	case 57:
						dataStack[prev.EX_MEM.ALUOutput] = prev.EX_MEM.B;
						break;
				}
			case "J-type": // empty for now
				break;
		}
	}
	
	private static void runEX() {
		if (prev.ID_EX.IR == 0)
			return;

		curr.EX_MEM.IR = prev.ID_EX.IR;
		switch(OpcodeDecoder.getType(prev.ID_EX.IR)) {
			case "R-type":
			case "Extended R-type":
				curr.EX_MEM.ALUOutput = OpcodeDecoder.executeOp(prev.ID_EX.A, prev.ID_EX.B, prev.ID_EX.IR);
				break;
			case "I-type":	// care for BEQ
			case "Shift-type":
				curr.EX_MEM.ALUOutput = OpcodeDecoder.executeOp(prev.ID_EX.A, prev.ID_EX.Imm, prev.ID_EX.IR);
				break;
			case "Mem-type":
				curr.EX_MEM.ALUOutput = OpcodeDecoder.executeOp(prev.ID_EX.A, prev.ID_EX.Imm, prev.ID_EX.IR);
				curr.EX_MEM.B = prev.ID_EX.B;
				break;
			case "J-type": // empty for now
				break;
		}
	}
	
	private static void runID() {
		if (prev.IF_ID.IR == 0)
			return;

		// handle the shift case, float cases, and load store cases
		switch(OpcodeDecoder.getType(prev.IF_ID.IR)) {
			case "R-type":
			case "I-type":
			case "Mem-type":
			case "J-type":
				curr.ID_EX.A = Regs[OpcodeDecoder.getRA(prev.IF_ID.IR)];
				curr.ID_EX.B = Regs[OpcodeDecoder.getRB(prev.IF_ID.IR)];
				curr.ID_EX.Imm = OpcodeDecoder.getImm(prev.IF_ID.IR);
				break;
			case "Extended R-type":
				curr.ID_EX.A = FRegs[OpcodeDecoder.getRC(prev.IF_ID.IR)];
				curr.ID_EX.B = FRegs[OpcodeDecoder.getRB(prev.IF_ID.IR)];
				curr.ID_EX.Imm = OpcodeDecoder.getImm(prev.IF_ID.IR);
				break;
			case "Shift-type":
				curr.ID_EX.A = FRegs[OpcodeDecoder.getRC(prev.IF_ID.IR)];
				curr.ID_EX.B = FRegs[OpcodeDecoder.getRB(prev.IF_ID.IR)];
				curr.ID_EX.Imm = OpcodeDecoder.getRD(prev.IF_ID.IR);
				break;
		}
		
		curr.ID_EX.IR = prev.IF_ID.IR;
		curr.ID_EX.NPC = prev.IF_ID.NPC;
	}
	
	private static void runIF() {
		curr.IF_ID.IR = opcodeStack[PC];
		curr.IF_ID.NPC = PC + 4;
		PC = PC + 4; // if branch, change this
	}
}

class State {
	public IF_ID IF_ID = new IF_ID();
	public ID_EX ID_EX = new ID_EX();
	public EX_MEM EX_MEM = new EX_MEM();
	public MEM_WB MEM_WB = new MEM_WB();
}

class IF_ID {
	public int IR = 0, NPC = 0;
}

class ID_EX {
	public int A = 0, B = 0, NPC = 0, IR = 0, Imm = 0;
}

class EX_MEM {
	public int IR = 0, ALUOutput = 0, B = 0;
}

class MEM_WB {
	public int IR = 0, ALUOutput = 0, LMD = 0;
}