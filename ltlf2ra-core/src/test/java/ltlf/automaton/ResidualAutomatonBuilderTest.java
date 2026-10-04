package ltlf.automaton;

import ltlf.alphabet.*;
import ltlf.ast.*;
import ltlf.semantics.*;
import ltlf.transform.*;
import ltlf.testsupport.TestSupport;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class ResidualAutomatonBuilderTest {

    private ResidualAutomaton build(Formula f, String... aps) {
        Alphabet a = new Alphabet(TestSupport.apSet(aps));
        return
                new ResidualAutomatonBuilder(
                        new DefaultResidualSemantics(),
                        new Normalizer(
                                List.of(
                                        new Simplifier(),
                                        new BooleanCanonicalizer())
                        ),
                        Formula::equals,
                        a
                ).build(f);
    }

    @Test
    void nullInitialFormulaIsRejected() {
        ResidualAutomatonBuilder b =
                new ResidualAutomatonBuilder(
                        new DefaultResidualSemantics(),
                        new Normalizer(List.of(new Simplifier())),
                        Formula::equals,
                        new Alphabet(TestSupport.apSet("p"))
                );

        assertThrows(NullPointerException.class, () -> b.build(null));
    }

    @Test
    void initialStateIsPresentAndHasIdZero() {

        ResidualAutomaton a =
                build(new AtomicProposition("p"), "p");

        assertEquals(0, a.getInitialState().getId());
        assertTrue(a.getStates().contains(a.getInitialState()));
    }

    @Test
    void aAndNextBProducesFourReachableStates() {

        ResidualAutomaton a =
                build(
                        new And(
                                new AtomicProposition("a"),
                                new Next(new AtomicProposition("b")
                                )
                        ),
                        "a", "b"
                );
        assertEquals(4, a.size());
    }

    @Test
    void untilProducesThreeReachableStates() {

        ResidualAutomaton a =
                build(
                        new Until(
                                new AtomicProposition("p"),
                                new AtomicProposition("q")),
                        "p", "q"
                );
        assertEquals(3, a.size());
    }

    @Test
    void eventuallyProducesTwoReachableStates() {

        ResidualAutomaton a =
                build(
                        new Eventually(new AtomicProposition("p")),
                        "p");

        assertEquals(2, a.size());
    }

    @Test
    void alwaysProducesTwoReachableStates() {
        ResidualAutomaton a =
                build(
                        new Always(new AtomicProposition("p")),
                        "p"
                );
        assertEquals(2, a.size());
    }

    @Test
    void everyReachableStateHasACompleteAlphabetRow() {
        ResidualAutomaton a =
                build(
                        new And(
                                new AtomicProposition("a"),
                                new Next(new AtomicProposition("b"))),
                        "a", "b"
                );

        for (ResidualState s : a.getStates())
            assertEquals(
                    a.getAlphabet().size(),
                    a.getOutgoingTransitions(s).size()
            );
    }

    @Test
    void noUnreachableStatesAreStored() {

        ResidualAutomaton a =
                build(
                        new Eventually(new AtomicProposition("p")),
                        "p"
                );

        Set<ResidualState> reached = new HashSet<>();
        ArrayDeque<ResidualState> q = new ArrayDeque<>();

        reached.add(a.getInitialState());
        q.add(a.getInitialState());
        while (!q.isEmpty()) {
            ResidualState s = q.remove();
            for (ResidualState d : a.getOutgoingTransitions(s).values())
                if (reached.add(d))
                    q.add(d);
        }

        assertEquals(new HashSet<>(a.getStates()), reached);
    }
}
