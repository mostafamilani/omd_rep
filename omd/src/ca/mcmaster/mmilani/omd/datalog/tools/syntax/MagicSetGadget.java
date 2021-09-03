package ca.mcmaster.mmilani.omd.datalog.tools.syntax;

import ca.mcmaster.mmilani.omd.datalog.engine.Program;
import ca.mcmaster.mmilani.omd.datalog.parsing.Parser;
import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MagicSetGadget {
    public static void rewrite(Program program, CQ cq) {
        Set<TGD> adornedRules = new HashSet<>();
        Set<Predicate> adornedPredicates = new HashSet<>();
        Set<Predicate> newPredicates = adornedQueryPredicates(cq, program);
        while (!newPredicates.isEmpty()) {
            Predicate adornedPredicate = newPredicates.iterator().next();
            adornedPredicates.add(adornedPredicate);
            for (TGD tgd : program.tgds) {
                TGD adornedRule = generateAdornedTGD(tgd, adornedPredicate, program);
                if (adornedRule != null && !RuleGadget.contains(adornedRules, adornedRule)) {
                    adornedRules.add(adornedRule);
                    for (Predicate newAdornedPredicate : getBodyAdornedPredicates(adornedRule)) {
                        if (!adornedPredicates.contains(newAdornedPredicate))
                            newPredicates.add(newAdornedPredicate);
                    }
                }
            }
            newPredicates.remove(adornedPredicate);
        }
        Set<Predicate> magicPredicates = addMagicPredicates(adornedRules, program);
        Set<TGD> magicRules = generateMagicRules(adornedRules, magicPredicates);
        program.tgds.addAll(adornedRules);
        program.tgds.addAll(generateLoadingRules(adornedPredicates, program));
    }

    private static Set<TGD> generateMagicRules(Set<TGD> adornedRules, Set<Predicate> magicPredicates) {
        HashSet<TGD> magicRules = new HashSet<>();
        for (TGD adornedRule : adornedRules) {
            List<PositiveAtom> atoms = adornedRule.body.getAtoms();
            PositiveAtom magicAtom = atoms.get(0);
            for (int i = 1; i < atoms.size(); i++) {
                TGD magicRule = getMagicRule(atoms.get(i), magicAtom);
                magicRules.add(magicRule);
                magicAtom = magicRule.head.getAtoms().get(0);
            }
        }
        return magicRules;
    }

    private static TGD getMagicRule(PositiveAtom atom, PositiveAtom magicAtom) {
        return null;
    }

    private static Set<Predicate> addMagicPredicates(Set<TGD> adornedRules, Program program) {
        HashSet<Predicate> magicPredicates = new HashSet<>();
        for (TGD adornedRule : adornedRules) {
            Predicate magicPredicate = program.schema.fetchMagicPredicate(adornedRule.head.getAtoms().get(0).predicate);
            List<Term> variables = getFreeVariables(adornedRule.head.getAtoms().get(0));
            PositiveAtom magicAtom = new PositiveAtom(magicPredicate, variables);
            adornedRule.body.addFirst(magicAtom);
            magicPredicates.add(magicPredicate);
        }
        return magicPredicates;
    }

    private static List<Term> getFreeVariables(Atom adornedAtom) {
        ArrayList<Term> terms = new ArrayList<>();
        int i = 0;
        for (Term term : adornedAtom.terms) {
            Variable v = (Variable) term;
            String adornment = adornedAtom.predicate.getAdornment();
            if (adornment.charAt(i) == 'f')
                terms.add(v);
        }
        return terms;
    }

    private static Set<TGD> generateLoadingRules(Set<Predicate> adornedPredicates, Program program) {
        Set<TGD> loadingRules = new HashSet<>();
        for (Predicate adorned : adornedPredicates) {
            loadingRules.add(generateLoadingRule(adorned, program));
        }
        return loadingRules;
    }

    private static TGD generateAdornedTGD(TGD tgd, Predicate adornedPredicate, Program program) {
        if (tgd.head.getAtoms().get(0).predicate != program.schema.fetchSimplePredicate(adornedPredicate))
            return null;
        TGD result = new TGD();
        Set<Variable> boundedVariables = new HashSet<>();
        Set<Variable> visited = new HashSet<>();
        Map<Variable, Variable> variableMappings = new HashMap<>();
        StringBuilder adornment = new StringBuilder(adornedPredicate.getAdornment());
        List<Term> terms = new ArrayList<>();
        for (int i = 0; i < adornment.length(); i++) {
            char c = adornment.charAt(i);
            Variable variable = (Variable) tgd.head.getAtoms().get(0).terms.get(i);
            Variable nv = tgd.fetchNewVariable();
            if (c == 'b') {
                if (variable.isExistential()) {
                    return null;
                }
                boundedVariables.add(nv);
            }
            variableMappings.put(variable, nv);
            terms.add(nv);
        }
        result.head = new Conjunct();
        result.head.add(new PositiveAtom(adornedPredicate, terms));
        result.body = new Conjunct();
        for (PositiveAtom atom : tgd.body.getAtoms()) {
            terms = new ArrayList<>();
            adornment = new StringBuilder();
            for (Term term : atom.terms) {
                Variable v = (Variable) term;
                Variable nv;
                if (!variableMappings.containsKey(v)) {
                    nv = result.fetchNewVariable();
                    variableMappings.put(v, nv);
                }
                nv = variableMappings.get(v);
                if (visited.contains(nv) || boundedVariables.contains(nv)) {
                    boundedVariables.add(nv);
                    adornment.append('b');
                } else {
                    adornment.append('f');
                }
                visited.add(nv);
                terms.add(nv);
            }
            result.body.add(new PositiveAtom(program.schema.fetchAdornedPredicate(atom.predicate, adornment.toString()), terms));
        }
        return result;
    }

    private static TGD generateLoadingRule(Predicate adorned, Program program) {
        TGD tgd = new TGD();
        tgd.head = new Conjunct();
        tgd.head.add(new PositiveAtom(adorned, fillVariables(tgd, adorned)));
        tgd.body = new Conjunct();
        ArrayList<Term> ts = new ArrayList<>();
        ts.addAll(tgd.head.getAtoms().get(0).terms);
        tgd.body.add(new PositiveAtom(program.schema.fetchSimplePredicate(adorned), ts));
        return tgd;
    }

    private static Set<Predicate> getBodyAdornedPredicates(TGD adornedRule) {
        HashSet<Predicate> result = new HashSet<>();
        for (PositiveAtom atom : adornedRule.body.getAtoms()) {
            result.add(atom.predicate);
        }
        return result;
    }

    private static List<Term> fillVariables(TGD tgd, Predicate predicate) {
        ArrayList<Term> terms = new ArrayList<>();
        for (int i = 0; i < predicate.arity; i++) {
            terms.add(tgd.fetchNewVariable());
        }
        return terms;
    }

    private static Set<Predicate> adornedQueryPredicates(CQ cq, Program program) {
        HashSet<Predicate> predicates = new HashSet<>();
        for (PositiveAtom atom : cq.body.getAtoms()) {
            predicates.add(adornedPredicate(atom, program));
        }
        return predicates;
    }

    private static Predicate adornedPredicate(PositiveAtom atom, Program program) {
        StringBuilder adornment = new StringBuilder();
        for (Term term : atom.terms) {
            if (term instanceof Variable) {
                adornment.append("f");
            } else if (term instanceof Constant) {
                adornment.append("b");
            }
        }
        return program.schema.fetchAdornedPredicate(atom.predicate, adornment.toString());
    }

    public static void main(String[] args) throws IOException {
        File in = new File("C:\\Users\\Mostafa\\Desktop\\omd_prj\\omd_rep\\omd\\rewrite.txt");
        Program program = Parser.parseProgram(in);
//        System.out.println("isLinear(program.rules) = " + SyntacticAnalyzer.isLinear(program.tgds));
//        System.out.println("isSticky(program.rules) = " + SyntacticAnalyzer.isSticky(program.tgds));
//        System.out.println("isWeaklyAcyclic(program.rules) = " + SyntacticAnalyzer.isWeaklyAcyclic(program.tgds));
//        System.out.println("isWeaklySticky(program.rules) = " + SyntacticAnalyzer.isWeaklySticky(program.tgds));
        List<CQ> cqs = Parser.parseQueries(in, program);
        CQ cq = cqs.get(0);
        System.out.println("program before rewriting = " + program);
        MagicSetGadget.rewrite(program, cq);
        System.out.println("after rewriting = " + program);
//        program.chase();
//        Set<Assignment> evaluate = program.evaluate(cq);
//        for (Assignment assignment : evaluate) {
//            System.out.println("assignment = " + assignment);
//        }
    }
}
