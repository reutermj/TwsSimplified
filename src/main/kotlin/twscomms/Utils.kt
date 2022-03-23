package twscomms

internal inline fun throwUninitialized(str: String): Nothing {
    throw Exception("Attempted to access $str before initialized. The likely means you did not call TwsCommManager.waitForUpdate() before attempting to access this value")
}