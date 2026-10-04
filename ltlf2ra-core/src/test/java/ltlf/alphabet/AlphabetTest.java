package ltlf.alphabet;

import java.util.*;

import ltlf.ast.AtomicProposition;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AlphabetTest {

    @Test
    void nullApSetIsRejected(){
        assertThrows(NullPointerException.class, () -> new Alphabet(null));
    }

    @Test
    void emptyUniverseHasOneSymbol() {
        Alphabet a = new Alphabet(new AtomicPropositionSet(List.of()));
        assertEquals(1, a.size());
    }

    @Test
    void twoPropositionsHaveFourSymbols() {
        Alphabet a = new Alphabet(
                new AtomicPropositionSet(
                        List.of(
                                new AtomicProposition("a"),
                                new AtomicProposition("b")
                        )));

        assertEquals(4, a.size());
    }

    @Test
    void enumeratesExactlyAllValuations() {
        Alphabet a = new Alphabet(
                new AtomicPropositionSet(
                        List.of(
                                new AtomicProposition("a"),
                                new AtomicProposition("b"))
                ));

        Set<Valuation> seen = new HashSet<>();
        for (Valuation v : a)
            assertTrue(seen.add(v));
        assertEquals(4, seen.size());
    }

    @Test
    void iteratorStartsWithEmptyValuation() {
        Alphabet a = new Alphabet(
                        new AtomicPropositionSet(
                                List.of(new AtomicProposition("a")
                    )));

        assertEquals(Valuation.empty(a.getAtomicPropositionSet()), a.iterator().next());
    }

    @Test
    void iteratorExhaustionThrows() {
        Alphabet a = new Alphabet(new AtomicPropositionSet(List.of(new AtomicProposition("a"))));
        Iterator<Valuation> it = a.iterator();
        it.next();
        it.next();

        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void iteratorsAreIndependent() {
        Alphabet a = new Alphabet(new AtomicPropositionSet(List.of(new AtomicProposition("a"))));
        Iterator<Valuation> i1 = a.iterator(), i2 = a.iterator();

        assertEquals(i1.next(), i2.next());
        assertEquals(i1.next(), i2.next());
    }

    @Test
    void sizeRejectsAnAlphabetThatDoesNotFitInLong() {
        List<AtomicProposition> aps = new ArrayList<>();
        for (int i = 0; i < 63; i++)
            aps.add(new AtomicProposition("p" + i));

        Alphabet a = new Alphabet(new AtomicPropositionSet(aps));
        assertThrows(ArithmeticException.class, a::size);
    }
}
