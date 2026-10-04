package ltlf.ast;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NotTest {

    @Test
    void rejectsNullOperand() {

        assertThrows(
                NullPointerException.class,
                () -> new Not(null)
        );
    }

    @Test
    void exposesOperand() {
        AtomicProposition p = new AtomicProposition("p");
        assertSame(p, new Not(p).getOperand());
    }

    @Test
    void equalOperandsProduceEqualFormulas() {
        assertEquals(
                new Not(new AtomicProposition("p")),
                new Not(new AtomicProposition("p"))
        );
    }

    @Test
    void differentOperandsProduceDifferentFormulas() {

        assertNotEquals(
                new Not(new AtomicProposition("p")),
                new Not(new AtomicProposition("q"))
        );
    }

    @Test
    void exactToString() {
        assertEquals(
                "¬(p)",
                new Not(new AtomicProposition("p")).toString()
        );
    }

    @Test
    void equalFormulasHaveEqualHashCodes() {

        assertEquals(
                new Not(new AtomicProposition("p")).hashCode(),
                new Not(new AtomicProposition("p")).hashCode()
        );
    }

    @Test
    void distinctUnaryOperatorsAreNotEqual() {

        assertNotEquals(
                new Not(new AtomicProposition("p")),
                new Next(new AtomicProposition("p"))
        );
    }

    @Test
    void visitorDispatches() {
        String result =
                new Not(new AtomicProposition("p")).accept(
                        new FormulaVisitor<>() {
                            public String visitTrue(TrueFormula f) {
                                return "true";
                            }

                            public String visitFalse(FalseFormula f) {
                                return "false";
                            }

                            public String visitAtomicProposition(AtomicProposition f) {
                                return "ap";
                            }

                            public String visitNot(Not f) {
                                return "not";
                            }

                            public String visitNext(Next f) {
                                return "next";
                            }

                            public String visitEventually(Eventually f) {
                                return "eventually";
                            }

                            public String visitAlways(Always f) {
                                return "always";
                            }

                            public String visitAnd(And f) {
                                return "and";
                            }

                            public String visitOr(Or f) {
                                return "or";
                            }

                            public String visitUntil(Until f) {
                                return "until";
                            }
                        }
                );
        assertEquals("not", result);
    }
}
