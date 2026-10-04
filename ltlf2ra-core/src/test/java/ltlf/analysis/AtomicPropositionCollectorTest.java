package ltlf.analysis;

import ltlf.ast.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class AtomicPropositionCollectorTest {

    private final AtomicPropositionCollector collector = new AtomicPropositionCollector();

    @Test
    void constantsContainNoAtoms() {
        assertTrue(collector.collect(new TrueFormula()).isEmpty());
        assertTrue(collector.collect(new FalseFormula()).isEmpty());
    }

    @Test
    void collectsSingleAtom() {
        assertEquals(
                Set.of(new AtomicProposition("p")),
                collector.collect(
                        new AtomicProposition("p")
                )
        );
    }

    @Test
    void removesDuplicateOccurrences() {
        Formula f = new And(
                            new AtomicProposition("p"),
                            new Or(
                                    new AtomicProposition("p"),
                                    new AtomicProposition("q")
                            )
        );

        assertEquals(
                Set.of(new AtomicProposition("p"), new AtomicProposition("q")),
                collector.collect(f)
        );
    }

    @Test
    void traversesEveryOperator() {
        Formula f =
                new Until(
                        new Not(
                                new AtomicProposition("a")),
                        new Always(
                                new Eventually(
                                        new Next(
                                                new AtomicProposition("b")
                                        )
                                )
                        )
                );

        assertEquals(
                Set.of(new AtomicProposition("a"), new AtomicProposition("b")),
                collector.collect(f)
        );
    }

    @Test
    void collectorCanBeReused() {

        collector.collect(
                new And(
                        new AtomicProposition("a"),
                        new AtomicProposition("b")
                )
        );

        assertEquals(
                Set.of(new AtomicProposition("c")),
                collector.collect(new AtomicProposition("c"))
        );
    }

    @Test
    void returnedSetIsDefensive() {

        Set<AtomicProposition> result = collector.collect(new AtomicProposition("a"));

        assertThrows(
                UnsupportedOperationException.class,
                () -> result.clear()
        );
    }

    @Test
    void nullFormulaIsRejectedByTraversal() {
        assertThrows(
                NullPointerException.class,
                () -> collector.collect(null)
        );
    }
}
