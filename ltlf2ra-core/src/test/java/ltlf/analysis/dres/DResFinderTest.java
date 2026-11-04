package ltlf.analysis.dres;

import ltlf.alphabet.Alphabet;
import ltlf.alphabet.AtomicPropositionSet;
import ltlf.alphabet.Valuation;
import ltlf.ast.*;
import ltlf.automaton.ResidualAutomaton;
import ltlf.automaton.ResidualState;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DResFinderTest {

    private final AtomicProposition p =
            new AtomicProposition("p");

    private final AtomicPropositionSet apSet =
            new AtomicPropositionSet(List.of(p));

    private final Alphabet alphabet =
            new Alphabet(apSet);

    private final Valuation empty =
            Valuation.empty(apSet);

    private final Valuation pTrue =
            Valuation.of(apSet, p);

    private final DResFinder finder =
            new DResFinder();

    private ResidualAutomaton validAutomaton(){

        ResidualState q0 =
                new ResidualState(0, p);

        ResidualState q1 =
                new ResidualState(1, new Not(p));

        ResidualState q2 =
                new ResidualState(2, new And(p, p));

        ResidualState qf =
                new ResidualState(3, new TrueFormula());

        ResidualState sink =
                new ResidualState(4, new FalseFormula());

        Map<ResidualState, Map<Valuation, ResidualState>>
                transitions = new LinkedHashMap<>();

        // Initial state.
        transitions.put(q0, Map.of(
                empty, q1,
                pTrue, q2
        ));

        // q1 --p--> qf
        transitions.put(q1, Map.of(
                empty, sink,
                pTrue, qf
        ));

        // q2 --{}--> qf
        transitions.put(q2, Map.of(
                empty, qf,
                pTrue, sink
        ));

        // Final state loops to itself.
        transitions.put(qf, Map.of(
                empty, qf,
                pTrue, qf
        ));

        // Nonaccepting sink loops to itself.
        transitions.put(sink, Map.of(
                empty, sink,
                pTrue, sink
        ));

        return new ResidualAutomaton(
                alphabet,
                List.of(q0, q1, q2, qf, sink),
                q0,
                transitions
        );
    }

    @Test
    void discoversInterchangeableStatePair(){

        ResidualAutomaton automaton =
                validAutomaton();

        DResResult result =
                finder.find(automaton);

        assertEquals(2, result.size());
        assertEquals(1, result.pairCount());

        Set<Integer> stateIds = new HashSet<>();

        for(ResidualState state : result.getStates()){
            stateIds.add(state.getId());
        }

        assertEquals(Set.of(1, 2), stateIds);

        DResResult.StatePair pair =
                result.getPairs().iterator().next();

        assertEquals(1, pair.first().getId());
        assertEquals(2, pair.second().getId());
    }

    @Test
    void returnsEmptyWhenThereAreNoFinalStates(){

        ResidualState q0 =
                new ResidualState(0, p);

        ResidualState q1 =
                new ResidualState(1, new Not(p));

        ResidualState q2 =
                new ResidualState(2, new And(p, p));

        Map<ResidualState, Map<Valuation, ResidualState>>
                transitions = new LinkedHashMap<>();

        transitions.put(q0, Map.of(
                empty, q1,
                pTrue, q2
        ));

        transitions.put(q1, Map.of(
                empty, q1,
                pTrue, q1
        ));

        transitions.put(q2, Map.of(
                empty, q2,
                pTrue, q2
        ));

        ResidualAutomaton automaton =
                new ResidualAutomaton(
                        alphabet,
                        List.of(q0, q1, q2),
                        q0,
                        transitions
                );

        DResResult result = finder.find(automaton);

        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void returnsEmptyWhenOnlyOneEligibleStateExists(){

        ResidualState q0 =
                new ResidualState(0, p);

        ResidualState q1 =
                new ResidualState(1, new Not(p));

        ResidualState qf =
                new ResidualState(2, new TrueFormula());

        Map<ResidualState, Map<Valuation, ResidualState>>
                transitions = new LinkedHashMap<>();

        transitions.put(q0, Map.of(
                empty, q1,
                pTrue, qf
        ));

        transitions.put(q1, Map.of(
                empty, q1,
                pTrue, q1
        ));

        transitions.put(qf, Map.of(
                empty, qf,
                pTrue, qf
        ));

        ResidualAutomaton automaton =
                new ResidualAutomaton(
                        alphabet,
                        List.of(q0, q1, qf),
                        q0,
                        transitions
                );

        DResResult result = finder.find(automaton);

        assertTrue(result.isEmpty());
    }

    @Test
    void emptyValuationIsConsumedAsAnOrdinarySymbol(){

        DResResult result =
                finder.find(validAutomaton());

        // the valid pair requires the empty valuation
        // as one of the actual trace positions.
        assertEquals(1, result.pairCount());
    }

    @Test
    void resultCollectionsAreImmutable() {

        DResResult result =
                finder.find(validAutomaton());

        assertThrows(
                UnsupportedOperationException.class,
                () -> result.getStates().clear()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> result.getPairs().clear()
        );
    }

    @Test
    void returnsEmptyWhenNoInterchangeablePairExists() {

        ResidualState q0 =
                new ResidualState(0, p);

        ResidualState q1 =
                new ResidualState(1, new Not(p));

        ResidualState q2 =
                new ResidualState(2, new And(p, p));

        ResidualState qf =
                new ResidualState(3, new TrueFormula());

        Map<ResidualState, Map<Valuation, ResidualState>>
                transitions = new LinkedHashMap<>();

        // initial state.
        transitions.put(q0, Map.of(
                empty, q1,
                pTrue, q2
        ));

        // q1 can reach the final state only through p.
        transitions.put(q1, Map.of(
                empty, q1,
                pTrue, qf
        ));

        // q2 cannot reach the final state.
        transitions.put(q2, Map.of(
                empty, q2,
                pTrue, q2
        ));

        // final state loops to itself.
        transitions.put(qf, Map.of(
                empty, qf,
                pTrue, qf
        ));

        ResidualAutomaton automaton =
                new ResidualAutomaton(
                        alphabet,
                        List.of(q0, q1, q2, qf),
                        q0,
                        transitions
                );

        DResResult result = finder.find(automaton);

        assertTrue(result.isEmpty());
        assertEquals(0, result.pairCount());
    }

    @Test
    void doesNotAcceptReversedCandidatePairs(){

        ResidualState q0 =
                new ResidualState(0, p);

        ResidualState q1 =
                new ResidualState(1, new Not(p));

        ResidualState q2 =
                new ResidualState(2, new And(p, p));

        ResidualState qf =
                new ResidualState(3, new TrueFormula());

        Map<ResidualState, Map<Valuation, ResidualState>>
                transitions = new LinkedHashMap<>();

        // Both valuations take the initial state to q1.
        transitions.put(q0, Map.of(
                empty, q1,
                pTrue, q1
        ));

        // q1 cannot reach the final state.
        transitions.put(q1, Map.of(
                empty, q1,
                pTrue, q1
        ));

        // Both valuations take q2 to the final state.
        transitions.put(q2, Map.of(
                empty, qf,
                pTrue, qf
        ));

        // Final state loops to itself.
        transitions.put(qf, Map.of(
                empty, qf,
                pTrue, qf
        ));

        ResidualAutomaton automaton =
                new ResidualAutomaton(
                        alphabet,
                        List.of(q0, q1, q2, qf),
                        q0,
                        transitions
                );

        DResResult result = finder.find(automaton);

        assertTrue(result.isEmpty());
        assertEquals(0, result.pairCount());
    }
}