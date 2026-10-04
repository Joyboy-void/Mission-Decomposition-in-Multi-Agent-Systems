package ltlf.ast;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TrueFormulaTest {

    @Test
    void equalsAnotherTrue() {
        assertEquals(new TrueFormula(), new TrueFormula());
    }

    @Test
    void notEqualNull() {
        assertNotEquals(new TrueFormula(), null);
    }

    @Test
    void hashCodeStableAndEqual() {
        assertEquals(new TrueFormula().hashCode(), new TrueFormula().hashCode());
    }

    @Test
    void exactToString() {
        assertEquals("⊤", new TrueFormula().toString());
    }

    @Test
    void visitorDispatches() {
        String result =
                new TrueFormula().accept(
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
        assertEquals("true", result);
    }

    @Test
    void equalityIsReflexive() {
        TrueFormula t = new TrueFormula();
        assertEquals(t, t);
    }

    @Test
    void doesNotEqualFalse() {
        assertNotEquals(new TrueFormula(), new FalseFormula());
    }
}
