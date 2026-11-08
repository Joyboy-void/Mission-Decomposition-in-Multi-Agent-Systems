package decomposition;

import agent.model.AgentState;
import automata.WeightedTransition;

import ltlf.alphabet.AtomicPropositionSet;
import ltlf.alphabet.Valuation;
import ltlf.analysis.dres.DResResult;
import ltlf.ast.AtomicProposition;
import ltlf.ast.TrueFormula;
import ltlf.automaton.ResidualState;

import product.automaton.ProductAutomaton;
import product.automaton.ProductState;

import team.automaton.TeamAction;
import team.automaton.TeamAutomaton;
import team.automaton.TeamAutomatonBuilder;
import team.automaton.TeamState;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ShortestPathFinderTest {

    private record Fixture(
            TeamAutomaton team,
            ProductState s0q0,
            ProductState s0q2,
            ProductState s1qf
    ){}

    private Fixture fixture(){

        AtomicProposition p = new AtomicProposition("p");

        AtomicPropositionSet apSet =
                new AtomicPropositionSet(List.of(p));

        Valuation empty = Valuation.empty(apSet);

        AgentState s0 = new AgentState("s0");
        AgentState s1 = new AgentState("s1");

        ResidualState q0 =
                new ResidualState(0, p);

        ResidualState q2 =
                new ResidualState(2, new AtomicProposition("r"));

        ResidualState q3 =
                new ResidualState(3, new AtomicProposition("s"));

        ResidualState qf =
                new ResidualState(4, new TrueFormula());

        ProductState s0q0 = new ProductState(s0, q0);
        ProductState s0q2 = new ProductState(s0, q2);
        ProductState s1q2 = new ProductState(s1, q2);
        ProductState s0q3 = new ProductState(s0, q3);
        ProductState s1q3 = new ProductState(s1, q3);
        ProductState s0qf = new ProductState(s0, qf);
        ProductState s1qf = new ProductState(s1, qf);

        List<ProductState> states = List.of(
                s0q0,
                s0q2,
                s1q2,
                s0q3,
                s1q3,
                s0qf,
                s1qf
        );

        List<WeightedTransition<
                ProductState,
                Valuation,
                Integer>> transitions = List.of(

                new WeightedTransition<>(
                        s0q0,
                        empty,
                        s0q2,
                        5
                ),

                new WeightedTransition<>(
                        s0q2,
                        empty,
                        s1qf,
                        2
                )
        );

        Map<ProductState, Collection<AtomicProposition>> labels =
                new LinkedHashMap<>();

        for(ProductState state : states){
            labels.put(state, Set.of());
        }

        ProductAutomaton product =
                new ProductAutomaton(
                        states,
                        Set.of(s0q0),
                        Set.of(s0qf, s1qf),
                        transitions,
                        labels
                );

        DResResult dres =
                new DResResult(
                        Set.of(
                                new DResResult.StatePair(q2, q3)
                        )
                );

        TeamAutomaton team =
                TeamAutomatonBuilder.build(
                        product,
                        dres,
                        2
                );

        return new Fixture(
                team,
                s0q0,
                s0q2,
                s1qf
        );
    }

    @Test
    void findsMinimumCostDecomposition(){

        Fixture f = fixture();

        ShortestPathFinder finder =
                new ShortestPathFinder();

        DecompositionResult result =
                finder.find(f.team());

        assertTrue(result.isDecomposable());

        assertEquals(7, result.getTotalCost());

        assertEquals(4, result.getPath().size());

        assertEquals(
                new TeamState(1, f.s0q0()),
                result.getPath().getFirst()
        );

        assertEquals(
                new TeamState(2, f.s1qf()),
                result.getPath().getLast()
        );
    }

    @Test
    void includesEpsilonTransitionInOptimalPath(){

        Fixture f = fixture();

        DecompositionResult result =
                new ShortestPathFinder().find(f.team());

        assertTrue(
                result.getTransitions().stream()
                        .anyMatch(edge ->
                                edge.label()
                                        == TeamAction.SwitchAction.EPSILON)
        );
    }

    @Test
    void reportsFailureWhenFinalStateIsUnreachable(){

        Fixture f = fixture();

        // use one copy: no switch is possible.
        // The Product Automaton has a path to a final state,
        // so use an empty DRes and two copies instead.
        TeamAutomaton disconnected =
                TeamAutomatonBuilder.build(
                        f.team().getProductAutomaton(),
                        new DResResult(Set.of()),
                        2
                );

        DecompositionResult result =
                new ShortestPathFinder().find(disconnected);

        assertFalse(result.isDecomposable());

        assertEquals(-1, result.getTotalCost());

        assertTrue(result.getPath().isEmpty());

        assertTrue(result.getTransitions().isEmpty());
    }

    @Test
    void singleAgentCaseDoesNotRequireDRes(){

        Fixture f = fixture();

        TeamAutomaton singleAgent =
                TeamAutomatonBuilder.build(
                        f.team().getProductAutomaton(),
                        new DResResult(Set.of()),
                        1
                );

        DecompositionResult result =
                new ShortestPathFinder().find(singleAgent);

        assertTrue(result.isDecomposable());

        assertEquals(7, result.getTotalCost());

        assertTrue(
                result.getPath().stream()
                        .allMatch(state -> state.copyId() == 1)
        );
    }
}
