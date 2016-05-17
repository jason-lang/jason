!start. // initial goal.
+!start : true <- .print("Adding belief b(10)."); +b(10); !test.

+!test : b(X) <- .print("I believe in ",b(X)).
+!test : true <- .print("I believe nothing!").

