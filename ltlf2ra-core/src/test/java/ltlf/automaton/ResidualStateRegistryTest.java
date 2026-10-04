package ltlf.automaton;

import ltlf.ast.*;
import ltlf.transform.*;
import ltlf.semantics.FormulaEquivalenceChecker;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class ResidualStateRegistryTest {

    private Normalizer n() {
        return new Normalizer(
                List.of(
                        new Simplifier(),
                        new BooleanCanonicalizer())
        );
    }

    @Test
    void firstFormulaCreatesStateZero() {

        ResidualStateRegistry r = new ResidualStateRegistry(n(), Formula::equals);
        StateLookupResult x = r.getOrCreate(new AtomicProposition("p"));

        assertTrue(x.isCreated());

        assertEquals(0, x.getState().getId());
        assertEquals(1, r.size());
    }

    @Test
    void structurallyEqualFormulaReusesState() {
        ResidualStateRegistry r = new ResidualStateRegistry(n(), Formula::equals);

        ResidualState a = r.getOrCreate(new AtomicProposition("p")).getState();
        StateLookupResult b = r.getOrCreate(new AtomicProposition("p"));

        assertFalse(b.isCreated());
        assertSame(a, b.getState());
        assertEquals(1, r.size());
    }

    @Test
    void canonicalBooleanEquivalentFormulaReusesState() {
        ResidualStateRegistry r = new ResidualStateRegistry(n(), Formula::equals);
        ResidualState a = r.getOrCreate(new And(new AtomicProposition("p"), new AtomicProposition("q"))).getState();

        StateLookupResult b = r.getOrCreate(new And(new AtomicProposition("q"), new AtomicProposition("p")));

        assertSame(a, b.getState());
        assertEquals(1, r.size());
    }

    @Test
    void semanticCheckerMergesNonCanonicalEquivalentFormulas() {
        class Counting implements FormulaEquivalenceChecker {
            int calls;

            public boolean equivalent(Formula a, Formula b) {
                calls++;
                return (
                        a instanceof Eventually leftEventually && b instanceof Until rightUntil
                        && rightUntil.getLeft() instanceof TrueFormula
                        && leftEventually.getOperand().equals(rightUntil.getRight()))
                        || (b instanceof Eventually rightEventually && a instanceof Until leftUntil
                        && leftUntil.getLeft() instanceof TrueFormula
                        && rightEventually.getOperand().equals(leftUntil.getRight()));
            }
        }

        Counting checker = new Counting();

        ResidualStateRegistry r = new ResidualStateRegistry(n(), checker);
        ResidualState a = r.getOrCreate(new Eventually(new AtomicProposition("p"))).getState();

        StateLookupResult b = r.getOrCreate(new Until(new TrueFormula(), new AtomicProposition("p")));

        assertSame(a, b.getState());
        assertFalse(b.isCreated());
        assertTrue(checker.calls >= 1);
    }

    @Test
    void semanticAliasIsCached() {
        class Counting implements FormulaEquivalenceChecker {
            int calls;

            public boolean equivalent(Formula a, Formula b) {
                calls++;
                return true;
            }
        }
        Counting checker = new Counting();

        ResidualStateRegistry r = new ResidualStateRegistry(n(), checker);
        ResidualState s = r.getOrCreate(new AtomicProposition("p")).getState();

        r.getOrCreate(new AtomicProposition("q"));

        int afterFirstAlias = checker.calls;
        r.getOrCreate(new AtomicProposition("q"));

        assertEquals(afterFirstAlias, checker.calls);
        assertSame(s, r.getOrCreate(new AtomicProposition("q")).getState());
    }

    @Test
    void stateIdsAreSequential() {
        ResidualStateRegistry r = new ResidualStateRegistry(n(), Formula::equals);

        assertEquals(0, r.getOrCreate(new AtomicProposition("a")).getState().getId());
        assertEquals(1, r.getOrCreate(new AtomicProposition("b")).getState().getId());
        assertEquals(2, r.getOrCreate(new AtomicProposition("c")).getState().getId());
    }

    @Test
    void statesListIsImmutable() {
        ResidualStateRegistry r = new ResidualStateRegistry(n(), Formula::equals);

        r.getOrCreate(new AtomicProposition("p"));

        assertThrows(
                UnsupportedOperationException.class,
                () -> r.getStates().clear()
        );
    }

    @Test
    void nullDependenciesAndFormulaAreRejected() {
        assertAll(

                () -> assertThrows(
                        NullPointerException.class,
                        () -> new ResidualStateRegistry(null, Formula::equals)),

                () -> assertThrows(
                        NullPointerException.class,
                        () -> new ResidualStateRegistry(n(), null)),

                () -> assertThrows(
                        NullPointerException.class,
                        () -> new ResidualStateRegistry(n(), Formula::equals).getOrCreate(null))
        );
    }
}
