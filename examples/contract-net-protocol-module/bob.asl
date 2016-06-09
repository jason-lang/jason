
// starts two CNPs into different namespaces

{include("initiator.asl",hall)}
{include("initiator.asl",comm)}

!hall::startCNP(build(park)).
!comm::startCNP(build(bridge)).
