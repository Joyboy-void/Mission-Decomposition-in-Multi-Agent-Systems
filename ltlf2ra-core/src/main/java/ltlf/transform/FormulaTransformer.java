package ltlf.transform;

import ltlf.ast.Formula;

public interface FormulaTransformer {

    public Formula transform(Formula formula);
}
