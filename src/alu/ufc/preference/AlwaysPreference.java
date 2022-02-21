package alu.ufc.preference;

import alu.ufc.action.Action;
import alu.ufc.file.ModelReader;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import javax.swing.*;
import java.util.*;

public class AlwaysPreference {

    List<BDD> lstRegression = new ArrayList<>();
    int indexlstRegression = 0;

    private Vector<Action> actionSet;

    private transient BDD goalState;
    private transient BDD initialState;
    private transient BDD constraints;
    private transient BDD auxiliar;
    private transient BDD preference;
    private boolean isSatEU = false;

    private boolean isRegFraca = true;
    private boolean isVisitEG= false;
    int i = 0;

    private transient List<String> lstAction;
    private transient  Hashtable<Integer,List<String>> hstOutput =  new Hashtable<>();

    public AlwaysPreference(Vector<Action> actionSet, BDD goalState, BDD initialState, BDD constraints, BDD auxiliar) {
        this.actionSet = actionSet;
        this.goalState = goalState;
        this.initialState = initialState;
        this.constraints = constraints;
        this.auxiliar = auxiliar;
    }

    public Hashtable<Integer, List<String>> getHstOutput() {
        return hstOutput;
    }

    public void setHstOutput(Hashtable<Integer, List<String>> hstOutput) {
        this.hstOutput = hstOutput;
    }

    public BDD satEG(BDD phi) {
        BDD X = phi;
        BDD Y = auxiliar; //One BDD -- any initialization
        BDD reg;


        while (X.equals(Y) == false) {
            Y = X;

            reg = regression(X);

            if (reg == null) {
                return X;
            } else {
                System.out.println("Aqui");
                reg.printDot();
                X = X.and(reg);
            }

        }
       // isVisitEG=true;
        return X;
    }

    public BDD satEU(BDD phi, BDD psi) {
        //   isSatEU=true;
        BDD W = phi;
        BDD Y = psi;
        BDD X = null; // -- valor empty (constante).
        BDD reg;
        BDD aux;

        System.out.println("Computando EU");

        while ((X == null) || (X.equals(Y) == false)) {
            X = Y;
            reg = regression(Y); //Y

            if (reg == null) {
                return Y;
            } else {
                Y = Y.or(W.and(reg));
            }
            hstOutput.put(i,lstAction);
            i++;
        }
        System.out.println("Quantidade de passos: " + i);

        return Y;
    }

    /* Deterministic Regression of a formula by a set of actions */
    public BDD regression(BDD formula) {//BDD formula
        BDD aux = null;
        BDD aux2 = null;
        BDD regFraca = null;
        BDD regForte = null;
        BDD output = null;
        lstAction = new ArrayList<>();
        for (Action a : actionSet) {
            if (isRegFraca == true) {
                aux = regressionFraca(formula, a);
            } else {
                aux = regressionForte(formula, a);
            }
            if (output == null) {
                output = aux;
            } else {
                output = output.orWith(aux);
            }
        }
        return output;
    }

    /* Regression based on action*/
    public BDD regressionFraca(BDD Y, Action a) {
        BDD reg = null, aux = null;
        reg = Y.and(a.getOrAndEffect());// (Y ^ effect(a)) reg.apply(Y, BDDFactory.diff).equals(a.getOrAndEffect())


        if (reg.isZero() == false) {
          // if (isVisitEG ){
              System.out.println("Precondiction" +"\n"+ a.getPreCondictionBDD()+ "*****");
              System.out.println("Action " + a.getName());
           //   lstAction.add(a.getName());
          // }


            aux = reg;
            for (BDD bdd : a.modifyAction()) {
                reg = reg.exist(bdd);//qbf computation
            }
            aux.free();

            reg = reg.and(a.getPreCondictionBDD()); //precondition(a) ^ E changes(a). test

        }
        return reg.and(constraints);
    }

    public BDD regressionForte(BDD Y, Action a) {
        BDD reg;// aux

        reg = a.getOrAndEffect().imp(Y);

        if (reg.isZero() == false) {
            for (BDD bdd : a.modifyAction()) {
                reg = reg.forAll(bdd);//qbf computation
            }
            if (reg.isZero() == false)
                System.out.println("Name action relevant:" + a.getName());

            reg = reg.and(a.getPreCondictionBDD()); //precondition(a) ^ E changes(a). test
        }
        return reg.and(constraints);
    }


    public void print(){
        Set<Integer> elements= hstOutput.keySet();
        for (Integer index: elements
             ) {
            int idx =(elements.size()-1)-index;
            System.out.println("**************************  regressão na camada: " + idx +" *********************");
            List<String> lstAction = hstOutput.get((elements.size()-1)-index);
            for (String s : lstAction) {
                 System.out.println(s);
            }
        }
    }

}
