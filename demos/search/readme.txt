Example of A* implemented in Jason

Agent called "numbers" solves the following problem:
    starting by a number,
    find a sequence of add1 and add2
    that transforms the initial number into a "goal" number

for instance:
    initial state: 0
    goal state   : 5
    solution     : [ op(initial,0), op(add2,2), op(add2,4), op(add1,5) ]

see numbers.asl for more details.

This demo also has implementations for the following problems:
   - Water Jugs (file waterjug.asl)
   - Missionaries & Cannibals (file missionaries.asl)
