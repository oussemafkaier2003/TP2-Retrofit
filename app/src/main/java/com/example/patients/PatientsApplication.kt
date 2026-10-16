package com.example.patients

import android.app.Application
import com.example.patients.di.AppContainer

class PatientsApplication : Application() {

    // Créé une seule fois et partagé par l'application.
    val container by lazy {
        AppContainer()
    }
}