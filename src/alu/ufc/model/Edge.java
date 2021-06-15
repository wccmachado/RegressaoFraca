package alu.ufc.model;

public class Edge<T> {
    private Vertex<T> source;
    private Vertex<T> destiny;
    private String name;

    //Vertex<T> source, Vertex<T> destiny
    public Edge(String name) {
       // this.source = source;
       // this.destiny = destiny;
        this.name= name;

    }

    public Vertex<T> getSource() {
        return source;
    }

    public void setSource(Vertex<T> source) {
        this.source = source;
    }

    public Vertex<T> getDestiny() {
        return destiny;
    }

    public void setDestiny(Vertex<T> destiny) {
        this.destiny = destiny;
    }
}
