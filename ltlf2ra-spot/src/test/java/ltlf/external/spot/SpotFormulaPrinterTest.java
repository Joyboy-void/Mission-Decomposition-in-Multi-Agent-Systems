package ltlf.external.spot;

import ltlf.ast.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SpotFormulaPrinterTest {

    private final SpotFormulaPrinter printer = new SpotFormulaPrinter();


    @Test
    void printsTrue() {
        assertEquals("1", printer.print(new TrueFormula()));
    }

    @Test
    void printsFalse() {
        assertEquals("0", printer.print(new FalseFormula()));
    }

    @Test
    void printsAtom() {
        assertEquals("a", printer.print(new AtomicProposition("a")));
    }

    @Test
    void printsNot() {
        assertEquals("!(a)", printer.print(new Not(new AtomicProposition("a"))));
    }

    @Test
    void printsNext() {
        assertEquals("X(a)", printer.print(new Next(new AtomicProposition("a"))));
    }

    @Test
    void printsEventually() {
        assertEquals("F(a)", printer.print(new Eventually(new AtomicProposition("a"))));
    }

    @Test
    void printsAlways() {
        assertEquals("G(a)", printer.print(new Always(new AtomicProposition("a"))));
    }

    @Test
    void printsNestedFormulaWithRequiredParentheses() {
        Formula f =
                new And(
                        new AtomicProposition("a"),
                        new Next(new Eventually(new AtomicProposition("b"))
                        )
                );
        assertEquals("(a & X(F(b)))", printer.print(f));
    }

}
