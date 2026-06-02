package jason.mind.api.application.mas.service;

import jason.mind.api.application.mas.model.Mas;
import jason.mind.api.application.mas.port.in.MasService;
import jason.mind.api.application.shared.port.out.JasonRuntimeGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StandardMasService implements MasService {

    private final JasonRuntimeGateway runtimeGateway;

    @Override
    public Mas getMas() {
        return new Mas(this.runtimeGateway.getMasName());
    }
}
