package ltlf.semantics;

import ltlf.alphabet.*;
import ltlf.ast.*;
import ltlf.testsupport.TestSupport;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DefaultResidualSemanticsTest {

    private final AtomicPropositionSet set = TestSupport.apSet("p");
    private final DefaultResidualSemantics s = new DefaultResidualSemantics();

    private Valuation v(String... ps) {
        return TestSupport.valuation(set, ps);
    }

    @Test
    void nullFormulaIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> s.residual(null, v())
        );
    }

    @Test
    void nullValuationIsRejected() {

        assertThrows(
                NullPointerException.class,
                () -> s.residual(new AtomicProposition("p"), null)
        );
    }

    @Test
    void trueRuleIsPreserved() {
        assertEquals(
                new TrueFormula(),
                s.residual(new TrueFormula(), v())
        );
    }

    @Test
    void atomRuleIsCorrect() {

        assertAll(
                () -> assertEquals(
                        new TrueFormula(),
                        s.residual(new AtomicProposition("p"), v("p"))
                ),
                () -> assertEquals(
                        new FalseFormula(),
                        s.residual(new AtomicProposition("p"), v())
                )
        );
    }

    @Test
    void nextRuleReturnsOperand() {
        Formula p = new AtomicProposition("p");
        assertSame(
                p,
                s.residual(new Next(p), v())
        );
    }

    @Test
    void eventuallyRuleMatchesResidualVisitor() {

        Formula f = new Eventually(new AtomicProposition("p"));

        assertEquals(
                new Or(new FalseFormula(), f),
                s.residual(f, v())
        );
    }

    @Test
    void booleanRulesAreCompositional() {

        Formula f =
                new And(
                        new AtomicProposition("p"),
                        new TrueFormula()
                );

        assertEquals(
                new And(new FalseFormula(), new TrueFormula()),
                s.residual(f, v())
        );
    }
}
