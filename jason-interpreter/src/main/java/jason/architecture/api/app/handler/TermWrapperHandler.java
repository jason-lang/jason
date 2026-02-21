package jason.architecture.api.app.handler;

import jason.architecture.api.app.model.state.SourceInfoWrapper;
import jason.architecture.api.app.model.state.term.*;
import jason.NoValueException;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;

import java.util.ArrayList;
import java.util.List;

public class TermWrapperHandler {

    public static final String UNNAMED_TERM_NAME = "_";

    public AtomWrapper extractAtom(Atom atom) {
        return new AtomWrapper(atom.getFunctor(), this.extractSourceInfo(atom));
    }

    public NamedValueTermWrapper extractVar(VarTerm varTerm, Unifier unifier) {
        SourceInfoWrapper src = this.extractSourceInfo(varTerm);

        List<TermWrapper> values = new ArrayList<>();
        if (unifier != null && !varTerm.isUnnamedVar()) {
            Term value = unifier.get(varTerm);
            if (value == null) {
                values = null;
            } else {
                values = new ArrayList<>();
                this.extractAll(values, value, unifier);
            }
        }

        String name;
        if (varTerm.isUnnamedVar()) {
            name = UNNAMED_TERM_NAME;
        } else {
            name = varTerm.getFunctor();
        }

        return new NamedValueTermWrapper(name,
                values == null || values.isEmpty() ? null : values.size() == 1 ? values.get(0) : values, TermType.var,
                src);
    }

    public PredWrapper extractPred(Pred pred, Unifier unifier) {
        StructureWrapper predStructureTerm = this.extractStructure(pred, unifier);

        List<TermWrapper> annotationTerms = new ArrayList<>();
        ListTerm annotations = pred.getAnnots();
        if (annotations != null) {
            for (Term annot : annotations) {
                this.extractAll(annotationTerms, annot, unifier);
            }
        }

        return new PredWrapper(predStructureTerm, annotationTerms, this.extractSourceInfo(pred));
    }

    public SourceInfoWrapper extractSourceInfo(Term term) {
        if (term.getSrcInfo() == null) {
            return null;
        }

        SourceInfo srcInfo = term.getSrcInfo();
        return new SourceInfoWrapper(srcInfo.getBeginSrcLine(), srcInfo.getBeginSrcLine());
    }

    public StructureWrapper extractStructure(Structure structureTerm, Unifier unifier) {
        List<TermWrapper> termParameters = new ArrayList<>();
        if (structureTerm.getTerms() != null) {
            for (Term term : structureTerm.getTerms()) {
                this.extractAll(termParameters, term, unifier);
            }
        }

        return new StructureWrapper(structureTerm.getFunctor(), structureTerm.getNS().toString(), termParameters,
                this.extractSourceInfo(structureTerm));
    }

    public TriggerWrapper extractTrigger(Trigger trigger, Unifier unifier) {
        PredWrapper predWrapper = this.extractPred((LiteralImpl) trigger.getLiteral(), unifier);
        return new TriggerWrapper(predWrapper, trigger.getOperator().toString(), trigger.getType().toString());
    }

    public void extractAll(List<TermWrapper> currentTerms, Term currentTerm, Unifier unifier) {
        if (currentTerm == null) {
            return;
        }

        if (currentTerm.isNumeric() && !currentTerm.isArithExpr()) {
            TermWrapper number;
            SourceInfoWrapper src = this.extractSourceInfo(currentTerm);
            try {
                number = new ValueTermWrapper(TermType.number, Long.parseLong(currentTerm.toString()), src);
            } catch (NumberFormatException e) {
                number = new ValueTermWrapper(TermType.number, Double.parseDouble(currentTerm.toString()), src);
            }
            currentTerms.add(number);
        } else if (currentTerm.isString()) {
            TermWrapper string = new ValueTermWrapper(TermType.string, ((StringTermImpl) currentTerm).getString(),
                    this.extractSourceInfo(currentTerm));
            currentTerms.add(string);
        } else if (currentTerm.isVar()) {
            NamedValueTermWrapper var = this.extractVar((VarTerm) currentTerm, unifier);
            if (var != null) {
                currentTerms.add(var);
            }
        } else if (currentTerm.isArithExpr()) {
            NamedValueTermWrapper arith;
            SourceInfoWrapper src = this.extractSourceInfo(currentTerm);

            try {
                double solve = ((NumberTerm) currentTerm).solve();
                arith = new NamedValueTermWrapper(currentTerm.toString(), solve, TermType.var, src);
            } catch (NoValueException e) {
                if (currentTerm instanceof ArithFunctionTerm) {
                    try {
                        StructureWrapper sw = this.extractStructure((Structure) currentTerm, unifier);
                        arith = new NamedValueTermWrapper(currentTerm.toString(), sw, TermType.var, src);
                    } catch (Exception ex) {
                        arith = new NamedValueTermWrapper(currentTerm.toString(), null, TermType.var, src);
                    }
                } else {
                    arith = new NamedValueTermWrapper(currentTerm.toString(), null, TermType.var, src);
                }
            }

            currentTerms.add(arith);
        } else if (currentTerm.isInternalAction()) {
            InternalActionLiteral internalActionLiteral = (InternalActionLiteral) currentTerm;

            List<TermWrapper> internalActionTerms = new ArrayList<>();
            for (Term term : internalActionLiteral.getTerms()) {
                this.extractAll(internalActionTerms, term, unifier);
            }

            currentTerms.addAll(internalActionTerms);
        } else if (currentTerm.isPred()) {
            currentTerms.add(this.extractPred((Pred) currentTerm, unifier));
        } else if (currentTerm.isStructure() && currentTerm instanceof ListTermImpl) {
            Term listNext = currentTerm;
            List<TermWrapper> valueWrappers = new ArrayList<>();
            while (listNext != null) {
                if (listNext instanceof ListTermImpl) {
                    this.extractAll(valueWrappers, ((ListTermImpl) listNext).getTerm(), unifier);
                    listNext = ((ListTermImpl) listNext).getNext();
                } else {
                    this.extractAll(valueWrappers, listNext, unifier);
                    break;
                }
            }
            currentTerms.add(new ListTermWrapper(valueWrappers, this.extractSourceInfo(currentTerm)));
        } else if (currentTerm.isStructure() && (currentTerm instanceof BinaryStructure logicalExpression)) {
            StructureWrapper structureWrapper = this.extractStructure(logicalExpression, unifier);
            structureWrapper.setType(TermType.expression);
            currentTerms.add(structureWrapper);
        } else if (currentTerm.isStructure()) {
            currentTerms.add(this.extractStructure((Structure) currentTerm, unifier));
        } else if (currentTerm.isAtom()) {
            currentTerms.add(this.extractAtom(((Atom) currentTerm)));
        }
    }

}
