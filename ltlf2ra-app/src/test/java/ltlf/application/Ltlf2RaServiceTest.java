package ltlf.application;

import ltlf.alphabet.AtomicPropositionSet;
import ltlf.alphabet.Valuation;
import ltlf.analysis.AtomicPropositionCollector;
import ltlf.ast.And;
import ltlf.ast.AtomicProposition;
import ltlf.ast.FalseFormula;
import ltlf.ast.Formula;
import ltlf.ast.TrueFormula;
import ltlf.automaton.ResidualAutomaton;
import ltlf.automaton.ResidualState;
import ltlf.parser.FormulaParser;
import ltlf.semantics.DefaultResidualSemantics;
import ltlf.transform.BooleanCanonicalizer;
import ltlf.transform.Normalizer;
import ltlf.transform.Simplifier;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class Ltlf2RaServiceTest {

    private final Normalizer normalizer =
            new Normalizer(List.of(
                    new Simplifier(),
                    new BooleanCanonicalizer()
            ));

    /**
     * Creates a service whose parser returns the supplied formula.
     *
     * This lets these tests focus on Ltlf2RaService itself,
     * without depending on Spot/JNI.
     */
    private Ltlf2RaService serviceReturning(Formula formula) {

        FormulaParser parser = ignored -> formula;

        return new Ltlf2RaService(
                parser,
                new AtomicPropositionCollector(),
                new DefaultResidualSemantics(),
                normalizer,
                Formula::equals
        );
    }

    @Test
    void nullFormulaIsRejected() {

        Ltlf2RaService service =
                serviceReturning(
                        new AtomicProposition("p")
                );

        assertThrows(
                NullPointerException.class,
                () -> service.build(null)
        );
    }

    @Test
    void blankFormulaIsRejected() {

        Ltlf2RaService service =
                serviceReturning(
                        new AtomicProposition("p")
                );

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> service.build("")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> service.build(" ")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> service.build("   ")
                )
        );
    }

    @Test
    void parserReceivesTrimmedFormula() {

        AtomicProposition p =
                new AtomicProposition("p");

        final String[] received =
                new String[1];

        FormulaParser parser = input -> {
            received[0] = input;
            return p;
        };

        Ltlf2RaService service =
                new Ltlf2RaService(
                        parser,
                        new AtomicPropositionCollector(),
                        new DefaultResidualSemantics(),
                        normalizer,
                        Formula::equals
                );

        service.build("   F(p)   ");

        assertEquals("F(p)", received[0]);
    }

    @Test
    void singleAtomicPropositionProducesCorrectAlphabet() {

        AtomicProposition p =
                new AtomicProposition("p");

        ResidualAutomaton automaton =
                serviceReturning(p).build("ignored");

        AtomicPropositionSet aps =
                automaton.getAlphabet()
                        .getAtomicPropositionSet();

        assertEquals(
                List.of(p),
                aps.asList()
        );

        // One proposition => 2 valuations.
        assertEquals(2, automaton.getAlphabet().size());
    }

    @Test
    void multipleAtomicPropositionsAreDiscoveredAutomatically() {

        AtomicProposition p =
                new AtomicProposition("p");

        AtomicProposition q =
                new AtomicProposition("q");

        Formula formula =
                new And(p, q);

        ResidualAutomaton automaton =
                serviceReturning(formula).build("ignored");

        AtomicPropositionSet aps =
                automaton.getAlphabet()
                        .getAtomicPropositionSet();

        assertEquals(
                Set.of(p, q),
                new LinkedHashSet<>(aps.asList())
        );

        // Two propositions => 2^2 = 4 valuations.
        assertEquals(4, automaton.getAlphabet().size());
    }

    @Test
    void duplicateAtomicPropositionsAreDiscoveredOnlyOnce() {

        AtomicProposition p =
                new AtomicProposition("p");

        Formula formula =
                new And(p, p);

        ResidualAutomaton automaton =
                serviceReturning(formula).build("ignored");

        AtomicPropositionSet aps =
                automaton.getAlphabet()
                        .getAtomicPropositionSet();

        assertEquals(
                List.of(p),
                aps.asList()
        );

        assertEquals(2, automaton.getAlphabet().size());
    }

    @Test
    void formulaWithNoAtomicPropositionsHasSingletonAlphabet() {

        Formula formula =
                new TrueFormula();

        ResidualAutomaton automaton =
                serviceReturning(formula).build("ignored");

        assertEquals(
                1,
                automaton.getAlphabet().size()
        );

        assertEquals(
                1,
                automaton.size()
        );

        assertTrue(
                automaton.isAccepting(
                        automaton.getInitialState()
                )
        );
    }

    @Test
    void initialStateIsTheParsedFormula() {

        AtomicProposition p =
                new AtomicProposition("p");

        ResidualAutomaton automaton =
                serviceReturning(p).build("ignored");

        assertEquals(
                p,
                automaton.getInitialState().getFormula()
        );
    }

    @Test
    void atomicPropositionFormulaProducesExpectedResidualStates() {

        AtomicProposition p =
                new AtomicProposition("p");

        ResidualAutomaton automaton =
                serviceReturning(p).build("ignored");

        assertEquals(3, automaton.size());

        Set<Formula> formulas =
                new LinkedHashSet<>();

        for (ResidualState state : automaton.getStates()) {
            formulas.add(state.getFormula());
        }

        assertTrue(
                formulas.contains(p)
        );

        assertTrue(
                formulas.contains(new TrueFormula())
        );

        assertTrue(
                formulas.contains(new FalseFormula())
        );
    }

    @Test
    void atomicPropositionTransitionsAreCorrect() {

        AtomicProposition p =
                new AtomicProposition("p");

        ResidualAutomaton automaton =
                serviceReturning(p).build("ignored");

        ResidualState initial =
                automaton.getInitialState();

        AtomicPropositionSet aps =
                automaton.getAlphabet()
                        .getAtomicPropositionSet();

        Valuation empty =
                Valuation.empty(aps);

        Valuation pTrue =
                Valuation.of(aps, p);

        ResidualState top =
                findState(
                        automaton,
                        new TrueFormula()
                );

        ResidualState bottom =
                findState(
                        automaton,
                        new FalseFormula()
                );

        assertEquals(
                bottom,
                automaton.getDestination(
                        initial,
                        empty
                )
        );

        assertEquals(
                top,
                automaton.getDestination(
                        initial,
                        pTrue
                )
        );
    }

    @Test
    void trueStateIsAccepting() {

        ResidualAutomaton automaton =
                serviceReturning(
                        new TrueFormula()
                ).build("ignored");

        assertTrue(
                automaton.isAccepting(
                        automaton.getInitialState()
                )
        );
    }

    @Test
    void falseStateIsNotAccepting() {

        ResidualAutomaton automaton =
                serviceReturning(
                        new FalseFormula()
                ).build("ignored");

        assertFalse(
                automaton.isAccepting(
                        automaton.getInitialState()
                )
        );
    }

    @Test
    void allResidualStatesAreReachable() {

        AtomicProposition p =
                new AtomicProposition("p");

        ResidualAutomaton automaton =
                serviceReturning(p).build("ignored");

        Set<ResidualState> states =
                new LinkedHashSet<>(
                        automaton.getStates()
                );

        Set<ResidualState> reached =
                new LinkedHashSet<>();

        reached.add(
                automaton.getInitialState()
        );

        boolean changed;

        do {
            changed = false;

            for (ResidualState source :
                    reached.toArray(new ResidualState[0])) {

                for (ResidualState destination :
                        automaton
                                .getOutgoingTransitions(source)
                                .values()) {

                    changed |= reached.add(destination);
                }
            }

        } while (changed);

        assertEquals(states, reached);
    }

    @Test
    void everyStateHasCompleteTransitionRelation() {

        AtomicProposition p =
                new AtomicProposition("p");

        ResidualAutomaton automaton =
                serviceReturning(p).build("ignored");

        long alphabetSize =
                automaton.getAlphabet().size();

        for (ResidualState state :
                automaton.getStates()) {

            assertEquals(
                    alphabetSize,
                    automaton
                            .getOutgoingTransitions(state)
                            .size()
            );

            for (Valuation valuation :
                    automaton.getAlphabet()) {

                assertNotNull(
                        automaton.getDestination(
                                state,
                                valuation
                        )
                );
            }
        }
    }

    private ResidualState findState(
            ResidualAutomaton automaton,
            Formula formula) {

        return automaton.getStates()
                .stream()
                .filter(state ->
                        state.getFormula().equals(formula)
                )
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "State not found: " + formula
                        )
                );
    }
}