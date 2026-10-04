package ltlf.alphabet;

import ltlf.ast.AtomicProposition;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class AtomicPropositionSetTest {

    @Test
    void sortsPropositionsDeterministically() {
        AtomicPropositionSet s = new AtomicPropositionSet(
                List.of(
                        new AtomicProposition("z"),
                        new AtomicProposition("a"),
                        new AtomicProposition("m")
                ));

        assertEquals(
                List.of(
                        new AtomicProposition("a"),
                        new AtomicProposition("m"),
                        new AtomicProposition("z")),

                s.asList());
    }

    @Test
    void assignsStableIndices() {
        AtomicPropositionSet s = new AtomicPropositionSet(
                List.of(
                        new AtomicProposition("b"),
                        new AtomicProposition("a")
                ));

        assertEquals(0, s.indexOf(new AtomicProposition("a")));
        assertEquals(1, s.indexOf(new AtomicProposition("b")));
    }

    @Test
    void containsRecognizesMembers() {
        AtomicPropositionSet s =
                new AtomicPropositionSet(List.of(new AtomicProposition("a")));

        assertTrue(s.contains(new AtomicProposition("a")));
    }

    @Test
    void containsRejectsNonMembers() {
        AtomicPropositionSet s =
                new AtomicPropositionSet(List.of(new AtomicProposition("a")));

        assertFalse(s.contains(new AtomicProposition("b")));
    }

    @Test
    void duplicatePropositionsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new AtomicPropositionSet(
                        List.of(
                                new AtomicProposition("a"),
                                new AtomicProposition("a"))
                )
        );
    }

    @Test
    void unknownIndexLookupIsRejected() {
        AtomicPropositionSet s =
                new AtomicPropositionSet(List.of(new AtomicProposition("a")));

        assertThrows(
                IllegalArgumentException.class,
                () -> s.indexOf(
                        new AtomicProposition("b")
                )
        );
    }

    @Test
    void invalidIndicesAreRejected() {
        AtomicPropositionSet s =
                new AtomicPropositionSet(List.of(new AtomicProposition("a")));

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> s.get(-1)),
                () -> assertThrows(IllegalArgumentException.class, () -> s.get(1)));
    }

    @Test
    void asListIsImmutable() {
        AtomicPropositionSet s =
                new AtomicPropositionSet(List.of(new AtomicProposition("a")));

        assertThrows(
                UnsupportedOperationException.class,
                () -> s.asList().add(new AtomicProposition("b"))
        );
    }
}
