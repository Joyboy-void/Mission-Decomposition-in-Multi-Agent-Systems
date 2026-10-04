package ltlf.automaton;

import ltlf.alphabet.*;
import ltlf.ast.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class ResidualAutomatonTest {

    private final AtomicPropositionSet aps =
            new AtomicPropositionSet( List.of(new AtomicProposition("p")));

    private final Alphabet alphabet = new Alphabet(aps);

    private final ResidualState q0 = new ResidualState(0, new AtomicProposition("p"));
    private final ResidualState q1 = new ResidualState(1, new TrueFormula());
    private final Valuation empty = Valuation.empty(aps);

    private ResidualAutomaton automaton() {
        Map<ResidualState, Map<Valuation, ResidualState>> m = new LinkedHashMap<>();

        m.put(q0, Map.of(empty, q1));

        return new ResidualAutomaton(alphabet, List.of(q0, q1), q0, m);
    }

    @Test
    void constructorRejectsNullAlphabet() {

        assertThrows(
                NullPointerException.class,
                () -> new ResidualAutomaton(null, List.of(q0), q0, Map.of())
        );
    }

    @Test
    void constructorRejectsNullInitialState() {

        assertThrows(
                NullPointerException.class,
                () -> new ResidualAutomaton(alphabet, List.of(q0), null, Map.of())
        );
    }

    @Test
    void gettersExposeCoreStructure() {
        ResidualAutomaton a = automaton();

        assertAll(
                () -> assertSame(alphabet, a.getAlphabet()),
                () -> assertEquals(List.of(q0, q1), a.getStates()),
                () -> assertSame(q0, a.getInitialState()), () -> assertEquals(2, a.size()));
    }

    @Test
    void destinationLookupWorks() {
        ResidualAutomaton a = automaton();

        assertSame(q1, a.getDestination(q0, empty));
        assertNull(a.getDestination(q1, empty));
    }

    @Test
    void missingOutgoingTransitionsAreEmpty() {
        assertTrue(automaton().getOutgoingTransitions(q1).isEmpty());
    }

    @Test
    void acceptingIsDerivedFromTrueFormula() {
        ResidualAutomaton a = automaton();
        assertTrue(a.isAccepting(q1));
        assertFalse(a.isAccepting(q0));
    }

    @Test
    void stateListIsImmutable() {
        ResidualAutomaton a = automaton();
        assertThrows(
                UnsupportedOperationException.class,
                () -> a.getStates().clear()
        );
    }

    @Test
    void transitionMapIsImmutable() {

        ResidualAutomaton a = automaton();

        assertThrows(
                UnsupportedOperationException.class,
                () -> a.getTransitions().clear()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> a.getOutgoingTransitions(q0).clear()
        );
    }

}
