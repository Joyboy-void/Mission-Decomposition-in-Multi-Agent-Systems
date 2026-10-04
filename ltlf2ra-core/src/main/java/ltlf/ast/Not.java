package ltlf.ast;

public final class Not extends UnaryFormula{

    public Not(Formula operand) {
        super(operand);
    }

    @Override
    public String toString() {
        return "¬(" + getOperand() + ")";
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor){
        return visitor.visitNot(this);
    }
}
