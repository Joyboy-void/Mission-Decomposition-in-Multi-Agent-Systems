package agent.model;

import java.util.Objects;

public record AgentTransition(
        AgentState source,
        AgentState destination,
        int cost
){
    public AgentTransition{
        Objects.requireNonNull(source, "Source");
        Objects.requireNonNull(destination, "Destination");

        if(cost < 0){
            throw new IllegalArgumentException(
                    "Transition cost can't be -ve"
            );
        }
    }
}