package ltlf.automaton;

import java.util.Objects;

import ltlf.ast.Formula;

/*
*   Objects of ResidualStates are created and maintained only by
*                                           Class::ltlf.automaton.ResidualStateRegistry
*
*/


public final class ResidualState {

    private final int id;
    private final Formula formula;

    public ResidualState(int id, Formula formula){
        this.id = id;
        this.formula = Objects.requireNonNull(formula);
    }

    public int getId(){
        return id;
    }

    public Formula getFormula(){
        return formula;
    }

    @Override
    public String toString(){
        return "q" + id + ": " + formula;
    }
}
