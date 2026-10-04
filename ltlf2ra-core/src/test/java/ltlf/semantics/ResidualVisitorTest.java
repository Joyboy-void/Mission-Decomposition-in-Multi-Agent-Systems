package ltlf.semantics;

import ltlf.alphabet.*;
import ltlf.ast.*;
import ltlf.testsupport.TestSupport;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ResidualVisitorTest {

    private final AtomicPropositionSet set = TestSupport.apSet("p", "q");

    private Valuation v(String... ps) {
        return TestSupport.valuation(set, ps);
    }

    @Test
    void trueResidualIsTrue() {
        assertSame(
                TrueFormula.class,
                new TrueFormula().accept(new ResidualVisitor(v())).getClass()
        );
    }

    @Test
    void falseResidualIsFalse() {
        assertEquals(
                new FalseFormula(),
                new FalseFormula().accept(new ResidualVisitor(v()))
        );
    }

    @Test
    void atomDependsOnValuation() {
        Formula p = new AtomicProposition("p");

        assertEquals(
                new TrueFormula(),
                p.accept(new ResidualVisitor(v("p")))
        );

        assertEquals(
                new FalseFormula(),
                p.accept(new ResidualVisitor(v("q")))
        );
    }

    @Test
    void notResidualWrapsChildResidual() {
        Formula f = new Not(new AtomicProposition("p"));

        assertEquals(
                new Not(new TrueFormula()),
                f.accept(new ResidualVisitor(v("p")))
        );
    }

    @Test
    void nextResidualIsItsOperand() {
        Formula p = new AtomicProposition("p");

        assertSame(
                p,
                new Next(p).accept(new ResidualVisitor(v()))
        );
    }

    @Test
    void eventuallyResidualUsesCurrentAndFutureObligation() {
        Formula f = new Eventually(new AtomicProposition("p"));
        Formula expected = new Or(new TrueFormula(), f);

        assertEquals(
                expected,
                f.accept(new ResidualVisitor(v("p")))
        );
    }

    @Test
    void alwaysResidualKeepsAlwaysObligation() {
        Formula f = new Always(new AtomicProposition("p"));
        Formula expected = new And(new TrueFormula(), f);

        assertEquals(
                expected,
                f.accept(new ResidualVisitor(v("p")))
        );
    }

    @Test
    void untilResidualUsesRightOrLeftAndOriginalUntil() {
        Formula f = new Until(new AtomicProposition("p"), new AtomicProposition("q"));
        Formula expected = new Or(new FalseFormula(), new And(new TrueFormula(), f));

        assertEquals(
                expected,
                f.accept(new ResidualVisitor(v("p")))
        );
    }
}
