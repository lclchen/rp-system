package simpleExample

case class TransferStartCommand(commandid:GUID, date:java.util.Date, account:GUID, passiveaccount:GUID, amount:Int) extends Command
case class TransferCommitCommand(commandid:GUID, date:java.util.Date, account:GUID, passiveaccount:GUID, amount:Int) extends Command
case class TransferCancelCommand(commandid:GUID, date:java.util.Date, account:GUID, passiveaccount:GUID, amount:Int) extends Command
case class TransferFailCommand(commandid:GUID, date:java.util.Date, account:GUID, passiveaccount:GUID, amount:Int) extends Command

case class WithdrawStartCommand(commandid:GUID, date:java.util.Date, account:GUID, amount:Int) extends Command
case class WithdrawCommitCommand(commandid:GUID, date:java.util.Date, account:GUID, amount:Int) extends Command
case class WithdrawCancelCommand(commandid:GUID, date:java.util.Date, account:GUID, amount:Int) extends Command
case class WithdrawFailCommand(commandid:GUID, date:java.util.Date, account:GUID, amount:Int) extends Command

case class DepositStartCommand(commandid:GUID, date:java.util.Date, account:GUID, amount:Int) extends Command
case class DepositCommitCommand(commandid:GUID, date:java.util.Date, account:GUID, amount:Int) extends Command
case class DepositCancelCommand(commandid:GUID, date:java.util.Date, account:GUID, amount:Int) extends Command
case class DepositFailCommand(commandid:GUID, date:java.util.Date, account:GUID, amount:Int) extends Command

case class RegisterAccountCommand(commandid:GUID, date:java.util.Date, account:GUID, username:String) extends Command
case class DeleteAccountCommand(commandid:GUID, date:java.util.Date, account:GUID) extends Command