package agent.model;

import java.util.Objects;

public record AgentState(String id){

    public AgentState{
        Objects.requireNonNull(id, "id");

        if(id.isBlank()){
            throw new IllegalArgumentException(
                "Agent State ID can't be blank"
            );
        }
    }

    @Override
    public String toString(){
        return id;
    }
}