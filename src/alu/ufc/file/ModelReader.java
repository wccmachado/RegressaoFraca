package alu.ufc.file;

import alu.ufc.action.Action;
import alu.ufc.bdd.BDDCreator;
import alu.ufc.preference.Operator;
import alu.ufc.preference.Preference;
import net.sf.javabdd.BDD;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/* Read the input file and call the methods of class BDDCreator */
public class ModelReader {
	Action action;
	private BDDCreator bddCreator;
	private String type;
	private Preference preference;
	
	//read the lines of the fileName. pType: model-checking or planner
	public void fileReader(String fileName, int nodenum, int cachesize){
		
		//type = pType;
		bddCreator = new BDDCreator(nodenum, cachesize);
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String line;
			String propositionsLine = "";
			String initialStateLine = "";
			String goalLine = "";
			String preferencesLine = "";
			String actionName = "";
			String actionPre = "";
			String actionEff = "";
			String init = "";
			String constraintsLine = "";
				
			while (in.ready()) {
				line = in.readLine();
				
				// read the lines of the fileName corresponding the variables
				if(line.equals("<predicates>")){
					line = in.readLine(); //read next line containing the propositions
					propositionsLine = line;
					bddCreator.initializeVarTable(propositionsLine);
					line = in.readLine(); //read <\predicates>
				}

				if(line.equals("<constraints>")){
					while(line.equals("<\\constraints>") == false){
						line = in.readLine(); //<constraints><\constraints>
						if(line.equals("<\\constraints>")) break;
						constraintsLine = line;
						bddCreator.createConstraintBDD(constraintsLine);
					}
				}

				
				// read the lines corresponding to the initial state
				if(line.equals("<initial>")){
					//System.out.println("initial begin");
					line = in.readLine(); //read next line containing the initial state specification
					initialStateLine = line;
					//System.out.println(initialStateLine);
					bddCreator.createInitialStateBdd(initialStateLine);
					line = in.readLine(); // read the line </initial>
				}
				
				// read the lines corresponding to the planning goal
				if(line.equals("<goal>")){
					line = in.readLine(); //read next line
					goalLine = line;
					//System.out.println(goalLine);
					bddCreator.createGoalBdd(goalLine);
					line = in.readLine(); // read the line <\goal>
				}
				
				if(line.equals("<preferences>")) {
					line = in.readLine(); //read next line
					preferencesLine = line;

					if(preferencesLine.startsWith("ALWAYS")) {
						preference = new Preference(Operator.ALWAYS,
								bddCreator.createPreferenceBdd(preferencesLine.substring(7)));
					} else {
						preference = new Preference(Operator.SOMETIME,
								bddCreator.createPreferenceBdd(preferencesLine.substring(9)));
					}
					line = in.readLine();
				}
				
				// read the lines corresponding to the actions
				if(line.equals("<actionsSet>")){

					line = in.readLine();
					int count=0;
					while(line.equals("<\\actionsSet>") == false) {

						//System.out.println(line);
						if(line.trim().equals("<action>")) {
							line = in.readLine(); //<name><\name>
							//System.out.println(line);
							actionName = line.substring(line.indexOf(">") + 1, line.indexOf("\\") - 1);
							//System.out.println(actionName);
							line = in.readLine(); //<pre><\pre>
							//System.out.println(line);
							actionPre = line.substring(line.indexOf(">") + 1, line.indexOf("\\") - 1);
							//System.out.println(actionPre);
							line = in.readLine(); //<pos><\pos>
							//System.out.println("fulerage"+line);
							actionEff = line.substring(line.indexOf(">") + 1, line.indexOf("\\") - 1);

							//System.out.println("Veja "+actionEff);
							action = new Action(actionName, actionPre, actionEff,bddCreator);
							//System.out.println("Ação: " + action);
							bddCreator.addAction(action);
							line = in.readLine(); //<\action>
							//System.out.println(line);
							line = in.readLine(); //<action>
							//System.out.println("quantidade de ações : " + count++);
						}
						//action.printAction();
					}
				}

			}
			in.close();
		} catch (Exception e) {
			//System.out.println("catch");
			e.getMessage();
		}		
	}
	
	//public String getType() {
	//	return type;
	//}
	public BDD getConstraints(){
		return bddCreator.getConstraintBDD();
	}
	public Hashtable<String,Integer> getVarTable() {
		return bddCreator.getVarTable();
	}
	
	public String getNamePredicate(BDD bdd){
		String output="";
		Hashtable<String, Integer> tabela = getVarTable();
		Set<String> predicate = getVarTable().keySet();

		// for-each loop
		for(String key : predicate) {
			if (tabela.get(key) == bdd.var()){
				output = key;
			}
		}

		return output;
	}
	public int getPropNum(){
		return bddCreator.getPropNum();
	}
	
	public BDD getInitialStateBDD(){
		return bddCreator.getInitiaStateBDD();
	}

	public BDD getInitBDD(){
		return bddCreator.getInitBDD();
	}
	
	public Vector<Action> getActionSet(){
		return bddCreator.getActionsSet();
	}
	
	public BDD getGoalSpec(){
		return bddCreator.getGoalBDD();
	}


	
	/*public BDD getConstraints(){
		return bddCreator.getConstraintBDD();
	}*/
	
	// Method that returns preference formulae
	public Preference getPreference(){
		return preference;
	}


	
	// Method that returns an auxiliar BDD to use in EG method
	public BDD getAuxiliarBDD() {
		return bddCreator.getFac().one();
	}
	public List<BDD> getBDDPropositions(){
		return bddCreator.getListPropositionBDD();
	}
	public BDD getPredicatesBDD(BDD X){
		return bddCreator.getBDDPrepositionalX(X);
	}
	
}