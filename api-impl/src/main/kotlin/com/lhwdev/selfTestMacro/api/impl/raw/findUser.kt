package com.lhwdev.selfTestMacro.api.impl.raw

import com.lhwdev.fetch.*
import com.lhwdev.fetch.http.HttpMethod
import com.lhwdev.io.jsonObjectString
import com.lhwdev.selfTestMacro.api.InstituteData
import com.lhwdev.selfTestMacro.api.InternalHcsApi
import com.lhwdev.selfTestMacro.transkey.Transkey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URL
import kotlin.random.Random


@InternalHcsApi
public val transkeyUrl: URL = URL("https://hcs.eduro.go.kr/transkeyServlet")


@InternalHcsApi
public sealed class PasswordResult {
	public abstract val isSuccess: Boolean
	
	@Serializable
	public data class Success(
		val token: UsersToken,
		
		@SerialName("pInfAgrmYn")
		@Serializable(YesNoSerializer::class) val agreement: Boolean
	) : PasswordResult() {
		override val isSuccess: Boolean get() = true
	}
	
	@Serializable
	public data class Failed(
		val isError: Boolean,
		val statusCode: Int,
		val errorCode: Int,
		val data: Data
	) : PasswordResult() {
		@Serializable
		public data class Data(
			@SerialName("failCnt") val failedCount: Int
		)
		
		
		val errorMessage: String?
			get() = when(statusCode) {
				252 -> when(errorCode) {
					1000 -> "비밀번호를 5회 틀려서 5분후 재시도 하실 수 있어요."
					1001 -> """
						사용자 비밀번호가 맞지 않아요.
						본인이나 가족이 이미 설정한 비밀번호를 입력해 주세요.
						5회 틀리실 경우 5분후에 재시도할 수 있어요.
						현재 ${data.failedCount}회 틀리셨습니다.
						""".trimIndent()
					1003 -> "비밀번호가 초기화되었으니 다시 로그인해주세요."
					else -> null
				}
				255 -> when(errorCode) {
					1004 -> "입력시간이 초과되어 다시 비밀번호를 입력해주세요."
					else -> null
				}
				else -> null
			}
		
		override val isSuccess: Boolean get() = false
		
		override fun toString(): String = errorMessage ?: "알 수 없는 오류: 에러코드 $errorCode (틀린 횟수: ${data.failedCount})"
	}
	
}


@InternalHcsApi
public suspend fun HcsSession.findUser(
	password: String,
	instituteCode: String,
	name: String,
	birthday: String,
	loginType: LoginType,
	searchKey: InstituteData.InternalSearchKey,
	deviceUuid: String = "",
	pageNumber: Int? = null
): PasswordResult = withContext(Dispatchers.Default) main@{
	val transkey = Transkey(this@findUser, transkeyUrl, Random)
	
	val keyPad = transkey.newKeypad(
		keyType = "number",
		name = "password",
		inputName = "password",
		fieldType = "password"
	)
	
	val encrypted = keyPad.encryptPassword(password)
	
	val hm = transkey.hmacDigest(encrypted.toByteArray())
	
	val raonPassword = jsonObjectString {
		"raon" jsonArray {
			addObject {
				"id" set "password"
				"enc" set encrypted
				"hmac" set hm
				"keyboardType" set "number"
				"keyIndex" set keyPad.keyIndex
				"fieldType" set "password"
				"seedKey" set transkey.crypto.encryptedKey
				"initTime" set transkey.initTime
				"ExE2E" set "false"
			}
		}
	}
	
	val result = fetch(
		requestUrl["/v3/findUser"],
		method = HttpMethod.post,
		headers = sDefaultFakeHeader + mapOf(
			"Accept" to "application/json, text/plain, */*"
		),
		body = Bodies.jsonObject {
			"password" set raonPassword
			"deviceUuid" set deviceUuid
			"makeSession" set true
			
			"orgCode" set instituteCode
			"name" set encrypt(name)
			"birthday" set encrypt(birthday)
			"stdntPNo" setNullable pageNumber
			"searchKey" set searchKey.token
			"loginType" set loginType.name
		}
	).getText()
	
	try {
		JsonLoose.decodeFromString(PasswordResult.Success.serializer(), result)
	} catch(e: Throwable) {
		JsonLoose.decodeFromString(PasswordResult.Failed.serializer(), result)
	}
}