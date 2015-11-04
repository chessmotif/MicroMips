public class InstructionFormat {
	public static String getType(String instruction) {
		switch(instruction) {
			case "DADDU":
			case "DMULT":
			case "OR":
			case "SLT":
				return "R-type";
			case "ADD.S":
			case "MUL.S":
				return "Extended R-type";
			case "BEQ":
			case "L.S":
			case "S.S":
			case "LW":
			case "LWU":
			case "SW":
			case "DSLL":
			case "ANDI":
			case "DADDIU":
				return "I-type";
			case "J":
				return "J-type";
				
			default:
				return "";
		}
	}
	
	public static boolean isLoadStore(String instruction) {
		switch(instruction) {
			case "LW":
			case "LWU":
			case "SW":
			case "L.S":
			case "S.S":
				return true;
			default:
				return false;
		}
	}
	
	public static int getOpcode(String instruction) {
		switch(instruction) {
			case "J":		return 2;
			case "BEQ":		return 4;
			case "LW":		return 35;
			case "LWU":		return 39;
			case "SW":		return 43;
			case "DSLL":	return 0;
			case "ANDI":	return 12;
			case "DADDIU":	return 25;
			
			case "L.S": 	return 49;
			case "S.S":		return 57;
			case "ADD.S":	return 17;
			case "MUL.S":	return 17;
			
			default:
				return 0;
		}
	}
	
	public static int getExtOpcode(String instruction) {
		switch(instruction) {
			case "ADD.S": 	return 16;
			case "MUL.S":	return 16;
			
			default:
				return 0;
		}
	}

	public static int getFunc(String instruction) {
		switch(instruction) {
			case "DADDU":	return 45;
			case "DMULT":	return 28;
			case "OR":		return 37;
			case "SLT":		return 42;
			
			case "ADD.S":	return 0;
			case "MUL.S":	return 2;
			
			default:
				return 0;
		}
	}
	
	public static int getArgCount(String instruction) {
		switch(instruction) {
			case "J":
				return 1;
				
			case "DMULT": 
				return 2;
			
			default:
				return 3;
		}
	}
	
	public static int getRegisterCount(String instruction) {
		switch(instruction) {
			case "J":
				return 0;
			
			case "DMULT": 
			case "BEQ": 
			case "LW": 
			case "LWU": 
			case "SW": 
			case "DSLL": 
			case "ANDI": 
			case "DADDIU": 
				return 2;
		
			default: 
				return 3;
		}
	}
	
	public static char getRegisterType(String instruction) {
		switch(instruction) {
			case "L.S":
			case "S.S":
			case "ADD.S":
			case "MUL.S":
				return 'F';
			
			default:
				return 'R';
		}
	}
}