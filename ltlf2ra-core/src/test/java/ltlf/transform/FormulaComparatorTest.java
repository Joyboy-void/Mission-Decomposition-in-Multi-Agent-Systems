package ltlf.transform;

import ltlf.ast.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FormulaComparatorTest {

    private final FormulaComparator c = FormulaComparator.INSTANCE;

    @Test
    void constantsComeBeforeAtoms() {
        assertTrue(
                c.compare(new TrueFormula(), new AtomicProposition("a")) < 0
        );
        assertTrue(
                c.compare(new FalseFormula(), new AtomicProposition("a")) < 0
        );
    }

    @Test
    void atomsAreOrderedByName() {
        assertTrue(
                c.compare(
                        new AtomicProposition("a"),
                        new AtomicProposition("b")
                ) < 0
        );
        assertEquals(
                0,
                c.compare(
                        new AtomicProposition("a"),
                        new AtomicProposition("a"))
        );
    }

    @Test
    void unaryRankFollowsDeclaredOrder() {
        assertTrue(
                c.compare(
                        new Not(new AtomicProposition("a")),
                        new Next(new AtomicProposition("a"))) < 0);

        assertTrue(
                c.compare(
                        new Next(new AtomicProposition("a")),
                        new Eventually(new AtomicProposition("a"))) < 0
        );

        assertTrue(
                c.compare(
                        new Eventually(new AtomicProposition("a")),
                        new Always(new AtomicProposition("a"))) < 0
        );
    }

    @Test
    void binaryRankFollowsDeclaredOrder() {
        assertTrue(
                c.compare(
                        new And(new AtomicProposition("a"), new AtomicProposition("b")),
                        new Or(new AtomicProposition("a"), new AtomicProposition("b"))) < 0
        );
        assertTrue(
                c.compare(
                        new Or(new AtomicProposition("a"), new AtomicProposition("b")),
                        new Until(new AtomicProposition("a"), new AtomicProposition("b"))) < 0
        );
    }

    @Test
    void unaryOperandsBreakTies() {
        assertTrue(
                c.compare(
                        new Not(new AtomicProposition("a")),
                        new Not(new AtomicProposition("b"))) < 0
        );
    }

    @Test
    void binaryLeftChildBreaksTieBeforeRightChild() {

        Formula f1 = new And(new AtomicProposition("a"), new AtomicProposition("z"));
        Formula f2 = new And(new AtomicProposition("b"), new AtomicProposition("a"));

        assertTrue(
                c.compare(f1, f2) < 0
        );
    }

    @Test
    void comparisonIsAntisymmetricForDistinctFormulas() {
        Formula a = new Eventually(new AtomicProposition("a")),
                b = new Eventually(new AtomicProposition("b"));

        assertTrue(c.compare(a, b) < 0);
        assertTrue(c.compare(b, a) > 0);
    }

    @Test
    void compareEqualFormulasReturnsZero() {
        Formula f =
                new Until(
                        new AtomicProposition("a"),
                        new AtomicProposition("b")
                );

        assertEquals(0, c.compare(f, f));

        assertEquals(
                0,
                c.compare(
                            f,
                            new Until(new AtomicProposition("a"), new AtomicProposition("b"))
                )
        );
    }
}
