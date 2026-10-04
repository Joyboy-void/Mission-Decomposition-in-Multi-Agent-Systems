package ltlf.ast;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrTest {

    @Test
    void rejectsNullLeft() {

        assertThrows(
                NullPointerException.class,
                () -> new Or(null, new AtomicProposition("p"))
        );
    }

    @Test
    void rejectsNullRight() {

        assertThrows(
                NullPointerException.class,
                () -> new Or(new AtomicProposition("p"), null)
        );
    }

    @Test
    void exposesChildren() {
        Formula p = new AtomicProposition("p"), q = new AtomicProposition("q");
        Or f = new Or(p, q);

        assertSame(p, f.getLeft());
        assertSame(q, f.getRight());
    }

    @Test
    void equalChildrenProduceEqualFormulas() {
        assertEquals(
                new Or(new AtomicProposition("p"), new AtomicProposition("q")),
                new Or(new AtomicProposition("p"), new AtomicProposition("q"))
        );
    }

    @Test
    void swappingChildrenChangesStructuralEquality() {
        assertNotEquals(
                new Or(new AtomicProposition("p"), new AtomicProposition("q")),
                new Or(new AtomicProposition("q"), new AtomicProposition("p"))
        );
    }

    @Test
    void exactToString() {
        assertEquals(
                "(p ∨ q)",
                new Or(new AtomicProposition("p"), new AtomicProposition("q")).toString()
        );
    }

    @Test
    void equalFormulasHaveEqualHashCodes() {
        assertEquals(
                new Or(new AtomicProposition("p"), new AtomicProposition("q")).hashCode(),
                new Or(new AtomicProposition("p"), new AtomicProposition("q")).hashCode()
        );
    }

    @Test
    void distinctBinaryOperatorsAreNotEqual() {
        assertNotEquals(
                new Or(new AtomicProposition("p"), new AtomicProposition("q")),
                new And(new AtomicProposition("p"), new AtomicProposition("q"))
        );
    }
}
