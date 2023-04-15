package test.asunit;

import org.junit.Before;
import org.junit.Test;

import jason.asunit.TestAgent;

/** based on bug reported by Cranefield */
public class BugInference2 {

    TestAgent ag;

    // initialisation of the agent test
    @Before
    public void setupAg() {
        ag = new TestAgent();

        // defines the agent's AgentSpeak code
        ag.parseAScode(
            "has_annot(Term[|AS], A) :- .member(A,AS). " +
            "plan_body_roles(PlanBody, Roles) :- \n" +
            "    plan_body_roles(PlanBody, [], Roles).\n" +
            "plan_body_roles({}, Roles, Roles).\n" +
            "plan_body_roles({BodyTerm;BTs}, KnownRoles, Roles) :-\n" +
            "    body_term_roles(BodyTerm, KnownRoles, KnownRoles2) & " +
            "    plan_body_roles(BTs, KnownRoles2, Roles). "+
            "body_term_roles(BodyTerm, KnownRoles, KnownRoles) :-\n" +
            "    not BodyTerm = achieve(gg(_,_)).\n" +
            "body_term_roles(achieve(gg(_, SubGoalList)), KnownRoles, Roles) :-\n" +
            "    gg_subgoal_roles(SubGoalList, KnownRoles, Roles).\n" +
            "\n" +
            "gg_subgoal_roles([], Roles, Roles).\n" +
            "gg_subgoal_roles([SubGoal|SubGoals], KnownRoles, Roles) :-\n" +
            "    not has_annot(SubGoal, role(R)) &\n" +
            "    gg_subgoal_roles(SubGoals, KnownRoles, Roles).\n" +
            "gg_subgoal_roles([SubGoal|SubGoals], KnownRoles, Roles) :-\n" +
            "    has_annot(SubGoal, role(R)) &\n" +
            "    .union(KnownRoles, [R], KnownRoles2) &\n" +
            "    gg_subgoal_roles(SubGoals, KnownRoles2, Roles)." +
            "+!house_built[template] <-\n" +
            "    !gg(build_foundations, [build_foundations[role(foundation_digger)], noop[role(wall_builder)], noop[role(overseer)]]); " +
            "    !gg(build_walls, [noop[role(foundation_digger)], build_walls[role(wall_builder)], noop[role(overseer)]]).\n" +
            "+!test <-\n" +
            "    .relevant_plans({+!house_built[template]}, [RPlan|_]);\n" +
            "    RPlan = plan(_,_,_,PlanBody);\n" +
            "    if (plan_body_roles(PlanBody, Roles)) {\n" +
            "        jason.asunit.print(Roles);\n" +
            "    } else {\n" +
            "        .print(\"Query for plan_body_roles/2 failed\");\n" +
            "    }."
        );
    }

    @Test(timeout=2000)
    public void testGoal() {
        ag.addGoal("test");
        ag.assertPrint("[foundation_digger,overseer,wall_builder]", 70);
    }

}
