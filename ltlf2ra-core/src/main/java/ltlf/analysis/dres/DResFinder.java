package ltlf.analysis.dres;

import ltlf.alphabet.Valuation;
import ltlf.automaton.ResidualAutomaton;
import ltlf.automaton.ResidualState;

import java.util.*;

public final class DResFinder {

    /**
     * internal product-automaton state.
     * both components may be identical.
     */
    private record ProductState(
            ResidualState first,
            ResidualState second) {
    }

    /**
     * ordered candidate pair (q, q').
     */
    private record CandidatePair(
            ResidualState first,
            ResidualState second) {
    }

    public DResResult find(ResidualAutomaton automaton){

        Objects.requireNonNull(automaton);

        ResidualState initial =
                automaton.getInitialState();

        boolean hasFinalState =
                automaton.getStates().stream()
                        .anyMatch(automaton::isFinalState);

        if(!hasFinalState){
            return new DResResult(Set.of());
        }

        List<ResidualState> eligibleStates =
                automaton.getStates().stream()
                        .filter(state -> state != initial)
                        .filter(state -> !automaton.isFinalState(state))
                        .toList();

        if (eligibleStates.size() < 2){
            return new DResResult(Set.of());
        }

        /*
         * condition 1:
         *
         * q0 --w1--> q
         * q' --w1--> qf
         */
        Set<CandidatePair> conditionOne =
                findConditionOne(automaton, eligibleStates);

        /*
         * condition 2:
         *
         * q --w2--> qf
         * q0 --w2--> q'
         */
        Set<CandidatePair> conditionTwo =
                findConditionTwo(automaton, eligibleStates);

        /*
         * intersect ORDERED pairs.
         *
         * The orientation must be identical in both conditions.
         */
        conditionOne.retainAll(conditionTwo);

        /*
         * convert ordered pairs into canonical,
         * unordered StatePair objects for the final result.
         */
        Set<DResResult.StatePair> validPairs =
                new LinkedHashSet<>();

        for(CandidatePair pair : conditionOne){

            validPairs.add(
                    new DResResult.StatePair(
                            pair.first(),
                            pair.second()
                    )
            );
        }

        return new DResResult(validPairs);
    }

    /**
     * condition 1:
     *
     * q0 --w1--> q
     * q' --w1--> qf
     */
    private Set<CandidatePair> findConditionOne(
            ResidualAutomaton automaton,
            List<ResidualState> eligibleStates) {

        Set<CandidatePair> result =
                new LinkedHashSet<>();

        ResidualState initial =
                automaton.getInitialState();

        for(ResidualState qPrime : eligibleStates){

            Set<ProductState> reachable =
                    reachablePairs(
                            automaton,
                            new ProductState(initial, qPrime)
                    );

            for(ProductState pair : reachable){

                ResidualState q = pair.first();

                if(isEligible(automaton, q, initial)
                        && q != qPrime
                        && automaton.isFinalState(pair.second())){

                    result.add(
                            new CandidatePair(q, qPrime)
                    );
                }
            }
        }

        return result;
    }

    /**
     * condition 2:
     *
     * q --w2--> qf
     * q0 --w2--> q'
     */
    private Set<CandidatePair> findConditionTwo(
            ResidualAutomaton automaton,
            List<ResidualState> eligibleStates){

        Set<CandidatePair> result =
                new LinkedHashSet<>();

        ResidualState initial =
                automaton.getInitialState();

        for(ResidualState q : eligibleStates){

            Set<ProductState> reachable =
                    reachablePairs(
                            automaton,
                            new ProductState(q, initial)
                    );

            for(ProductState pair : reachable){

                ResidualState qPrime = pair.second();

                if(automaton.isFinalState(pair.first())
                        && isEligible(automaton, qPrime, initial)
                        && q != qPrime) {

                    result.add(
                            new CandidatePair(q, qPrime)
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
            ResidualState initial) {

        return state != null
                && state != initial
                && !automaton.isFinalState(state);
    }

    /**
     * BFS over the synchronized product graph Q x Q.
     *
     * both components consume exactly the same valuation.
     */
    private Set<ProductState> reachablePairs(
            ResidualAutomaton automaton,
            ProductState start) {

        Set<ProductState> visited =
                new LinkedHashSet<>();

        Queue<ProductState> queue =
                new ArrayDeque<>();

        visited.add(start);
        queue.add(start);

        while(!queue.isEmpty()){

            ProductState current = queue.remove();

            for(Valuation valuation : automaton.getAlphabet()){

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

                if(nextFirst == null || nextSecond == null){
                    continue;
                }

                ProductState next =
                        new ProductState(nextFirst, nextSecond);

                if(visited.add(next)){
                    queue.add(next);
                }
            }
        }

        return visited;
    }
}
