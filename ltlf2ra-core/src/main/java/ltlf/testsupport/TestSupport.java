package ltlf.testsupport;

import ltlf.alphabet.AtomicPropositionSet;
import ltlf.alphabet.Valuation;
import ltlf.ast.*;

import java.util.List;

public class TestSupport {
    private TestSupport() {
    }

    public static AtomicProposition a() {
        return new AtomicProposition("a");
    }

    public static AtomicProposition b() {
        return new AtomicProposition("b");
    }

    public static AtomicProposition c() {
        return new AtomicProposition("c");
    }

    public static AtomicProposition p() {
        return new AtomicProposition("p");
    }

    public static AtomicProposition q() {
        return new AtomicProposition("q");
    }

    public static AtomicPropositionSet apSet(String... names) {
        return new AtomicPropositionSet(
                java.util.Arrays.stream(names)
                        .map(AtomicProposition::new)
                        .toList());
    }

    public static Valuation valuation(AtomicPropositionSet set, String... names) {
        AtomicProposition[] aps =
                java.util.Arrays.stream(names)
                .map(AtomicProposition::new)
                .toArray(AtomicProposition[]::new);

        return Valuation.of(set, aps);
    }

    public static Formula pipeline(Formula formula) {
        return new ltlf.transform.Normalizer(
                List.of(
                        new ltlf.transform.Simplifier(),
                        new ltlf.transform.BooleanCanonicalizer())
                ).normalize(formula);
    }
}
