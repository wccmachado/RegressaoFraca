package alu.ufc.bdd;

import alu.ufc.action.Action;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BDDCreator {
	//Number of propositions (including variables v and v')
	private int propNum = 0;
	private transient BDD initialStateBDD;
	private transient BDD goalBDD;
	private transient BDD constraintBDD=null;
	private transient BDD initBDD ;

	//private transient Action action;
	private transient BDD preCondiction;
	private transient BDD effect;


	private Vector<Integer> propGoal = new Vector<Integer>();
			
	//Associates the name of the variable to its position in the BDD VariableSet
	private transient Hashtable<String,Integer> varTable = new Hashtable<String,Integer>();
	//private transient Hashtable<Integer,String> varTable2 = new Hashtable<Integer,String>();
	private List<BDD> listPropositionBDD = new ArrayList<>();


	private Vector<Action> actionsSet = new Vector<Action>();
	private BDDFactory fac;

	public BDD getConstraintBDD() {
		return constraintBDD;
	}

	public void setConstraintBDD(BDD constraintBDD) {
		this.constraintBDD = constraintBDD;
	}

	/***** Get Methods *******/

	public BDDCreator(int nodenum, int cachesize){
		//fac = JFactory.init(9000000, 9000000);	
		fac = JFactory.init(nodenum, cachesize);
	}

	/*
	*
	* return precondictions of an action as BDD list
	*
	* */

	public BDD getEffect() {
		return effect;
	}

	public void setEffect(BDD effect) {
		this.effect = effect;
	}

	public List<BDD> getPreCondictionBDD(Action action) {
		List<BDD> aux = new ArrayList<>();
		Set<String> names = varTable.keySet();
		Iterator<String> iterator = names.iterator();
		while (iterator.hasNext()){
			String key = iterator.next();
			for (String precondiction: action.getListPropositionPreCondition()
			) {
				if (precondiction.equals(iterator)){
					aux.add(fac.ithVar(varTable.get(key)));
				}
			}
		}
		return aux;
	}

	public BDD getPreCondictionBDD(List<String> preCondictions){
		List<String> lstAux = preCondictions;
		BDD bdd=null;
		String prop;
		int index;

		for (String precondiction: lstAux
			 ) {
			if (bdd == null){
				if(precondiction.startsWith("~")) {
					prop = precondiction.substring(1); // without the signal ~
					index = varTable.get(prop);
					bdd = fac.nithVar(index);
				} else {
					index = varTable.get(precondiction);
					bdd = fac.ithVar(index);
				}
			}else{
				if(precondiction.startsWith("~")) {
					prop = precondiction.substring(1);  // without the signal ~
					index = varTable.get(prop);
					bdd.andWith(fac.nithVar(index));
				} else {
					index = varTable.get(precondiction);
					bdd.andWith(fac.ithVar(index));
				}
			}
		}
		return bdd;
	}
	public List<BDD> getEffectBDD(Action action) {
		List<BDD> aux = new ArrayList<>();
		Set<String> names = varTable.keySet();
		Iterator<String> iterator = names.iterator();
		while (iterator.hasNext()){
			String key = iterator.next();
			for (String precondiction: action.getListPropositionEffect()
			) {
				if (precondiction.equals(iterator)){
					aux.add(fac.ithVar(varTable.get(key)));
				}
			}
		}
		return aux;
	}

	public int getPropNum(){
		return propNum;
	}
	
	public Hashtable<String,Integer> getVarTable(){
		return varTable;
	}
	

	public BDD getInitiaStateBDD(){
		return initialStateBDD;
	}
	
	public BDD getGoalBDD(){
		return goalBDD;
	}


	public BDD getInitBDD() {
		return initBDD;
	}

	public Vector<Action> getActionsSet() {
		return actionsSet;
	}
	

	/*[input: just the actions] Initializes the BDD variables table with the propositions, propositions primed and actions*/
	public void initializeVarTable(String propLine) throws IOException {
		StringTokenizer tknProp = new StringTokenizer(propLine, ",");

		propNum = tknProp.countTokens(); //Propositions
		System.out.println("quantidade de proposições : " + propNum);
		fac.setVarNum(propNum);
		
		//Filling the table positions corresponding to the propositions
		String propName = "";
		for (int i = 0; i < propNum; i++) {
			propName = tknProp.nextToken();
			varTable.put(propName,i);
		}
		
	}
	public List<BDD> getListPropositionBDD(){
		for (int i = 0; i < varTable.size(); i++) {
			listPropositionBDD.add(fac.ithVar(i));
		}
		return listPropositionBDD;
	}

	/**Creates a BDD representing the initial state.*/
	public void createInitialStateBdd(String readLine){
		initialStateBDD = createAndBdd(readLine);
	}
	/**Creates a BDD representing the prepositions init.*/
	public void createInitBdd(String readLine){
		initBDD = createAndBdd(readLine);
	}
	
	/**Creates a BDD representing the goal **/
	public void createGoalBdd(String readLine){
		goalBDD = createAndBdd(readLine);
		getGoalPropositions(readLine);
	}
	
	public BDD createPreferenceBdd(String proposition) {
		int index = varTable.get(proposition);
		System.out.println("index: " + index);
		return fac.ithVar(index);
	}
			
	/**Creates a vector of propositions indexes in the goal **/
	public void getGoalPropositions(String readLine){
		StringTokenizer tkn = new StringTokenizer(readLine, ",");
		String tknPiece = ""; 
		String prop; 
		int index;
	
		while(tkn.hasMoreTokens()) {
			tknPiece = tkn.nextToken().trim();
			if(tknPiece.startsWith("~")){
				prop = tknPiece.substring(1);  // without the signal ~
				index = varTable.get(prop);
			}else{
				index = varTable.get(tknPiece);
			}
			propGoal.add(index);		
		}
	}


	public BDDFactory getFac() {
		return fac;
	}

	public void setFac(BDDFactory fac) {
		this.fac = fac;
	}

	public List<BDD> createAndBddWithEffectUncertain(Hashtable<Integer,List<String>> hsteffect) {
		List<BDD> listEffect = new ArrayList<>();
		String prop;
		int index;

		BDD bdd = null;
		//effect=null;
		Set<Integer> key = hsteffect.keySet();

		for (Integer indice: key ) {
			for (String effectProposition: hsteffect.get(indice)	) {
				if (bdd == null){
					if(effectProposition.startsWith("~")) {
						prop = effectProposition.substring(1); // without the signal ~
						index = varTable.get(prop);
						bdd = fac.nithVar(index);
						//effect= bdd;

					} else {
						index = varTable.get(effectProposition);
						bdd = fac.ithVar(index);


					}
					//effect= bdd;
				}else{
					if(effectProposition.startsWith("~")) {
						prop = effectProposition.substring(1);  // without the signal ~
						index = varTable.get(prop);
						bdd.andWith(fac.nithVar(index));
					} else {
						index = varTable.get(effectProposition);
						bdd.andWith(fac.ithVar(index));
					}

				}
			}

			listEffect.add(bdd);
			bdd = null;
		}

		return listEffect;
	}

	public BDD createOrAndBddWithEffectUncertain(Hashtable<Integer,List<String>> hsteffect) {
		List<BDD> listEffect = new ArrayList<>();
		String prop;
		int index=0;

		BDD andBdd = null;
		BDD orBdd = null;
		//effect=null;
		Set<Integer> key = hsteffect.keySet();

		for (Integer indice: key ) {
			for (String effectProposition: hsteffect.get(indice)	) {
				if (andBdd == null){
					if(effectProposition.startsWith("~")) {
						prop = effectProposition.substring(1); // without the signal ~
						index = varTable.get(prop);
						andBdd = fac.nithVar(index);
						//effect= bdd;

					} else {
						index = varTable.get(effectProposition);
						andBdd = fac.ithVar(index);


					}
					//effect= bdd;
				}else{
					if(effectProposition.startsWith("~")) {
						prop = effectProposition.substring(1);  // without the signal ~
						index = varTable.get(prop);
						andBdd.andWith(fac.nithVar(index));
					} else {
						index = varTable.get(effectProposition);
						andBdd.andWith(fac.ithVar(index));

					}

				}
			}

		//	System.out.println(index);
			if (orBdd == null){
				orBdd = andBdd;
			}else
				orBdd = orBdd.xor(andBdd);

			listEffect.add(andBdd);
			andBdd = null;
		}

		return orBdd;
	}

	/** Create a BDD representing the conjunction of the propositions in readLine */
	public BDD createAndBdd(String readLine) {
		StringTokenizer tkn = new StringTokenizer(readLine, ",");
		String tknPiece = tkn.nextToken().trim();
		//System.out.println("modelReader "+ tknPiece);
		String prop; 
		int index;
		BDD bdd = null;
		
		//System.out.println("tknPiece: " + tknPiece);
		if(tknPiece.startsWith("~")) {
			prop = tknPiece.substring(1); // without the signal ~
			index = varTable.get(prop);
			bdd = fac.nithVar(index);
		} else {
			index = varTable.get(tknPiece);
			bdd = fac.ithVar(index);
		}
		while(tkn.hasMoreTokens()) {
			tknPiece = tkn.nextToken();
			//System.out.println("tknPiece: " + tknPiece);
			if(tknPiece.startsWith("~")) {
				prop = tknPiece.substring(1);  // without the signal ~
				index = varTable.get(prop);
				bdd.andWith(fac.nithVar(index));
			} else {
				index = varTable.get(tknPiece);
				bdd.andWith(fac.ithVar(index));
			}
		}
		//System.out.println("bdd: " + bdd);
		return bdd;
	}
	
	/** Create a BDD representing the conjunction of the propositions in readLine */
	public Vector<BDD> createBddVector(String readLine) {
		Vector<BDD> bddVector = new Vector<BDD>();
		String tknPiece;
		String prop; 
		int index;
		BDD bdd = null;
		StringTokenizer tkn = new StringTokenizer(readLine, ",");
	
		while(tkn.hasMoreTokens()){
			tknPiece = tkn.nextToken().trim();
			if(tknPiece.startsWith("~")){
				prop = tknPiece.substring(1); // without the signal ~
				index = varTable.get(prop);
				bdd = fac.nithVar(index);
			}else{
				index = varTable.get(tknPiece);
				bdd = fac.ithVar(index);
			}
			bddVector.add(bdd);
		}
		return bddVector;
	}
	public BDD createOrBddEffect(List<BDD> list) {

		BDD aux = null;
		for (BDD bdd: list
			 ) {
			if (aux==null){
				aux = bdd;
			}else
				aux=aux.orWith(bdd);
		}
		return aux;
	}
	
	/** Create a BDD representing the conjunction of the propositions in readLine */
	public BDD createOrBdd(String readLine) {
		StringTokenizer tkn = new StringTokenizer(readLine, ",");
		String tknPiece = tkn.nextToken().trim(); 
		int index;
		BDD bdd = null;
		
		index = varTable.get(tknPiece);
		bdd = fac.ithVar(index);
		
		while(tkn.hasMoreTokens()) {
			tknPiece = tkn.nextToken();
			index = varTable.get(tknPiece);
			bdd.orWith(fac.ithVar(index));
		}
		return bdd;
	}
	
	/*Creates a BDD that represents an exclusive or*/
	public BDD createExclusiveOrBdd(String line){
		StringTokenizer tkn = new StringTokenizer(line, ",");
		String tknPiece; 

		String beforeTkn, afterTkn;
		int init, end, index;
		BDD bdd, aux;
		BDD returnedBdd = null;
		
		while(tkn.hasMoreTokens()){
			tknPiece = tkn.nextToken();
			index = varTable.get(tknPiece);
			bdd = fac.ithVar(index);
			
			init = line.indexOf(tknPiece);
			end = init + tknPiece.length() + 1;
			
			beforeTkn = line.substring(0,init);
			afterTkn = line.substring(end);
			
			if(beforeTkn.equals("")){
				aux = createOrBdd(afterTkn).not();
				bdd.andWith(aux);
			}else if(afterTkn.equals("")){
				aux = createOrBdd(beforeTkn).not();
				bdd.andWith(aux);
			}else {	
				aux = createOrBdd(beforeTkn).not();
				bdd.andWith(aux);
				
				aux = createOrBdd(afterTkn).not();
				bdd.andWith(aux);
				 
			}			
	
			if(returnedBdd == null){
				returnedBdd = bdd;
			}else{
				returnedBdd.orWith(bdd);
			}
		}
		return returnedBdd;
	}
	
	public void addAction(Action action){
		actionsSet.add(action);
	}
	
	/** Create a BDD representing the conjunction of the propositions in List of effect */
	public List<BDD> createAndBdd(Hashtable<Integer, List<String>> effect) {

		List<String> listEffectAux = new ArrayList<>();
		List<BDD>  listAux = new ArrayList<>();

		String prop;
		int index, count=0, indexList=0;
		BDD bdd = null, aux=null, temp=null;
		for (Map.Entry<Integer, List<String>> e : effect.entrySet()){
			for (String proposition : e.getValue()) {
					if (proposition.startsWith("~")) {
						prop = proposition.substring(1); // without the signal ~
						index = varTable.get(prop);
						aux = fac.nithVar(index);
					} else {
						index = varTable.get(proposition);
						aux = fac.ithVar(index);
					}
					if (count == 0) {

						temp=aux;
					}
					else{

						temp.andWith(aux);
					}
					count++;

				}
				listAux.add(indexList,temp);
				count =0;
			indexList++;
			temp=null;

		}

		return listAux;
	}
	/** Create a BDD representing the conjunction of the propositions in List of effect */
	public BDD createOrBdd(List<BDD> conjunctionOfProposition) {
		BDD bdd=null;
		for (int i = 0; i < conjunctionOfProposition.size(); i++) {
			if (i==0){
				bdd=conjunctionOfProposition.get(i);
			}else{
				bdd.orWith(conjunctionOfProposition.get(i));
			}
		}
		System.out.println("Este é o  or de conjunction ");
		bdd.printDot();
		return bdd;
	}

	public BDD createAndBdd2( List<String> effect) {
		Hashtable<Integer, List<BDD>>  hstAux = new Hashtable<>();
		List<String> listEffectAux = new ArrayList<>();

		String prop;
		int index;
		int count=0;
		BDD bdd = null;


			for (String proposition: effect
			) {
				if (count == 0) {
					if (proposition.startsWith("~")) {
						prop = proposition.substring(1); // without the signal ~
						index = varTable.get(prop);
						bdd = fac.nithVar(index);
					} else {
						index = varTable.get(proposition);
						bdd = fac.ithVar(index);
					}

				} else {
					if (proposition.startsWith("~")) {
						prop = proposition.substring(1); // without the signal ~
						index = varTable.get(prop);
						bdd.andWith(fac.nithVar(index));
					} else {
						index = varTable.get(proposition);
						bdd.andWith(fac.ithVar(index));
					}
				}
				count++;

			}

		return bdd;
	}
	public BDD change(List<String> effect){
		BDD bdd=null, aux=null;
		String prop;
		int count=0, index=0;
		for (String proposition : effect) {
				if (proposition.startsWith("~")) {
						proposition = proposition.substring(1); // without the signal ~

				}
				index = varTable.get(proposition);
				aux = fac.ithVar(index);
				if (count==0)
					bdd=aux;
				else
					bdd.andWith(aux);

				count++;


		}
	//	System.out.println("Veja aqui a modifica ação: ");
		//bdd.printDot();
		return bdd;
	}
	public List<BDD> changeSet2(List<String> effect){
		List<BDD> list =  new ArrayList<>();
		BDD bdd=null, aux=null;
		String prop;
		int  index=0;
		for (String proposition : effect) {
			if (proposition.startsWith("~")) {
				proposition = proposition.substring(1); // without the signal ~

			}
			index = varTable.get(proposition);
			aux = fac.ithVar(index);
			if (!list.contains(aux))
			      list.add(aux);

		}
		return list;
	}
	/*Creates the constraint bdd */
	public void createConstraintBDD(String line){
		BDD constr = createExclusiveOrBdd(line);
		if(constraintBDD == null){
			constraintBDD = constr;
		}else{
			constraintBDD.andWith(constr);
		}
	}

	/* Create a BDD with all predicates*/
	public BDD getBDDPrepositionalX( BDD bdd){
		BDD aux = null;
		for (int i  = 0;  i <  varTable.size();  i++) {
			if (i==0)
				aux = fac.ithVar(i).not();
			else
				if (i==bdd.var())
					aux = aux.and(bdd);
				else
				   aux = aux.and(fac.ithVar(i).not());
		}
		return aux;
	}

}