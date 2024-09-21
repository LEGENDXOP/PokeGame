package com.legendx.pokehexa.setup.tools

sealed class ResultSignUp{
    data class Success(val data: String): ResultSignUp()
    data class Error(val error: String): ResultSignUp()
}