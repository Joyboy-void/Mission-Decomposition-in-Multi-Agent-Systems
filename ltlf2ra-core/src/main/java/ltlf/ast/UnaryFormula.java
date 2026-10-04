package ltlf.ast;

import java.util.Objects;

public abstract class UnaryFormula implements Formula{
    private final Formula operand;

    protected UnaryFormula(Formula operand){
        this.operand = Objects.requireNonNull(operand);
    }

    public Formula getOperand(){
        return operand;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj)
            return true;

        if(obj == null || getClass() != obj.getClass())
            return false;

        UnaryFormula other = (UnaryFormula) obj;

        return operand.equals(other.operand);
    }

    @Override
    public int hashCode(){
        return Objects.hash(getClass(), operand);
    }
}
