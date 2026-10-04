package ltlf.semantics;

import ltlf.alphabet.Valuation;
import ltlf.ast.Formula;

import java.util.Objects;

public final class DefaultResidualSemantics implements ResidualSemantics{

    @Override
    public Formula residual(Formula formula, Valuation valuation){

        Objects.requireNonNull(formula);
        Objects.requireNonNull(valuation);

        ResidualVisitor visitor = new ResidualVisitor(valuation);

        return formula.accept(visitor);
    }

}
