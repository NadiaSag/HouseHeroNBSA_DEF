package com.nasch.househero.SealedClasses

sealed class Services(var name: String, var isSelected:Boolean = true) {
    object Fontaneria : Services("Fontanería")
    object Jardineria : Services("Jardinería")
    object Piscinas : Services("Piscinas")
    object Construccion : Services("Construcción")
    object Pintura : Services("Pintura")

    object Cristaleria : Services("Cristalería")

    object Climatizacion : Services("Climatización")

    object Cerrajeria : Services("Cerrajería")
    object PequeObras : Services("Pequeñas obras")
    object Limpieza : Services("Limpieza")
    object Electricidad : Services("Electricidad")

    object Carpinteria : Services("Carpintería")

    object Ascensores : Services("Ascensores")

}