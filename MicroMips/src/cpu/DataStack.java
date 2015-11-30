package cpu;

public class DataStack extends MemoryStack {
	public DataStack(int startAddress, int size) {
		super(startAddress, size);
	}

	public long loadData(int add) {
		int target = (add - startAdd) / 4;
		
		return stack[target];
	}
	
	public void writeData(long data, int add) {
		int target = (add - startAdd) / 4;
		stack[target] = (int)data;
	}
	
	public Object[][] getMemoryData() {
		Object[][] out = new Object[stack.length][3];
		
		for (int i = 0; i < stack.length; i++) {
			out[i][0] = String.format("%04x",startAdd + i * 4); // address
			out[i][1] = PipelinedCPU.formatString(stack[i]); // data
		}
		
		return out;
	}
}