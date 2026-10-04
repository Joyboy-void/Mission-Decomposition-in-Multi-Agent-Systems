package ltlf.automaton;

import java.util.Objects;

public final class StateLookupResult {

    private final ResidualState state;
    private final boolean created;

    public StateLookupResult(ResidualState state, boolean created){
        this.state = Objects.requireNonNull(state);
        this.created = created;
    }

    public ResidualState getState(){
        return state;
    }

    public boolean isCreated(){
        return created;
    }
}
