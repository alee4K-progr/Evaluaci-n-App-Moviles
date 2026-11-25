package cl.corpaliv.albalab.vinculacion;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import cl.corpaliv.albalab.vinculacion.model.EstadoSolicitud;

class EstadoSolicitudTest {
    @Test
    void flujoNormalYDescarte() {
        assertTrue(EstadoSolicitud.RECIBIDA.puedePasarA(EstadoSolicitud.EN_REVISION));
        assertTrue(EstadoSolicitud.EN_REVISION.puedePasarA(EstadoSolicitud.CONTACTADO));
        assertTrue(EstadoSolicitud.CONTACTADO.puedePasarA(EstadoSolicitud.CONCRETADO));
        for (EstadoSolicitud e : new EstadoSolicitud[]{EstadoSolicitud.RECIBIDA, EstadoSolicitud.EN_REVISION, EstadoSolicitud.CONTACTADO}) {
            assertTrue(e.puedePasarA(EstadoSolicitud.DESCARTADO));
        }
    }

    @Test
    void noSeSaltaPasosNiSeRetrocede() {
        assertFalse(EstadoSolicitud.RECIBIDA.puedePasarA(EstadoSolicitud.CONTACTADO));
        assertFalse(EstadoSolicitud.CONTACTADO.puedePasarA(EstadoSolicitud.RECIBIDA));
        assertFalse(EstadoSolicitud.RECIBIDA.puedePasarA(EstadoSolicitud.RECIBIDA));
    }

    @Test
    void estadosFinalesNoTienenSalida() {
        assertTrue(EstadoSolicitud.CONCRETADO.siguientes().isEmpty());
        assertTrue(EstadoSolicitud.DESCARTADO.siguientes().isEmpty());
    }
}
