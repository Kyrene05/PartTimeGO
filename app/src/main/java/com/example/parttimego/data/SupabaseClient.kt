package com.example.parttimego.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val client= createSupabaseClient(
        supabaseUrl = "https://lplaxhdbelbgxxqveokn.supabase.co",
        supabaseKey = "sb_publishable_XJmvA6qC9aV9vhCVWDufLA_ICKQybb_"
    ){
        install(Auth)
        install(Postgrest)
    }
}