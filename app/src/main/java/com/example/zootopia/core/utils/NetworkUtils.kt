package com.example.zootopia.core.utils

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object NetworkUtils {
    val client = createSupabaseClient(
        supabaseUrl = "https://nmrotznuvmdlfcraogku.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5tcm90em51dm1kbGZjcmFvZ2t1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzM0MDEyMjksImV4cCI6MjA4ODk3NzIyOX0.WPKfUgqeBijHc5CvxXeQbfDq6_eSA6BKxN986yYv2EQ"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}
