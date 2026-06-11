package com.iptvpro.app

import android.content.Context
import android.content.SharedPreferences

object FavoritesManager {

    private const val PREFS_NAME = "iptv_favorites"
    private const val KEY_FAVS   = "fav_ids"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getIds(ctx: Context): Set<Int> =
        prefs(ctx).getStringSet(KEY_FAVS, emptySet())
            ?.mapNotNull { it.toIntOrNull() }?.toHashSet() ?: hashSetOf()

    fun toggle(ctx: Context, id: Int): Boolean {
        val ids = getIds(ctx).toMutableSet()
        val added = ids.add(id)
        if (!added) ids.remove(id)
        prefs(ctx).edit().putStringSet(KEY_FAVS, ids.map { it.toString() }.toSet()).apply()
        return added
    }

    fun isFavorite(ctx: Context, id: Int) = getIds(ctx).contains(id)
}
