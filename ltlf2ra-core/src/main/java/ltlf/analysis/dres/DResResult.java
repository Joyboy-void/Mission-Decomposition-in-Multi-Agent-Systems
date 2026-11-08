package ltlf.analysis.dres;

import ltlf.alphabet.Valuation;
import ltlf.automaton.ResidualState;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * stores the result of DRes discovery.
 *
 * both interchangeable state pairs and the union of all states
 * participating in those pairs are retained.
 *
 * when produced by DResFinder, the result also retains synchronized
 * witness words for the two conditions defining each DRes candidate.
 */
public final class DResResult {

    /**
     * Represents two distinct states satisfying the DRes condition.
     *
     * The pair is stored canonically (lower state ID first).
     */
    public record StatePair(
            ResidualState first,
            ResidualState second
    ) {
        public StatePair{
            Objects.requireNonNull(first);
            Objects.requireNonNull(second);

            if(first == second){
                throw new IllegalArgumentException(
                        "A DRes pair must contain distinct states."
                );
            }

            if(first.getId() > second.getId()){
                ResidualState temp = first;
                first = second;
                second = temp;
            }
        }
    }

    /**
     * a synchronized witness for an ordered candidate (q, qPrime).
     *
     * w1 witnesses:
     * q0 --w1--> q
     * qPrime --w1--> qFinal
     *
     * w2 witnesses:
     * q --w2--> qFinal
     * q0 --w2--> qPrime
     *
     * the ordered q and qPrime fields are retained separately from
     * the canonical StatePair because orientation matters during
     * DRes detection.
     */
    public record Certificate(
            StatePair pair,
            ResidualState q,
            ResidualState qPrime,
            List<Valuation> w1,
            List<Valuation> w2
    ){
        public Certificate {
            Objects.requireNonNull(pair);
            Objects.requireNonNull(q);
            Objects.requireNonNull(qPrime);
            Objects.requireNonNull(w1);
            Objects.requireNonNull(w2);

            if(q == qPrime){
                throw new IllegalArgumentException(
                        "A DRes certificate requires distinct states."
                );
            }

            if(!pair.equals(new StatePair(q, qPrime))){
                throw new IllegalArgumentException(
                        "Certificate pair does not match q and qPrime."
                );
            }

            w1 = List.copyOf(w1);
            w2 = List.copyOf(w2);
        }
    }

    private final Set<ResidualState> states;
    private final Set<StatePair> pairs;
    private final Set<Certificate> certificates;

    /**
     * ackward-compatible constructor.
     *
     * Results constructed using this constructor contain DRes pairs
     * but do not contain witness certificates.
     */
    public DResResult(Set<StatePair> pairs){

        Objects.requireNonNull(pairs);

        Set<StatePair> pairCopy =
                new LinkedHashSet<>(pairs);

        Set<ResidualState> stateCopy =
                new LinkedHashSet<>();

        for (StatePair pair : pairCopy) {
            stateCopy.add(pair.first());
            stateCopy.add(pair.second());
        }

        this.pairs =
                Collections.unmodifiableSet(pairCopy);

        this.states =
                Collections.unmodifiableSet(stateCopy);

        this.certificates = Set.of();
    }

    /**
     * private constructor for certificate-based results.
     */
    private DResResult(List<Certificate> certificates) {

        Objects.requireNonNull(certificates);

        Set<Certificate> certificateCopy =
                new LinkedHashSet<>(certificates);

        Set<StatePair> pairCopy =
                new LinkedHashSet<>();

        Set<ResidualState> stateCopy =
                new LinkedHashSet<>();

        for (Certificate certificate : certificateCopy) {

            pairCopy.add(certificate.pair());

            stateCopy.add(certificate.pair().first());
            stateCopy.add(certificate.pair().second());
        }

        this.certificates =
                Collections.unmodifiableSet(certificateCopy);

        this.pairs =
                Collections.unmodifiableSet(pairCopy);

        this.states =
                Collections.unmodifiableSet(stateCopy);
    }

    /**
     * creates a result containing DRes pairs and their certificates.
     */
    public static DResResult withCertificates(
            Set<Certificate> certificates
    ){
        Objects.requireNonNull(certificates);

        return new DResResult(
                List.copyOf(certificates)
        );
    }

    public Set<ResidualState> getStates() {
        return states;
    }

    public Set<StatePair> getPairs() {
        return pairs;
    }

    public Set<Certificate> getCertificates() {
        return certificates;
    }

    public int size() {
        return states.size();
    }

    public int pairCount() {
        return pairs.size();
    }

    public boolean isEmpty() {
        return pairs.isEmpty();
    }
}