package ltlf.automaton;

import java.util.*;

import ltlf.ast.Formula;
import ltlf.alphabet.Alphabet;
import ltlf.alphabet.Valuation;
import ltlf.semantics.FormulaEquivalenceChecker;
import ltlf.semantics.ResidualSemantics;
import ltlf.transform.Normalizer;


public final class ResidualAutomatonBuilder {

    private final ResidualSemantics residualSemantics;
    private final Normalizer normalizer;
    private final FormulaEquivalenceChecker equivalenceChecker;
    private final Alphabet alphabet;

    public ResidualAutomatonBuilder(
            ResidualSemantics residualSemantics,
            Normalizer normalizer,
            FormulaEquivalenceChecker equivalenceChecker,
            Alphabet alphabet ){

        this.residualSemantics =
                Objects.requireNonNull(residualSemantics);

        this.normalizer =
                Objects.requireNonNull(normalizer);

        this.equivalenceChecker =
                Objects.requireNonNull(equivalenceChecker);

        this.alphabet =
                Objects.requireNonNull(alphabet);

    }

    public ResidualAutomaton build(Formula initialFormula){

        Objects.requireNonNull(initialFormula);


        // fresh registry per automaton
        ResidualStateRegistry registry =
                new ResidualStateRegistry(
                        normalizer,
                        equivalenceChecker
                );

        // create initialState q0
        StateLookupResult initialResult =
                registry.getOrCreate(initialFormula);

        ResidualState initialState =
                initialResult.getState();

        // BFS queue
        Queue<ResidualState> queue =
                new ArrayDeque<>();

        queue.add(initialState);

        Map<ResidualState, Map<Valuation, ResidualState>> transitions =
            new LinkedHashMap<>();


        // BFS over all reachable states
        while(!queue.isEmpty()){

            ResidualState current = queue.remove();

            Map<Valuation, ResidualState> outgoing =
                    transitions.computeIfAbsent(
                            current,
                            ignored -> new LinkedHashMap<>()
                    );


            // compute residual on each alphabet
            for(Valuation valuation : alphabet){

                Formula rawResidual =
                        residualSemantics.residual(
                                current.getFormula(),
                                valuation
                        );

                StateLookupResult result =
                        registry.getOrCreate(
                                rawResidual
                        );

                ResidualState destination
                        = result.getState();


                outgoing.put(
                        valuation,
                        destination
                );

                if(result.isCreated())
                    queue.add(destination);
            }
        }

        return new ResidualAutomaton(
                alphabet,
                registry.getStates(),
                initialState,
                transitions
        );
    }
}
