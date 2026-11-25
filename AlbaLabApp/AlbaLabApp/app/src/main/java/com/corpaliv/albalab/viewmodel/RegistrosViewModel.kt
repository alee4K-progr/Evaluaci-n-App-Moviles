package com.corpaliv.albalab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corpaliv.albalab.data.local.RegistroInteresEntity
import com.corpaliv.albalab.data.remote.Resultado
import com.corpaliv.albalab.data.repository.InteresRepository
import com.corpaliv.albalab.domain.ReglasGestion
import com.corpaliv.albalab.domain.model.EstadoPublico
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Consulta pública del estado de una solicitud mediante su código. */
data class ConsultaUiState(
    val codigo: String = "",
    val consultando: Boolean = false,
    val resultado: EstadoPublico? = null,
    val error: String? = null
)

class RegistrosViewModel(private val repositorio: InteresRepository) : ViewModel() {

    val registros: StateFlow<List<RegistroInteresEntity>> = repositorio.registros
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), emptyList())

    private val _actualizando = MutableStateFlow(false)
    val actualizando: StateFlow<Boolean> = _actualizando

    private val _consulta = MutableStateFlow(ConsultaUiState())
    val consulta: StateFlow<ConsultaUiState> = _consulta

    init {
        // Al abrir "Mis registros" se reintenta enviar los pendientes y se refrescan los estados.
        sincronizar()
    }

    fun sincronizar() {
        if (_actualizando.value) return
        viewModelScope.launch {
            _actualizando.value = true
            try {
                repositorio.sincronizarPendientes()
                repositorio.refrescarEstados()
            } finally {
                _actualizando.value = false
            }
        }
    }

    fun eliminar(id: Long) {
        viewModelScope.launch { repositorio.eliminar(id) }
    }

    fun cambiarCodigo(valor: String) {
        _consulta.update {
            it.copy(
                codigo = valor.uppercase().filter { c -> c.isLetterOrDigit() || c == '-' }.take(11),
                error = null,
                resultado = null
            )
        }
    }

    fun consultar() {
        val actual = _consulta.value
        if (actual.consultando) return
        val errorFormato = ReglasGestion.errorCodigo(actual.codigo)
        if (errorFormato != null) {
            _consulta.update { it.copy(error = errorFormato) }
            return
        }
        viewModelScope.launch {
            _consulta.update { it.copy(consultando = true, error = null, resultado = null) }
            when (val r = repositorio.consultarEstado(actual.codigo)) {
                is Resultado.Ok -> _consulta.update { it.copy(consultando = false, resultado = r.valor) }
                is Resultado.Fallo -> _consulta.update { it.copy(consultando = false, error = r.mensaje) }
            }
        }
    }
}
