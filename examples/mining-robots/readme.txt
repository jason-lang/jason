The scenario of this small Jason application is as follows. A "builder" agent
is located at the centre of the grid (representing some spatial
environment). That agent is doing some construction work for which it needs 3
different types of resources. There are 3 "miner agents" in the simulation,
represented as black squares with A, B, and C written in them. When searching
for mines, the miners search the grid exhaustively, but in different
directions. Agent A goes left to right, top to bottom; agent B goes right to
left, bottom to top; and agent C goes top to bottom, left to right. To
represent the idea of "mining for resources", an agent only perceives if there
is resource in a cell of the grid once it is located in that particular
cell. It also perceives the type of resource found in the mine. If it is the
resource the agent is looking for, it grabs one unit of it and take it to the
builder agent, then goes back to the position where the mine was found. Each
mine has 5 units of a certain type of resource. Initially the resource type
needed is known to be type 1 (shown as orange circles on the grid) and miners
go after that type of resource. When a certain (random) number of units of a
type has been used by the builder, it asks all agents to go after resource
type 2 (shown as green circles), and then type 3 (shown as blue circles). When
the construction is finished, the builder informs this to all miners who then
make their way back to the builder's location.

The code in this Jason application was written by Rob Clarke and Andy Buck
as 2nd coursework for the Multi-Agent Systems Module run in 2004-2005 at
the University of Durham, U.K.
The AgentSpeak code and planetEnv.java were edited by Rafael Bordini.
