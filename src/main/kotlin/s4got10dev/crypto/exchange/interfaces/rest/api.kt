package s4got10dev.crypto.exchange.interfaces.rest

const val API_V1 = "/api/v1"
const val API_V1_LOGIN = "$API_V1/login"
const val API_V1_LOGOUT = "$API_V1/logout"

const val API_V1_USERS = "$API_V1/users"
const val API_V1_USERS_REGISTER = "$API_V1_USERS/register"
const val API_V1_USERS_GET_BY_ID = "$API_V1_USERS/{id}"
const val API_V1_USERS_ME = "$API_V1_USERS/me"

const val API_V1_WALLETS = "$API_V1/wallets"
const val API_V1_WALLETS_CREATE = "$API_V1_WALLETS/"
const val API_V1_WALLETS_GET_BY_ID = "$API_V1_WALLETS/{id}"
const val API_V1_WALLETS_DEPOSIT = "$API_V1_WALLETS/{id}/deposit"
const val API_V1_WALLETS_WITHDRAWAL = "$API_V1_WALLETS/{id}/withdrawal"

const val API_V1_ORDERS = "$API_V1/orders"
const val API_V1_ORDERS_PLACE = "$API_V1_ORDERS/"
const val API_V1_ORDERS_GET = "$API_V1_ORDERS/{id}"
const val API_V1_ORDERS_CANCEL = "$API_V1_ORDERS/{id}/cancel"

const val API_V1_TRANSACTIONS = "$API_V1/transactions"

const val COOKIE_AUTH = "X-Auth"
