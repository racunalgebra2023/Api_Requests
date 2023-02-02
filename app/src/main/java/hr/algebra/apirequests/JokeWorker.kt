package hr.algebra.apirequests

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import hr.algebra.apirequests.model.Joke
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset

const val INPUT_PARAM_URL = "joke_host"
const val OUTPUT_PARAM_JOKE = "joke_text"

class JokeWorker( ctx: Context, params: WorkerParameters )
                                : Worker( ctx, params ) {
    private val TAG = "JokeWorker"

    override fun doWork( ) : Result {
        try {
            val stringURL = inputData.getString( INPUT_PARAM_URL )
            val url = createURL( stringURL!! )
            val response = makeHTTPRequest(url)
            Log.i( TAG, "I have a response from server '${url?.host}':")
            Log.i( TAG, response )
            val joke = getJokeFromJSONString( response )
            Log.i( TAG, joke.toString( ) )
            return Result.success( createOutputData( joke.value ) )
        } catch ( e : Exception ) {
            return Result.failure( )
        }
    }

    private fun createOutputData( jokeText : String ) : Data {
        return Data
                .Builder( )
                .putString( OUTPUT_PARAM_JOKE, jokeText )
                .build( )
    }

    private fun getJokeFromJSONString( response : String ) : Joke {
        val json = JSONObject( response )
        val joke = Joke(
            json.getString( "created_at" ),
            json.getString( "icon_url" ),
            json.getString( "id" ),
            json.getString( "updated_at" ),
            json.getString( "url" ),
            json.getString( "value" )
        )
        return joke
    }

    private fun createURL( stringURL : String ) : URL? {
        return try {
            URL( stringURL )
        } catch ( e : MalformedURLException) {
            null
        }
    }

    private fun makeHTTPRequest( url: URL? ) : String {
        var httpURLConnection : HttpURLConnection? = null
        var inputStream       : InputStream?       = null

        try {
            httpURLConnection = url?.openConnection( ) as HttpURLConnection
            httpURLConnection.readTimeout = 2000
            httpURLConnection.connectTimeout = 2000
            httpURLConnection.requestMethod = "GET"
            httpURLConnection.connect( )
        } catch ( e : Exception ) {
            throw RuntimeException( e )
        }

        try {
            if( httpURLConnection.responseCode==200 ) {
                inputStream = httpURLConnection.inputStream
                return getStringFromInputStream( inputStream )
            } else {
                throw RuntimeException( "RESPONSE ERROR: response code: ${httpURLConnection.responseCode}" )
            }
        } catch ( e : Exception ) {
            throw RuntimeException( e )
        } finally {
            inputStream?.close( )
        }
    }

    private fun getStringFromInputStream( inputStream : InputStream) : String {
        val output = StringBuilder( )
        val reader = BufferedReader( InputStreamReader( inputStream, Charset.forName( "UTF-8" ) ) )
        var line = reader.readLine( )
        while ( line!=null ) {
            output.append( line )
            line = reader.readLine( )
        }
        return output.toString( )
    }
}