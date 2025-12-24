package pt.estga.shared.models;

import lombok.Builder;

@Builder
public final class ServiceAccountPrincipal implements AuthenticatedPrincipal {

    private final Long id;
    private final String serviceName;

    @Override
    public Long getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }
}
