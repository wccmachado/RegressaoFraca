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

    private BDD goalState;
    private BDD initialState;
    private BDD constraints;
    private BDD auxiliar;
    private BDD preference;
    private boolean isSatEU= false;

    private boolean isRegFraca = false;


    public AlwaysPreference(Vector<Action> actionSet, BDD goalState, BDD initialState, BDD constraints, BDD auxiliar) {
        this.actionSet = actionSet;
        this.goalState = goalState;
        this.initialState = initialState;
        this.constraints = constraints;
        this.auxiliar = auxiliar;
    }

    public BDD satEG(BDD phi) {
        preference = phi;
        BDD aux = null;
        BDD X = phi;

        BDD Y = auxiliar; //One BDD -- any initialization
        BDD reg;
        int i = 1;
        while (X.equals(Y) == false) {
            Y = X;
            System.out.println("Quantidade de regressoes " + i);
            reg = regression(X);
            reg.printSet();
           // System.out.println("Aqui ---> ");
            //aux.printSet();
            if (reg == null) {
                return X;
            } else {
                //X = X.and(reg);
                X= reg;
            }
            i++;
        }
        return X;
    }

    public BDD satEU(BDD phi, BDD psi) {
     //   isSatEU=true;
        BDD W = phi.id();
        BDD Y = psi.id();
        BDD X = null; // -- valor empty (constante).
        BDD reg;
        BDD aux;
        int i = 0;

        int j = 0;
        System.out.println("Computando EU");

        while ((X == null) || (!X.equals(Y))) {
            System.out.println("camada " + i);
            X = Y;

            // aux = reg;
            Y.printSet();
            reg = regression(Y); //Y

            System.out.println("regressão na camada: " + i);

            reg.printSet();

            if (reg == null) {
                return Y;
            } else {
                Y = Y.or(W.and(Y));
            }
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


            for (Action a : actionSet) {
                if (isRegFraca == true) {
                    aux = regressionFraca(formula, a);
                }else{
                    aux = regressionForte(formula, a);
                }
                //if (isSatEU == false) {
                 //   aux2 = aux.and(preference);
                   // if (aux2.equals(aux)) {
                        if (regFraca == null) {
                            regFraca = aux;
                        } else {
                            regFraca = regFraca.orWith(aux);
                        }
                        output = regFraca;
                  //  }
               // }
           // }
        /*}else{
            for (Action a : actionSet) {
                aux = regressionForte(formula, a);
               // aux2 = aux.and(preference);
              //  if (aux2.equals(aux)) {
                    if (regForte == null) {
                        regForte = aux;

                    } else {
                        regForte = regForte.orWith(aux);
                    }
                    output = regForte;
                }
           // }*/

        }

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
        return reg.and(constraints);
    }

    public BDD regressionForte(BDD Y, Action a) {
        BDD reg;// aux

        reg = a.getOrAndEffect().imp(Y);

        if (reg.isZero() == false) {
            for (BDD bdd : a.modifyAction()) {
                reg = reg.forAll(bdd);//qbf computation
            }
            if (reg.isZero()==false)
                System.out.println("Name action relevant:" + a.getName());

                reg = reg.and(a.getPreCondictionBDD()); //precondition(a) ^ E changes(a). test
        }
        return reg.and(constraints);
    }

}
