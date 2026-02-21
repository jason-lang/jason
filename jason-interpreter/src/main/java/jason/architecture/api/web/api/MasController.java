package jason.architecture.api.web.api;

import jason.architecture.api.app.service.mas.MasService;
import net.peelweb.context.controller.Controller;
import net.peelweb.context.controller.Mapping;
import net.peelweb.context.endpoint.Response;
import net.peelweb.context.endpoint.Responses;

@Controller("/api/mas")
public class MasController {

    private final MasService masService;

    public MasController(MasService masService) {
        this.masService = masService;
    }

    @Mapping("/logs")
    public Response getLogs() {
        return Responses.ok(this.masService.getLogs());
    }

    @Mapping
    public Response getMas() {
        return Responses.ok(this.masService.getMas());
    }


}
