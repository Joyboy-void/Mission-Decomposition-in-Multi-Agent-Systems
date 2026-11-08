
package team.automaton;

import automata.WeightedAutomaton;
import automata.WeightedTransition;

import ltlf.analysis.dres.DResResult;
import ltlf.automaton.ResidualState;

import product.automaton.ProductAutomaton;
import product.automaton.ProductState;

import java.util.*;

/**
 * implicit representation of the Team Automaton.
 *
 * The Team Automaton consists of n logical copies of the
 * Product Automaton, connected through epsilon transitions
 * determined by DRes.
 *
 * product Automaton states and transitions are not copied.
 * They are reused and lifted into the appropriate copy.
 */
public final class TeamAutomaton
        implements WeightedAutomaton<
        TeamState,
        TeamAction,
        Integer> {

    private final ProductAutomaton productAutomaton;

    private final DResResult dres;

    private final int numberOfAgents;

    /*
     * maps each residual state to all Product Automaton states containing that residual state.
     *
     * this is used for efficient epsilon generation.
     */
    private final Map<
            ResidualState,
            List<ProductState>
            > statesByResidual;

    private final Set<ProductState> productStateSet;

    public TeamAutomaton(
            ProductAutomaton productAutomaton,
            DResResult dres,
            int numberOfAgents
    ){

        this.productAutomaton = Objects.requireNonNull(
                productAutomaton,
                "Product Automaton must not be null"
        );

        this.dres = Objects.requireNonNull(
                dres,
                "DRes result must not be null"
        );

        if (numberOfAgents < 1) {
            throw new IllegalArgumentException(
                    "Number of agents must be >= 1"
            );
        }

        this.numberOfAgents = numberOfAgents;

        this.productStateSet = Collections.unmodifiableSet(
                new HashSet<>(productAutomaton.getStates())
        );

        this.statesByResidual = buildResidualIndex();
    }

    // builds an index of Product Automaton states grouped by their residual state.

    private Map<ResidualState, List<ProductState>>
    buildResidualIndex(){

        Map<ResidualState, List<ProductState>> index =
                new LinkedHashMap<>();

        for(ProductState state : productAutomaton.getStates()){

            index.computeIfAbsent(
                    state.residualState(),
                    ignored -> new ArrayList<>()
            ).add(state);
        }

        Map<ResidualState, List<ProductState>> frozen =
                new LinkedHashMap<>();

        index.forEach(
                (residual, states) ->
                        frozen.put(residual, List.copyOf(states))
        );

        return Collections.unmodifiableMap(frozen);
    }

    public int getNumberOfAgents(){
        return numberOfAgents;
    }

    public ProductAutomaton getProductAutomaton(){
        return productAutomaton;
    }

    public DResResult getDres(){
        return dres;
    }


    /*
     * returns the number of logical Team Automaton states.
     *
     * The states themselves are not eagerly materialized.
     */
    public long getLogicalStateCount() {
        return (long) numberOfAgents
                * productAutomaton.getStates().size();
    }

    /*
     * lazily enumerates all logical Team Automaton states.
     */
    @Override
    public Collection<TeamState> getStates(){

        return new AbstractCollection<>(){

            @Override
            public Iterator<TeamState> iterator(){

                return new Iterator<>(){

                    private int copyId = 1;

                    private int productIndex = 0;

                    private final List<ProductState> states =
                            productAutomaton.getStates();

                    @Override
                    public boolean hasNext(){
                        return copyId <= numberOfAgents
                                && !states.isEmpty();
                    }

                    @Override
                    public TeamState next(){

                        if(!hasNext()){
                            throw new NoSuchElementException();
                        }

                        TeamState result = new TeamState(
                                copyId,
                                states.get(productIndex)
                        );

                        productIndex++;

                        if(productIndex >= states.size()){
                            productIndex = 0;
                            copyId++;
                        }

                        return result;
                    }
                };
            }

            @Override
            public int size() {

                return (int) Math.min(
                        getLogicalStateCount(),
                        Integer.MAX_VALUE
                );
            }

            @Override
            public boolean contains(Object object) {

                if(!(object instanceof TeamState state)){
                    return false;
                }

                return state.copyId() >= 1
                        && state.copyId() <= numberOfAgents
                        && productStateSet.contains(
                        state.productState()
                );
            }
        };
    }

    // All initial states belong exclusively to copy 1.

    @Override
    public TeamState getInitialState() {
        return new TeamState(
                1,
                productAutomaton.getInitialState()
        );
    }

    @Override
    public Collection<TeamState> getInitialStates() {

        return productAutomaton.getInitialStates()
                .stream()
                .map(state -> new TeamState(1, state))
                .toList();
    }

    // Only accepting states in copy n are accepting in the Team Automaton.

    @Override
    public boolean isFinalState(TeamState state){

        validateState(state);

        return state.copyId() == numberOfAgents
                && productAutomaton.isFinalState(
                state.productState()
        );
    }

    /*
     * generates outgoing transitions on demand.
     *
     * Two kinds of transitions are generated:
     *
     * 1. ordinary transitions lifted from the Product Automaton.
     * 2. epsilon switches to the immediately following copy.
     */
    @Override
    public Iterable<? extends WeightedTransition<
            TeamState,
            TeamAction,
            Integer>> getOutgoingEdges(TeamState state){

        validateState(state);

        List<WeightedTransition<
                TeamState,
                TeamAction,
                Integer>> result = new ArrayList<>();

        addOrdinaryTransitions(state, result);

        addSwitchTransitions(state, result);

        return List.copyOf(result);
    }

    /*
     * lifts ordinary Product Automaton transitions.
     *
     * The copy ID remains unchanged.
     */
    private void addOrdinaryTransitions(
            TeamState source,
            List<WeightedTransition<
                    TeamState,
                    TeamAction,
                    Integer>> result
    ){

        for(var edge :
                productAutomaton.getOutgoingEdges(
                        source.productState()
                )){

            TeamState destination = new TeamState(
                    source.copyId(),
                    edge.destination()
            );

            result.add(
                    new WeightedTransition<>(
                            source,
                            new TeamAction.ValuationAction(
                                    edge.label()
                            ),
                            destination,
                            edge.weight()
                    )
            );
        }
    }

    /*
     * Generates epsilon switches.
     *
     * A switch is allowed only when:
     *
     * 1. The current residual state belongs to DRes.
     * 2. There exists a next agent copy.
     *
     * every Product Automaton state with the same residual state is a possible destination.
     */
    private void addSwitchTransitions(
            TeamState source,
            List<WeightedTransition<
                    TeamState,
                    TeamAction,
                    Integer>> result
    ){

        // no switch is possible from the last copy.
        if(source.copyId() >= numberOfAgents){
            return;
        }

        ResidualState residual =
                source.productState().residualState();

        // switches are only allowed at DRes states.
        if (!dres.getStates().contains(residual)) {
            return;
        }

        List<ProductState> destinations =
                statesByResidual.getOrDefault(
                        residual,
                        List.of()
                );

        int nextCopyId = source.copyId() + 1;

        for(ProductState destination : destinations){

            TeamState target = new TeamState(
                    nextCopyId,
                    destination
            );

            result.add(
                    new WeightedTransition<>(
                            source,
                            TeamAction.SwitchAction.EPSILON,
                            target,
                            0
                    )
            );
        }
    }

    // ensures that a state belongs to this Team Automaton.
    private void validateState(TeamState state) {

        Objects.requireNonNull(
                state,
                "Team state must not be null"
        );

        if(state.copyId() < 1
                || state.copyId() > numberOfAgents){

            throw new IllegalArgumentException(
                    "Invalid Team Automaton copy ID: "
                            + state.copyId()
            );
        }

        if(!productStateSet.contains(state.productState())){

            throw new IllegalArgumentException(
                    "Product state does not belong to "
                            + "the underlying Product Automaton"
            );
        }
    }
}
