package ltlf.application;

import ltlf.analysis.dres.DResResult;
import ltlf.analysis.dres.DResResult.StatePair;
import ltlf.automaton.ResidualState;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class DResFormatter {

    public String format(DResResult result){

        Objects.requireNonNull(result);

        StringBuilder out = new StringBuilder();

        out.append("DRes Analysis")
                .append(System.lineSeparator());

        out.append("==============================")
                .append(System.lineSeparator());

        out.append(System.lineSeparator());

        out.append("Number of DRes states : ")
                .append(result.size())
                .append(System.lineSeparator());

        out.append("Number of interchangeable pairs : ")
                .append(result.pairCount())
                .append(System.lineSeparator());

        out.append(System.lineSeparator());

        if(result.isEmpty()){

            out.append("No DRes states found.")
                    .append(System.lineSeparator());

            return out.toString();
        }

        // print DRes states.
        out.append("DRes States:")
                .append(System.lineSeparator());

        out.append("------------------------------")
                .append(System.lineSeparator());

        List<ResidualState> states =
                result.getStates().stream()
                        .sorted(Comparator.comparingInt(
                                ResidualState::getId))
                        .toList();

        for(ResidualState state : states){

            out.append("q")
                    .append(state.getId())
                    .append(" : ")
                    .append(state.getFormula())
                    .append(System.lineSeparator());
        }

        out.append(System.lineSeparator());

        // print interchangeable pairs.
        out.append("Interchangeable Pairs:")
                .append(System.lineSeparator());

        out.append("------------------------------")
                .append(System.lineSeparator());

        List<StatePair> pairs =
                result.getPairs().stream()
                        .sorted(
                                Comparator.comparingInt(
                                        (StatePair pair) ->
                                                pair.first().getId()
                                ).thenComparingInt(
                                        pair -> pair.second().getId()
                                )
                        )
                        .toList();

        for(StatePair pair : pairs){

            out.append("(q")
                    .append(pair.first().getId())
                    .append(", q")
                    .append(pair.second().getId())
                    .append(")")
                    .append(System.lineSeparator());
        }

        out.append(System.lineSeparator());

        return out.toString();
    }
}
