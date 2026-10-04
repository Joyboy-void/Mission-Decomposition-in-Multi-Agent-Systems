package automata;


public interface WeightedAutomaton<S, L, W>
    extends Automaton<S, L> {

    @Override
    Iterable<? extends WeightedTransition<S, L, W>> getOutgoingEdges(S state);
}
