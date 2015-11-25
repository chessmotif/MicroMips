public class OpcodeDecoder {
	public static String getType(int opcode) {
		int op = getOp(opcode);
		
		switch(op) {
			case 0:
				return "R-type";
			case 17:
				return "Extended R-type";
			case 35:	case 39:	case 43:
			case 49:	case 57:
				return "Mem-type";
			case 25:	case 12:	case 4:
				return "I-type";
			case 2:
				return "J-type";
			default:
				return "";
		}
	}
	
	public static int getOp(int opcode) {
		return opcode >>> 26;
	}
	
	public static int getFunc(int opcode) {
		return opcode & 63;
	}
	
	public static int getRA(int opcode) {
		return (opcode >> 21) & 31; // IR 6-10
	}
	
	public static int getRB(int opcode) {
		return (opcode >> 16) & 31; // IR 11-15
	}
	
	public static int getRC(int opcode) {
		return (opcode >> 11) & 31; // IR 16-21
	}

	public static int getRD(int opcode) {
		return (opcode >> 6) & 31; // IR 21-25
	}

	public static int getImm(int opcode) {
		return opcode & 0x0000FFFF;
	}
	
	public static int executeOp(int input1, int input2, int opcode) {
		switch(getOp(opcode)) {
			case 0:
				switch(getFunc(opcode)) {
					case 45:
						return input1 + input2;
					case 28: // long
						return input1 * input2;
					case 37:
						return input1 | input2;
					case 42:
						return input1 < input2? 1 : 0;
					case 56:
						return input1 << input2;
				}
			case 17:
				return Float.floatToIntBits(Float.intBitsToFloat(input1) + Float.intBitsToFloat(input2));
			case 35:	case 39:	case 43:
			case 49:	case 57:
				return input1 + input2;
			case 25:	
				return input1 + input2;
			case 12:	
				return input1 & input2;
			case 4: // branch
			case 2: // jump
			default:
				return 0;
		}
		
	}
}