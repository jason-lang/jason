/**
 * A mock agent with mock helpers to be used by tester_agents
 * The name of the agent that creates the mock is stored in
 * the belief mock_owner(TesterAgent)
 */

/**
 * using default jag_sleeping and jag_awaking to inform
 * to the mock owner that the agent is sleeping or awake
 */
 +jag_sleeping :
    .my_name(ME) &
    mock_owner(TAg)
    <-
    .send(TAg, tell, sleeping(ME));
.
 +jag_awaking :
    .my_name(ME) &
    mock_owner(TAg)
    <-
    .send(TAg, untell, sleeping(ME));
.
