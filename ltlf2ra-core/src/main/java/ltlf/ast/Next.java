package ltlf.ast;

public final class Next extends UnaryFormula {
    public Next(Formula operand){
        super(operand);
    }

    @Override
    public String toString(){
        return "X("+ getOperand() + ")";
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor){
        return visitor.visitNext(this);
    }
}
