package com.example.data.util

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.IOException

object ErrorMapper {
    fun mapException(throwable: Throwable): Exception {
        return when (throwable) {
            is SecurityException -> Exception("Access Denied: ${throwable.message ?: "Insufficient privileges."}")
            is FirebaseAuthInvalidCredentialsException -> Exception("Invalid email or password. Please check your credentials.")
            is FirebaseAuthInvalidUserException -> Exception("Account not found or has been disabled.")
            is FirebaseAuthUserCollisionException -> Exception("An account already exists with this email.")
            is FirebaseNetworkException, is IOException -> Exception("Network error. Working with local cached data. Please check your internet connection.")
            is FirebaseFirestoreException -> {
                when (throwable.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                        Exception("Security Policy: You do not have permission to perform this action.")
                    FirebaseFirestoreException.Code.UNAUTHENTICATED ->
                        Exception("Your session has expired. Please log in again.")
                    FirebaseFirestoreException.Code.UNAVAILABLE ->
                        Exception("Server is temporarily unavailable. Changes will sync when connection is restored.")
                    FirebaseFirestoreException.Code.NOT_FOUND ->
                        Exception("The requested record does not exist or was deleted.")
                    FirebaseFirestoreException.Code.ALREADY_EXISTS ->
                        Exception("A record with this identifier already exists.")
                    else -> Exception("Operation failed: ${throwable.localizedMessage ?: "Unknown server error."}")
                }
            }
            is IllegalArgumentException -> Exception(throwable.message ?: "Invalid input provided.")
            is IllegalStateException -> Exception(throwable.message ?: "Application state error.")
            else -> Exception(throwable.message ?: "An unexpected error occurred. Please try again.")
        }
    }
}
