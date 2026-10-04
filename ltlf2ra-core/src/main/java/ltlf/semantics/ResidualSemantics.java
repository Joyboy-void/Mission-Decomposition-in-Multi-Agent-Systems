package ltlf.semantics;

import ltlf.alphabet.Valuation;
import ltlf.ast.Formula;

public interface ResidualSemantics {

    public Formula residual(Formula formula, Valuation valuation);
}
