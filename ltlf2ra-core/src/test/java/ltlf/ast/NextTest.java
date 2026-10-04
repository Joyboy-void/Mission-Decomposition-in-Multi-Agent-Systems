package ltlf.ast;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NextTest {

    @Test
    void rejectsNullOperand() {
        assertThrows(
                NullPointerException.class,
                () -> new Next(null)
        );
    }

    @Test
    void exposesOperand() {
        AtomicProposition p = new AtomicProposition("p");
        assertSame(
                p,
                new Next(p).getOperand()
        );
    }

    @Test
    void equalOperandsProduceEqualFormulas() {

        assertEquals(
                new Next(new AtomicProposition("p")),
                new Next(new AtomicProposition("p"))
        );
    }

    @Test
    void differentOperandsProduceDifferentFormulas() {

        assertNotEquals(
                new Next(new AtomicProposition("p")),
                new Next(new AtomicProposition("q"))
        );
    }

    @Test
    void exactToString() {

        assertEquals(
                "X(p)",
                new Next(new AtomicProposition("p")).toString()
        );
    }

    @Test
    void equalFormulasHaveEqualHashCodes() {

        assertEquals(
                new Next(new AtomicProposition("p")).hashCode(),
                new Next(new AtomicProposition("p")).hashCode()
        );
    }

    @Test
    void distinctUnaryOperatorsAreNotEqual() {

        assertNotEquals(
                new Next(new AtomicProposition("p")),
                new Not(new AtomicProposition("p"))
        );
    }

    @Test
    void visitorDispatches() {
        String result =
                new Next(new AtomicProposition("p")).accept(
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
        assertEquals("next", result);
    }
}
