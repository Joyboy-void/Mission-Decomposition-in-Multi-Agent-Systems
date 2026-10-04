package ltlf.ast;

public final class Or extends BinaryFormula {
    public Or(Formula left, Formula right){
        super(left, right);
    }

    @Override
    public String toString(){
        return "(" + getLeft() + " ∨ " + getRight() + ")";
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor){
        return visitor.visitOr(this);
    }
}
