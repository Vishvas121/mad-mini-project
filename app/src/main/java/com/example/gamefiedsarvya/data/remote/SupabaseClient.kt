package com.example.gamefiedsarvya.data.remote

import com.example.gamefiedsarvya.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

/**
 * Singleton Supabase client.
 * URL and anon key come from BuildConfig (injected from local.properties at build time).
 * Never hardcoded in source.
 */
object SupabaseClient {

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }

    /** Convenience accessor for Postgrest queries */
    val db get() = client.postgrest
}
