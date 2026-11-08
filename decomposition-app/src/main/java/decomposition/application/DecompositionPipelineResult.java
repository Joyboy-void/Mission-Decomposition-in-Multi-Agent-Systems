package decomposition.application;

import decomposition.DecompositionResult;
import ltlf.analysis.dres.DResResult;
import ltlf.automaton.ResidualAutomaton;
import product.automaton.ProductAutomaton;
import team.automaton.TeamAutomaton;

import java.util.Objects;

/**
 * stores all intermediate and final results of the
 * machine decomposition pipeline.
 */

public record DecompositionPipelineResult(
        ResidualAutomaton residualAutomaton,
        ProductAutomaton productAutomaton,
        DResResult dresResult,
        TeamAutomaton teamAutomaton,
        DecompositionResult decompositionResult
){
    public DecompositionPipelineResult {
        Objects.requireNonNull(residualAutomaton);
        Objects.requireNonNull(productAutomaton);
        Objects.requireNonNull(dresResult);
        Objects.requireNonNull(teamAutomaton);
        Objects.requireNonNull(decompositionResult);
    }
}