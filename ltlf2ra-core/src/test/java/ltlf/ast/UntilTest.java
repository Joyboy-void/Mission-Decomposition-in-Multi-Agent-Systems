package ltlf.ast;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UntilTest {

    @Test
    void rejectsNullLeft() {
        assertThrows(
                NullPointerException.class,
                () -> new Until(null, new AtomicProposition("p"))
        );
    }

    @Test
    void rejectsNullRight() {

        assertThrows(
                NullPointerException.class,
                () -> new Until(new AtomicProposition("p"), null)
        );
    }

    @Test
    void exposesChildren() {
        Formula p = new AtomicProposition("p"), q = new AtomicProposition("q");
        Until f = new Until(p, q);

        assertSame(p, f.getLeft());
        assertSame(q, f.getRight());
    }

    @Test
    void equalChildrenProduceEqualFormulas() {
        assertEquals(
                new Until(new AtomicProposition("p"), new AtomicProposition("q")),
                new Until(new AtomicProposition("p"), new AtomicProposition("q"))
        );
    }

    @Test
    void swappingChildrenChangesStructuralEquality() {
        assertNotEquals(
                new Until(new AtomicProposition("p"), new AtomicProposition("q")),
                new Until(new AtomicProposition("q"), new AtomicProposition("p"))
        );
    }

    @Test
    void exactToString() {

        assertEquals(
                "(p U q)",
                new Until(new AtomicProposition("p"), new AtomicProposition("q")).toString()
        );
    }

    @Test
    void equalFormulasHaveEqualHashCodes() {
        assertEquals(
                new Until(new AtomicProposition("p"), new AtomicProposition("q")).hashCode(),
                new Until(new AtomicProposition("p"), new AtomicProposition("q")).hashCode()
        );
    }

    @Test
    void distinctBinaryOperatorsAreNotEqual() {
        assertNotEquals(
                new Until(new AtomicProposition("p"), new AtomicProposition("q")),
                new And(new AtomicProposition("p"), new AtomicProposition("q"))
        );
    }
}
