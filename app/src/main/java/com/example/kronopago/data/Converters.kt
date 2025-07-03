package com.example.kronopago.data

import androidx.room.TypeConverter
import com.example.kronopago.model.TipoTransaccion
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTipoTransaccion(tipo: TipoTransaccion): String {
        return tipo.name
    }

    @TypeConverter
    fun toTipoTransaccion(tipo: String): TipoTransaccion {
        return TipoTransaccion.valueOf(tipo)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
} 