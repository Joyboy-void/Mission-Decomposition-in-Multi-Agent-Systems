package team.automaton;

import product.automaton.ProductState;

import java.util.Objects;

/**
 * A state in the Team Automaton.
 *
 * copyId identifies the homogeneous agent.
 * productState identifies the underlying Product Automaton state.
 */
public record TeamState(
        int copyId,
        ProductState productState
){

    public TeamState {

        if(copyId < 1){
            throw new IllegalArgumentException(
                    "Team copy ID must be >= 1"
            );
        }

        Objects.requireNonNull(
                productState,
                "Product state must not be null"
        );
    }

    @Override
    public String toString(){
        return "c" + copyId + ":" + productState;
    }
}
