import ltlf.alphabet.*;
import ltlf.automaton.*;
import ltlf.ast.*;
import ltlf.external.spot.SpotEquivalenceChecker;
import ltlf.semantics.*;
import ltlf.transform.*;


import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ZResidualAutomatonDemoTest {

    private final FormulaTransformer simplifier =
            new Simplifier();

    private final FormulaTransformer canonicalizer =
            new BooleanCanonicalizer();

    private final Normalizer normalizer =
            new Normalizer(
                    List.of(
                            simplifier,
                            canonicalizer
                    )
            );

    private final ResidualSemantics residualSemantics =
            new DefaultResidualSemantics();

    private final FormulaEquivalenceChecker equivalenceChecker =
            new SpotEquivalenceChecker();



    // EXAMPLE 1
    //
    // φ = p ∨ X(q)
    //
    // States:
    //
    //   p ∨ X(q)
    //   q
    //   ⊤
    //   ⊥


    @Test
    void Example1_pOrNextQ() {

        AtomicProposition p =
                new AtomicProposition("p");

        AtomicProposition q =
                new AtomicProposition("q");

        AtomicPropositionSet apSet =
                apSet(p, q);

        Alphabet alphabet =
                new Alphabet(apSet);

        Formula formula =
                new Or(
                        p,
                        new Next(q)
                );

        ResidualAutomatonBuilder builder =
                newBuilder(alphabet);

        printHeader(
                "EXAMPLE 1",
                formula,
                apSet
        );

        ResidualAutomaton automaton =
                builder.build(formula);


        Map<Formula, Map<Valuation, Formula>> expected =
                new LinkedHashMap<>();

        Formula qFormula = q;
        Formula top = new TrueFormula();
        Formula bottom = new FalseFormula();


        /*
         * p ∨ Xq
         *
         * {p}, {p,q} -> ⊤
         * {}, {q}     -> q
         */
        addTransitions(
                expected,
                formula,
                top,
                valuations(
                        alphabet,
                        valuation(apSet, p),
                        valuation(apSet, p, q)
                )
        );

        addTransitions(
                expected,
                formula,
                qFormula,
                valuations(
                        alphabet,
                        valuation(apSet),
                        valuation(apSet, q)
                )
        );


        /*
         * q
         *
         * {q}, {p,q} -> ⊤
         * {}, {p}     -> ⊥
         */
        addTransitions(
                expected,
                qFormula,
                top,
                valuations(
                        alphabet,
                        valuation(apSet, q),
                        valuation(apSet, p, q)
                )
        );

        addTransitions(
                expected,
                qFormula,
                bottom,
                valuations(
                        alphabet,
                        valuation(apSet),
                        valuation(apSet, p)
                )
        );


        /*
         * ⊤ -- Σ --> ⊤
         */
        addTransitions(
                expected,
                top,
                top,
                allValuations(alphabet)
        );


        /*
         * ⊥ -- Σ --> ⊥
         */
        addTransitions(
                expected,
                bottom,
                bottom,
                allValuations(alphabet)
        );


        assertAutomaton(
                "Example 1",
                formula,
                automaton,
                expected,
                Set.of(
                        formula,
                        qFormula,
                        top,
                        bottom
                )
        );
    }



    // EXAMPLE 2
    //
    // φ = p ∧ X(q) ∧ X(X(r))
    //
    // States:
    //
    //   p ∧ X(q) ∧ X(X(r))
    //   q ∧ X(r)
    //   r
    //   ⊤
    //   ⊥


    @Test
    void Example2_pAndNextQAndNextNextR() {

        AtomicProposition p =
                new AtomicProposition("p");

        AtomicProposition q =
                new AtomicProposition("q");

        AtomicProposition r =
                new AtomicProposition("r");

        AtomicPropositionSet apSet =
                apSet(p, q, r);

        Alphabet alphabet =
                new Alphabet(apSet);

        Formula formula =
                new And(
                        new And(
                                p,
                                new Next(q)
                        ),
                        new Next(
                                new Next(r)
                        )
                );

        Formula qNextR =
                new And(
                        q,
                        new Next(r)
                );

        Formula top =
                new TrueFormula();

        Formula bottom =
                new FalseFormula();

        ResidualAutomatonBuilder builder =
                newBuilder(alphabet);

        printHeader(
                "EXAMPLE 2",
                formula,
                apSet
        );

        ResidualAutomaton automaton =
                builder.build(formula);


        Map<Formula, Map<Valuation, Formula>> expected =
                new LinkedHashMap<>();


        /*
         * Initial:
         *
         * valuations containing p -> q ∧ X(r)
         * valuations not containing p -> ⊥
         */
        addTransitions(
                expected,
                formula,
                qNextR,
                valuationsContaining(
                        alphabet,
                        p
                )
        );

        addTransitions(
                expected,
                formula,
                bottom,
                valuationsNotContaining(
                        alphabet,
                        p
                )
        );


        /*
         * q ∧ X(r)
         *
         * q true -> r
         * q false -> ⊥
         */
        Formula rFormula = r;

        addTransitions(
                expected,
                qNextR,
                rFormula,
                valuationsContaining(
                        alphabet,
                        q
                )
        );

        addTransitions(
                expected,
                qNextR,
                bottom,
                valuationsNotContaining(
                        alphabet,
                        q
                )
        );


        /*
         * r
         *
         * r true -> ⊤
         * r false -> ⊥
         */
        addTransitions(
                expected,
                rFormula,
                top,
                valuationsContaining(
                        alphabet,
                        r
                )
        );

        addTransitions(
                expected,
                rFormula,
                bottom,
                valuationsNotContaining(
                        alphabet,
                        r
                )
        );


        /*
         * ⊤ and ⊥ self-loop on every valuation.
         */
        addTransitions(
                expected,
                top,
                top,
                allValuations(alphabet)
        );

        addTransitions(
                expected,
                bottom,
                bottom,
                allValuations(alphabet)
        );


        assertAutomaton(
                "Example 2",
                formula,
                automaton,
                expected,
                Set.of(
                        formula,
                        qNextR,
                        rFormula,
                        top,
                        bottom
                )
        );
    }



    // PDF EXAMPLE 3
    //
    // φ = p U q
    //
    // States:
    //
    //   p U q
    //   ⊤
    //   ⊥


    @Test
    void Example3_pUntilQ() {

        AtomicProposition p =
                new AtomicProposition("p");

        AtomicProposition q =
                new AtomicProposition("q");

        AtomicPropositionSet apSet =
                apSet(p, q);

        Alphabet alphabet =
                new Alphabet(apSet);

        Formula formula =
                new Until(p, q);

        Formula top =
                new TrueFormula();

        Formula bottom =
                new FalseFormula();

        ResidualAutomatonBuilder builder =
                newBuilder(alphabet);

        printHeader(
                "EXAMPLE 3",
                formula,
                apSet
        );

        ResidualAutomaton automaton =
                builder.build(formula);


        Map<Formula, Map<Valuation, Formula>> expected =
                new LinkedHashMap<>();


        /*
         * q true:
         *
         * {}? no
         *
         * {q}, {p,q} -> ⊤
         */
        addTransitions(
                expected,
                formula,
                top,
                valuationsContaining(
                        alphabet,
                        q
                )
        );


        /*
         * q false and p true:
         *
         * {p} -> p U q
         */
        addTransitions(
                expected,
                formula,
                formula,
                List.of(
                        valuation(apSet, p)
                )
        );


        /*
         * neither p nor q:
         *
         * {} -> ⊥
         */
        addTransitions(
                expected,
                formula,
                bottom,
                List.of(
                        valuation(apSet)
                )
        );


        /*
         * ⊤ and ⊥ self-loop.
         */
        addTransitions(
                expected,
                top,
                top,
                allValuations(alphabet)
        );

        addTransitions(
                expected,
                bottom,
                bottom,
                allValuations(alphabet)
        );


        assertAutomaton(
                "Example 3",
                formula,
                automaton,
                expected,
                Set.of(
                        formula,
                        top,
                        bottom
                )
        );
    }



    // EXAMPLE 4
    //
    // φ = F(p)
    //
    // States:
    //
    //   F(p)
    //   ⊤


    @Test
    void Example4_eventuallyP() {

        AtomicProposition p =
                new AtomicProposition("p");

        AtomicPropositionSet apSet =
                apSet(p);

        Alphabet alphabet =
                new Alphabet(apSet);

        Formula formula =
                new Eventually(p);

        Formula top =
                new TrueFormula();

        ResidualAutomatonBuilder builder =
                newBuilder(alphabet);

        printHeader(
                "PDF EXAMPLE 4",
                formula,
                apSet
        );

        ResidualAutomaton automaton =
                builder.build(formula);


        Map<Formula, Map<Valuation, Formula>> expected =
                new LinkedHashMap<>();


        /*
         * p true -> ⊤
         * p false -> F(p)
         */
        addTransitions(
                expected,
                formula,
                top,
                List.of(
                        valuation(apSet, p)
                )
        );

        addTransitions(
                expected,
                formula,
                formula,
                List.of(
                        valuation(apSet)
                )
        );


        /*
         * ⊤ -- Σ --> ⊤
         */
        addTransitions(
                expected,
                top,
                top,
                allValuations(alphabet)
        );


        assertAutomaton(
                "Example 4",
                formula,
                automaton,
                expected,
                Set.of(
                        formula,
                        top
                )
        );
    }



    // Example 5
    //
    // φ = F(G(p))
    //
    // Production RA with semantic state merging:
    //
    //   State:
    //      F(G(p))
    //
    // Transitions:
    //
    //   F(G(p)) -- {p} --> F(G(p))
    //   F(G(p)) -- {}  --> F(G(p))
    //
    // The raw residual for {p} is:
    //
    //      G(p) ∨ F(G(p))
    //
    // but Spot proves:
    //
    //      G(p) ∨ F(G(p)) ≡ F(G(p))
    //
    // so both residuals correspond to the same RA state.


    @Test
    void Example5_eventuallyAlwaysP() {

        AtomicProposition p =
                new AtomicProposition("p");

        AtomicPropositionSet apSet =
                apSet(p);

        Alphabet alphabet =
                new Alphabet(apSet);

        Formula formula =
                new Eventually(
                        new Always(p)
                );

        ResidualAutomatonBuilder builder =
                newBuilder(alphabet);

        printHeader(
                "EXAMPLE 5",
                formula,
                apSet
        );

        ResidualAutomaton automaton =
                builder.build(formula);

        Map<Formula, Map<Valuation, Formula>> expected =
                new LinkedHashMap<>();

        /*
         * F(G(p)):
         *
         * p true  -> F(G(p))
         * p false -> F(G(p))
         *
         * The p-true case originally produces the raw residual
         *
         *     G(p) ∨ F(G(p))
         *
         * which is semantically equivalent to F(G(p)).
         */

        addTransitions(
                expected,
                formula,
                formula,
                List.of(
                        valuation(apSet, p),
                        valuation(apSet)
                )
        );

        assertAutomaton(
                "Example 5",
                formula,
                automaton,
                expected,
                Set.of(formula)
        );
    }



    // Builder

    private ResidualAutomatonBuilder newBuilder(
            Alphabet alphabet) {

        return new ResidualAutomatonBuilder(
                residualSemantics,
                normalizer,
                equivalenceChecker,
                alphabet
        );
    }

    // main assertion

    private void assertAutomaton(
            String exampleName,
            Formula input,
            ResidualAutomaton actual,
            Map<Formula, Map<Valuation, Formula>> expectedTransitions,
            Set<Formula> expectedStates) {


        /*
         * Normalize expected formulas in exactly the same way
         * the production registry normalizes formulas.
         *
         * This is NOT semantic equivalence.
         *
         * It simply lets our expected representation use the
         * same canonical state representation as the real
         * ResidualStateRegistry.
         */
        Set<Formula> normalizedExpectedStates =
                expectedStates.stream()
                        .map(normalizer::normalize)
                        .collect(Collectors.toCollection(
                                LinkedHashSet::new
                        ));


        Map<Formula, ResidualState> actualStates =
                actual.getStates()
                        .stream()
                        .collect(Collectors.toMap(
                                ResidualState::getFormula,
                                state -> state,
                                (a, b) -> a,
                                LinkedHashMap::new
                        ));


        System.out.println();
        System.out.println(
                "╔══════════════════════════════════════════════════════╗"
        );
        System.out.printf(
                "║ %-52s ║%n",
                exampleName
        );
        System.out.println(
                "╚══════════════════════════════════════════════════════╝"
        );

        System.out.println();
        System.out.println("INPUT FORMULA");
        System.out.println("-------------");
        System.out.println("  " + input);

        System.out.println();
        System.out.println("EXPECTED RESIDUAL AUTOMATON");
        System.out.println("----------------------------");

        printExpected(
                expectedTransitions,
                normalizedExpectedStates
        );

        System.out.println();
        System.out.println("CALCULATED RESIDUAL AUTOMATON");
        System.out.println("------------------------------");

        printActual(actual);


       // State Correctness

        assertEquals(
                normalizedExpectedStates.size(),
                actual.size(),
                exampleName + ": wrong number of states"
        );

        assertEquals(
                normalizedExpectedStates,
                actualStates.keySet(),
                exampleName + ": wrong residual state set"
        );


        // Initial State

        assertEquals(
                normalizer.normalize(input),
                actual.getInitialState().getFormula(),
                exampleName + ": wrong initial state"
        );


        // Every expected source state must exist.

        for(Formula expectedSource :
                normalizedExpectedStates){

            ResidualState source =
                    actualStates.get(expectedSource);

            assertNotNull(
                    source,
                    exampleName +
                            ": expected state missing: " +
                            expectedSource
            );


            Map<Valuation, ResidualState> outgoing =
                    actual.getOutgoingTransitions(source);



            // Every valuation in Σ must have a transition.

            assertEquals(
                    actual.getAlphabet().size(),
                    outgoing.size(),
                    exampleName +
                            ": incomplete transition function for " +
                            expectedSource
            );



            // Every destination must be an actual state.

            for(ResidualState destination :
                    outgoing.values()){

                assertTrue(
                        actual.getStates().contains(destination),
                        exampleName +
                                ": transition points to unknown state"
                );
            }
        }


        // Expected Transitions

        for(var sourceEntry :
                expectedTransitions.entrySet()){

            Formula sourceFormula =
                    normalizer.normalize(
                            sourceEntry.getKey()
                    );

            ResidualState source =
                    actualStates.get(sourceFormula);

            assertNotNull(source);


            for(var transition :
                    sourceEntry.getValue().entrySet()){

                Valuation valuation =
                        transition.getKey();

                Formula expectedDestinationFormula =
                        normalizer.normalize(
                                transition.getValue()
                        );

                ResidualState expectedDestination =
                        actualStates.get(
                                expectedDestinationFormula
                        );

                assertNotNull(
                        expectedDestination,
                        exampleName +
                                ": expected destination missing: " +
                                expectedDestinationFormula
                );


                ResidualState actualDestination =
                        actual.getDestination(
                                source,
                                valuation
                        );


                assertEquals(
                        expectedDestination,
                        actualDestination,
                        exampleName +
                                ": wrong transition\n" +
                                "    source = " +
                                sourceFormula +
                                "\n" +
                                "    valuation = " +
                                valuation +
                                "\n" +
                                "    expected = " +
                                expectedDestinationFormula +
                                "\n" +
                                "    actual = " +
                                (
                                        actualDestination == null
                                                ? "<null>"
                                                : actualDestination.getFormula()
                                )
                );
            }
        }


        // Acceptance

        for(ResidualState state :
                actual.getStates()){

            boolean expectedAccepting =
                    state.getFormula()
                            instanceof TrueFormula;

            assertEquals(
                    expectedAccepting,
                    actual.isAccepting(state),
                    exampleName +
                            ": incorrect acceptance for " +
                            state.getFormula()
            );
        }


        /*
         * Reachability
         *
         * Since the builder uses BFS starting at q0, every
         * state in the returned automaton should be reachable.
         */

        Set<ResidualState> reachable =
                reachableStates(actual);

        assertEquals(
                new HashSet<>(actual.getStates()),
                reachable,
                exampleName +
                        ": automaton contains unreachable state(s)"
        );


        System.out.println();
        System.out.println(
                "✓ PASS: " + exampleName
        );
    }


    // Printing

    private void printExpected(
            Map<Formula, Map<Valuation, Formula>> transitions,
            Set<Formula> states) {

        System.out.println();
        System.out.println("States:");

        int id = 0;

        Map<Formula, Integer> ids =
                new LinkedHashMap<>();

        for(Formula state : states){

            ids.put(state, id);

            System.out.printf(
                    "  q%d = %s%n",
                    id,
                    state
            );

            id++;
        }

        System.out.println();
        System.out.println("Transitions:");

        for(var sourceEntry :
                transitions.entrySet()){

            Formula source =
                    normalizer.normalize(
                            sourceEntry.getKey()
                    );

            printGroupedTransitions(
                    "  q" + ids.get(source) +
                            " [" + source + "]",
                    sourceEntry.getValue(),
                    ids
            );
        }
    }


    private void printActual(
            ResidualAutomaton automaton) {

        System.out.println();
        System.out.println("States:");

        for(ResidualState state :
                automaton.getStates()){

            String accepting =
                    automaton.isAccepting(state)
                            ? "  [ACCEPTING]"
                            : "";

            System.out.printf(
                    "  q%d = %s%s%n",
                    state.getId(),
                    state.getFormula(),
                    accepting
            );
        }

        System.out.println();
        System.out.println("Transitions:");

        Map<ResidualState, Map<Valuation, Formula>>
                printable =
                new LinkedHashMap<>();

        for(ResidualState source :
                automaton.getStates()){

            Map<Valuation, Formula> outgoing =
                    new LinkedHashMap<>();

            for(var transition :
                    automaton
                            .getOutgoingTransitions(source)
                            .entrySet()){

                outgoing.put(
                        transition.getKey(),
                        transition.getValue().getFormula()
                );
            }

            printable.put(
                    source,
                    outgoing
            );
        }


        for(var sourceEntry :
                printable.entrySet()){

            ResidualState source =
                    sourceEntry.getKey();

            printGroupedTransitions(
                    "  q" + source.getId() +
                            " [" + source.getFormula() + "]",
                    sourceEntry.getValue(),
                    actualStateIds(automaton)
            );
        }
    }


    private void printGroupedTransitions(
            String source,
            Map<Valuation, Formula> transitions,
            Map<Formula, Integer> ids) {

        /*
         * Group valuations that lead to the same destination.
         *
         * This produces output such as:
         *
         * q0 -- {p}, {p,q} --> q2
         *
         * instead of printing four separate lines.
         */

        Map<Formula, List<Valuation>> grouped =
                new LinkedHashMap<>();

        for(var transition :
                transitions.entrySet()){

            grouped.computeIfAbsent(
                    transition.getValue(),
                    ignored -> new ArrayList<>()
            ).add(transition.getKey());
        }


        for(var entry :
                grouped.entrySet()){

            Formula destination =
                    entry.getKey();

            List<Valuation> valuations =
                    entry.getValue();

            String valuationText =
                    valuations.stream()
                            .map(Valuation::toString)
                            .collect(Collectors.joining(", "));

            Integer destinationId =
                    ids.get(destination);

            System.out.printf(
                    "    %s -- %-25s --> q%d [%s]%n",
                    source,
                    valuationText,
                    destinationId,
                    destination
            );
        }
    }


    private Map<Formula, Integer> actualStateIds(
            ResidualAutomaton automaton) {

        Map<Formula, Integer> result =
                new LinkedHashMap<>();

        for(ResidualState state :
                automaton.getStates()){

            result.put(
                    state.getFormula(),
                    state.getId()
            );
        }

        return result;
    }


    // Reachability

    private Set<ResidualState> reachableStates(
            ResidualAutomaton automaton) {

        Set<ResidualState> visited =
                new HashSet<>();

        Queue<ResidualState> queue =
                new ArrayDeque<>();

        queue.add(
                automaton.getInitialState()
        );

        while(!queue.isEmpty()){

            ResidualState current =
                    queue.remove();

            if(!visited.add(current))
                continue;

            for(ResidualState destination :
                    automaton
                            .getOutgoingTransitions(current)
                            .values()){

                if(!visited.contains(destination))
                    queue.add(destination);
            }
        }

        return visited;
    }


    // Expected Transition Helpers

    private void addTransitions(
            Map<Formula, Map<Valuation, Formula>> transitions,
            Formula source,
            Formula destination,
            Collection<Valuation> valuations) {

        Map<Valuation, Formula> outgoing =
                transitions.computeIfAbsent(
                        source,
                        ignored -> new LinkedHashMap<>()
                );

        for(Valuation valuation : valuations){

            Formula previous =
                    outgoing.put(
                            valuation,
                            destination
                    );

            assertNull(
                    previous,
                    "Expected transition specified twice for " +
                            source +
                            " / " +
                            valuation
            );
        }
    }


    // Valuation helpers

    private static AtomicPropositionSet apSet(
            AtomicProposition... propositions) {

        return new AtomicPropositionSet(
                Arrays.asList(propositions)
        );
    }


    private static Valuation valuation(
            AtomicPropositionSet apSet,
            AtomicProposition... propositions) {

        return Valuation.of(
                apSet,
                propositions
        );
    }


    private static List<Valuation> allValuations(
            Alphabet alphabet) {

        List<Valuation> result =
                new ArrayList<>();

        for(Valuation valuation : alphabet)
            result.add(valuation);

        return result;
    }


    private static List<Valuation> valuations(
            Alphabet alphabet,
            Valuation... valuations) {

        return List.of(valuations);
    }


    private static List<Valuation> valuationsContaining(
            Alphabet alphabet,
            AtomicProposition proposition) {

        List<Valuation> result =
                new ArrayList<>();

        for(Valuation valuation : alphabet){

            if(valuation.contains(proposition))
                result.add(valuation);
        }

        return result;
    }


    private static List<Valuation> valuationsNotContaining(
            Alphabet alphabet,
            AtomicProposition proposition) {

        List<Valuation> result =
                new ArrayList<>();

        for(Valuation valuation : alphabet){

            if(!valuation.contains(proposition))
                result.add(valuation);
        }

        return result;
    }


    // Header

    private static void printHeader(
            String exampleName,
            Formula formula,
            AtomicPropositionSet apSet) {

        System.out.println();
        System.out.println();
        System.out.println(
                "╔══════════════════════════════════════════════════════════╗"
        );
        System.out.printf(
                "║ %-56s ║%n",
                exampleName
        );
        System.out.println(
                "╠══════════════════════════════════════════════════════════╣"
        );
        System.out.printf(
                "║ Formula : %-46s ║%n",
                formula
        );
        System.out.printf(
                "║ AP      : %-46s ║%n",
                apSet
        );
        System.out.println(
                "╚══════════════════════════════════════════════════════════╝"
        );
    }
}
