package s4got10dev.crypto.exchange.infrastructure.persistence.mapper

import s4got10dev.crypto.exchange.domain.entity.User
import s4got10dev.crypto.exchange.infrastructure.persistence.table.UserTable

fun User.toUserTable(): UserTable = UserTable(
  id = id,
  username = username,
  password = password,
  email = email,
  firstName = firstName,
  lastName = lastName
)

fun UserTable.toUser(): User = User(
  id = id,
  username = username,
  password = password,
  email = email,
  firstName = firstName,
  lastName = lastName
)
