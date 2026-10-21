package product.automaton;

import automata.WeightedAutomaton;
import automata.WeightedTransition;
import ltlf.alphabet.Valuation;
import ltlf.ast.AtomicProposition;

import java.util.*;

public final class ProductAutomaton
    implements WeightedAutomaton<ProductState, Valuation, Integer> {

    private final List<ProductState> states;
    private final Set<ProductState> initialStates;
    private final Set<ProductState> acceptingStates;

    private final Map<ProductState,
            List<WeightedTransition<ProductState,
                    Valuation, Integer>>> outgoing;

    private final Map<ProductState, Set<AtomicProposition>> labels;

    public ProductAutomaton(
            Collection<ProductState> states,
            Collection<ProductState> initialStates,
            Collection<ProductState> acceptingStates,
            Collection<WeightedTransition<ProductState,
                    Valuation, Integer>> transitions,
            Map<ProductState, ? extends Collection<AtomicProposition>> labels
    ){
        this.states = List.copyOf(states);

        Set<ProductState> known = Set.copyOf(this.states);

        if(known.size() != this.states.size()){
            throw new IllegalArgumentException(
                    "Duplicate Product State"
            );
        }

        if(initialStates.isEmpty()
                || !known.containsAll(initialStates)
                || !known.containsAll(acceptingStates)){

            throw new IllegalArgumentException(
                    "Invalid initial or accepting product states"
            );
        }

        this.initialStates = Set.copyOf(initialStates);
        this.acceptingStates = Set.copyOf(acceptingStates);

        Map<ProductState, List<WeightedTransition<ProductState,
                ltlf.alphabet.Valuation, Integer>>> adjacency = new LinkedHashMap<>();


        for(ProductState state : this.states){
            adjacency.put(state, new ArrayList<>());
        }

        for(var transition : transitions){

            if(!known.contains(transition.source())
                    || !known.contains(transition.destination())){
                throw new IllegalArgumentException(
                        "Transition references an unknown product state"
                );
            }

            if(transition.weight() == null || transition.weight() <= 0){
                throw new IllegalArgumentException(
                        "Product transition weight must be positive"
                );
            }

            adjacency.get(transition.source()).add(transition);
        }

        Map<ProductState,
                List<WeightedTransition<ProductState,
                        ltlf.alphabet.Valuation, Integer>>> frozen =
                new LinkedHashMap<>();

        adjacency.forEach(
                (state, edges) -> frozen.put(state, List.copyOf(edges))
        );

        this.outgoing = Collections.unmodifiableMap(frozen);


        Map<ProductState, Set<AtomicProposition>> labelCopy =
                new LinkedHashMap<>();

        for(ProductState state : this.states){

            Collection<AtomicProposition> stateLabel =
                    Objects.requireNonNull(
                            labels.get(state),
                            "Missing label for product state: " + state
                    );

            labelCopy.put(
                    state,
                    Collections.unmodifiableSet(
                            new LinkedHashSet<>(stateLabel)
                    )
            );
        }

        if(labels.size() != this.states.size()){
            throw new IllegalArgumentException(
                    "Labels must be defined for exactly the product states"
            );
        }

        this.labels = Collections.unmodifiableMap(labelCopy);
    }

    public List<ProductState> getStates(){
        return states;
    }

    public Set<ProductState> getAcceptingStates(){
        return acceptingStates;
    }

    public Set<AtomicProposition> getLabel(ProductState state){
        return Objects.requireNonNull(
                labels.get(state),
                "Unknown Product State :" + state
        );
    }

    @Override
    public ProductState getInitialState(){
        return initialStates.iterator().next();
    }

    @Override
    public Collection<ProductState> getInitialStates(){
        return initialStates;
    }

    @Override
    public boolean isFinalState(ProductState state){
        return acceptingStates.contains(state);
    }

    @Override
    public Iterable<? extends WeightedTransition<ProductState,
            Valuation, Integer>> getOutgoingEdges(ProductState state){

        return Objects.requireNonNull(
                outgoing.get(state),
                "Unknown ProductState : " +  state
        );
    }

}
