/*
 * XOR rule
*/
xor(a, b) :- (a & ~b) | (b & ~a).