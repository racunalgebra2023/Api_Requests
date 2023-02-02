package hr.algebra.apirequests

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val API_URL = "https://api.chucknorris.io/jokes/random"

    override fun onCreate( savedInstanceState: Bundle? ) {
        super.onCreate( savedInstanceState )
        setContentView( R.layout.activity_main )

        findViewById< Button >( R.id.bJokeAsync ).setOnClickListener {
            MyTask( findViewById( R.id.tvJoke ) ).execute( API_URL )
        }

        findViewById< Button >( R.id.bJokeWorker ).setOnClickListener {
            val data  = Data
                            .Builder( )
                            .putString( INPUT_PARAM_URL, API_URL )
                            .build( )
            val request = OneTimeWorkRequestBuilder< JokeWorker >( )
                                .setInputData( data )
                                .build( )
            val workManager = WorkManager.getInstance( this )
            workManager.enqueue( request )
            workManager.getWorkInfoByIdLiveData( request.id )
                .observe(
                    this,
                    Observer {
                        when( it.state ) {
                            WorkInfo.State.SUCCEEDED -> {
                                val successData = it.outputData
                                findViewById< TextView >( R.id.tvJoke ).text =
                                        successData.getString( OUTPUT_PARAM_JOKE )
                            }
                            WorkInfo.State.FAILED -> {
                                Toast
                                    .makeText( this@MainActivity,
                                                "Problems while loading joke...",
                                                Toast.LENGTH_SHORT )
                                    .show( )
                            }
                            else -> {
                                Log.i( TAG, "Ignoring status: ${it.state}..." )
                            }
                        }
                    }
                )
        }
    }
}