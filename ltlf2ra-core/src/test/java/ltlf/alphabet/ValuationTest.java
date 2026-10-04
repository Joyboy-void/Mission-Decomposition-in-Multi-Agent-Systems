package ltlf.alphabet;

import ltlf.ast.AtomicProposition;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class ValuationTest {

    private AtomicPropositionSet set() {
        return new AtomicPropositionSet(
                List.of(
                        new AtomicProposition("a"),
                        new AtomicProposition("b"))
        );
    }

    @Test
    void emptyValuationContainsNothing() {
        Valuation v = Valuation.empty(set());

        assertFalse(v.contains(new AtomicProposition("a")));
        assertFalse(v.contains(new AtomicProposition("b")));
    }

    @Test
    void ofCreatesExpectedMembership() {
        AtomicPropositionSet s = set();

        Valuation v = Valuation.of(s, new AtomicProposition("a"));

        assertTrue(v.contains(new AtomicProposition("a")));
        assertFalse(v.contains(new AtomicProposition("b")));
    }

    @Test
    void unknownPropositionIsRejected() {
        AtomicPropositionSet s = set();

        assertThrows(
                IllegalArgumentException.class,
                () -> Valuation.of(
                        s,
                        new AtomicProposition("c"))
        );
    }

    @Test
    void asBitSetIsDefensive() {
        AtomicPropositionSet s = set();

        Valuation v = Valuation.of(s, new AtomicProposition("a"));
        BitSet b = v.asBitSet();

        b.clear();

        assertTrue(v.contains(new AtomicProposition("a")));
    }

    @Test
    void fromBitsCopiesInput() {
        AtomicPropositionSet s = set();

        BitSet b = new BitSet();
        b.set(0);
        Valuation v = Valuation.fromBits(s, b);
        b.clear();

        assertTrue(v.contains(new AtomicProposition("a")));
    }

    @Test
    void equalValuationsAreEqual() {
        AtomicPropositionSet s = set();
        assertEquals(
                Valuation.of(
                        s,
                        new AtomicProposition("a")),
                Valuation.of(
                        s,
                        new AtomicProposition("a"))
        );
    }

    @Test
    void differentValuationsAreNotEqual() {
        AtomicPropositionSet s = set();

        assertNotEquals(
                Valuation.of(s, new AtomicProposition("a")),
                Valuation.of(s, new AtomicProposition("b")));
    }

    @Test
    void exactToStringIsStable() {
        AtomicPropositionSet s = set();

        assertEquals(
                "{a, b }",
                Valuation.of(
                        s,
                        new AtomicProposition("a"),
                        new AtomicProposition("b")
                ).toString()
        );
    }
}
