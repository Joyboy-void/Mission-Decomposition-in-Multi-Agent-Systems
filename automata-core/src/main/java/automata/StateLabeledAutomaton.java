package automata;

public interface StateLabeledAutomaton <S, L, SL>
    extends Automaton<S, L>{

    SL getStateLabel(S state);
}
