package ltlf.ast;

public final class FalseFormula implements Formula {
    @Override
    public String toString(){
        return "⊥";
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof FalseFormula;
    }

    @Override
    public int hashCode(){
        return 2;
    }

    @Override
    public <T> T accept(FormulaVisitor<T> visitor){
        return visitor.visitFalse(this);
    }
}
