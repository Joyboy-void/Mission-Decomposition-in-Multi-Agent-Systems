package agent.model;

import automata.WeightedTransition;
import automata.WeightedAutomaton;
import ltlf.ast.AtomicProposition;

import java.util.*;

public final class AgentModel
    implements WeightedAutomaton<AgentState, AgentTransition, Integer> {

    private final List<AgentState> states;
    private final Set<AgentState> initialStates;

    private final Map<AgentState, Set<AtomicProposition>> labels;
    private final Map<AgentState, List<AgentTransition>> outgoing;

    public AgentModel(
            Collection<AgentState> states,
            Collection<AgentState> initialStates,
            Map<AgentState, ? extends Collection<AtomicProposition>> labels,
            Collection<AgentTransition> transitions
    ){
        Objects.requireNonNull(states);
        Objects.requireNonNull(initialStates);
        Objects.requireNonNull(labels);
        Objects.requireNonNull(transitions);

        LinkedHashSet<AgentState> uniqueStates =  new LinkedHashSet<>(states);

        if(uniqueStates.isEmpty()){
            throw new IllegalArgumentException(
                    "Agent Model must contain at-least one state"
            );
        }

        if(uniqueStates.size() != states.size()){
            throw new IllegalArgumentException(
                    "Duplicate Agent State"
            );
        }

        this.states = List.copyOf(uniqueStates);
        Set<AgentState> stateSet = Set.copyOf(uniqueStates);

        if(initialStates.isEmpty() || !stateSet.containsAll(initialStates)){
            throw new IllegalArgumentException(
                    "Initial State must be non Empty and belong to S(Set of Agent-model States)"
            );
        }

        this.initialStates =
                Collections.unmodifiableSet(
                        new LinkedHashSet<>(initialStates)
                );

        Map<AgentState, Set<AtomicProposition>> labelCopy =
                new LinkedHashMap<>();

        for(AgentState state : this.states){
            Collection<AtomicProposition> stateLabel =
                    Objects.requireNonNull(labels.get(state), "Missing Label for : " + state);

            labelCopy.put(
                    state,
                    Collections.unmodifiableSet(
                            new LinkedHashSet<>(stateLabel)
                    )
            );
        }

        if(labels.size() != this.states.size()){
            throw new IllegalArgumentException(
                    "Labels must be defined for exactly the model states"
            );
        }

        this.labels = Collections.unmodifiableMap(labelCopy);

        Map<AgentState, List<AgentTransition>> adjacency =
                new LinkedHashMap<>();

        for(AgentState state : this.states){
            adjacency.put(state, new ArrayList<>());
        }

        for (AgentTransition transition : transitions) {
            if (!stateSet.contains(transition.source())
                    || !stateSet.contains(transition.destination())) {
                throw new IllegalArgumentException(
                        "Transition references an unknown state: "
                                + transition
                );
            }

            adjacency.get(transition.source()).add(transition);
        }

        Map<AgentState, List<AgentTransition>> frozen =
                new LinkedHashMap<>();

        adjacency.forEach(
                (state, edges) -> frozen.put(state, List.copyOf(edges))
        );

        this.outgoing = Collections.unmodifiableMap(frozen);
    }

    /**
     * Returns the union of all atomic propositions used by state labels.
     * Propositions absent from a state's label are interpreted as false
     * for that state.
     */
    public Set<AtomicProposition> getAtomicPropositions(){
        Set<AtomicProposition> propositions = new LinkedHashSet<>();
        labels.values().forEach(propositions::addAll);

        return Collections.unmodifiableSet(propositions);
    }

    public List<AgentState> getStates(){
        return states;
    }

    @Override
    public AgentState getInitialState() {
        return initialStates.iterator().next();
    }

    @Override
    public Collection<AgentState> getInitialStates() {
        return initialStates;
    }

    public Set<AtomicProposition> getLabel(AgentState state) {
        return Objects.requireNonNull(
                labels.get(state),
                "Unknown agent state: " + state
        );
    }

    public List<AgentTransition> getTransitions() {
        return outgoing.values()
                .stream()
                .flatMap(Collection::stream)
                .toList();
    }

    public List<AgentTransition> getAgentOutgoingTransitions(
            AgentState state) {

        return Objects.requireNonNull(
                outgoing.get(state),
                "Unknown agent state: " + state
        );
    }

    @Override
    public boolean isFinalState(AgentState state){
        if(!labels.containsKey(state)){
            throw new IllegalArgumentException(
                    "Unknown AGent State : " + state
            );
        }

        return false;
    }

    @Override
    public Iterable<? extends WeightedTransition<
            AgentState, AgentTransition, Integer>>
    getOutgoingEdges(AgentState state) {

        return getAgentOutgoingTransitions(state)
                .stream()
                .map(t -> new WeightedTransition<>(
                        t.source(),
                        t,
                        t.destination(),
                        t.cost()
                ))
                .toList();
    }
}
