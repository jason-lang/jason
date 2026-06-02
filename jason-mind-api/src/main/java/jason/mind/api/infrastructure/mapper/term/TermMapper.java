package jason.mind.api.infrastructure.mapper.term;

import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.mind.api.application.snapshot.model.SourceInfoWrapper;
import jason.mind.api.application.snapshot.model.term.*;

import java.util.List;

public interface TermMapper {
    String UNNAMED_TERM_NAME = "_";

    AtomWrapper extractAtom(Atom atom);

    NamedValueTermWrapper extractVar(VarTerm varTerm, Unifier unifier);

    PredWrapper extractPred(Pred pred, Unifier unifier);

    SourceInfoWrapper extractSourceInfo(Term term);

    StructureWrapper extractStructure(Structure structureTerm, Unifier unifier);

    TriggerWrapper extractTrigger(Trigger trigger, Unifier unifier);

    void extractAll(List<TermWrapper> currentTerms, Term currentTerm, Unifier unifier);
}
