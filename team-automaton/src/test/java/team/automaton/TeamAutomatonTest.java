package team.automaton;

import agent.model.AgentState;
import automata.WeightedTransition;
import ltlf.alphabet.Alphabet;
import ltlf.alphabet.Valuation;
import ltlf.alphabet.AtomicPropositionSet;
import ltlf.analysis.dres.DResResult;
import ltlf.ast.AtomicProposition;
import ltlf.ast.TrueFormula;
import ltlf.automaton.ResidualState;
import product.automaton.ProductAutomaton;
import product.automaton.ProductState;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TeamAutomatonTest {

    private record Fixture(
            ProductAutomaton product,
            ResidualState q0,
            ResidualState q2,
            ProductState s0q0,
            ProductState s0q2,
            ProductState s1q2,
            DResResult dres
    ) {}

    private Fixture fixture(){

        AgentState s0 = new AgentState("s0");
        AgentState s1 = new AgentState("s1");

        ResidualState q0 =
                new ResidualState(
                        0,
                        new AtomicProposition("a")
                );

        ResidualState q2 =
                new ResidualState(
                        2,
                        new TrueFormula()
                );

        ProductState s0q0 = new ProductState(s0, q0);
        ProductState s0q2 = new ProductState(s0, q2);
        ProductState s1q2 = new ProductState(s1, q2);

        List<ProductState> states =
                List.of(s0q0, s0q2, s1q2);

        Set<AtomicProposition> aps =
                Set.of(new AtomicProposition("a"));

        AtomicPropositionSet apSet =
                new AtomicPropositionSet(aps);

        Alphabet alphabet = new Alphabet(apSet);

        Valuation valuation = alphabet.iterator().next();

        List<WeightedTransition<ProductState,
                Valuation, Integer>> edges =
                List.of(
                        new WeightedTransition<>(
                                s0q0,
                                valuation,
                                s0q2,
                                5
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
                        Set.of(s0q2, s1q2),
                        edges,
                        labels
                );

        DResResult dres =
                new DResResult(
                        Set.of(
                                new DResResult.StatePair(q0, q2)
                        )
                );

        return new Fixture(
                product,
                q0,
                q2,
                s0q0,
                s0q2,
                s1q2,
                dres
        );
    }

    @Test
    void createsCorrectNumberOfLogicalStates(){

        Fixture f = fixture();

        TeamAutomaton team =
                TeamAutomatonBuilder.build(
                        f.product(),
                        f.dres(),
                        3
                );

        assertEquals(9, team.getLogicalStateCount());
        assertEquals(9, team.getStates().size());
    }

    @Test
    void initialStateExistsOnlyInFirstCopy(){

        Fixture f = fixture();

        TeamAutomaton team =
                TeamAutomatonBuilder.build(
                        f.product(),
                        f.dres(),
                        3
                );

        assertEquals(
                Set.of(new TeamState(1, f.s0q0())),
                new HashSet<>(team.getInitialStates())
        );
    }

    @Test
    void finalStatesExistOnlyInLastCopy(){

        Fixture f = fixture();

        TeamAutomaton team =
                TeamAutomatonBuilder.build(
                        f.product(),
                        f.dres(),
                        3
                );

        assertFalse(
                team.isFinalState(
                        new TeamState(1, f.s0q2())
                )
        );

        assertFalse(
                team.isFinalState(
                        new TeamState(2, f.s0q2())
                )
        );

        assertTrue(
                team.isFinalState(
                        new TeamState(3, f.s0q2())
                )
        );
    }

    @Test
    void ordinaryTransitionsPreserveCopyIdAndWeight(){

        Fixture f = fixture();

        TeamAutomaton team =
                TeamAutomatonBuilder.build(
                        f.product(),
                        f.dres(),
                        3
                );

        List<? extends WeightedTransition<
                TeamState, TeamAction, Integer>> edges =
                new ArrayList<>(
                        (Collection<? extends WeightedTransition<
                                TeamState, TeamAction, Integer>>)
                                team.getOutgoingEdges(
                                        new TeamState(2, f.s0q0())
                                )
                );

        var ordinary = edges.stream()
                .filter(e -> e.label()
                        instanceof TeamAction.ValuationAction)
                .findFirst()
                .orElseThrow();

        assertEquals(2, ordinary.destination().copyId());
        assertEquals(f.s0q2(), ordinary.destination().productState());
        assertEquals(5, ordinary.weight());
    }

    @Test
    void generatesSwitchesOnlyToNextCopy(){

        Fixture f = fixture();

        TeamAutomaton team =
                TeamAutomatonBuilder.build(
                        f.product(),
                        f.dres(),
                        3
                );

        List<WeightedTransition<
                TeamState, TeamAction, Integer>> edges =
                new ArrayList<>();

        team.getOutgoingEdges(
                new TeamState(1, f.s0q2())
        ).forEach(edges::add);

        List<WeightedTransition<
                TeamState, TeamAction, Integer>> switches =
                edges.stream()
                        .filter(e -> e.label()
                                == TeamAction.SwitchAction.EPSILON)
                        .toList();

        assertEquals(2, switches.size());

        assertTrue(
                switches.stream().allMatch(
                        e -> e.destination().copyId() == 2
                )
        );

        assertTrue(
                switches.stream().allMatch(
                        e -> e.weight() == 0
                )
        );

        assertTrue(
                switches.stream().allMatch(
                        e -> e.destination().productState()
                                .residualState() == f.q2()
                )
        );
    }

    @Test
    void lastCopyDoesNotGenerateSwitches() {

        Fixture f = fixture();

        TeamAutomaton team =
                TeamAutomatonBuilder.build(
                        f.product(),
                        f.dres(),
                        3
                );

        List<WeightedTransition<
                TeamState, TeamAction, Integer>> edges =
                new ArrayList<>();

        team.getOutgoingEdges(
                new TeamState(3, f.s0q2())
        ).forEach(edges::add);

        assertTrue(
                edges.stream().noneMatch(
                        e -> e.label()
                                == TeamAction.SwitchAction.EPSILON
                )
        );
    }

    @Test
    void rejectsInvalidAgentCount() {

        Fixture f = fixture();

        assertThrows(
                IllegalArgumentException.class,
                () -> TeamAutomatonBuilder.build(
                        f.product(),
                        f.dres(),
                        0
                )
        );
    }
}