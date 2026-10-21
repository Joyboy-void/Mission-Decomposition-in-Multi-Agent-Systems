package product.automaton;

import agent.model.AgentModel;
import agent.model.AgentState;
import agent.model.AgentTransition;
import automata.WeightedTransition;
import ltlf.alphabet.Valuation;
import ltlf.ast.AtomicProposition;
import ltlf.automaton.ResidualAutomaton;
import ltlf.automaton.ResidualState;

import java.util.*;

public final class ProductAutomatonBuilder{

    private ProductAutomatonBuilder(){

    }

    public static ProductAutomaton build(
            ResidualAutomaton residualAutomaton,
            AgentModel agentModel){

        Objects.requireNonNull(residualAutomaton);
        Objects.requireNonNull(agentModel);

        List<ProductState> states = new ArrayList<>();
        Map<ProductState, Set<AtomicProposition>> productLabels =
                new LinkedHashMap<>();
        Map<AgentState, Map<ResidualState, ProductState>> registry =
                new LinkedHashMap<>();

        // construct cartesian-product S X Q
        for (AgentState s : agentModel.getStates()) {
            Map<ResidualState, ProductState> row =
                    new LinkedHashMap<>();

            for (ResidualState q : residualAutomaton.getStates()) {
                ProductState pair = new ProductState(s, q);

                row.put(q, pair);
                states.add(pair);

                // Product-state label inherits the agentModel-state label.
                productLabels.put(pair, agentModel.getLabel(s));
            }

            registry.put(s, row);
        }

        // initial States S0 X q0

        Set<ProductState> initialStates = new LinkedHashSet<>();

        for(AgentState agentState : agentModel.getInitialStates()){
            initialStates.add(
                    registry.get(agentState).get(residualAutomaton.getInitialState())
            );
        }

        // acceptance only depends on the residual State
        Set<ProductState> acceptingStates = new LinkedHashSet<>();
        for(ProductState pair : states){
            if(residualAutomaton.isAccepting(pair.residualState())){

                acceptingStates.add(
                        pair
                );
            }
        }

        // transitions
        List<WeightedTransition<ProductState, Valuation, Integer>>
                transitions = new ArrayList<>();

        for(AgentTransition move : agentModel.getTransitions()){

            AgentState sourceAgent = move.source();
            AgentState destinationAgent = move.destination();

            Set<AtomicProposition> available = agentModel.getLabel(sourceAgent);

            for(ResidualState q : residualAutomaton.getStates()){
                ProductState source =
                        registry.get(sourceAgent).get(q);

                for(Valuation valuation : residualAutomaton.getAlphabet()){

                    if(!isCompatible(valuation, available)){
                        continue;
                    }

                    ResidualState next =
                            residualAutomaton.getDestination(q, valuation);

                    if(next == null){
                        continue;
                    }

                    ProductState destination =
                            registry.get(destinationAgent).get(next);

                    transitions.add(
                            new WeightedTransition<>(
                                    source,
                                    valuation,
                                    destination,
                                    move.cost()
                            )
                    );
                }
            }
        }


        return new ProductAutomaton(
                states,
                initialStates,
                acceptingStates,
                transitions,
                productLabels
        );
    }


    // assumes that valuation's AtomicPropositionSet is the Same universe used by the agent labels
    private static boolean isCompatible(
            Valuation valuation,
            Set<AtomicProposition> available){

        Set<AtomicProposition> expected = new LinkedHashSet<>();
        Set<AtomicProposition> actual = new LinkedHashSet<>();

        for(AtomicProposition proposition : valuation.getAtomicPropositionSet().asList()){

            if(available.contains(proposition)){
                expected.add(proposition);
            }
            if(valuation.contains(proposition)){
                actual.add(proposition);
            }
        }

        return actual.equals(expected);
    }
}
