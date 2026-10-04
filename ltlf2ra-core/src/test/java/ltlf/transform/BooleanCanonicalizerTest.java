package ltlf.transform;

import ltlf.ast.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BooleanCanonicalizerTest {

    private final BooleanCanonicalizer canonicalizer = new BooleanCanonicalizer();

    private Formula p() {
        return new AtomicProposition("p");
    }

    private Formula q() {
        return new AtomicProposition("q");
    }

    private Formula r() {
        return new AtomicProposition("r");
    }

    @Test
    void andIsCommutative() {
        assertEquals(
                canonicalizer.transform(new And(p(), q())),
                canonicalizer.transform(new And(q(), p()))
        );
    }

    @Test
    void orIsCommutative() {
        assertEquals(
                canonicalizer.transform(new Or(p(), q())),
                canonicalizer.transform(new Or(q(), p()))
        );
    }

    @Test
    void andIsAssociative() {
        Formula x = new And(p(),new And(q(), r())),
                y = new And(new And(p(), q()), r());

        assertEquals(
                canonicalizer.transform(x),
                canonicalizer.transform(y
                ));
    }

    @Test
    void orIsAssociative() {
        Formula x = new Or(p(), new Or(q(), r())),
                y = new Or(new Or(p(), q()), r());

        assertEquals(
                canonicalizer.transform(x),
                canonicalizer.transform(y)
        );
    }

    @Test
    void duplicateAndOperandsAreRemoved() {
        assertEquals(
                p(),
                canonicalizer.transform(new And(p(), p()))
        );
    }

    @Test
    void duplicateOrOperandsAreRemoved() {
        assertEquals(
                p(),
                canonicalizer.transform(new Or(p(), p()))
        );
    }

    @Test
    void nestedSameOperatorIsFlattenedAndSorted() {
        Formula f = new And(r(), new And(q(), p()));

        assertEquals(
                "((p ∧ q) ∧ r)",
                canonicalizer.transform(f).toString()
        );
    }

    @Test
    void canonicalizationIsIdempotent() {
        Formula f = new Or(r(), new Or(p(), new Or(q(), p())));

        Formula once = canonicalizer.transform(f);

        assertEquals(once, canonicalizer.transform(once));
    }
}
