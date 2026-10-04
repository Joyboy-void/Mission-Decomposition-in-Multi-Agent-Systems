package ltlf.analysis;

import ltlf.ast.*;

public final class FormulaSizeVisitor implements FormulaVisitor<Integer>{

    @Override
    public Integer visitTrue(TrueFormula formula) {
        return 1;
    }

    @Override
    public Integer visitFalse(FalseFormula formula) {
        return 1;
    }

    @Override
    public Integer visitAtomicProposition(AtomicProposition formula) {
        return 1;
    }

    @Override
    public Integer visitNot(Not formula) {
        return 1 + formula.getOperand().accept(this);
    }

    @Override
    public Integer visitNext(Next formula) {
        return 1 + formula.getOperand().accept(this);
    }

    @Override
    public Integer visitEventually(Eventually formula) {
        return 1 + formula.getOperand().accept(this);
    }

    @Override
    public Integer visitAlways(Always formula) {
        return 1 + formula.getOperand().accept(this);
    }

    @Override
    public Integer visitAnd(And formula) {
        return 1 + formula.getLeft().accept(this) +
                    formula.getRight().accept(this);
    }

    @Override
    public Integer visitOr(Or formula) {
        return 1 + formula.getLeft().accept(this) +
                formula.getRight().accept(this);
    }

    @Override
    public Integer visitUntil(Until formula) {
        return 1 + formula.getLeft().accept(this) +
                formula.getRight().accept(this);
    }
}
