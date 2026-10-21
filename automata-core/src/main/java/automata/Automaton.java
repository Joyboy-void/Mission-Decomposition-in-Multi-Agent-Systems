package automata;

import java.util.Collection;
import java.util.List;

public interface Automaton<S, L> {

    Collection<S> getStates();

    S getInitialState();

    default Collection<S> getInitialStates(){
        return List.of(getInitialState());
    }

    boolean isFinalState(S state);

    Iterable<? extends Edge<S, L>> getOutgoingEdges(S state);
}
