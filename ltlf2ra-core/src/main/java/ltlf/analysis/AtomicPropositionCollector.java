package ltlf.analysis;

import java.util.Set;
import java.util.HashSet;
import ltlf.ast.*;

public final class AtomicPropositionCollector
        implements FormulaVisitor<Void> {

    private final Set<AtomicProposition> result = new HashSet<>();

    public Set<AtomicProposition> collect(Formula formula){
        result.clear();

        formula.accept(this);

        // // returning a copy to protect the internal mutable state of the collector
        return Set.copyOf(result);
    }
    @Override
    public Void visitTrue(TrueFormula formula) {
        return null;
    }

    @Override
    public Void visitFalse(FalseFormula formula) {
        return null;
    }

    @Override
    public Void visitAtomicProposition(AtomicProposition formula) {
        result.add(formula);

        return null;
    }

    @Override
    public Void visitNot(Not formula) {
        formula.getOperand().accept(this);
        return null;
    }

    @Override
    public Void visitNext(Next formula) {
        formula.getOperand().accept(this);
        return null;
    }

    @Override
    public Void visitEventually(Eventually formula) {
        formula.getOperand().accept(this);
        return null;
    }

    @Override
    public Void visitAlways(Always formula) {
        formula.getOperand().accept(this);
        return null;
    }

    @Override
    public Void visitAnd(And formula) {
        formula.getLeft().accept(this);
        formula.getRight().accept(this);

        return null;
    }

    @Override
    public Void visitOr(Or formula) {
        formula.getLeft().accept(this);
        formula.getRight().accept(this);

        return null;
    }

    @Override
    public Void visitUntil(Until formula) {
        formula.getLeft().accept(this);
        formula.getRight().accept(this);

        return null;
    }
}
