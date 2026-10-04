package ltlf.semantics;

import java.util.Objects;

import ltlf.ast.*;
import ltlf.alphabet.Valuation;


public final class ResidualVisitor implements FormulaVisitor<Formula> {

    private final Valuation valuation;

    public ResidualVisitor(Valuation valuation){
        this.valuation = Objects.requireNonNull(valuation);
    }

    //Res(⊤,a) = ⊤
    @Override
    public Formula visitTrue(TrueFormula formula) {
        return formula;
    }

    //Res(⊥,a) = ⊥
    @Override
    public Formula visitFalse(FalseFormula formula) {
        return formula;
    }

    // Res(p,a) = ⊤, if p ∈ a. Otherwise ⊥
    @Override
    public Formula visitAtomicProposition(AtomicProposition formula) {

        if(valuation.contains(formula))
            return new TrueFormula();

        return new FalseFormula();
    }


    // Res(¬φ,a) = ¬Res(φ,a)
    @Override
    public Formula visitNot(Not formula) {

        Formula operandResidual = formula.getOperand().accept(this);

        return new Not(operandResidual);
    }


    // Res(⃝φ,a) = φ.
    @Override
    public Formula visitNext(Next formula) {

        return formula.getOperand();
    }

    // Res(♢φ,a) = Res(φ,a)∨♢φ
    @Override
    public Formula visitEventually(Eventually formula) {

        Formula operandResidual = formula.getOperand().accept(this);

        return new Or(operandResidual, formula);
    }


    //  Res(Gφ,a) = Res(φ,a)∧Gφ.
    @Override
    public Formula visitAlways(Always formula) {

        Formula operandResidual = formula.getOperand().accept(this);

        return new And(operandResidual, formula);
    }

    //  Res(φ∧ψ,a) = Res(φ,a)∧Res(ψ,a)
    @Override
    public Formula visitAnd(And formula) {

        Formula leftResidual = formula.getLeft().accept(this);
        Formula rightResidual = formula.getRight().accept(this);

        return new And(leftResidual, rightResidual);
    }


    //  Res(φ∨ψ,a) = Res(φ,a)∨Res(ψ,a)
    @Override
    public Formula visitOr(Or formula) {

        Formula leftResidual = formula.getLeft().accept(this);
        Formula rightResidual = formula.getRight().accept(this);

        return new Or(leftResidual, rightResidual);
    }


    // Res(φUψ,a) = Res(ψ,a)∨(Res(φ,a)∧φUψ).
    @Override
    public Formula visitUntil(Until formula) {

        Formula leftResidual = formula.getLeft().accept(this);
        Formula rightResidual = formula.getRight().accept(this);

        Formula rightPart = new And(leftResidual, formula);

        return new Or(rightResidual, rightPart);
    }
}
