package ltlf.analysis;

import ltlf.ast.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FormulaSizeVisitorTest {

    private final FormulaSizeVisitor v = new FormulaSizeVisitor();

    @Test
    void constantsHaveSizeOne() {

        assertAll(
                () -> assertEquals(1, new TrueFormula().accept(v)),
                () -> assertEquals(1, new FalseFormula().accept(v))
        );
    }

    @Test
    void atomHasSizeOne() {

        assertEquals(1, new AtomicProposition("p").accept(v));
    }

    @Test
    void unaryAddsOneNode() {
        Formula p = new AtomicProposition("p");

        assertAll(
                () -> assertEquals(2, new Not(p).accept(v)),
                () -> assertEquals(2, new Next(p).accept(v)),
                () -> assertEquals(2, new Eventually(p).accept(v)),
                () -> assertEquals(2, new Always(p).accept(v))
        );
    }

    @Test
    void binaryAddsRootAndBothChildren() {
        Formula p = new AtomicProposition("p"),
                q = new AtomicProposition("q");

        assertAll(
                () -> assertEquals(3, new And(p, q).accept(v)),
                () -> assertEquals(3, new Or(p, q).accept(v)),
                () -> assertEquals(3, new Until(p, q).accept(v))
        );
    }

    @Test
    void nestedFormulaCountsEveryNode() {
        Formula f =
                new And(
                        new Not(
                                new AtomicProposition("p")
                        ),
                        new Next(
                                new AtomicProposition("q")
                        )
                );

        assertEquals(5, f.accept(v));
    }

    @Test
    void sharedObjectDoesNotChangeTreeSize() {
        Formula p = new AtomicProposition("p");

        assertEquals(3, new And(p, p).accept(v));
    }

    @Test
    void visitorIsReusable() {
        FormulaSizeVisitor x = new FormulaSizeVisitor();

        assertEquals(1, new AtomicProposition("a").accept(x));
        assertEquals(3,
                new And(
                        new AtomicProposition("a"),
                        new AtomicProposition("b")
                ).accept(x));
    }
}
