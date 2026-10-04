package ltlf.external.spot;

import ltlf.ast.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class SpotEquivalenceCheckerTest {

    private final SpotEquivalenceChecker checker =
                new SpotEquivalenceChecker();

    @Test
    void identicalFormulasAreEquivalent(){

        Formula left =
                    new And(
                            new AtomicProposition("a"),
                            new AtomicProposition("b")
                    );

        Formula right =
                new And(
                        new AtomicProposition("a"),
                        new AtomicProposition("b")
                );

        assertTrue(checker.equivalent(left, right));
    }


    @Test
    void commutativeAndIsEquivalent(){

        Formula left =
                new And(
                        new AtomicProposition("a"),
                        new AtomicProposition("b")
                );


        Formula right =
                new And(
                        new AtomicProposition("b"),
                        new AtomicProposition("a")
                );

        assertTrue(checker.equivalent(left, right));
    }

    @Test
    void differentFormulasAreNotEquivalent(){

        Formula left =
                new Until(
                        new AtomicProposition("a"),
                        new AtomicProposition("b")
                );

        Formula right =
                new Until(
                        new AtomicProposition("b"),
                        new AtomicProposition("a")
                );

        assertFalse(checker.equivalent(left, right));
    }

    @Test
    void nextTrueIsNotEquivalentToTrue() {

        Formula left =
                new Next(new TrueFormula());

        Formula right =
                new TrueFormula();

        assertTrue(checker.equivalent(left, right));
    }

    @Test
    void identicalAtomsAreEquivalent() {

        assertTrue(
                checker.equivalent(
                        new AtomicProposition("a"),
                        new AtomicProposition("a")
                )
        );
    }

    @Test
    void differentAtomsAreNotEquivalent() {

        assertFalse(
                checker.equivalent(
                        new AtomicProposition("a"),
                        new AtomicProposition("b"))
        );
    }

    @Test
    void andIsCommutativeSemantically() {

        assertTrue(
                checker.equivalent(
                        new And(new AtomicProposition("a"), new AtomicProposition("b")),
                        new And(new AtomicProposition("b"), new AtomicProposition("a"))
                ));
    }

    @Test
    void distributivityIsRecognized() {
        Formula a = new AtomicProposition("a"),
                b = new AtomicProposition("b"),
                d = new AtomicProposition("c");

        assertTrue(
                checker.equivalent(
                        new And(a, new Or(b, d)),
                        new Or(new And(a, b), new And(a, d))
                ));
    }

    @Test
    void eventuallyMatchesTrueUntil() {
        Formula p = new AtomicProposition("p");

        assertTrue(
                checker.equivalent(
                        new Eventually(p),
                        new Until(new TrueFormula(), p)
                )
        );
    }

    @Test
    void doubleNegationIsEquivalent() {
        Formula p = new AtomicProposition("p");
        assertTrue(
                checker.equivalent(new Not(new Not(p)), p)
        );
    }

    @Test
    void nextOfSameOperandIsEquivalent() {
        Formula p = new AtomicProposition("p");

        assertTrue(
                checker.equivalent(new Next(p), new Next(p))
        );
    }

    @Test
    void xAIsNotEquivalentToA() {
        Formula p = new AtomicProposition("p");

        assertFalse(
                checker.equivalent(new Next(p), p)
        );
    }
}
