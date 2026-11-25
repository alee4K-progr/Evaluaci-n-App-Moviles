package com.corpaliv.albalab.data.repository

import com.corpaliv.albalab.data.remote.IndicadoresApi
import com.corpaliv.albalab.domain.model.TipoCambio
import kotlinx.coroutines.CancellationException

/** Consulta el dólar observado en la API externa (mindicador.cl). Devuelve null si no se pudo obtener. */
class IndicadoresEconomicosRepository(private val api: IndicadoresApi) {

    suspend fun dolarObservado(): TipoCambio? = try {
        val dolar = api.indicadoresDelDia().dolar
        val valor = dolar?.valor
        if (valor != null && valor > 0.0) TipoCambio(valor, dolar?.fecha) else null
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
}
