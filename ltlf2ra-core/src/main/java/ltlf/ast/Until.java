package ltlf.ast;

public final class Until extends BinaryFormula {
    public Until(Formula left, Formula right){
        super(left, right);
    }

    @Override
    public String toString(){
        return "(" + getLeft() + " U " + getRight() + ")";
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor){
        return visitor.visitUntil(this);
    }
}
