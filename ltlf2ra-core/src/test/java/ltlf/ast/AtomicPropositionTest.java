package ltlf.ast;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AtomicPropositionTest {

    @Test
    void rejectsNullName() {

        assertThrows(
                NullPointerException.class,
                () -> new AtomicProposition(null)
        );
    }

    @Test
    void rejectsEmptyName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new AtomicProposition("")
        );
    }

    @Test
    void rejectsBlankName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new AtomicProposition("   ")
        );
    }

    @Test
    void storesName() {

        assertEquals(
                "sensor",
                new AtomicProposition("sensor").getName()
        );
    }

    @Test
    void sameNamesAreEqual() {

        assertEquals(
                new AtomicProposition("p"),
                new AtomicProposition("p")
        );
    }

    @Test
    void equalNamesHaveEqualHashCodes() {

        assertEquals(
                new AtomicProposition("p").hashCode(),
                new AtomicProposition("p").hashCode()
        );
    }

    @Test
    void differentNamesAreNotEqual() {

        assertNotEquals(
                new AtomicProposition("p"),
                new AtomicProposition("q")
        );
    }

    @Test
    void visitorDispatches() {

        String result =
                new AtomicProposition("p").accept(
                        new FormulaVisitor<>() {
                            public String visitTrue(TrueFormula f) {
                                return "true";
                            }

                            public String visitFalse(FalseFormula f) {
                                return "false";
                            }

                            public String visitAtomicProposition(AtomicProposition f) {
                                return f.getName();
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

        assertEquals("p", result);
    }
}
