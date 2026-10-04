package automata;

public interface Edge<S, L> {

    S source();

    L label();

    S destination();
}
