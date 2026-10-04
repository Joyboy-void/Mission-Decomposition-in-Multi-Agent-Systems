package ltlf.ast;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AlwaysTest {

    @Test
    void rejectsNullOperand() {
        assertThrows(NullPointerException.class, () -> new Always(null));
    }

    @Test
    void exposesOperand() {
        AtomicProposition p = new AtomicProposition("p");

        assertSame(p, new Always(p).getOperand());
    }

    @Test
    void equalOperandsProduceEqualFormulas() {

        assertEquals(
                new Always(new AtomicProposition("p")),
                new Always(new AtomicProposition("p"))
        );
    }

    @Test
    void differentOperandsProduceDifferentFormulas() {

        assertNotEquals(
                new Always(new AtomicProposition("p")),
                new Always(new AtomicProposition("q"))
        );
    }

    @Test
    void exactToString() {

        assertEquals(
                "G(p)",
                new Always(new AtomicProposition("p")).toString()
        );
    }

    @Test
    void equalFormulasHaveEqualHashCodes() {
        assertEquals(
                new Always(new AtomicProposition("p")).hashCode(),
                new Always(new AtomicProposition("p")).hashCode()
        );
    }

    @Test
    void distinctUnaryOperatorsAreNotEqual() {

        assertNotEquals(
                new Always(new AtomicProposition("p")),
                new Next(new AtomicProposition("p"))
        );
    }

    @Test
    void visitorDispatches() {

        String result =
                new Always(
                        new AtomicProposition("p")).accept(

                            new FormulaVisitor<>() {
                                @Override
                                public String visitTrue(TrueFormula f) {
                                    return "true";
                                }

                                @Override
                                public String visitFalse(FalseFormula f) {
                                    return "false";
                                }

                                @Override
                                public String visitAtomicProposition(AtomicProposition f) {
                                    return "ap";
                                }

                                @Override
                                public String visitNot(Not f) {
                                    return "not";
                                }

                                @Override
                                public String visitNext(Next f) {
                                    return "next";
                                }


                                @Override
                                public String visitEventually(Eventually f) {
                                    return "eventually";
                                }

                                @Override
                                public String visitAlways(Always f) {
                                    return "always";
                                }

                                @Override
                                public String visitAnd(And f) {
                                    return "and";
                                }

                                @Override
                                public String visitOr(Or f) {
                                    return "or";
                                }

                                @Override
                                public String visitUntil(Until f) {
                                    return "until";
                                }
                            }
                    );
        assertEquals("always", result);
    }
}
