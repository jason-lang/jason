package jason.architecture.api.infrastructure.adapter.in.web.view;

import net.peelweb.context.controller.Controller;
import net.peelweb.context.controller.Mapping;
import net.peelweb.context.endpoint.Response;
import net.peelweb.context.endpoint.Responses;

@Controller("/mind")
public class IndexController {

    @Mapping
    public Response index() {
        return Responses.page("index.html");
    }

    @Mapping("/agents/{name}")
    public Response agent() {
        return Responses.page("index.html");
    }

}
