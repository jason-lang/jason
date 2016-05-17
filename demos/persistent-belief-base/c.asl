!start. // initial goal.

+!start : not a(_) 
  <- .print("First run."); 
     +a(1).
     
+!start : a(X) 
  <- -+a(X+1); // a is stored in a DB
     .print("It is not my first run, I already run ",X," times.");
     !show_book(X);
     .println; .print("All titles:");
     ?show_all_titles.
     

+!show_book(X) 
  :  Id = (X mod 3)+1 &
     book(Id, Title, PubId, Year, ISBN) & publisher(PubId, Publisher)
     // book and publisher are tables in a DB
  <- .print(Title, ". ", Publisher, ", ",Year, ". ",ISBN);
     .findall(Author, book_author(Id,Author), LA);
     .print("Authors: ");
     !show_authors(LA).
       
+!show_authors([]).
+!show_authors([A|T])
  :  author(A,Name)
  <- .print("          ",Name);
     !show_authors(T).
+!show_authors([_|T])
  <- !show_authors(T).

// Rules used to show all books  
show_all_titles :- book(_, Title, _, _Year, _ISBN) & .println("-- ", Title) & false.
show_all_titles :- true.

