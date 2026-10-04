package ltlf.ast;

public final class Eventually extends UnaryFormula{

    public Eventually(Formula operand){
        super(operand);
    }

    @Override
    public String toString(){
        return "F(" + getOperand() + ")";
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor){
        return visitor.visitEventually(this);
    }
}
