package ltlf.semantics;

import ltlf.ast.Formula;

public interface FormulaEquivalenceChecker {

    public boolean equivalent(Formula left, Formula right);
}
