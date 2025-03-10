package com.example.mifinanza

data class Partida(val id: Int, val nombre: String) {
    override fun toString(): String {
        return nombre
    }
}