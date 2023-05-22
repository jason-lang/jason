// Default plans to handle KQML performatives
// Users can override them in their own AS code
//
// Variables:
//   Sender:  the sender (an atom)
//   Content: content (typically a literal)
//   MsgId:   message id (an atom)
//

/* ---- tell performatives ---- */

@kqmlReceivedTellStructure[atomic]
+!kqml_received(Sender, tell, NS::Content, _)
   :  .literal(Content) &
      .ground(Content) &
      not .list(Content) &
      .add_nested_source(Content, Sender, CA)
   <- ++NS::CA.   // add with new focus (as external event)
@kqmlReceivedTellList
+!kqml_received(Sender, tell, Content, _)
   :  .list(Content)
   <- !add_all_kqml_received(Sender,Content).

@kqmlReceivedTellList1
+!add_all_kqml_received(_,[]).

@kqmlReceivedTellList2
+!add_all_kqml_received(Sender,[NS::H|T])
   :  .literal(H) &
      .ground(H)
   <- .add_nested_source(H, Sender, CA);
      ++NS::CA;
      !add_all_kqml_received(Sender,T).

@kqmlReceivedTellList3
+!add_all_kqml_received(Sender,[_|T])
   <- !add_all_kqml_received(Sender,T).

@kqmlReceivedUnTell[atomic]
+!kqml_received(Sender, untell, NS::Content, _)
    : .add_nested_source(Content, Sender, CA)
   <- --NS::CA.


/* ---- achieve performatives ---- */

@kqmlReceivedAchieve
+!kqml_received(Sender, achieve, NS::Content, _)
    : not .list(Content) & .add_nested_source(Content, Sender, CA)
   <- !!NS::CA.
@kqmlReceivedAchieveList
+!kqml_received(Sender, achieve, Content, _)
    : .list(Content)
   <- !add_all_kqml_achieve(Sender,Content).


@kqmlReceivedAchieveList1
+!add_all_kqml_achieve(_,[]).

@kqmlReceivedAchieveList2
+!add_all_kqml_achieve(Sender,[NS::H|T])
   <- .add_nested_source(H, Sender, CA);
      !!NS::CA;
      !add_all_kqml_achieve(Sender,T).


@kqmlReceivedUnAchieve[atomic]
+!kqml_received(_, unachieve, NS::Content, _)
   <- .drop_desire(NS::Content).


/* ---- ask performatives ---- */

@kqmlReceivedAskOne1d
+!kqml_received(Sender, askOne, NS::Content, MsgId)
    : kqml::bel_no_source_self(NS, Content, Ans)
   <- .send(Sender, tell, NS::Ans, MsgId).

//@kqmlReceivedAskOne1a // (self belief, do not send back the source)
//+!kqml_received(Sender, askOne, NS::Content, MsgId)
//    : .remove_source_annot(Content,C2) & NS::C2[source(self)]
//   <- .send(Sender, tell, NS::Content, MsgId).

//@kqmlReceivedAskOne1b // (belief from single other, sends back the source)
//+!kqml_received(Sender, askOne, NS::Content, MsgId)
//    : NS::Content[source(AGS)]
//   <- .send(Sender, tell, NS::Content[source(AGS)], MsgId).

@kqmlReceivedAskOne1c // (no belief, try to trigger a plan with +?)
+!kqml_received(Sender, askOne, NS::Content, MsgId)
   <- .add_nested_source(Content, Sender, CA);
      ?NS::CA;
      // remove source annot from CA
      .remove_source_annot(CA,CA2);
      .send(Sender, tell, NS::CA2, MsgId).

@kqmlReceivedAskOne2 // error in askOne by ?, send untell
-!kqml_received(Sender, askOne, NS::Content, MsgId)[error(test_goal_failed)]
   <- .send(Sender, untell, NS::Content, MsgId).

@kqmlReceivedAskAll2
+!kqml_received(Sender, askAll, NS::Content, MsgId)
   <- .findall(NS::Ans, kqml::bel_no_source_self(NS, Content, Ans), List);
      .send(Sender, tell, List, MsgId).

kqml::clear_source_self([],[])[hide_in_mind_inspector].
kqml::clear_source_self([source(self)|T],NT)[hide_in_mind_inspector]     :- kqml::clear_source_self(T,NT).
kqml::clear_source_self([A|T],           [A|NT])[hide_in_mind_inspector] :- A \== source(self) & kqml::clear_source_self(T,NT).

kqml::bel_no_source_self(NS, Content, Ans)[hide_in_mind_inspector] :-
   NS::Content[|LA] &
   kqml::clear_source_self(LA, NLA) &
   Content =.. [F,T,_] &
   NS::Ans =.. [NS,F,T,NLA].

/* ---- know-how performatives ---- */

// In tellHow, content must be a string representation
// of the plan (or a list of such strings)

@kqmlReceivedTellHow
+!kqml_received(Sender, tellHow, Content, _)
   <- .remove_source_annot(Content, ContentAn);
      .add_plan(ContentAn, Sender).

// In untellHow, content must be a plan's
// label (or a list of labels)
@kqmlReceivedUnTellHow
+!kqml_received(Sender, untellHow, Content, _)
   <- .remove_plan(Content, Sender).

// In askHow, content must be a string or plan term representing
// the triggering event
@kqmlReceivedAskHow
+!kqml_received(Sender, askHow, Content, MsgId)
   <- .relevant_plans(Content, ListOfPlans);
      .remove_source_annot(ListOfPlans, ListOfPlansAn); // remove "source(self)" 
      .send(Sender, tellHow, ListOfPlansAn, MsgId).


/* ---- signal performatives ---- */

@kqmlReceivedSignal
+!kqml_received(Sender, signal, NS::Content, _)
    : not .list(Content) & .add_nested_source(Content, Sender, CA)
   <- .signal( { +NS::CA }, type_signal ).

/* general communication error handler */

@kqmlError
-!kqml_received(_Sender, _Per, _Content, _MsgId)[error(EID), error_msg(EMsg)]
   <- .print("Communication error -- ",EID, ": ", EMsg).
