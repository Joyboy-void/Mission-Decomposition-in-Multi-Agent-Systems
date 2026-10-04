package ltlf.alphabet;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class Alphabet implements Iterable<Valuation> {

    private final AtomicPropositionSet apSet;

    public Alphabet(AtomicPropositionSet apSet){
        this.apSet = Objects.requireNonNull(apSet);
    }

    public AtomicPropositionSet getAtomicPropositionSet(){
        return apSet;
    }

    public long size(){

        int n = apSet.size();

        if(n >= 63){
            throw new ArithmeticException(
                    "Alphabet size dose not fit in long"
            );
        }

        return 1L << n;
    }

    @Override
    public Iterator<Valuation> iterator(){

        return new Iterator<>(){

            private long current = 0;
            private final long total = size();

            @Override
            public boolean hasNext(){
                return current < total;
            }

            @Override
            public Valuation next(){

                if(!(hasNext())){
                    throw new NoSuchElementException();
                }

                BitSet bits = new BitSet(apSet.size());

                for(int i = 0; i < apSet.size(); i++){

                    if((current & (1L << i)) != 0){
                        bits.set(i);
                    }
                }

                current++;

                return Valuation.fromBits(apSet, bits);
            }
        };
    }
}
