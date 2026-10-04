package ltlf.ast;

import java.util.Objects;

public abstract class BinaryFormula implements Formula{

    private final Formula left;
    private final Formula right;

    protected BinaryFormula(Formula left, Formula right){
        this.left = Objects.requireNonNull(left);
        this.right = Objects.requireNonNull(right);
    }

    public Formula getLeft(){
        return left;
    }
    public Formula getRight(){
        return right;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj)
            return true;

        if(obj == null || getClass() != obj.getClass())
            return false;

        BinaryFormula other = (BinaryFormula) obj;

        return left.equals(other.left) &&
                right.equals(other.right);
    }

    @Override
    public int hashCode(){
        return Objects.hash(getClass(), left, right);
    }
}
