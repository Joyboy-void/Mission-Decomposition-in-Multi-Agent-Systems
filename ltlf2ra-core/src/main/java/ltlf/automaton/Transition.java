package ltlf.automaton;

import java.util.Objects;

import ltlf.alphabet.Valuation;

public final class Transition{

    private final ResidualState source;
    private final Valuation valuation;
    private final ResidualState destination;

    public Transition(
        ResidualState source,
        Valuation valuation,
        ResidualState destination ){

        this.source =
                Objects.requireNonNull(source);
        this.valuation =
                Objects.requireNonNull(valuation);
        this.destination =
                Objects.requireNonNull(destination);

    }

    public ResidualState getSource(){
        return source;
    }

    public Valuation getValuation(){
        return valuation;
    }

    public ResidualState getDestination(){
        return destination;
    }

    @Override
    public String toString(){
        return source.getId() +
                " --- " +
                valuation +
                " --> " +
                destination.getId();
    }
}
