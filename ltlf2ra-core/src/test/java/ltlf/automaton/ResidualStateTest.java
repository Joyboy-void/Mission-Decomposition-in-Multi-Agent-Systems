package ltlf.automaton;

import ltlf.ast.AtomicProposition;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ResidualStateTest {

    @Test
    void storesId() {

        assertEquals(
                7,
                new ResidualState(7, new AtomicProposition("p")).getId()
        );
    }

    @Test
    void storesFormula() {
        AtomicProposition p = new AtomicProposition("p");

        assertSame(p, new ResidualState(0, p).getFormula());
    }

    @Test
    void nullFormulaRejected() {

        assertThrows(
                NullPointerException.class,
                () -> new ResidualState(0, null)
        );
    }

    @Test
    void exactToString() {

        assertEquals(
                "q3: p",
                new ResidualState(3, new AtomicProposition("p")).toString()
        );
    }

    @Test
    void differentInstancesAreIdentityBased() {
        ResidualState a = new ResidualState(0, new AtomicProposition("p")),
                      b = new ResidualState(0, new AtomicProposition("p"));

        assertNotEquals(a, b);
    }

    @Test
    void distinctIdsMayExist() {
        assertNotEquals(
                new ResidualState(0, new AtomicProposition("p")).getId(),
                new ResidualState(1, new AtomicProposition("p")).getId()
        );
    }
}
