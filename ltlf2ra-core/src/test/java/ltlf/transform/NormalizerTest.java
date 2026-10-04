package ltlf.transform;

import ltlf.ast.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class NormalizerTest {

    @Test
    void emptyPipelineIsIdentity() {
        Formula p = new AtomicProposition("p");

        assertSame(
                p,
                new Normalizer(List.of()).normalize(p)
        );
    }

    @Test
    void transformersRunInOrder() {

        Formula p = new AtomicProposition("p");

        FormulaTransformer first = new FormulaTransformer() {
            @Override
            public Formula transform(Formula formula) {
                return new Not(formula);
            }
        };
        FormulaTransformer second = new FormulaTransformer() {
            @Override
            public Formula transform(Formula formula) {
                return new Not(formula);
            }
        };;

        Formula result = new Normalizer(List.of(first, second)).normalize(p);

        assertEquals(
                p,
                new Simplifier().transform(result)
        );
    }

    @Test
    void simplifierCanPrecedeCanonicalizer() {
        Formula p = new AtomicProposition("p");
        Formula f = new And(p, new TrueFormula());

        Normalizer n = new Normalizer(
                List.of(new Simplifier(), new BooleanCanonicalizer())
        );
        assertEquals(p, n.normalize(f));
    }

    @Test
    void suppliedTransformerListIsDefensivelyCopied() {
        List<FormulaTransformer> list = new ArrayList<>();
        list.add(Not::new);

        Normalizer n = new Normalizer(list);
        list.clear();

        Formula p = new AtomicProposition("p");
        assertEquals(new Not(p), n.normalize(p));
    }

    @Test
    void nullTransformerListIsRejected() {
        assertThrows(NullPointerException.class, () -> new Normalizer(null));
    }

    @Test
    void nullTransformerElementIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new Normalizer(Collections.singletonList((FormulaTransformer) null))
        );
    }

    @Test
    void nullFormulaFailsWhenTransformerTouchesIt() {
        Normalizer n = new Normalizer(List.of(new Simplifier()));
        assertThrows(
                NullPointerException.class,
                () -> n.normalize(null)
        );
    }
}
