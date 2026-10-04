package ltlf.application;

import ltlf.alphabet.Alphabet;
import ltlf.alphabet.AtomicPropositionSet;
import ltlf.analysis.AtomicPropositionCollector;
import ltlf.ast.Formula;
import ltlf.automaton.ResidualAutomaton;
import ltlf.automaton.ResidualAutomatonBuilder;
import ltlf.external.spot.SpotEquivalenceChecker;
import ltlf.external.spot.SpotFormulaParser;
import ltlf.parser.FormulaParser;
import ltlf.semantics.FormulaEquivalenceChecker;
import ltlf.semantics.DefaultResidualSemantics;
import ltlf.semantics.ResidualSemantics;
import ltlf.transform.*;


import java.util.List;
import java.util.Objects;


public final class Ltlf2RaService {

    private final FormulaParser parser;
    private final AtomicPropositionCollector propositionCollector;
    private final ResidualSemantics residualSemantics;
    private final Normalizer normalizer;
    private final FormulaEquivalenceChecker equivalenceChecker;

    public Ltlf2RaService(){
        this(
                new SpotFormulaParser(),
                new AtomicPropositionCollector(),
                new DefaultResidualSemantics(),
                defaultNormalizer(),
                new SpotEquivalenceChecker()
        );
    }

    public Ltlf2RaService(
            FormulaParser parser,
            AtomicPropositionCollector propositionCollector,
            ResidualSemantics residualSemantics,
            Normalizer normalizer,
            FormulaEquivalenceChecker equivalenceChecker){

        this.parser = Objects.requireNonNull(parser);
        this.propositionCollector = Objects.requireNonNull(propositionCollector);
        this.residualSemantics = Objects.requireNonNull(residualSemantics);
        this.normalizer = Objects.requireNonNull(normalizer);
        this.equivalenceChecker = Objects.requireNonNull(equivalenceChecker);
    }

    private static Normalizer defaultNormalizer(){

        List<FormulaTransformer> transformers = List.of(
                new Simplifier(),
                new BooleanCanonicalizer()
        );

        return new Normalizer(transformers);
    }

    public ResidualAutomaton build(String formulaText){

        Objects.requireNonNull(formulaText, "Formula must not be null");

        String input = formulaText.trim();

        if(input.isEmpty()){
            throw new IllegalArgumentException("The Formula must not be blank");
        }

        Formula formula = parser.parse(input);

        AtomicPropositionSet propositionSet =
                new AtomicPropositionSet(
                    propositionCollector.collect(formula)
                );

        Alphabet alphabet = new Alphabet(propositionSet);

        ResidualAutomatonBuilder builder =
                new ResidualAutomatonBuilder(
                        residualSemantics,
                        normalizer,
                        equivalenceChecker,
                        alphabet
                );

        return builder.build(formula);
    }

}
