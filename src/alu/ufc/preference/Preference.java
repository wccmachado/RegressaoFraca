package alu.ufc.preference;

import alu.ufc.file.ModelReader;
import net.sf.javabdd.BDD;

import java.util.List;

public class Preference {

	private BDD bddProposition;
	private Operator operator;
	private String namePreference;
	
	public Preference(){}

	public Preference(Operator operator, BDD bddProposition) {
		this.bddProposition = bddProposition;
		this.operator = operator;
	}

	public BDD getBddProposition() {
		return bddProposition;
	}

	public void setBddProposition(BDD bddProposition) {
		this.bddProposition = bddProposition;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	
}
