package ltlf.ast;

public final class Always extends UnaryFormula{

    public Always(Formula operand){
        super(operand);
    }

    @Override
    public String toString(){
        return "G(" + getOperand() + ")";
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor){
        return visitor.visitAlways(this);
    }
}
