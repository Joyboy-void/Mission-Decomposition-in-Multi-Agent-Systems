package ltlf.ast;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FalseFormulaTest {

    @Test
    void equalsAnotherFalse() {
        assertEquals(new FalseFormula(), new FalseFormula());
    }

    @Test
    void notEqualNull() {
        assertNotEquals(new FalseFormula(), null);
    }

    @Test
    void hashCodeStableAndEqual() {
        assertEquals(new FalseFormula().hashCode(), new FalseFormula().hashCode());
    }

    @Test
    void exactToString() {
        assertEquals("⊥", new FalseFormula().toString());
    }

    @Test
    void visitorDispatches() {
        String result =
                new FalseFormula().accept(
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
        assertEquals("false", result);
    }

    @Test
    void equalityIsReflexive() {
        FalseFormula f = new FalseFormula();
        assertEquals(f, f);
    }

    @Test
    void doesNotEqualTrue() {
        assertNotEquals(new FalseFormula(), new TrueFormula());
    }
}
