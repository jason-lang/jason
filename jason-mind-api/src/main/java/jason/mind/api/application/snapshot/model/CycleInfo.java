package jason.mind.api.application.snapshot.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class CycleInfo {

    private final int currentCycleNumber;

    private int newerCycleNumber;

    private int olderCycleNumber;

}
