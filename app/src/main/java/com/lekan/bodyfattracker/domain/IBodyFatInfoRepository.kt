package com.lekan.bodyfattracker.domain

import com.lekan.bodyfattracker.model.BodyFatInfo

interface IBodyFatInfoRepository {
    fun saveBodyFatInfoList(bodyFatInfoList: List<BodyFatInfo>)
    fun getBodyFatInfoList(): List<BodyFatInfo>
    fun addBodyFatInfo(bodyFatInfo: BodyFatInfo)
    fun clearAllBodyFatInfo()
    fun getLastBodyFatInfo(): BodyFatInfo?
}
