package automata;

import java.util.Collection;

public interface Automaton<S, L> {

    Collection<S> getStates();

    S getInitialState();

    boolean isFinalState(S state);

    Iterable<? extends Edge<S, L>> getOutgoingEdges(S state);
}
