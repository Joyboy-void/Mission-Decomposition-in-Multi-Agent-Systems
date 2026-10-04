package ltlf.ast;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EventuallyTest {

    @Test
    void rejectsNullOperand() {
        assertThrows(
                NullPointerException.class,
                () -> new Eventually(null)
        );
    }

    @Test
    void exposesOperand() {

        AtomicProposition p = new AtomicProposition("p");

        assertSame(p, new Eventually(p).getOperand());
    }

    @Test
    void equalOperandsProduceEqualFormulas() {

        assertEquals(
                new Eventually( new AtomicProposition("p")),
                new Eventually(
                        new AtomicProposition("p")
                )
        );
    }

    @Test
    void differentOperandsProduceDifferentFormulas() {

        assertNotEquals(
                new Eventually(new AtomicProposition("p")),
                new Eventually(new AtomicProposition("q"))
        );
    }

    @Test
    void exactToString() {

        assertEquals(
                "F(p)",
                new Eventually(new AtomicProposition("p")).toString()
        );
    }

    @Test
    void equalFormulasHaveEqualHashCodes() {

        assertEquals(
                new Eventually(new AtomicProposition("p")).hashCode(),
                new Eventually(new AtomicProposition("p")).hashCode()
        );
    }

    @Test
    void distinctUnaryOperatorsAreNotEqual() {
        assertNotEquals(
                new Eventually(new AtomicProposition("p")),
                new Next(new AtomicProposition("p"))
        );
    }

    @Test
    void visitorDispatches() {
        String result =
                new Eventually(new AtomicProposition("p")).accept(
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
        assertEquals("eventually", result);
    }
}
