package com.lhwdev.selfTestMacro.database

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import com.lhwdev.selfTestMacro.App
import java.util.WeakHashMap


class PreferenceState(pref: PreferenceHolder) {
	init {
		val p = pref.pref
		// version migration
		when(p.getInt("lastVersion", -1)) {
			in -1..999 -> p.edit { clear() }
			in 1000..1999 -> p.edit { clear() }
			App.versionCode -> Unit // latest
		}
		
		p.edit { putInt("lastVersion", App.versionCode) }
	}
	
	
	var isDebugEnabled by pref.preferenceBoolean("isDebugEnabled", false)
	var isVirtualServer by pref.preferenceBoolean("isVirtualServer", false)
	
	var isFirstTime by pref.preferenceBoolean("first", true)
	
	val db: DatabaseManager = DatabaseManager(pref)
	
	var headUser by pref.preferenceInt(key = "headUser", defaultValue = 0)
	
	var shownNotices: Set<String> by pref.preferenceStringSet("shownNotices", emptySet())
	var doNotShowAgainNotices: Set<String>
		by pref.preferenceStringSet("doNotShowAgainNotices", emptySet())
}


private val preferenceStateMap = WeakHashMap<Context, PreferenceState>()

val Context.preferenceState: PreferenceState
	get() = preferenceStateMap.getOrPut(applicationContext) {
		PreferenceState(PreferenceHolder(prefMain()))
	}


fun Context.prefMain(): SharedPreferences =
	getSharedPreferences("main", AppCompatActivity.MODE_PRIVATE)

