package ltlf.application;

import ltlf.alphabet.Valuation;
import ltlf.automaton.ResidualAutomaton;
import ltlf.automaton.ResidualState;

import java.util.Objects;

public final class ResidualAutomatonFormatter {

    public String format(ResidualAutomaton automaton){
        Objects.requireNonNull(automaton);

        StringBuilder out = new StringBuilder();

        out.append("Residual Automaton").append(System.lineSeparator());
        out.append("==============================").append(System.lineSeparator());
        out.append(System.lineSeparator());

        out.append("Number of states : ").
                append(automaton.size()).
                append(System.lineSeparator());

        out.append("Alphabet Size : ").
                append(automaton.getAlphabet().size()).
                append(System.lineSeparator());

        out.append("Atomic Propositions : ").
                append(automaton.getAlphabet().getAtomicPropositionSet()).
                append(System.lineSeparator());

        out.append(System.lineSeparator());
        out.append("States : ").append(System.lineSeparator());
        out.append("-------------------").append(System.lineSeparator());


        for(ResidualState state : automaton.getStates()){

            out.append("q").append(state.getId());

            if(state.equals(automaton.getInitialState())){
                out.append("[initial]");
            }

            out.append(" : ").
                    append(state.getFormula()).
                    append(System.lineSeparator());
        }

        out.append(System.lineSeparator());

        out.append("Transitions : ").
            append(System.lineSeparator());

        out.append("-------------------------").
                append(System.lineSeparator());

        for(ResidualState source : automaton.getStates()){

            for(Valuation valuation : automaton.getAlphabet()){

                ResidualState destination = automaton.getDestination(source, valuation);

                out.append("q")
                        .append(source.getId())
                        .append("--")
                        .append(valuation)
                        .append("-->q")
                        .append(destination.getId())
                        .append(System.lineSeparator());
            }
        }

        out.append(System.lineSeparator());

        return out.toString();
    }
}
