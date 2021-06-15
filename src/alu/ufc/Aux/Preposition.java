package alu.ufc.Aux;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preposition {
   // private List<String> listPreposition;

    public List<String> getListPreposition(String line, String regex) {
        List<String> listPreConditionAux = new ArrayList<String>();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(line);
        int count = 0;
        int indiceInicial = 0;
        if (m.find()) {
            String aux = line.substring(indiceInicial, m.start()).trim();
            if (aux.length() != 0) {
                listPreConditionAux.add(count, aux);
                ++count;
            }


            for (indiceInicial = m.end(); m.find(); ++count) {
                //  System.out.println(m.start());
                listPreConditionAux.add(count, line.substring(indiceInicial, m.start()).trim());
                indiceInicial = m.end();
            }

            if (indiceInicial != line.length()) {
                listPreConditionAux.add(count++, line.substring(indiceInicial, line.length()).trim());
            }
        } else {
            listPreConditionAux.add(count, line);
        }


        return listPreConditionAux;
    }


    public List<String> getListPrepositionEffect(List<String> effects, String regex) {
        List<String> listPreConditionAux = new ArrayList<String>();
        Pattern p = Pattern.compile(regex);
        int count = 0;
        for (String effect : effects) {
            Matcher m = p.matcher(effect);
            int indiceInicial = 0;
            if (m.find()) {
                String aux = effect.substring(indiceInicial, m.start()).trim();
                if (aux.length() != 0) {
                    listPreConditionAux.add(count, aux);
                    ++count;
                }


                for (indiceInicial = m.end(); m.find(); ++count) {
                    //  System.out.println(m.start());
                    listPreConditionAux.add(count, effect.substring(indiceInicial, m.start()).trim());
                    indiceInicial = m.end();
                }

                if (indiceInicial != effect.length()) {
                    listPreConditionAux.add(count++, effect.substring(indiceInicial, effect.length()).trim());
                }
            } else {
                listPreConditionAux.add(count, effect);
            }
        }

        return listPreConditionAux;
    }
}
