 See description at http://www.cs.otago.ac.nz/staffpriv/ok/santa/

 This is a Jason solution to "The Santa Claus problem",
 as discussed by Simon Peyton Jones (with a Haskell solution using
 Software Transactional Memory) in "Beautiful code".
 He quotes J.A.Trono "A new exercise in concurrency", SIGCSE 26:8-10, 1994.

  Santa repeatedly sleeps until wakened by either all of his
  nine reindeer, back from their holidays, or by a group of three
  of his ten elves.  If awakened by the reindeer, he harnesses
  each of them to his sleight, delivers toys with them, and finally
  unharnesses them (allowing them to go off on holiday).  If
  awakened by a group of elves, he shows each of the group into
  his study, consults with them on toy R&D, and finally shows them
  each out (allowing them to go back to work).  Santa should give
  priority to the reindeer in the case that there is both a group
  of elves and a group of reindeer waiting.

  Inspired by an old example of Dijkstra's, Richard solve this problem by
  introducing two secretaries: Robin and Edna.  The reindeer ask Robin
  for appointments.  As soon as she has nine waiting reindeer she sends
  them as a group to Santa.  The elves as Edna for appointments.  As
  soon as she has three waiting elves she sends them as a group to Santa.
