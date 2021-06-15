package alu.ufc.regression;

import alu.ufc.action.Action;
import alu.ufc.bdd.BDDCreator;
import alu.ufc.file.ModelReader;
import alu.ufc.model.Grafo;
import alu.ufc.model.Vertex;
import alu.ufc.preference.Operator;
import alu.ufc.preference.Preference;
import com.sun.source.tree.NewArrayTree;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import java.net.http.HttpClient;
import java.util.*;

public class ModelChecker {

    List<BDD> lstRegression = new ArrayList<>();
    int indexlstRegression=0;
    private Preference preference;
    private Vector<Action> actionSet;
    private Grafo<BDD, Action> grafo = new Grafo<>();
    ModelReader model;
   // Hashtable<BDD, List<String>> path = new Hashtable<>();
    //private Hashtable<Integer,String> varTable;
    private BDD goalState;
    private BDD initialState;
    private BDD constraints;
    private BDD auxiliar;
    private BDD preferences;
    private List<BDD> listaProposition;

    private Hashtable<Integer, List<String> > politic = new Hashtable<>();
    private List<String> lstNameActionPolitic = new ArrayList<>();
    private int indexForPolitic=0;

    private Hashtable<Integer, List<BDD> > statesReached = new Hashtable<>();
    private List<BDD> lstStatesReached = new ArrayList<>();
    private int indexForStatesReached=0;

    private List<String> actionRelevante = new ArrayList<>();

    private String nameAction="";
    private int indexPolitic=0;

    private boolean isAlways = true;

    //private  Hashtable<String,Integer> varTable = new Hashtable<String,Integer>();
    /* Constructor */
    public ModelChecker(ModelReader model) {
        this.model = model;
        this.actionSet = model.getActionSet();
        this.initialState = model.getInitialStateBDD();
        this.goalState = model.getGoalSpec();
       // this.constraints = model.getConstraints();
        this.preference = model.getPreference();
        this.auxiliar = model.getAuxiliarBDD();
        this.listaProposition = model.getBDDPropositions();
        //politic.put(0,"meta");
    }
	
	public BDD run() {

		if(preference.getOperator() == Operator.ALWAYS) {
            return satEU(this.initialState, this.goalState, preference.getBddProposition() );
			//return satEU(satEG(preference.getBddProposition()), this.goalState);
		}
       // System.out.println("Aqui "+ actionSet.size());
       //rr BDD bdd = preference.getBddProposition();
		//return satEU(satEF(preference.getBddProposition()), this.goalState);

        return null;// satEU(satEF(preference.getBddProposition()), this.goalState);
	}

    public BDD satEF(BDD phi) {//BDD phi

        System.out.println("Computando EF");
        phi.printDot();
        BDD reached = phi;
        BDD Z = reached.id(); // Only new states reached
        Z.printDot();
        BDD aux;
        int i = 0;

        while (Z.isZero() == false) {

            aux = Z.and(initialState.id());
          //  System.out.println("and z - initialState");
           // Z.and(initialState.id()).printDot();

           // aux.free();
            aux = Z;

            Z = regression(Z);


            aux.free();

            aux = Z;

            System.out.println("i = " + i);
            System.out.println("Aqui -> ");
            Z.printDot();

            System.out.println("Reached -> ");
            reached.printDot();
            // The new reachable states in this layer
            Z = Z.apply(reached, BDDFactory.diff);
            aux.free();

            System.out.println("ABDDFactory.diff ");
            Z.printDot();
            aux = reached;

            //Union with the new reachable states
            reached = reached.or(Z);
            aux.free();
            i++;
        }
        System.out.println("SatEF");
        reached.printDot();
        return reached;
    }

/*	public BDD satEG(BDD phi) {
		BDD X = phi;
		BDD Y = auxiliar; //One BDD -- any initialization
		BDD reg;
		while(X.equals(Y) == false) {
			Y = X;
			reg = regression(X); 
			if(reg == null) {
				return X;
			} else {
				X = X.and(reg);
			}
		}
		return X;
	}*/



    /*
    * phi - estado inicial
    * psi - meta
    * */
    public BDD satEU(BDD phi, BDD psi, BDD pref) {
        BDD alwaysPrefe = pref;
        System.out.println("Computando EU");
        BDD W = phi.id();
        BDD meta = psi.id();
       // BDD X = null; // -- valor empty (constante).
        BDD reached = psi.id();
        BDD reg = reached.id();

        BDD aux;

        int j=0;

        reg= getRepresentationPropositionalSetX(reg);
        lstStatesReached.add(reg);
        statesReached.put(0,lstStatesReached);

        while (reg.isZero() == false){
           System.out.println("camada " + indexPolitic);


           reg = regression(reg); //Y

           aux = reg;

           reg.printDot();

           reg = reg.apply(reached, BDDFactory.diff);
            System.out.println("Aqui ---> ");
            reg.printDot();
           if (!reg.apply(initialState.id(),BDDFactory.and).isZero() && !reg.apply(meta.id(),BDDFactory.and).isZero()){
                System.out.println("Estado inicial alcançado. ");
                reached= reached.or(reg);
                return reg;
            }

            aux.free();
            aux= reached;
            reached= reached.or(reg);

          // reached.printDot();
           aux.free();
           indexPolitic++;

        }

        System.out.println("valor de i " + indexPolitic);
        reg.printDot();

        return reg;
    }

