package com.gamecrawl.simplektorclient

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gamecrawl.simplektorclient.ui.theme.SimpleKtorClientTheme
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: HomeViewModel by viewModels()

        Log.d("MainActivity", "onCreate: ${viewModel.users[0].username}")

        setContent {
            SimpleKtorClientTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting(viewModel)
                }
            }
        }
    }
}

class HomeViewModel : ViewModel() {

    private var _users = mutableListOf(User(0, "test", bio = "test bio"))
    val users get() = _users

    init {
        CoroutineScope(Dispatchers.IO).launch {

            _users = KtorClient.httpClient.get("http://10.0.2.2:3000/get_user") {
                header("Content-Type", "application/json")
            }
            Log.d("HomeViewModel", "init: ${_users[0].username}")

        }

    }

    fun addUser(user: User) {
        CoroutineScope(Dispatchers.IO).launch {

            val user: User = KtorClient.httpClient.post("http://10.0.2.2:3000/create_user") {
                header("Content-Type", "application/json")
            }
        }
    }


    fun addPost() {
        CoroutineScope(Dispatchers.IO).launch {

            val myPost = Post("new title one", "new content body one")

            val post: Post = KtorClient.httpClient.post("http://10.0.2.2:3000/addpost") {
                header("Content-Type", "application/json")
                body = myPost
            }
            Log.d("HomeViewModel", "addPost: ${post.title}")
        }

    }
}

@Serializable
data class Post(
    val title: String,
    val content: String
)

@Serializable
data class User(
    val id: Int,
    val username: String,
    val bio: String,
)


@Composable
fun Greeting(viewModel: HomeViewModel) {

    Column {
        Text(text = "Hello World!")
        Button(onClick = {
            viewModel.addPost()
        }) {
            Text(text = "Add User")
        }
        LazyColumn {
            items(viewModel.users.size) {
                Text(text = viewModel.users[it].username)
            }
        }
    }


}


object KtorClient {


    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = true
    }


    val httpClient = HttpClient(Android) {

        install(HttpTimeout) {
            socketTimeoutMillis = 30000
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 30000
        }

        install(Logging) {

            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("TAG", "log: $message")
                }
            }

        }

        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }

        defaultRequest {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }

    }

}
