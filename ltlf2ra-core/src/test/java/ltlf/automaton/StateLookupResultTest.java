package ltlf.automaton;

import ltlf.ast.AtomicProposition;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StateLookupResultTest {

    private ResidualState state() {
        return new ResidualState(0, new AtomicProposition("p"));
    }

    @Test
    void nullStateRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new StateLookupResult(null, true)
        );
    }

    @Test
    void storesStateReference() {
        ResidualState s = state();

        assertSame(
                s,
                new StateLookupResult(s, true).getState()
        );
    }

    @Test
    void storesCreatedTrue() {

        assertTrue(
                new StateLookupResult(state(), true).isCreated());
    }

    @Test
    void storesCreatedFalse() {
        assertFalse(
                new StateLookupResult(state(), false).isCreated());
    }

    @Test
    void createdFlagDoesNotAlterState() {
        ResidualState s = state();
        StateLookupResult a = new StateLookupResult(s, true),
                          b = new StateLookupResult(s, false);

        assertSame(a.getState(), b.getState());
    }
}
