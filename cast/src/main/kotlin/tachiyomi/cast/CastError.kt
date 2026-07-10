package tachiyomi.cast

class CastStartException(val code: Int) : Exception()

class CastNotConnectedException : Exception()
