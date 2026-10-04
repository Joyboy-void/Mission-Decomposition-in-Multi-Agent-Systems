package ltlf.external.spot;

import ltlf.ast.*;
import ltlf.automaton.ResidualState;
import ltlf.automaton.ResidualStateRegistry;
import ltlf.automaton.StateLookupResult;
import ltlf.transform.BooleanCanonicalizer;
import ltlf.transform.Normalizer;
import ltlf.transform.Simplifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SpotResidualStateRegistryIntegrationTest {

    private Normalizer normalizer() {
        return new Normalizer(
                List.of(new Simplifier(), new BooleanCanonicalizer())
        );
    }

    private ResidualStateRegistry registry() {
        return new ResidualStateRegistry(
                normalizer(),
                new SpotEquivalenceChecker()
        );
    }

    @Test
    void semanticallyEquivalentEventuallyAndUntilShareState() {
        ResidualStateRegistry r = registry();

        ResidualState first =
                r.getOrCreate(
                    new Eventually(new AtomicProposition("p"))
                ).getState();

        StateLookupResult second = r.getOrCreate(
                new Until(
                        new TrueFormula(),
                        new AtomicProposition("p")
                )
        );

        assertSame(first, second.getState());
        assertFalse(second.isCreated());
        assertEquals(1, r.size());
    }

    @Test
    void doubleNegationAndAtomShareState() {
        ResidualStateRegistry r = registry();
        ResidualState p = r.getOrCreate(new AtomicProposition("p")).getState();

        StateLookupResult result = r.getOrCreate(
                new Not(new Not(new AtomicProposition("p")))
        );

        assertSame(p, result.getState());
        assertFalse(result.isCreated());
        assertEquals(1, r.size());
    }

    @Test
    void genuinelyDifferentAtomsRemainDifferentStates() {
        ResidualStateRegistry r = registry();

        ResidualState p = r.getOrCreate(new AtomicProposition("p")).getState();
        ResidualState q = r.getOrCreate(new AtomicProposition("q")).getState();

        assertNotSame(p, q);
        assertEquals(2, r.size());
    }

    @Test
    void equivalentLookupDoesNotConsumeAnotherStateId() {
        ResidualStateRegistry r = registry();

        ResidualState p = r.getOrCreate(new AtomicProposition("p")).getState();
        StateLookupResult equivalent = r.getOrCreate(
                new Not(new Not(new AtomicProposition("p")))
        );

        ResidualState q = r.getOrCreate(new AtomicProposition("q")).getState();

        assertSame(p, equivalent.getState());
        assertEquals(1, q.getId());
        assertEquals(2, r.size());
    }

    @Test
    void canonicalBooleanEquivalenceAndSpotDoNotCreateDuplicateState() {
        ResidualStateRegistry r = registry();

        ResidualState first = r.getOrCreate(
                new And(new AtomicProposition("p"), new AtomicProposition("q"))
        ).getState();

        StateLookupResult second = r.getOrCreate(
                new And(new AtomicProposition("q"), new AtomicProposition("p"))
        );

        assertSame(first, second.getState());
        assertFalse(second.isCreated());
        assertEquals(1, r.size());
    }
}
