package automata;

public interface DeterministicAutomaton<S, L>
    extends Automaton<S, L>{

    boolean hasTransition(S state, L label);

    S getDestination(S state, L label);
}

