package ltlf.automaton;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

import ltlf.ast.Formula;
import ltlf.transform.Normalizer;
import ltlf.semantics.FormulaEquivalenceChecker;


public final class ResidualStateRegistry{

    private final Normalizer normalizer;
    private final FormulaEquivalenceChecker equivalenceChecker;

    private final Map<Formula, ResidualState> statesByCanonicalFormula;
    private final List<ResidualState> states;

    private int nextId = 0;

    public ResidualStateRegistry(
            Normalizer normalizer,
            FormulaEquivalenceChecker equivalenceChecker ){

        this.normalizer =
                Objects.requireNonNull(normalizer);

        this.equivalenceChecker =
                Objects.requireNonNull(equivalenceChecker);

        this.statesByCanonicalFormula =
                new HashMap<>();

        this.states =
                new ArrayList<>();
    }

    public StateLookupResult getOrCreate(Formula formula){

        Formula canonical =
                normalizer.normalize(formula);


        // cheep canonical lookup
        ResidualState existing =
                statesByCanonicalFormula.get(canonical);

        if(existing != null){
            return new StateLookupResult(
                    existing,
                    false
            );
        }

        // costly equivalence lookup
        for(ResidualState state : states){

            if(equivalenceChecker.equivalent(
                    canonical,
                    state.getFormula() )){

                statesByCanonicalFormula.put(
                        canonical,
                        state
                );

                return new StateLookupResult(
                        state,
                        false
                );
            }
        }

        // create new ResidualState

        ResidualState newState =
                new ResidualState(
                        nextId++,
                        canonical
                );

        statesByCanonicalFormula.put(
                canonical,
                newState
        );

        states.add(newState);

        return new StateLookupResult(
                newState,
                true
        );
    }

    public List<ResidualState> getStates(){
        return List.copyOf(states);
    }

    public int size(){
        return states.size();
    }
}
