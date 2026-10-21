package product.automaton;

import agent.model.AgentModel;
import agent.model.AgentState;
import agent.model.AgentTransition;
import automata.WeightedTransition;
import ltlf.alphabet.Alphabet;
import ltlf.alphabet.AtomicPropositionSet;
import ltlf.alphabet.Valuation;
import ltlf.ast.AtomicProposition;
import ltlf.ast.FalseFormula;
import ltlf.ast.TrueFormula;
import ltlf.automaton.ResidualAutomaton;
import ltlf.automaton.ResidualState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ProductAutomatonBuilderCompatibilityTest {

    @Test
    void onlyExactMatchingValuationIsCompatibleWithAgentStateLabel(){
        AtomicProposition a = new AtomicProposition("a");
        AtomicProposition b = new AtomicProposition("b");
        AtomicPropositionSet apSet = new AtomicPropositionSet(List.of(a, b));
        Alphabet alphabet = new Alphabet(apSet);

        ResidualState q0 = new ResidualState(0, new TrueFormula());
        ResidualState q2 = new ResidualState(2, new FalseFormula());
        ResidualState q3 = new ResidualState(3, a);

        Map<Valuation, ResidualState> q0Transitions = new LinkedHashMap<>();
        for(Valuation valuation : alphabet){
            boolean isEmpty = !valuation.contains(a) && !valuation.contains(b);
            boolean isBoth = valuation.contains(a) && valuation.contains(b);

            // q0 -> q2 on {} and {a,b}; q0 -> q3 on {a} and {b}.
            q0Transitions.put(valuation, isEmpty || isBoth ? q2 : q3);
        }

        ResidualAutomaton ra = new ResidualAutomaton(
                alphabet,
                List.of(q0, q2, q3),
                q0,
                Map.of(q0, q0Transitions)
        );

        AgentState s0 = new AgentState("s0");
        AgentState s1 = new AgentState("s1");

        Map<AgentState, Set<AtomicProposition>> labels = new LinkedHashMap<>();
        labels.put(s0, Set.of(a));
        labels.put(s1, Set.of(b));

        AgentModel model = new AgentModel(
                List.of(s0, s1),
                List.of(s0),
                labels,
                List.of(new AgentTransition(s0, s1, 5))
        );

        ProductAutomaton product = ProductAutomatonBuilder.build(ra, model);
        ProductState productSource = product.getStates().stream()
                .filter(state -> state.agentState().equals(s0)
                        && state.residualState() == q0)
                .findFirst()
                .orElseThrow();

        List<WeightedTransition<ProductState, Valuation, Integer>> outgoing =
                new ArrayList<>();
        for (var edge : product.getOutgoingEdges(productSource)) {
            outgoing.add(edge);
        }

        // {a} is the only valuation compatible with label {a}.
        assertEquals(1, outgoing.size());
        assertEquals(Valuation.of(apSet, a), outgoing.getFirst().label());
        assertSame(q3, outgoing.getFirst().destination().residualState());
        assertEquals(5, outgoing.getFirst().weight());
    }
}
