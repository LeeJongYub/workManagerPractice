package com.example.workmanagerpractice


import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.work.*
import java.lang.Thread.sleep

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. 일반적인 스레드 작업 (워크매니저와 비교 위함)
        simpleThread().start()

        // 2. 워크매니저를 통한 스레드 작업
        val useWorkManagerA = OneTimeWorkRequestBuilder<workManagerA>().build()
        WorkManager.getInstance(this).enqueue(useWorkManagerA)
        // OneTimeWorkRequestBuilder : 한번 실행되는 작업 만들시 사용
        // WorkManager 클래스의 getInstance() 메서드로 싱글톤 객체를 받음 -> .enqueue 로 작업할 큐 선택하여 실행


        // 3. 워크매니저를 통해 데이터를 주고받는 작업

        // 타입(Data), workDateOf()로 전달할 데이터 설정
        val inputData1: Data = workDataOf(
            "input_data1" to 1,
            "input_data2" to 2,
            "input_data3" to 3
        )

        // setInputData() 를 통해 전달할 데이터 세팅
        val useWorkManagerB =
            OneTimeWorkRequestBuilder<workManagerB>().setInputData(inputData1).build()
        WorkManager.getInstance(this).enqueue(useWorkManagerB)

        // 반환받은 outputData 를 사용(?)하는 방법

        // getWorkInfoByIdLiveData() 에 워크매니저 실행에 사용중인 변수 useWorkManagerB 의 id,
        // 이를 관찰하기 위한 observe 메서드를 통해 outputData 를 가져올 수 있다.
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(useWorkManagerB.id)
            .observe(this, Observer { workInfo ->

                // workInfo 가 널값이 아니고, 상태가 완료되었을 때를 조건으로 두었다.
                if (workInfo != null && workInfo.state.isFinished) {

                    val getOutputData1 = workInfo.outputData.getInt("output_Data1", 0)
                    val getOutputData2 = workInfo.outputData.getInt("output_Data2", 0)
                    val getOutputData3 = workInfo.outputData.getInt("output_Data3", 0)
                    // 이때, outputData4 는 없으므로 defaultValue 가 출력된다.
                    val getOutputData4 = workInfo.outputData.getInt("output_Data4", 0)

                    Log.d("getOutputData1", getOutputData1.toString()) // 10
                    Log.d("getOutputData2", getOutputData2.toString()) // 20
                    Log.d("getOutputData3", getOutputData3.toString()) // 30
                    Log.d("getOutputData4", getOutputData4.toString()) // 0

                }
            })

    }

}

class simpleThread : Thread() {

    override fun run() {
        super.run()

        for (i in 1..10) {
            Log.d("noWorkManager", "$i")
            sleep(1000)
        }
        Log.d("noWorkManager", "end work without workManager")
    }

}

class workManagerA(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    // doWork() 메서드는 WorkManager 에서 제공하는 백그라운드 스레드에서 비동기적으로 실행
    override fun doWork(): Result {

        for (j in 1..10) {
            Log.d("useWorkManger", "$j")
            sleep(1000)
        }
        Log.d("useWorkManager", "end work with workManager")

        return Result.success()
        // Result 로 리턴할 수 있는 객체 3가지

        // 1. Result.success() : 작업이 성공적으로 완료되었음을 알림
        // 2. Result.failure() : 작업이 실패하였고, 다시 시작하지 않아도 됨을 알림
        // 3. Result.retry() : 작업이 실패하였고, 다시 시작해야 함을 알림

    }

}

class workManagerB(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    override fun doWork(): Result {

        // inputData.getInt()를 통해 inputData 를 전달받은 상태
        val getInputData1 = inputData.getInt("input_data1", 0)
        val getInputData2 = inputData.getInt("input_data2", 0)
        val getInputData3 = inputData.getInt("input_data3", 0)
        // 이때, inputData4 는 없으므로 defaultValue 가 출력된다.
        val getInputData4 = inputData.getInt("input_data4", 0)

        Log.d("getData1", getInputData1.toString()) // 1
        Log.d("getData2", getInputData2.toString()) // 2
        Log.d("getData3", getInputData3.toString()) // 3
        Log.d("getData4", getInputData4.toString()) // 0

        // outPutData 를 전달하는 과정
        val outputData1: Data = workDataOf(
            "output_Data1" to 10,
            "output_Data2" to 20,
            "output_Data3" to 30
        )

        return Result.success(outputData1)
    }

}