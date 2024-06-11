package com.example.recipeappfinalcase.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeappfinalcase.data.repository.RecipeRepository
import com.example.recipeappfinalcase.data.source.local.FavoriteRecipe
import com.example.recipeappfinalcase.data.source.network.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DetailVM @Inject constructor(
    private val repository: RecipeRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _uiState = MutableStateFlow(DetailState())
    val uiState : StateFlow<DetailState> = _uiState

    init {
        val recipeId = savedStateHandle.get<Int>("id") ?: 0
        println(recipeId)
        fetchRecipeDetails(recipeId)
    }


    private fun fetchRecipeDetails(recipeId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            repository.getRecipeDetails(recipeId).collect { networkState ->
                _uiState.update { state ->
                    when(networkState) {
                        is NetworkState.Success -> {
                             state.copy(isLoading = false, isError = false, recipe = networkState.data)
                        }
                        is NetworkState.Error -> {
                            state.copy(isLoading = false, isError = true)
                        }

                        NetworkState.Loading -> { state.copy(isLoading = true)}
                    }
                }

            }
        }
    }

    fun addRecipeToFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            uiState.value.recipe?.let { recipe ->
                val favoriteRecipe = FavoriteRecipe(
                    id = recipe.id,
                    title = recipe.title,
                    image = recipe.image,
                    summary = recipe.summary
                )
                repository.insertFavoriteRecipe(favoriteRecipe)
            }
        }
    }


}