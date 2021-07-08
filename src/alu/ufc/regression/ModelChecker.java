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
    int indexlstRegression = 0;
    private Preference preference;
    private Vector<Action> actionSet;
    private ModelReader model;

    private BDD goalState;
    private BDD initialState;
    private BDD constraints;
    private BDD auxiliar;
    private BDD preferences;
    private BDD init;
    private List<BDD> listaProposition;

    private Hashtable<Integer, List<String>> politic = new Hashtable<>();
    private List<String> lstNameActionPolitic = new ArrayList<>();
    private int indexForPolitic = 0;

    private Hashtable<Integer, List<BDD>> statesReached = new Hashtable<>();
    private List<BDD> lstStatesReached = new ArrayList<>();
    private int indexForStatesReached = 0;

    private List<String> actionRelevante = new ArrayList<>();

    private String nameAction = "";
    private  boolean isRegeForte = true;


    private boolean isAlways = true;

    //private  Hashtable<String,Integer> varTable = new Hashtable<String,Integer>();
    /* Constructor */
    public ModelChecker(ModelReader model) {
        this.model = model;
        this.constraints = model.getConstraints();
        this.actionSet = model.getActionSet();
        this.initialState = model.getInitialStateBDD();
        this.init = model.getInitBDD();
        this.goalState = model.getGoalSpec();
        this.preference = model.getPreference();
        this.auxiliar = model.getAuxiliarBDD();
        this.listaProposition = model.getBDDPropositions();
    }

    public BDD run() {

        if (preference.getOperator() == Operator.ALWAYS) {
            return satEF(goalState);
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

            // aux = Z.and(initialState.id());
            //  System.out.println("and z - initialState");
            // Z.and(initialState.id()).printDot();

            // aux.free();
            //  aux = Z;

            Z = regression(Z);


            //aux.free();

            aux = Z;

            System.out.println("i = " + i);
            System.out.println("Aqui regressao -> ");
            Z.printSet();
            //Z.printDot();

            System.out.println("Reached -> ");
            reached.printSet();
            //reached.printDot();
            // The new reachable states in this layer
            Z = Z.apply(reached, BDDFactory.diff);
            aux.free();

            System.out.println("regressao - reached ");
            Z.printSet();
            // Z.printDot();
            aux = reached;

            //Union with the new reachable states
            System.out.println("Union with the new reachable states: ");
            reached = reached.or(Z).and(constraints);
            reached.printSet();
            aux.free();
            i++;
        }
        System.out.println("SatEF");
        // Era apenas reached.printDot.
        reached.and(constraints).printSet();

        // reached.printDot();
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
    public BDD satEU(BDD phi, BDD psi) {
        // BDD alwaysPrefe = pref;

        BDD W = phi.id();
        BDD meta = psi.id();
        BDD X = null; // -- valor empty (constante).
        BDD reg;
        BDD aux;
        int i = 0;


        int j = 0;
        System.out.println("Computando EU");

        while ((X == null) || (!X.equals(meta))) {
            System.out.println("camada " + i);
            X = meta;

            // aux = reg;
            meta.printSet();
            reg = regression(meta); //Y
            System.out.println("regressão na camada: " + i);
            reg.printSet();
            //   reg = reg.apply(meta, BDDFactory.diff);
            // meta = meta.apply(reg,BDDFactory.diff);
            if (reg == null) {
                return meta;
            } else {
                meta = meta.or(W.and(meta));
                System.out.println("Meta: ");
                meta.printSet();
            }
            i++;

        }
        System.out.println("Y");
        meta.printSet();
        System.out.println("Não achou");


        System.out.println("Quantidade de passos: " + i);

        return meta;
    }

    /* Deterministic Regression of a formula by a set of actions */
    public BDD regression(BDD formula) {//BDD formula
        BDD auxteste = null;
        BDD regFraca = null;
        BDD teste = null, regForte = null, teste2 = null;
        BDD aux = null;


        for (Action a : actionSet) {


           // teste = regressionFraca(formula, a);
            teste2 = regressionForte(formula, a);

          //  aux = teste;
            //\teste = teste.and(constraints);
            teste2 = teste2.and(constraints);
            //aux.free();

            if (regFraca == null) {
                //regFraca = teste;
                regForte= teste2;
            } else {
              //  regFraca = regFraca.orWith(teste);
                regForte = regForte.orWith(teste2);

            }

        }

        nameAction = "";
     /*   if (isRegeForte == true) {
            System.out.println("Regressao forte:  ---> ");
            regForte.and(constraints).printSet();
            return regForte.and(constraints);
        }
*/

      //  return regFraca.and(constraints);
        return regForte.and(constraints);
    }

    /* Regression based on action*/
    public BDD regressionFraca(BDD Y, Action a) {

        BDD reg, aux;

        reg = Y.and(a.getTeste());// (Y ^ effect(a))
        aux = reg;
        reg = reg.and(constraints);
        aux.free();


        if (reg.isZero() == false) {

            System.out.println("Name action relevant:" + a.getName());
            aux = reg;
            for (BDD bdd : a.modifyAction()) {
                reg = reg.exist(bdd);//qbf computation
            }
            aux.free();

            reg = reg.and(a.getPreCondictionBDD()); //precondition(a) ^ E changes(a). test


        }


        return reg;
    }

      public BDD regressionForte(BDD Y, Action a) {

          // stores the conjunction of the propositions
          List<BDD> listAndBDD = new ArrayList<>();
          //List<BDD> listAndBDD = new ArrayList<>();
          List<BDD> changeSet = new ArrayList<>();
          BDD reg;// aux



         // listAndBDD = List.copyOf(a.getAndEffetBDD());


          //checks whether the effects of an action are relevant
          //reg = a.getOrEffect(listAndBDD).imp(Y); //(Y ^ effect(a))
          reg = a.getTeste().imp(Y);

          //System.out.println("Name action :" + a.getName());
          //System.out.println("Precondiction :");
          //a.getPreCondictionBDD().printDot();


          if (reg.isZero() == false) {

              changeSet.addAll(a.modifyAction());
              for (BDD bdd : changeSet) {
                  reg = reg.forAll(bdd);//qbf computation
              }
              reg = reg.and(a.getPreCondictionBDD()); //precondition(a) ^ E changes(a). test
              System.out.println("Nome da ação : " + a.getName());
          }

          return reg;
      }

    private void politica() {
        Set<Integer> key = politic.keySet();
        for (Integer index : key
        ) {
            System.out.println("Index " + index + " Action " + politic.get(index));
        }
    }

    private BDD notGoal(BDD goal) {
        BDD aux = null;

        for (int i = 0; i < goal.scanSet().length; i++) {
            if (aux == null) {
                aux = goal.getFactory().nithVar(goal.scanSet()[i]);
            } else {
                aux = aux.and(goal.getFactory().nithVar(goal.scanSet()[i]));
            }
        }
        return aux;
    }

    /*private boolean isEffectRelevant(BDD eff , BDD Y) {
        boolean isEffectRelevantTemp = false;

        effect:
        {
            for (int i = 0; i < eff.scanSet().length; i++) {
                for (int j = 0; j < Y.scanSet().length; j++) {
                    if (!(eff.getFactory().ithVar(eff.scanSet()[i]).equals(Y.getFactory().ithVar(Y.scanSet()[j])))) {
                        isEffectRelevantTemp = false;
                        if (j == Y.scanSet().length - 1)
                            break;
                    } else {
                        isEffectRelevantTemp = true;
                        break;
                    }

                }
            }

        }

    return  isEffectRelevantTemp;
    }
*/


     public BDD getRepresentationPropositionalSetX( BDD formulaInitial){
        BDD aux=null;
         for (int i = 0; i < formulaInitial.allsat().size(); i++) {
             formulaInitial.allsat().get(i);
            // if (formulaInitial.allsat().get())
         }

        return aux;

    }


    /*  private  void printRegression(){
      System.out.println("Tamanho da regressão: " + lstRegression.size());

      for (BDD bdd: lstRegression
      ) {
          bdd.printDot();
      }
  }
*/

}
