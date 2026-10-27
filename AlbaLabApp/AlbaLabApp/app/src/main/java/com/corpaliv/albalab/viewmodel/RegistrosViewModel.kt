package com.corpaliv.albalab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corpaliv.albalab.data.local.RegistroInteresEntity
import com.corpaliv.albalab.data.repository.InteresRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RegistrosViewModel(private val repositorio: InteresRepository) : ViewModel() {

    val registros: StateFlow<List<RegistroInteresEntity>> = repositorio.registros
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun eliminar(id: Long) {
        viewModelScope.launch { repositorio.eliminar(id) }
    }
}
