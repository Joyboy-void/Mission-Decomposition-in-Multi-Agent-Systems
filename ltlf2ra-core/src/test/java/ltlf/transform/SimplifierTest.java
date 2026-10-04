package ltlf.transform;

import ltlf.ast.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimplifierTest {

    private final Simplifier s = new Simplifier();

    @Test
    void negationOfTrueBecomesFalse() {
        assertEquals(
                new FalseFormula(),
                s.transform(new Not(new TrueFormula()))
        );
    }

    @Test
    void negationOfFalseBecomesTrue() {
        assertEquals(
                new TrueFormula(),
                s.transform(new Not(new FalseFormula()))
        );
    }

    @Test
    void doubleNegationIsRemoved() {
        Formula p = new AtomicProposition("p");

        assertEquals(
                p,
                s.transform(new Not(new Not(p)))
        );
    }

    @Test
    void andIdentityAndAnnihilatorWork() {
        Formula p = new AtomicProposition("p");

        assertAll(
                () -> assertEquals(p, s.transform(new And(new TrueFormula(), p))),
                () -> assertEquals(new FalseFormula(), s.transform(new And(new FalseFormula(), p))),
                () -> assertEquals(p, s.transform(new And(p, new TrueFormula()))),
                () -> assertEquals(new FalseFormula(), s.transform(new And(p, new FalseFormula())))
        );
    }

    @Test
    void orIdentityAndAnnihilatorWork() {
        Formula p = new AtomicProposition("p");

        assertAll(
                () -> assertEquals(p, s.transform(new Or(new FalseFormula(), p))),
                () -> assertEquals(new TrueFormula(), s.transform(new Or(new TrueFormula(), p))),
                () -> assertEquals(p, s.transform(new Or(p, new FalseFormula()))),
                () -> assertEquals(new TrueFormula(), s.transform(new Or(p, new TrueFormula())))
        );
    }

    @Test
    void duplicateBooleanOperandsCollapse() {
        Formula p = new AtomicProposition("p");

        assertEquals(p, s.transform(new And(p, p)));
        assertEquals(p, s.transform(new Or(p, p)));
    }

    @Test
    void nestedEventuallyAndAlwaysCollapse() {
        Formula p = new AtomicProposition("p");

        assertEquals(new Eventually(p), s.transform(new Eventually(new Eventually(p))));
        assertEquals(new Always(p), s.transform(new Always(new Always(p))));
    }

    @Test
    void recursionSimplifiesChildrenBeforeParent() {
        Formula p = new AtomicProposition("p");
        Formula f = new And(new Not(new Not(p)), new TrueFormula());

        assertEquals(p, s.transform(f));
    }
}
