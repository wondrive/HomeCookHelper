package com.example.homecookhelper.agric

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agriculturalauctioningapp.network.NetworkModule
import com.example.homecookhelper.entity.AgricEntity
import com.example.homecookhelper.entity.AgricWrapper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.wait

class AgricViewModel : ViewModel() {

    val moshi by lazy {
        //data class를 JSON처럼 다룰 수 있도록 해주는 Moshi 플러그인
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }

    private val agricList: MutableLiveData<List<AgricEntity>> = MutableLiveData()
    //getter 선언
    fun agricList(): LiveData<List<AgricEntity>> = agricList

    //검색 결과를 SaveItem 테이블과 Fresh 테이블에 저장
    fun saveResult(context: Context, saveName: String) {
        /*viewModelScope.launch(Dispatchers.IO) {
            *//* SaveItem 테이블에 경락가격정보 저장
              - database Dao를 형태로 가져옵니다.
              - 먼저 저장 항목리스트를 만들어서 autoGenerated된 ID값을 받은다음,
              - freshdata(각각의 개별 데이터)를 리스트 ID값과 함께 저장해줍니다.
            *//*
            DatabaseModule.getDatabase(context).agricDao().(
                    SaveItem(id = null, saveTitle = saveName)//saveName: "2020-06-15 사과 검색결과"
                    ).run {
                //Fresh 테이블에 경락가격정보 저장
                resultList.value?.let { datas ->
                    datas.forEach { it.saveId = this }
                    Log.i("SAVERESULT", "datas: $datas")
                    *//* FreshData(id=null, saveId=1, lname=과일과채류, mname=수박..cprName=동화청과....)
                       FreshData(id=null, saveId=1, lname=과일과채류, mname=수박..cprName=서울청과....)
                       ...
                       ** Fresh table에
                         - id(자동발급), saveId(1), lname=과일과채류, mname=수박..cprName=동화청과...
                         - id(자동발급), saveId(1), lname=과일과채류, mname=수박..cprName=동화청과...
                         .... 저장 됨
                     *//*
                    DatabaseModule.getDatabase(context).freshDao().????(datas)
                }
            }
        }*/
    }//end of saveResult


    val errorHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("error", exception.message)
//        userNoticeMsg.postValue(exception.message)
    }

    //Request 객체 생성 함수
    fun loadDataFromURL() {
        /* Request 객체 생성 */
        val request = NetworkModule.makeHttprequest(
            /* 인자 없이 HttpUrl 객체 생성 함수를 호출 -> HttpUrl 객체 생성 */
            NetworkModule.makeHttpUrl()
        )
        Log.i("HTTP", request.toString())

        /* Coroutine을 이용하여 IO 스레드에서 API 서버에 요청  */
        viewModelScope.launch(Dispatchers.IO + errorHandler) {
            Log.i("AGRIC", request.url.toString())

            /* response(응답) 객체 - 제철 농산물 리스트 요청
               - OkHTTPClient.newCall()의 인자로 Request 객체(request)를 전달하여 실행(요청)*/
            val response = NetworkModule.client.newCall(request).wait() //.await() 왜 안되지....

            /* String을 Moshi를 이용 JSON Body로 파싱 */
            //val agricData = response.body?.string

            //resultList(LiveData) 저장
            //resultList.postValue(agricData)
        }
    }//end of loadDataFromURL

    //Moshi를 이용 JSON Body로 파싱
    fun mapppingStringToNews(jsonBody: String): List<AgricEntity> {
        // json 스트링을 데이터 클래스(FreshWrapper)에 맞게 자동으로 맵핑해주는 어댑터를 생성
        val newsStringToJsonAdapter = moshi.adapter(AgricWrapper::class.java)
        val newsResponse:AgricWrapper? = newsStringToJsonAdapter.fromJson(jsonBody)
        Log.i("LIST","${newsResponse}")  //list=[AgricEntity(id=null...)]

        return newsResponse?.list ?: emptyList()
    }//end of mapppingStringToNews
}

