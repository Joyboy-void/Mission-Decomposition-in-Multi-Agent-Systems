package ltlf.automaton;

import ltlf.alphabet.*;
import ltlf.ast.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TransitionTest {

    private final AtomicPropositionSet set =
            new AtomicPropositionSet(List.of(new AtomicProposition("p")));
    private final ResidualState s = new ResidualState(0, new AtomicProposition("p"));
    private final ResidualState d = new ResidualState(1, new TrueFormula());
    private final Valuation v = Valuation.empty(set);

    @Test
    void nullSourceRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new Transition(null, v, d)
        );
    }

    @Test
    void nullValuationRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new Transition(s, null, d)
        );
    }

    @Test
    void nullDestinationRejected() {

        assertThrows(
                NullPointerException.class,
                () -> new Transition(s, v, null)
        );
    }

    @Test
    void gettersReturnSuppliedObjects() {
        Transition t = new Transition(s, v, d);

        assertAll(
                () -> assertSame(s, t.getSource()),
                () -> assertSame(v, t.getValuation()),
                () -> assertSame(d, t.getDestination()));
    }

    @Test
    void exactToString() {
        assertEquals(
                "0 --- { } --> 1",
                new Transition(s, v, d).toString()
        );
    }
}
