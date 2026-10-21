package product.automaton;

import agent.model.AgentState;
import ltlf.automaton.ResidualState;

import java.util.Objects;

public record ProductState(
        AgentState agentState,
        ResidualState residualState
){

    public ProductState{
        Objects.requireNonNull(agentState);
        Objects.requireNonNull(residualState);
    }

    @Override
    public String toString(){
        return "(" + agentState + ",q"
                    + residualState.getId() + ")" ;
    }
}