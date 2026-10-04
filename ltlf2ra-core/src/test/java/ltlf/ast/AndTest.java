package ltlf.ast;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AndTest {

    @Test
    void rejectsNullLeft() {
        assertThrows(
                NullPointerException.class,
                () -> new And(
                        null,
                        new AtomicProposition("p")
                )
        );
    }

    @Test
    void rejectsNullRight() {
        assertThrows(
                NullPointerException.class,
                () -> new And
                        (new AtomicProposition("p"), null)
        );
    }

    @Test
    void exposesChildren() {
        Formula p = new AtomicProposition("p"),
                q = new AtomicProposition("q");

        And f = new And(p, q);

        assertSame(p, f.getLeft());
        assertSame(q, f.getRight());
    }

    @Test
    void equalChildrenProduceEqualFormulas() {

        assertEquals(
                new And(new AtomicProposition("p"), new AtomicProposition("q")),
                new And(new AtomicProposition("p"), new AtomicProposition("q"))
        );
    }

    @Test
    void swappingChildrenChangesStructuralEquality() {

        assertNotEquals(
                new And(new AtomicProposition("p"), new AtomicProposition("q")),
                new And(new AtomicProposition("q"), new AtomicProposition("p"))
        );
    }

    @Test
    void exactToString() {
        assertEquals(
                "(p ∧ q)",
                new And(
                        new AtomicProposition("p"),
                        new AtomicProposition("q")).toString()
        );
    }

    @Test
    void equalFormulasHaveEqualHashCodes() {

        assertEquals(
                new And(new AtomicProposition("p"), new AtomicProposition("q")).hashCode(),
                new And(new AtomicProposition("p"), new AtomicProposition("q")).hashCode()
        );
    }

    @Test
    void distinctBinaryOperatorsAreNotEqual() {

        assertNotEquals(
                new And(new AtomicProposition("p"), new AtomicProposition("q")),
                new Or(new AtomicProposition("p"), new AtomicProposition("q"))
        );
    }
}
