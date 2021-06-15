package alu.ufc.model;

import java.util.ArrayList;

public class Grafo<V,E>{
    private ArrayList<V> vertex;
    private ArrayList<E> edges;

    public Grafo(){
        this.vertex = new ArrayList<V>();
        this.edges = new ArrayList<E>();
    }

    public ArrayList<V> getVertex() {
        return vertex;
    }

    public void setVertex(ArrayList<V> vertex) {
        this.vertex = vertex;
    }

    public ArrayList<E> getEdges() {
        return edges;
    }

    public void setEdges(ArrayList<E> edges) {
        this.edges = edges;
    }

    public void addVertex(V vertex){
        //Vertex<V> newVertex = new Vertex<V>(vertex,edge);
        this.vertex.add(vertex);
    }
    public V getObject(int index){
        return vertex.get(index);

    }
    //T source, T destiny
    public void addEdge(E edge){
      //  Vertex<T> src = new Vertex<>(source);
      //  Vertex<T> dst = new Vertex<>(destiny);
       this.edges.add(edge);
    }
    public boolean isEmpty(){
        return vertex.isEmpty();
    }
    public int size(){
        return vertex.size();
    }
}
