package ltlf.automaton;

import java.util.*;

import automata.Edge;
import automata.Transition;
import automata.DeterministicAutomaton;


import ltlf.alphabet.Alphabet;
import ltlf.alphabet.Valuation;
import ltlf.ast.TrueFormula;

public final class ResidualAutomaton
    implements DeterministicAutomaton<ResidualState, Valuation>{

    private final Alphabet alphabet;

    private final List<ResidualState> states;

    private final ResidualState initialState;

    private final Map<ResidualState, Map<Valuation, ResidualState>> transitions;

    public ResidualAutomaton(
        Alphabet alphabet,
        List<ResidualState> states,
        ResidualState initialState,
        Map<ResidualState, Map<Valuation, ResidualState>> transitions ){

        this.alphabet =
                Objects.requireNonNull(alphabet);

        this.states =
                List.copyOf(states);

        this.initialState =
                Objects.requireNonNull(initialState);

        this.transitions =
                copyTransitions(transitions);
    }

    private Map<ResidualState,
            Map<Valuation, ResidualState>>
    copyTransitions(Map<ResidualState, Map<Valuation, ResidualState>> original){

        // linkedHashMap preserves insertion Order...(deterministic order)
        Map<ResidualState, Map<Valuation, ResidualState>> copy =
                new LinkedHashMap<>();

        for(var entry : original.entrySet()){

            copy.put(
                    entry.getKey(),
                    Map.copyOf(entry.getValue())
            );
        }

        return Map.copyOf(copy);
    }



    @Override
    public Collection<ResidualState> getStates() {
        return states;
    }

    @Override
    public ResidualState getInitialState(){
        return initialState;
    }

    @Override
    public boolean isFinalState(ResidualState state) {
        return isAccepting(state);
    }

    @Override
    public ResidualState getDestination(
            ResidualState state,
            Valuation valuation){

        return getOutgoingTransitions(state).get(valuation);
    }

    /**
     * The residual automaton has a total transition function:
     * every state has a transition for every valuation in its alphabet.
     *
     * Therefore, for valid states and alphabet labels, this always
     * returns true.
     */
    @Override
    public boolean hasTransition(ResidualState state, Valuation label) {

        return true;
    }



    @Override
    public Iterable<? extends Edge<
                ResidualState,
                Valuation>>
                    getOutgoingEdges( ResidualState state) {

        Map<Valuation, ResidualState> outgoing =
                getOutgoingTransitions(state);

        return outgoing.entrySet()
                .stream()
                .map(entry ->
                        new Transition<ResidualState, Valuation>(
                                state,
                                entry.getKey(),
                                entry.getValue()
                        )
                )
                .toList();
    }

    public Alphabet getAlphabet(){
        return alphabet;
    }

    public Map<ResidualState,
            Map<Valuation, ResidualState>> getTransitions(){

        return transitions;
    }

    public Map<Valuation, ResidualState>
    getOutgoingTransitions(ResidualState state){

        return transitions.getOrDefault(
                state,
                Map.of()
        );
    }

    public boolean isAccepting(ResidualState state){
        return state.getFormula() instanceof TrueFormula;
    }

    public int size(){
        return states.size();
    }
}
