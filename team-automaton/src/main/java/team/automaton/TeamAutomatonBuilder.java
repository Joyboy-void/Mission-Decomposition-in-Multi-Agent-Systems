
package team.automaton;

import ltlf.analysis.dres.DResResult;
import product.automaton.ProductAutomaton;

import java.util.Objects;

/**
 * Constructs Team Automata from a Product Automaton,
 * DRes result and number of homogeneous agents (n).
 */
public final class TeamAutomatonBuilder {

    private TeamAutomatonBuilder(){
    }

    public static TeamAutomaton build(
            ProductAutomaton productAutomaton,
            DResResult dres,
            int numberOfAgents
    ){

        Objects.requireNonNull(
                productAutomaton,
                "Product Automaton must not be null"
        );

        Objects.requireNonNull(
                dres,
                "DRes result must not be null"
        );

        if (numberOfAgents < 1){
            throw new IllegalArgumentException(
                    "Number of agents must be >= 1"
            );
        }

        return new TeamAutomaton(
                productAutomaton,
                dres,
                numberOfAgents
        );
    }
}
