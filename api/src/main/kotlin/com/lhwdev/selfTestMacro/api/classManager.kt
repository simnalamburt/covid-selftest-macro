@file:Suppress("SpellCheckingInspection")

package com.lhwdev.selfTestMacro.api

import com.lhwdev.selfTestMacro.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


/**
 * ```
 * POST /joinClassList HTTP/1.1
 * <common headers>
 * Authorization: UserToken(..)
 *
 * Response:
 * {
 *   "classList": [
 *     {
 *       "orgCode":"Xnnnnnnnnnn",
 *       "dghtCrseScCode":"n",
 *       "ordScCode":"nn",
 *       "dddepCode":"nnnn",
 *       "grade":"<grade>",
 *       "classCode":"<class number code>",
 *       "schulCrseScCode":"n",
 *       "ay":"<year>",
 *       "kraOrgNm":"<school name>",
 *       "dghtCrseScNm":"<classifier>",
 *       "classNm":"<class number>"
 *     },
 *     ...
 *   ]
 * }
 * ```
 */
@Serializable
public data class ClassList(val classList: List<ClassInfo>)

@Serializable
public data class ClassInfo(
	@SerialName("orgCode") val instituteCode: String,
	@Serializable(IntAsStringSerializer::class) val grade: Int,
	@SerialName("classNm") @Serializable(IntAsStringSerializer::class) val classNumber: Int,
	@SerialName("classCode") val classCode: String
)

public suspend fun getClassList(institute: InstituteInfo, manager: User): ClassList = fetch(
	institute.requestUrl["joinClassList"],
	method = HttpMethod.post,
	headers = sDefaultFakeHeader + mapOf("Authorization" to manager.token.token)
).toJsonLoose(ClassList.serializer())


/**
 * ```
 * POST /join HTTP/1.1
 * <common headers>
 * Authorization: UserToken(..)
 *
 * Request payload: ClassInfo(..)
 *
 * Response:
 * {
 *   "joinList":[
 *     {
 *       "orgCode":"Xnnnnnnnnnn",
 *       "inveYmd":"YYYYMMDD <last register date>",
 *       "grade":"<grade>",
 *       "classCode":"<class number code>",
 *       "serveyTime":"n <???>",
 *       "name":"<name>",
 *       "userPNo":"<user id>", // see getUserInfo request payload
 *       "surveyYn":"Y/N",
 *       "rspns00":"Y/N",
 *       "deviceUuidYn":"Y/N", // if installed the offical app
 *       "registerDtm":"YYYY-MM-DD HH:MM:SS.ssssss <last register time>",
 *       "stdntCnEncpt":"n",
 *       "upperUserName":"<also name>"
 *     },
 *     ...
 *   ]
 * }
 * ```
 */
@Serializable
public data class ClassSurveyStatus(val joinList: List<ClassSurveyStudentStatus>)

@Serializable
public data class ClassSurveyStudentStatus(
	val name: String,
	@Serializable(IntAsStringSerializer::class) val grade: Int,
	@SerialName("classCode") val classCode: String,
	@SerialName("userPNo") val userCode: String,
	@SerialName("surveyYn") @Serializable(YesNoSerializer::class) val registeredSurvey: Boolean,
	@SerialName("registerDtm") val lastRegisterAt: String? = null,
	@SerialName("deviceUuidYn") @Serializable(YesNoSerializer::class) val installedOfficalApp: Boolean
)

public suspend fun getClassSurveyStatus(
	institute: InstituteInfo, manager: User, classInfo: ClassInfo
): ClassSurveyStatus = fetch(
	institute.requestUrl["join"],
	method = HttpMethod.post,
	headers = sDefaultFakeHeader + mapOf(
		"Authorization" to manager.token.token
	),
	body = HttpBodies.json(ClassInfo.serializer(), classInfo)
).toJsonLoose(ClassSurveyStatus.serializer())


/**
 * {
 *   "joinInfo": {
 *     "grade":"<number>",
 *     "classCode":"<class code>",
 *     "name":"<name>",
 *     "surveyYn":"Y/N",
 *     "isHealthy":boolean,
 *     "atptOfcdcConctUrl":"???hcs.eduro.go.kr",
 *     "pInfAgrmYn":"Y/N",
 *     "mobnuEncpt":"<phone number>"
 *   }
 * }
 */
@Serializable
public data class ClassSurveyStudentStatusDetail(
	@Serializable(IntAsStringSerializer::class) val grade: Int,
	val classCode: String,
	val name: String,
	@SerialName("surveyYn") @Serializable(YesNoSerializer::class) val registeredSurvey: Boolean,
	val isHealthy: Boolean,
	@SerialName("mobnuEncpt") val phoneNumber: String
)

public suspend fun getStudentSurveyStatusDetail(
	institute: InstituteInfo, manager: User, student: ClassSurveyStudentStatus
): ClassSurveyStudentStatusDetail = fetch(
	institute.requestUrl["joinDetail"],
	method = HttpMethod.post,
	headers = sDefaultFakeHeader + mapOf(
		"Authorization" to manager.token.token
	),
	body = HttpBodies.json(ClassSurveyStudentStatus.serializer(), student)
).toJsonLoose(ClassSurveyStudentStatusDetail.serializer())
