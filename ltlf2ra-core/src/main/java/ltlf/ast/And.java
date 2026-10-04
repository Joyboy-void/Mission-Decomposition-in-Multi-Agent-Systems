package ltlf.ast;

public final class And extends BinaryFormula{

    public And(Formula left, Formula right){
        super(left, right);
    }

    @Override
    public String toString(){
        return "(" + getLeft() + " ∧ " + getRight() + ")";
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor){
        return visitor.visitAnd(this);
    }

}
