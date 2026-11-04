package ltlf.analysis.dres;

import ltlf.automaton.ResidualState;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;


/**
 * stores the result of DRes discovery.
 *
 * both the interchangeable state pairs and the union
 * of all states participating in those pairs are retained.
 */

public final class DResResult {

    /**
     * represents two distinct states that satisfy
     * the DRes interchangeability condition.
     */

    public record StatePair(
            ResidualState first,
            ResidualState second) {

        public StatePair{
            Objects.requireNonNull(first);
            Objects.requireNonNull(second);

            if(first == second){
                throw new IllegalArgumentException(
                        "A DRes pair must contain distinct states."
                );
            }

            // canonical ordering: the lower state ID comes first.
            if(first.getId() > second.getId()){
                ResidualState temp = first;
                first = second;
                second = temp;
            }
        }
    }

    private final Set<ResidualState> states;
    private final Set<StatePair> pairs;

    public DResResult(Set<StatePair> pairs){
        Objects.requireNonNull(pairs);

        Set<StatePair> pairCopy =
                new LinkedHashSet<>(pairs);

        Set<ResidualState> stateCopy =
                new LinkedHashSet<>();

        for (StatePair pair : pairCopy) {
            stateCopy.add(pair.first());
            stateCopy.add(pair.second());
        }

        this.pairs = Collections.unmodifiableSet(pairCopy);
        this.states = Collections.unmodifiableSet(stateCopy);
    }

    public Set<ResidualState> getStates() {
        return states;
    }

    public Set<StatePair> getPairs() {
        return pairs;
    }

    public int size() {
        return states.size();
    }

    public int pairCount() {
        return pairs.size();
    }

    public boolean isEmpty() {
        return pairs.isEmpty();
    }
}
