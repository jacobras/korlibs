package korlibs.io.concurrent

import kotlinx.coroutines.*

actual fun Dispatchers.createFixedThreadDispatcher(name: String, threadCount: Int): CoroutineDispatcher {
    return createRedirectedDispatcher(name, Dispatchers.Main)
}
