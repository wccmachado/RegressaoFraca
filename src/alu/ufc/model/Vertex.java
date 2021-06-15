package alu.ufc.model;

import java.util.ArrayList;

public class Vertex<T>{
    private T type;
    private ArrayList<Edge<T>> inputEdge;
    private ArrayList<Edge<T>> outputEdge;

    public Vertex(T type) {
        this.type = type;
    }

    public T getType() {
        return type;
    }

    public void setType(T type) {
        this.type = type;
    }

    public ArrayList<Edge<T>> getInputEdge() {
        return inputEdge;
    }

    public void setInputEdge(ArrayList<Edge<T>> inputEdge) {
        this.inputEdge = inputEdge;
    }

    public ArrayList<Edge<T>> getOutputEdge() {
        return outputEdge;
    }

    public void setOutputEdge(ArrayList<Edge<T>> outputEdge) {
        this.outputEdge = outputEdge;
    }
}
