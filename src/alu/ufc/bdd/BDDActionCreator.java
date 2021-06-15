package alu.ufc.bdd;

import alu.ufc.action.Action;
import net.sf.javabdd.BDD;

public class BDDActionCreator{
    private Action action;
    private BDDCreator creator;
    private String actionName;
    private String preCondiction;
    private String effect;

    public BDDActionCreator(Action action) {
        this.action = action;
    }

    public BDD getBDDEffect(){
        BDD efect = null;
        action.getListPropositionPreCondition();


        return  efect;
    }
}
