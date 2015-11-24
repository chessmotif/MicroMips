public class PipelinedCPU {
	// registers
	public static int[] Regs = new int[32];
	public static float[] FRegs = new float[12];
	
	// mem
	public static int[] opcodeStack = new int[800];
	public static int[] dataStack = new int[800];

	// board state
	public static int PC = 0;
	public static State prev;
	public static State curr;
	
	public static void loadOpcodes(int[] instructions) {
		for (int i = 0; i < instructions.length; i++)
			opcodeStack[i] = instructions[i];
	}
	
	private static void runWB() {
		if (prev.MEM_WB.IR == 0)
			return;
		
		
	}
	
	
}

class State {
	public IF_ID IF_ID;
	public ID_EX ID_EX;
	public EX_MEM EX_MEM;
	public MEM_WB MEM_WB;
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