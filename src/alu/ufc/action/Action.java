package alu.ufc.action;

import alu.ufc.Aux.Preposition;

import alu.ufc.bdd.BDDCreator;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Action {
    private String name;
    private String preCond;

    private List<String> listPropositionPreCondition = new ArrayList<>();

    /*
     * Armazena o valor dos efeitos de uma ação
     */
    private List<String> listEffect = new ArrayList<>();
    private List<String> listPropositionEffect = new ArrayList<>();
    private Hashtable<List<String>, List<String>> hstAux = new Hashtable<>();

    // stores the tuple value <name_action, precondition, effect >
    private Hashtable<String, Hashtable<List<String>, List<String>>> hstAction = new Hashtable<>();

    // stores the the proposition  that make up effect
    private Hashtable<Integer,List<String> > hstListPrepositionEffect = new Hashtable<>();


    /*For Ritanen's regression */
    private Vector<BDD> effectVec = new Vector<BDD>();
    Preposition preposition = new Preposition();

    BDDCreator creator;
//	Hashtable<Integer,String> varTable;

//	Hashtable<String,Integer> varTable2;


    /*** Constructor ***/
    public Action(String actionName, String preCond, String eff,BDDCreator cre) { //

        creator = cre;
        name = actionName;
        listPropositionPreCondition = preposition.getListPreposition(preCond, "\\,");
        listEffect = getListEffect(eff, "\\:");
        hstAux.put(listPropositionPreCondition, listEffect);
        hstAction.put(name, hstAux);
        getListPrepositionOfEffect();
        hstListPrepositionEffect = getListPrepositionOfEffect();

    }


    public List<String> getListEffect() {
        return listEffect;
    }

    public void setListEffect(List<String> listEffect) {
        this.listEffect = listEffect;
    }

    public List<String> getListPropositionEffect(){
       List<String> aux = new ArrayList<>();
       int j=0;
        for (String effect: listEffect
             ) {
            String[] propositionEffect = effect.split(",");
            for (int i = 0; i < propositionEffect.length; i++) {
                aux.add(j,propositionEffect[i]);
                j++;
            }
        }
        return aux;
    }

    public List<String> getListEffect(String eff, String regex) {
        List<String> listEffectAux = new ArrayList<>();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(eff);
        int count = 0;
        int indiceInicial = 0;
        if (m.find()) {
            String aux = eff.substring(indiceInicial, m.start()).trim();
            if (aux.length() != 0) {
                listEffectAux.add(count, aux);
                ++count;
            }

            //int indiceInicial;
            for (indiceInicial = m.end(); m.find(); ++count) {
               // System.out.println(m.start());
                listEffectAux.add(count, eff.substring(indiceInicial, m.start()).trim());
                indiceInicial = m.end();
            }

            if (indiceInicial != eff.length()) {
                listEffectAux.add(count++, eff.substring(indiceInicial, eff.length()).trim());
            }
        } else {
            listEffectAux.add(count, eff);
        }


        return listEffectAux;
    }

    /*
    *
    * Effect:
    * return: {efect1, efect2, effect3}
     */

    /**
     * this method obtains the propositions that make up an effect. Effects
     * can be separated by the ":" character,i.e, efect1:efect2:effect3.
     * Effects are composed of one or more propositions: efect1= p1,p2 ou
     * efect2= p3.
     *
     * @return  Hashtable containing propositions
     */
    public Hashtable<Integer,List<String> > getListPrepositionOfEffect(){

        int count=0;
        for (String item : listEffect
             ) {

            hstListPrepositionEffect.put(count, preposition.getListPreposition(item, "\\,"));
            count++;
        }
        return hstListPrepositionEffect;

    }



    public List<BDD> getAndEffetBDD(){

        return creator.createAndBddWithEffectUncertain(hstListPrepositionEffect);

    }

    public BDD getEffect(){
        return creator.getEffect();

    }
    public BDD getPreCondictionBDD(){
        return creator.getPreCondictionBDD(listPropositionPreCondition);

    }



    public  BDD getOrEffect(List<BDD> lst){
       return creator.createOrBddEffect(lst);
    }

    public List<BDD> modifyAction(){
        List<String> listEffectsAux= new ArrayList<>();
        listEffectsAux.addAll(preposition.getListPrepositionEffect(listEffect,"\\,"));


        return creator.changeSet2(listEffectsAux);


    }

 /*   public BDD getPreCondBDD(){

        return creator.createAndBdd2(lisOfPropositionPreCondition);

    }
/*
    public void printAction() {

        hstAction.forEach((k, v) -> {
                    String name = k;
                    v.forEach((key, value) -> {
                                System.out.println("<name> : " + k + "\n"+
                                        "<pre>: " + String.join(",", key) + "\n"+
                                        "<pos>: " + String.join(",", value)
                                );

                            }
                    );
                }
        );
    }
*/
    /**
     * Creates the change set which is the union of all propositions involved in the effect list, without negation
     **/
    public Vector<String> createChangeSet(String eff) {
        StringTokenizer tknEff = new StringTokenizer(eff, ",");
        int effNum = tknEff.countTokens();
        String tknPiece;

        Vector<String> changeSet = new Vector<String>(effNum);

        /* Adding the effect propositions in the change set*/
        for (int i = 0; i < effNum; i++) {
            tknPiece = tknEff.nextToken();
            if (tknPiece.startsWith("~")) {
                tknPiece = tknPiece.substring(1); // deletes the signal ~
            }
            if (changeSet.contains(tknPiece) == false) {
                changeSet.add(tknPiece);
            }
        }

        return changeSet;
    }


    public String getName() {
        return name;
    }

    public List<String> getListPropositionPreCondition() {
        return listPropositionPreCondition;
    }

    public void setListPropositionPreCondition(List<String> listPropositionPreCondition) {
        this.listPropositionPreCondition = listPropositionPreCondition;
    }


}