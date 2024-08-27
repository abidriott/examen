package com.abi.agendaws.Objects

import java.io.Serializable

data class Contacts(
    var _ID: Int = 0,
    var nombre: String = "",
    var telefono1: String = "",
    var telefono2: String = "",
    var direccion: String = "",
    var notas: String = "",
    var favorite: Int = 0,
    var idMovil: String = "",
) : Serializable