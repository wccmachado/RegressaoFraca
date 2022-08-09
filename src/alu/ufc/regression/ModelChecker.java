package alu.ufc.regression;

import alu.ufc.action.Action;
import alu.ufc.bdd.BDDCreator;
import alu.ufc.file.ModelReader;
import alu.ufc.model.Grafo;
import alu.ufc.model.Vertex;
import alu.ufc.preference.AlwaysPreference;
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
    private  AlwaysPreference alwaysPreference;
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

    private BDD preferenceBDD;

    private Hashtable<Integer, List<String>> politic = new Hashtable<>();
    private List<String> lstNameActionPolitic = new ArrayList<>();


    private Hashtable<Integer, List<BDD>> statesReached = new Hashtable<>();
    private List<BDD> lstStatesReached = new ArrayList<>();

    private String nameAction = "";

    private boolean isRegFraca = true;

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
    //    this.preferenceBDD = model.getPredicatesBDD(preference.getBddProposition());
        alwaysPreference = new AlwaysPreference(this.actionSet,this.goalState,this.initialState,this.constraints,this.auxiliar);
    }

    public BDD run() {
        try {
            if (preference.getOperator() == Operator.always) {
                long start = System.currentTimeMillis();
               // alwaysPreference.satEG(preference.getBddProposition());
                BDD out  =   alwaysPreference.satEU(preference.getBddProposition(),this.goalState);
               // alwaysPreference.satAG(out);
               // BDD out  =   alwaysPreference.satAU(preference.getBddProposition(),this.goalState);
                long elapsed = System.currentTimeMillis() - start;
             //   alwaysPreference.print();
                System.out.println("Tempo de execução: " + elapsed+ "ms");
                return out;
            }
        }catch (NullPointerException ex){
            System.out.println("Preferência nula: Preferência não pertence ao conjunto P" + ex.getCause() + " problema " + ex.getMessage());
        }

       return null;// sa'tEU(satEF(preference.getBddProposition()), this.goalState);
    }

    public BDD satEF(BDD phi) {//BDD phi

        System.out.println("Computando EF");
        BDD reached = phi;
        BDD Z = reached.id(); // Only new states reached
        BDD aux;
        int i = 0;

        while (Z.isZero() == false) {

            System.out.println("i = " + i);
            aux = Z.and(initialState.id());

            aux.free();
            aux = Z;
            Z = regression(Z);
            aux.free();

            aux = Z;
            Z = Z.apply(reached, BDDFactory.diff); // The new reachable states in this layer
            aux.free();

            aux = reached;
            reached = reached.or(Z); //Union with the new reachable states
            aux.free();

			/*aux = reached;
			reached = reached.and(constraints);
			aux.free();*/

            i++;
        }
        System.out.println("SatEF");
      //  reached.printDot();
        return reached;
    }

    public BDD satEG(BDD phi) {

        BDD X = phi;

        BDD Y = auxiliar; //One BDD -- any initialization
        BDD reg;

        while (X.equals(Y) == false) {
            Y = X;
            reg = regression(X);
          //  reg.printSet();
            if (reg == null) {
                return X;
            } else {
                X = X.and(reg);
            }

        }
        return X;
    }

    /*
     * phi - estado inicial
     * psi - meta
     * */
    public BDD satEU(BDD phi, BDD psi) {

        BDD W = phi;
        BDD Y = psi;
        BDD X = null; // -- valor empty (constante).
        BDD reg;
        BDD aux;
        int i = 0;
        System.out.println("Computando EU");

        while ((X == null) || (X.equals(Y)==false)) {
            System.out.println("camada " + i);
            X = Y;
            reg = regression(Y);

            if (reg == null) {
                return Y;
            } else {
                Y = Y.or(W.and(reg));
            }
            i++;
            System.out.println("Camada: " + i);

        }

        System.out.println("Quantidade de passos: " + i);

        return Y;
    }

    /* Deterministic Regression of a formula by a set of actions */
    public BDD regression(BDD formula) {//BDD formula
        BDD aux = null;
        BDD regFraca = null;
        BDD regForte = null;
        BDD output = null;

        if (isRegFraca == true) {
            for (Action a : actionSet) {
                aux = regressionFraca(formula, a);
              //  System.out.println("printSet ");
              //  aux.printSet();
                if (regFraca == null) {
                    regFraca = aux;

                } else {
                    regFraca = regFraca.orWith(aux);
                    output = regFraca;

                }
            }
        }else{
            for (Action a : actionSet) {
                aux = regressionForte(formula, a);

                if (regForte == null) {
                    regForte = aux;

                } else {
                    regForte = regForte.orWith(aux);
                    output = regForte;
                }
            }

        }

        nameAction = "";

        return output;
    }

    /* Regression based on action*/
    public BDD regressionFraca(BDD Y, Action a) {
        BDD reg, aux;

        reg = Y.and(a.getOrAndEffect());// (Y ^ effect(a)) reg.apply(Y, BDDFactory.diff).equals(a.getOrAndEffect())

        if (reg.isZero()==false) {

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
        BDD reg;// aux

        reg = a.getOrAndEffect().imp(Y).and(constraints);

        reg = reg.apply(Y, BDDFactory.diff);


        if (reg.isZero() == false) {
            for (BDD bdd : a.modifyAction()) {
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


     /*public BDD getRepresentationPropositionalSetX( BDD formulaInitial){
        BDD aux=null, representation=null;
         for (int i = 0; i < formulaInitial.allsat().size(); i++) {
              byte[] arrByte = (byte[]) formulaInitial.allsat().get(i);
             for (int j = 0; j < arrByte.length ; j++) {
                    if (arrByte[j]==-1){
                        if (j==0) {
                            aux = formulaInitial.getFactory().ithVar(0).not();
                        }else {
                            aux= aux.and(formulaInitial.getFactory().ithVar(j).not());
                        }
                 }else{
                        if (j==0) {
                            aux = formulaInitial.getFactory().ithVar(0).not();
                        }else {
                            if (arrByte[j] == 0) {
                                aux = aux.and(formulaInitial.getFactory().ithVar(j).not());
                            } else {
                                aux = aux.and(formulaInitial.getFactory().ithVar(j));
                            }
                        }
                    }
             }
             if (representation == null){
                 representation= aux;
             } else
                 representation = representation.or(aux);
         }
        return representation;
    }*/



    /*  private  void printRegression(){
      System.out.println("Tamanho da regressão: " + lstRegression.size());
      for (BDD bdd: lstRegression
      ) {
          bdd.printDot();
      }
  }
*/

}
