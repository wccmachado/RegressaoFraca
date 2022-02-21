package alu.ufc.preference;

public enum Operator {

	always(0), sometime(1);
	
	private int valueOp;
    
	Operator(int valueOp) {
        this.valueOp = valueOp;
    }
	
	public int getValueOp() {
		return valueOp;
	}
	
}
