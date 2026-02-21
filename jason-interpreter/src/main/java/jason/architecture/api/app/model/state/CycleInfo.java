package jason.architecture.api.app.model.state;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class CycleInfo {

    private final int currentCycleNumber;

    private int newerCycleNumber;

    private int olderCycleNumber;

}
