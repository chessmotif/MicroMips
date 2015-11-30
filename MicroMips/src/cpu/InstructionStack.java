package cpu;

public class InstructionStack extends MemoryStack {
	public InstructionStack(int startAddress, int size) {
		super(startAddress, size);
	}

	public void setInstructionSet(Instruction[] inst) {
		for (int i = 0; i < inst.length; i++)
			stack[i] = inst[i].opcode;
	}
	
	public int loadInst(int add) {
		int target = (add - startAdd) >> 2;
		
		return stack[target];
	}
	
	public Object[][] getInstructionData() {
		int instructionCount = 0;
		while(stack[instructionCount] != 0)
			instructionCount++;
		
		Object[][] out = new Object[instructionCount][3];
		
		for (int i = 0; i < instructionCount; i++) {
			out[i][0] = String.format("%04x",startAdd + i * 4); // address
			out[i][1] = PipelinedCPU.formatString(stack[i]); // opcode
			out[i][2] = OpcodeDecoder.generateInstruction(stack[i]); // inst
		}
		
		return out;
		
	}

}
