package ec.sgi.backend.infrastructure.sri;

import ec.sri.einvoice.application.port.out.SriResponse;

public record SriResponseSnapshot(SriResponse response, String xmlFirmado) {}
