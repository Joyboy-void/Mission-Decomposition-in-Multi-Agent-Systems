package ltlf.transform;

import ltlf.ast.*;

public final class Simplifier
    implements FormulaTransformer, FormulaVisitor<Formula> {

    @Override
    public Formula transform(Formula formula){
        return formula.accept(this);
    }

    @Override
    public Formula visitTrue(TrueFormula formula) {
        return formula;
    }

    @Override
    public Formula visitFalse(FalseFormula formula) {
        return formula;
    }

    @Override
    public Formula visitAtomicProposition(AtomicProposition formula) {
        return formula;
    }

    // simplifies ~T == F , ~F == T , ~~P = P
    @Override
    public Formula visitNot(Not formula) {

        Formula operand = formula.getOperand().accept(this);

        if(operand instanceof TrueFormula)
            return new FalseFormula();

        if(operand instanceof FalseFormula)
            return new TrueFormula();

        if(operand instanceof Not nestedNot)
            return nestedNot.getOperand();

        return new Not(operand);
    }

    @Override
    public Formula visitNext(Next formula) {
        Formula operand =
                    formula.getOperand().accept(this);

        return new Next(operand);
    }

    @Override
    public Formula visitEventually(Eventually formula) {

        Formula operand =
                    formula.getOperand().accept(this);


        // F F (ϕ) = F ϕ
        if(operand instanceof Eventually)
            return operand;

        return new Eventually(operand);
    }

    @Override
    public Formula visitAlways(Always formula) {

        Formula operand =
                    formula.getOperand().accept(this);

        // G G (ϕ) = G ϕ

        if(operand instanceof Always)
            return operand;

        return new Always(operand);
    }

    @Override
    public Formula visitAnd(And formula) {

        Formula left = formula.getLeft().accept(this);
        Formula right = formula.getRight().accept(this);

        if(left instanceof FalseFormula || right instanceof FalseFormula)
            return new FalseFormula();

        if(left instanceof TrueFormula)
            return right;

        if(right instanceof TrueFormula)
            return left;

        if(left.equals(right))  // only structural equivalence
            return left;

        return new And(left, right);
    }

    @Override
    public Formula visitOr(Or formula) {

        Formula left = formula.getLeft().accept(this);
        Formula right = formula.getRight().accept(this);

        if(left instanceof TrueFormula || right instanceof TrueFormula)
            return new TrueFormula();

        if(left instanceof FalseFormula)
            return right;

        if(right instanceof FalseFormula)
            return left;

        if(left.equals(right)) // only structural equivalence
            return left;

        return new Or(left, right);
    }

    @Override
    public Formula visitUntil(Until formula) {

        Formula left =
                    formula.getLeft().accept(this);

        Formula right =
                    formula.getRight().accept(this);

        return new Until(left, right);
    }
}
