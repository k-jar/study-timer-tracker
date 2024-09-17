package com.example.studytimertracker.data

import androidx.room.TypeConverter
import com.example.studytimertracker.model.SessionActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromSessionActivityList(value: List<SessionActivity>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toSessionActivityList(value: String?): List<SessionActivity>? {
        val listType = object : TypeToken<List<SessionActivity>>() {}.type
        return Gson().fromJson(value, listType)
    }
}