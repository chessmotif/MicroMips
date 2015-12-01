package cpu;
public class OpcodeDecoder {
	public static String getType(int opcode) {
		int op = getOp(opcode);
		
		switch(op) {
			case 0:
				if (getFunc(opcode) == 56)
					return "Shift-type";
				return "R-type";
			case 17:
				return "Extended R-type";
			case 35:	case 39:	case 43:
			case 49:	case 57:
				return "Mem-type";
			case 25:	case 12:
				return "I-type";
			case 2:		case 4:
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
		return opcode & 0xFFFF;
	}
	
	public static long executeOp(long input1, long input2, int opcode) {
		switch(getOp(opcode)) {
			case 0:
				switch(getFunc(opcode)) {
					case 45:
						return input1 + input2;
					case 28: // long
						return 0;
					case 37:
						return input1 | input2;
					case 42:
						return input1 < input2? 1 : 0;
					case 56:
						System.out.println(input1 + " " + input2);
						return input1 << input2;
				}
			case 17:
				return Float.floatToIntBits(Float.intBitsToFloat((int)input1) + Float.intBitsToFloat((int)input2));
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
	
	public static String generateInstruction(int opcode) {
		switch(getOp(opcode)) {
			case 0:
				switch(getFunc(opcode)) {
					case 45:
						return "DADDU R" + getRC(opcode) + ", R" + getRA(opcode) + ", R" + getRB(opcode);
					case 28: // long
						return "DMULT R" + getRA(opcode) + ", R" + getRB(opcode);
					case 37:
						return "OR R" + getRC(opcode) + ", R" + getRA(opcode) + ", R" + getRB(opcode);
					case 42:
						return "SLT R" + getRC(opcode) + ", R" + getRA(opcode) + ", R" + getRB(opcode);
					case 56:
						return "DSLL R" + getRC(opcode) + ", R" + getRB(opcode) + ", " + getRD(opcode);
				}
			case 17:
				switch(getFunc(opcode)) {
					case 0:
						return "ADD.S F" + getRD(opcode) + ", F" + getRC(opcode) + ", F" + getRB(opcode);
					case 2:
						return "MUL.S F" + getRD(opcode) + ", F" + getRC(opcode) + ", F" + getRB(opcode);
				}
			case 35:	
				return "LW R" + getRB(opcode) + ", " + String.format("%04x", getImm(opcode)) + "(R" + getRA(opcode) + ")";
			case 39:	
				return "LWU R" + getRB(opcode) + ", " + String.format("%04x", getImm(opcode)) + "(R" + getRA(opcode) + ")";
			case 43:
				return "SW R" + getRB(opcode) + ", " + String.format("%04x", getImm(opcode)) + "(R" + getRA(opcode) + ")";
			case 49:	
				return "S.S F" + getRB(opcode) + ", " + String.format("%04x", getImm(opcode)) + "(R" + getRA(opcode) + ")";
			case 57:
				return "L.S F" + getRB(opcode) + ", " + String.format("%04x", getImm(opcode)) + "(R" + getRA(opcode) + ")";
			case 25:	
				return "DADDIU R" + getRB(opcode) + ", R" + getRA(opcode) + ", " + String.format("%04x", getImm(opcode));
			case 12:
				return "ANDI R" + getRB(opcode) + ", R" + getRA(opcode) + ", " + String.format("%04x", getImm(opcode));
			case 2:		
				return "J " + String.format("%04x", getImm(opcode) << 2);
			case 4:
				return "BEQ R" + getRB(opcode) + ", R" + getRA(opcode) + ", " + String.format("%04x", getImm(opcode));
			default:
				return "";
		}
		
	}
}