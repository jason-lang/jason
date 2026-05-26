package jason.architecture.api.infrastructure.adapter.in.web.api;

import jason.architecture.api.application.log.port.in.AgentLogService;
import jason.architecture.api.application.mas.port.in.MasService;
import lombok.RequiredArgsConstructor;
import net.peelweb.context.controller.Controller;
import net.peelweb.context.controller.Mapping;
import net.peelweb.context.endpoint.Response;
import net.peelweb.context.endpoint.Responses;

@Controller("/api/mas")
@RequiredArgsConstructor
public class MasController {

    private final MasService masService;

    private final AgentLogService agentLogService;

    @Mapping("/logs")
    public Response getLogs() {
        return Responses.ok(this.agentLogService.findAllLogs());
    }

    @Mapping
    public Response getMas() {
        return Responses.ok(this.masService.getMas());
    }
}
