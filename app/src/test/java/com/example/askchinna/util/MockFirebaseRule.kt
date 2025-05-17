/**
 * File: app/src/test/java/com/example/askchinna/util/MockFirebaseRule.kt
 * Copyright (c) 2025 askChinna App Development Team
 * Created: May 16, 2025
 * Version: 3.0
 *
 * Enhanced MockFirebaseRule for preventing Firebase initialization errors in unit tests.
 * This rule mocks Firebase classes to prevent actual initialization.
 */

package com.example.askchinna.util

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * JUnit rule to mock Firebase initialization in tests.
 * Apply this rule as the FIRST rule in any test class that
 * depends on Firebase components.
 *
 * Usage:
 * ```
 * @get:Rule
 * val mockFirebaseRule = MockFirebaseRule()
 * ```
 */
class MockFirebaseRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                mockFirebaseApp()
                mockFirestore()
                mockFirebaseAuth()
                mockFirebaseStorage()
                base.evaluate()
            }
        }
    }

    private fun mockFirebaseApp() {
        mockkStatic(FirebaseApp::class)
        val mockApp = mockk<FirebaseApp>(relaxed = true)

        every { FirebaseApp.getInstance() } returns mockApp
        every { FirebaseApp.getInstance(any<String>()) } returns mockApp
        every { FirebaseApp.initializeApp(any()) } returns mockApp
        every { FirebaseApp.initializeApp(any(), any<FirebaseOptions>(), any<String>()) } returns mockApp
        every { FirebaseApp.getApps(any()) } returns listOf(mockApp)
    }

    private fun mockFirestore() {
        mockkStatic(FirebaseFirestore::class)
        val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)

        every { FirebaseFirestore.getInstance() } returns mockFirestore
        every { FirebaseFirestore.getInstance(any<FirebaseApp>()) } returns mockFirestore
    }

    private fun mockFirebaseAuth() {
        mockkStatic(FirebaseAuth::class)
        val mockAuth = mockk<FirebaseAuth>(relaxed = true)
        val mockUser = mockk<FirebaseUser>(relaxed = true)

        every { FirebaseAuth.getInstance() } returns mockAuth
        every { mockAuth.currentUser } returns mockUser
    }

    private fun mockFirebaseStorage() {
        mockkStatic(FirebaseStorage::class)
        val mockStorage = mockk<FirebaseStorage>(relaxed = true)

        every { FirebaseStorage.getInstance() } returns mockStorage
    }
}