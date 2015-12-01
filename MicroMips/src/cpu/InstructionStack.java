package cpu;

public class InstructionStack extends MemoryStack {
	Instruction[] set;
	
	public InstructionStack(int startAddress, int size) {
		super(startAddress, size);
	}

	public void setInstructionSet(Instruction[] inst) {
		set = inst;
		
		for (int i = 0; i < inst.length; i++) {
			if (inst[i].label.length() != 0) {
				PipelinedCPU.labelMap.put(inst[i].label, (long) (i<<2));
			}
		}
		
		for (int i = 0; i < inst.length; i++) {
			try {
				set[i].generateOpcode();
				stack[i] = set[i].opcode;
			} catch(Exception ex) {
				System.err.println("generate opcode failed");
				System.err.println(ex.getMessage());
			}
		}
	}
	
	public int loadInst(int add) {
		int target = (add - startAdd) >> 2;
		
		return stack[target];
	}
	
	public Object[][] getInstructionData() {
		int instructionCount = 0;
		while(stack[instructionCount] != 0)
			instructionCount++;
		
		Object[][] out = new Object[instructionCount][4];
		
		for (int i = 0; i < instructionCount; i++) {
			out[i][0] = String.format("%04x",startAdd + i * 4); // address
			out[i][1] = set[i].label;
			out[i][2] = PipelinedCPU.formatString(stack[i]); // opcode
			out[i][3] = OpcodeDecoder.generateInstruction(stack[i]); // inst
		}
		
		return out;
		
	}

}
