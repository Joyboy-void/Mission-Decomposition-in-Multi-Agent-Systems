package ltlf.external.spot;

import ltlf.ast.*;

public final class SpotFormulaPrinter
        implements FormulaVisitor<String> {

    public String print(Formula formula){
        return formula.accept(this);
    }

    @Override
    public String visitTrue(TrueFormula formula){
        return "1";
    }

    @Override
    public String visitFalse(FalseFormula formula){
        return "0";
    }

    @Override
    public String visitAtomicProposition(
            AtomicProposition formula) {

        // assume proposition names use Spot-safe identifiers.

        return formula.getName();
    }

    @Override
    public String visitNot(Not formula){
        return "!("
                + formula.getOperand().accept(this)
                + ")";
    }

    @Override
    public String visitNext(Next formula){
        return "X(" +
                formula.getOperand().accept(this) +
                ")";
    }

    @Override
    public String visitEventually(Eventually formula) {

        return "F("
                + formula.getOperand().accept(this)
                + ")";
    }

    @Override
    public String visitAlways(Always formula){

        return "G("
                + formula.getOperand().accept(this)
                + ")";
    }

    @Override
    public String visitAnd(And formula) {

        return "("
                + formula.getLeft().accept(this)
                + " & "
                + formula.getRight().accept(this)
                + ")";
    }

    @Override
    public String visitOr(Or formula) {

        return "("
                + formula.getLeft().accept(this)
                + " | "
                + formula.getRight().accept(this)
                + ")";
    }

    @Override
    public String visitUntil(Until formula) {

        return "("
                + formula.getLeft().accept(this)
                + " U "
                + formula.getRight().accept(this)
                + ")";
    }
}