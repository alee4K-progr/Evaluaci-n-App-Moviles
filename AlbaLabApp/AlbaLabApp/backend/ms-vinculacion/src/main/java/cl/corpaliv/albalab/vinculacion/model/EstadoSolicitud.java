package cl.corpaliv.albalab.vinculacion.model;

import java.util.EnumSet;
import java.util.Set;

/** Estados de la propuesta tecnica: RECIBIDA, EN_REVISION, CONTACTADO, CONCRETADO, DESCARTADO. */
public enum EstadoSolicitud {
    RECIBIDA, EN_REVISION, CONTACTADO, CONCRETADO, DESCARTADO;

    /** Transiciones permitidas desde este estado. CONCRETADO y DESCARTADO son finales. */
    public Set<EstadoSolicitud> siguientes() {
        return switch (this) {
            case RECIBIDA -> EnumSet.of(EN_REVISION, DESCARTADO);
            case EN_REVISION -> EnumSet.of(CONTACTADO, DESCARTADO);
            case CONTACTADO -> EnumSet.of(CONCRETADO, DESCARTADO);
            case CONCRETADO, DESCARTADO -> EnumSet.noneOf(EstadoSolicitud.class);
        };
    }

    public boolean puedePasarA(EstadoSolicitud nuevo) {
        return siguientes().contains(nuevo);
    }
}