    /* Deterministic Regression of a formula by a set of actions */
    public BDD regression(BDD formula) {//BDD formula

        BDD regFraca = null;
        BDD teste = null, regForte = null, teste2 = null;
        BDD aux = null;

        for (Action a : actionSet) {

                teste = regressionFraca(formula, a);

                if (regFraca == null) {

                        regFraca = teste;

                    // regForte = teste2;
                } else {
                    //if (regFraca.and(preference.getBddProposition()).isOne())

                          regFraca = regFraca.xor(teste);
                       //   path.put(teste,a.getName());
                    // regForte.orWith(teste2);
                }

            }
        //}
      //  lstRegression.add(indexlstRegression,regFraca);
       // actionRelevante.add(indexPolitic, nameAction);
      //  politic.put(indexPolitic,nameAction);
        nameAction="";
       // indexlstRegression++;


        return regFraca;
    }
    private  void printRegression(){
        System.out.println("Tamanho da regressão: " + lstRegression.size());

        for (BDD bdd: lstRegression
             ) {
            bdd.printDot();
        }
    }

    /* Propplan regression based on action: Qbf based computation */
    public BDD regressionFraca(BDD Y, Action a) {

        List<BDD> changeSet = new ArrayList<>();

        //checks whether the effects of an action are relevant

     /*   System.out.println("Nome da ação: " + a.getName());
        for (BDD bdd: a.getAndEffetBDD()){
            System.out.println("########################");
            bdd.printDot();
            System.out.println("######################## OU");
            a.getOrEffect(a.getAndEffetBDD()).printDot();
            System.out.println("########################");
        }*/
        BDD reg = Y.and(a.getOrEffect(a.getAndEffetBDD())); //(Y ^ effect(a))

        if (reg.isZero() == false) {

            if (nameAction.equals(""))
               nameAction= a.getName();
            else
                 nameAction = nameAction + ": "+ a.getName();

            changeSet.addAll(a.modifyAction());
            for (BDD bdd : changeSet) {
                reg = reg.exist(bdd);//qbf computation
            }

            reg = reg.and(a.getPreCondictionBDD()); //precondition(a) ^ E changes(a). test


            System.out.println("Name action :" + a.getName());
            reg.printDot();
        }


        return reg;
    }

  /*  public BDD regressionForte(BDD Y, Action a) {

        // stores the conjunction of the propositions
        List<BDD> listAndBDD = new ArrayList<>();
        //List<BDD> listAndBDD = new ArrayList<>();
        List<BDD> changeSet = new ArrayList<>();
        BDD reg;// aux


        System.out.println("Nome da ação : " + a.getName());
        listAndBDD = List.copyOf(a.getAndEffetBDD());


        //checks whether the effects of an action are relevant
        reg = a.getOrEffect(listAndBDD).imp(Y); //(Y ^ effect(a))


        System.out.println("Name action :" + a.getName());
        System.out.println("Precondiction :");
        a.getPreCondBDD().printDot();


        if (reg.isZero() == false) {

            changeSet.addAll(a.modifyAction());
            for (BDD bdd : changeSet) {
                reg = reg.forAll(bdd);//qbf computation
            }
            reg = reg.and(a.getPreCondBDD()); //precondition(a) ^ E changes(a). test
        }

        return reg;
    }*/
    // Propositional representation of a set X
    public BDD getRepresentationPropositionalSetX( BDD formulaInitial){
        int[] var = formulaInitial.varProfile();
        BDD aux=null;
        for (int i = 0; i < listaProposition.size(); i++) {
            if (i==0){
                if (var[i]==1)
                    aux= listaProposition.get(i);
                else
                    aux = listaProposition.get(i).not();
            }else {
                if (var[i] == 1)
                        aux = aux.and(listaProposition.get(i));
                else {
                        aux = aux.and(listaProposition.get(i).not());
                    }
                }
            }
       // System.out.println("estado X ");
       // aux.printDot();
        return aux;

    }
    private  void politica(){
        Set<Integer> key = politic.keySet();
        for (Integer index : key
        ) {
            System.out.println("Index " + index + " Action " + politic.get(index));
        }
    }


    // public BDD getRepresentationPropositionalSetX1( BDD formulaInitial){
  //      BDD aux=null;
   //     BDD meta = getRepresentationPropositionalSetX(initialState.id());

  //      return aux;

  //  }

}
