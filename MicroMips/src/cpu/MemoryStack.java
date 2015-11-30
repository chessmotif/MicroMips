package cpu;
public class MemoryStack {
	protected int[] stack;
	public int size;
	public int startAdd;
	
	public MemoryStack(int startAddress, int size) {
		startAdd = startAddress;
		this.size = size;
		stack = new int[size/8];
	}
}