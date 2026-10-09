package com.ramya.learnhub.data.local

import androidx.room.RoomDatabase
import androidx.room.withTransaction

/** Abstraction over a DB transaction so repository logic can be unit-tested without Room. */
interface TransactionRunner {
    suspend fun <T> run(block: suspend () -> T): T
}

class RoomTransactionRunner(private val db: RoomDatabase) : TransactionRunner {
    override suspend fun <T> run(block: suspend () -> T): T = db.withTransaction { block() }
}
