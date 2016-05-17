// Used by both agents

// Knowledge of the PD payoff matrix
payoffs(c,d,0,5). // temptation to deffect = 5 and ...
payoffs(d,c,5,0). // ... sucker's payoff = 0
payoffs(c,c,3,3). // reward for mutual cooperation = 3
payoffs(d,d,2,2). // punishment for mutual defect = 2

/* format: payoff(move_ag1,move_ag2,score_ag1,score_ag2) */

