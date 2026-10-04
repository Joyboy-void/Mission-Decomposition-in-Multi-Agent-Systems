package ltlf.integration;

import java.util.List;
import ltlf.alphabet.*;
import ltlf.ast.*;
import ltlf.semantics.*;
import ltlf.testsupport.TestSupport;
import ltlf.transform.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NormalizationResidualIntegrationTest {

    private final AtomicPropositionSet aps = TestSupport.apSet("a", "b");
    private final Valuation ab = TestSupport.valuation(aps, "a", "b");
    private final Valuation a = TestSupport.valuation(aps, "a");
    private final Normalizer n =
            new Normalizer(List.of(new Simplifier(), new BooleanCanonicalizer()));

    @Test
    void aAndNextBResidualOnAIsB() {
        Formula f =
                new And(
                        new AtomicProposition("a"),
                        new Next(new AtomicProposition("b")
                        )
                );
        Formula raw = new DefaultResidualSemantics().residual(f, a);

        assertEquals(new AtomicProposition("b"), n.normalize(raw));
    }

    @Test
    void aAndNextBResidualOnEmptyIsFalse() {

        Formula f =
                new And(
                        new AtomicProposition("a"),
                        new Next(new AtomicProposition("b")
                        )
                );

        Formula raw =
                new DefaultResidualSemantics().residual(f, TestSupport.valuation(aps));

        assertEquals(new FalseFormula(), n.normalize(raw));
    }

    @Test
    void pUntilQBecomesTrueWhenQHolds() {
        Formula f =
                new Until(
                        new AtomicProposition("a"),
                        new AtomicProposition("b")
                );

        Formula raw = new DefaultResidualSemantics().residual(f, ab);

        assertEquals(new TrueFormula(), n.normalize(raw));
    }

    @Test
    void eventuallyPBecomesTrueWhenPHolds() {
        Formula f =
                new Eventually(new AtomicProposition("a"));

        Formula raw = new DefaultResidualSemantics().residual(f, a);

        assertEquals(new TrueFormula(), n.normalize(raw));
    }

    @Test
    void alwaysPBecomesFalseWhenPDoesNotHold() {
        Formula f = new Always(new AtomicProposition("a"));

        Formula raw =
                new DefaultResidualSemantics().residual(f, TestSupport.valuation(aps, "b"));

        assertEquals(new FalseFormula(), n.normalize(raw));
    }
}
