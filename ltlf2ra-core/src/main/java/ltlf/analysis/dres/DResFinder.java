package ltlf.analysis.dres;

import ltlf.alphabet.Valuation;
import ltlf.automaton.ResidualAutomaton;
import ltlf.automaton.ResidualState;

import java.util.*;

/**
 * finds DRes state pairs using synchronized product-state exploration.
 */
public final class DResFinder {

    // Internal product-automaton state. Both components may be identical.

    private record ProductState(
            ResidualState first,
            ResidualState second
    ){}

    // ordered candidate pair (q, qPrime). orientation is significant.

    private record CandidatePair(
            ResidualState first,
            ResidualState second
    ){}

    // One predecessor link in a synchronized-product BFS tree.

    private record ProductStep(
            ProductState previous,
            Valuation valuation
    ) {}

    public DResResult find(ResidualAutomaton automaton){

        Objects.requireNonNull(automaton);

        ResidualState initial =
                automaton.getInitialState();

        boolean hasFinalState =
                automaton.getStates().stream()
                        .anyMatch(automaton::isFinalState);

        if(!hasFinalState){
            return DResResult.withCertificates(Set.of());
        }

        List<ResidualState> eligibleStates =
                automaton.getStates().stream()
                        .filter(state -> state != initial)
                        .filter(state -> !automaton.isFinalState(state))
                        .toList();

        if (eligibleStates.size() < 2){
            return DResResult.withCertificates(Set.of());
        }

        /*
         * condition 1:
         *
         * q0 --w1--> q
         * qPrime --w1--> qFinal
         */
        Map<CandidatePair, List<Valuation>> conditionOne =
                findConditionOne(automaton, eligibleStates);

        /*
         * condition 2:
         *
         * q --w2--> qFinal
         * q0 --w2--> qPrime
         */
        Map<CandidatePair, List<Valuation>> conditionTwo =
                findConditionTwo(automaton, eligibleStates);

        // preserve the original ordered-pair intersection semantics.

        conditionOne.keySet().retainAll(conditionTwo.keySet());

        Set<DResResult.Certificate> certificates =
                new LinkedHashSet<>();

        Set<DResResult.StatePair> certifiedPairs =
                new LinkedHashSet<>();

        for (CandidatePair candidate : conditionOne.keySet()) {

            ResidualState q = candidate.first();
            ResidualState qPrime = candidate.second();

            DResResult.StatePair canonicalPair =
                    new DResResult.StatePair(q, qPrime);

            /*
             * the public result represents unordered canonical pairs.
             *
             * if both orientations satisfy the conditions, retain the
             * first deterministic certificate for that canonical pair.
             */
            if(certifiedPairs.add(canonicalPair)){
                certificates.add(
                        new DResResult.Certificate(
                                canonicalPair,
                                q,
                                qPrime,
                                conditionOne.get(candidate),
                                conditionTwo.get(candidate)
                        )
                );
            }
        }

        return DResResult.withCertificates(certificates);
    }

    /**
     * finds shortest w1 witnesses for condition 1.
     */
    private Map<CandidatePair, List<Valuation>> findConditionOne(
            ResidualAutomaton automaton,
            List<ResidualState> eligibleStates
    ){

        Map<CandidatePair, List<Valuation>> result =
                new LinkedHashMap<>();

        ResidualState initial =
                automaton.getInitialState();

        for(ResidualState qPrime : eligibleStates){

            Map<ProductState, ProductStep> reachable =
                    reachablePairs(
                            automaton,
                            new ProductState(initial, qPrime)
                    );

            for (ProductState pair : reachable.keySet()) {

                ResidualState q = pair.first();

                if(isEligible(automaton, q, initial)
                        && q != qPrime
                        && automaton.isFinalState(pair.second())){

                    result.putIfAbsent(
                            new CandidatePair(q, qPrime),
                            reconstructWord(reachable, pair)
                    );
                }
            }
        }

        return result;
    }

    /**
     * Finds shortest w2 witnesses for condition 2.
     */
    private Map<CandidatePair, List<Valuation>> findConditionTwo(
            ResidualAutomaton automaton,
            List<ResidualState> eligibleStates
    ){

        Map<CandidatePair, List<Valuation>> result =
                new LinkedHashMap<>();

        ResidualState initial =
                automaton.getInitialState();

        for(ResidualState q : eligibleStates){

            Map<ProductState, ProductStep> reachable =
                    reachablePairs(
                            automaton,
                            new ProductState(q, initial)
                    );

            for (ProductState pair : reachable.keySet()) {

                ResidualState qPrime = pair.second();

                if(automaton.isFinalState(pair.first())
                        && isEligible(automaton, qPrime, initial)
                        && q != qPrime) {

                    result.putIfAbsent(
                            new CandidatePair(q, qPrime),
                            reconstructWord(reachable, pair)
                    );
                }
            }
        }

        return result;
    }

    /**
     * checks whether a state is neither initial nor final.
     */
    private boolean isEligible(
            ResidualAutomaton automaton,
            ResidualState state,
            ResidualState initial
    ) {

        return state != null
                && state != initial
                && !automaton.isFinalState(state);
    }

    /**
     * BFS over the synchronized product graph Q x Q.
     *
     * both components consume exactly the same valuation.
     */
    private Map<ProductState, ProductStep> reachablePairs(
            ResidualAutomaton automaton,
            ProductState start
    ) {

        Map<ProductState, ProductStep> predecessor =
                new LinkedHashMap<>();

        Queue<ProductState> queue =
                new ArrayDeque<>();

        predecessor.put(start, null);
        queue.add(start);

        while(!queue.isEmpty()){

            ProductState current = queue.remove();

            for (Valuation valuation : automaton.getAlphabet()) {

                ResidualState nextFirst =
                        automaton.getDestination(
                                current.first(),
                                valuation
                        );

                ResidualState nextSecond =
                        automaton.getDestination(
                                current.second(),
                                valuation
                        );

                if (nextFirst == null || nextSecond == null) {
                    continue;
                }

                ProductState next =
                        new ProductState(nextFirst, nextSecond);

                if (!predecessor.containsKey(next)) {

                    predecessor.put(
                            next,
                            new ProductStep(current, valuation)
                    );

                    queue.add(next);
                }
            }
        }

        return predecessor;
    }

    /**
     * Reconstructs the shortest synchronized word to a reached
     * product state.
     */
    private List<Valuation> reconstructWord(
            Map<ProductState, ProductStep> predecessor,
            ProductState target
    ) {

        List<Valuation> word =
                new ArrayList<>();

        ProductState current = target;

        while (true) {

            ProductStep step =
                    predecessor.get(current);

            if (step == null) {
                break;
            }

            word.add(step.valuation());
            current = step.previous();
        }

        Collections.reverse(word);

        return List.copyOf(word);
    }
}