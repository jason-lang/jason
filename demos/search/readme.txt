Example of A* implemented in Jason

problem:
    starting by a number,
    find a sequence of add1 and add2
    that transforms the initial number into a "goal" number

for instance:
    initial state: 0
    goal state   : 5
    solution     : [ op(initial,0), op(add2,2), op(add2,4), op(add1,5) ]
