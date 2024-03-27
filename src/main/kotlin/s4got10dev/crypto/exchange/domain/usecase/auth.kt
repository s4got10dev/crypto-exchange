package s4got10dev.crypto.exchange.domain.usecase

data class PerformLoginCommand(
  val username: String,
  val password: String
) : Command
