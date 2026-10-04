package ltlf.external.spot;

import ltlf.ast.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class SpotFormulaParserTest {

    private final SpotFormulaParser parser =
                new SpotFormulaParser();

    @Test
    void parsesBooleanFormula() {

        Formula formula =
                parser.parse("a & b");

        assertInstanceOf(
                And.class,
                formula
        );
    }

    @Test
    void parsesEventually() {

        Formula formula =
                parser.parse("F(a)");

        assertInstanceOf(
                Eventually.class,
                formula
        );
    }

    @Test
    void parsesStrongNext() {

        // operator not supported
        assertThrows(
                IllegalArgumentException.class,
                ()->parser.parse("X[!](a)")
        );
    }

    @Test
    void parsesTrue() {
        assertEquals(new TrueFormula(), parser.parse("1"));
    }

    @Test
    void parsesFalse() {
        assertEquals(new FalseFormula(), parser.parse("0"));
    }

    @Test
    void parsesAtom() {
        assertEquals(new AtomicProposition("a"), parser.parse("a"));
    }

    @Test
    void parsesNot() {
        assertEquals(new Not(new AtomicProposition("a")), parser.parse("!(a)"));
    }

    @Test
    void parsesNext() {
        assertEquals(new Next(new AtomicProposition("a")), parser.parse("X(a)"));
    }

    @Test
    void parsesEventuallyAndAlways() {
        assertAll(
                () -> assertEquals(
                        new Eventually(new AtomicProposition("a")), parser.parse("F(a)")
                ),
                () -> assertEquals(
                        new Always(new AtomicProposition("a")), parser.parse("G(a)")
                )
        );
    }

    @Test
    void parsesAndOrUntil() {
        assertAll(
                () -> assertEquals(
                        new And(new AtomicProposition("a"), new AtomicProposition("b")),
                        parser.parse("(a & b)")
                ),
                () -> assertEquals(
                        new Or(new AtomicProposition("a"), new AtomicProposition("b")),
                        parser.parse("(a | b)")
                ),
                () -> assertEquals(
                        new Until(new AtomicProposition("a"), new AtomicProposition("b")),
                        parser.parse("(a U b)")
                )
        );
    }

    @Test
    void rejectsInvalidFormula() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("a &"));
    }
}
