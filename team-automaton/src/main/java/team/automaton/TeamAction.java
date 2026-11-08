package team.automaton;

import ltlf.alphabet.Valuation;

import java.util.Objects;

/**
 * Actions supported by the Team Automaton.
 *
 * An action is either:
 * 1. An ordinary product Automaton transition.
 * 2. An epsilon switch between consecutive agent copies.
 */
public sealed interface TeamAction {

    record ValuationAction(
            Valuation valuation
    ) implements TeamAction {

        public ValuationAction {
            Objects.requireNonNull(
                    valuation,
                    "Valuation must not be null"
            );
        }

        @Override
        public String toString() {
            return valuation.toString();
        }
    }

    enum SwitchAction implements TeamAction {
        EPSILON;

        @Override
        public String toString(){
            return "EPSILON";
        }
    }
}
