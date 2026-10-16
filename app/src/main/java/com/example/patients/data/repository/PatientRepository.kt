package com.example.patients.data.repository

import com.example.patients.data.model.ApiError
import com.example.patients.data.model.Patient
import com.example.patients.data.model.PatientRequest
import com.example.patients.data.remote.PatientApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

class PatientRepository(
    private val api: PatientApi,
    private val json: Json
) {

    suspend fun getPatients(): Result<List<Patient>> =
        safeCall { api.getPatients() }

    // Ajouter un patient : POST
    suspend fun createPatient(
        request: PatientRequest
    ): Result<Patient> =
        safeCall { api.createPatient(request) }

    // Supprimer un patient : DELETE
    suspend fun deletePatient(id: String): Result<Unit> =
        safeCall {
            val response = api.deletePatient(id)

            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            Unit
        }

    private suspend fun <T> safeCall(
        call: suspend () -> T
    ): Result<T> =
        try {
            Result.success(call())
        } catch (e: HttpException) {
            Result.failure(Exception(messageFrom(e)))
        } catch (e: IOException) {
            Result.failure(
                Exception(
                    "Serveur injoignable. Vérifiez l'adresse et le réseau."
                )
            )
        } catch (e: SerializationException) {
            Result.failure(
                Exception("Réponse inattendue du serveur.")
            )
        }

    private fun messageFrom(e: HttpException): String {
        val body = e.response()?.errorBody()?.string()

        val apiError = body?.let {
            runCatching {
                json.decodeFromString(ApiError.serializer(), it)
            }.getOrNull()
        }

        return when {
            apiError == null -> "Erreur ${e.code()}"
            apiError.erreurs.isNotEmpty() ->
                apiError.erreurs.joinToString("\n")
            else -> apiError.message
        }
    }
}