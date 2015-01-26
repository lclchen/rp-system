package simpleExample

import java.util.Date

//event-transation aggregate
case class TransferStartTransEvent(transactionid:GUID, state:String, committedtime:Date, account:GUID, amount:Int, accountpassive:GUID) extends Event
case class TransferCommitTransEvent(transactionid:GUID, state:String, committedtime:Date, account:GUID, amount:Int, accountpassive:GUID) extends Event
case class TransferCancelTransEvent(transactionid:GUID, state:String, committedtime:Date, account:GUID, amount:Int, accountpassive:GUID) extends Event
case class TransferFailTransEvent(transactionid:GUID, state:String, committedtime:Date, account:GUID, amount:Int, accountpassive:GUID) extends Event
case class WithdrawStartTransEvent(transactionid:GUID, state:String, committedtime:Date, account:GUID, amount:Int) extends Event
case class WithdrawCommitTransEvent(transactionid:GUID, state:String, committedtime:Date, account:GUID, amount:Int) extends Event
case class WithdrawCancelTransEvent(transactionid:GUID, state:String, committedtime:Date, account:GUID, amount:Int) extends Event
case class WithdrawFailTransEvent(transactionid:GUID, state:String, committedtime:Date, account:GUID, amount:Int) extends Event
case class DepositStartTransEvent(transactionid:GUID, state:String, committedtime:Date, account:GUID, amount:Int) extends Event
case class DepositCommitTransEvent(transactionid:GUID, state:String, committedtime:Date, account:GUID, amount:Int) extends Event
case class DepositCancelTransEvent(transactionid:GUID, state:String, committedtime:Date, account:GUID, amount:Int) extends Event
case class DepositFailTransEvent(transactionid:GUID, state:String, committedtime:Date, account:GUID, amount:Int) extends Event
case class RegisterAccountTransEvent(transactionid:GUID, committedtime:Date, account:GUID, username:String) extends Event
case class DeleteAccountTransEvent(transactionid:GUID, committedtime:Date, account:GUID) extends Event


//event-account aggregate
case class TransferActiveAccEvent(transactionid:GUID, committedtime:Date, account:GUID, amount:Int, otheraccount:GUID) extends Event
case class TransferPassiveAccEvent(transactionid:GUID, committedtime:Date, account:GUID, amount:Int, otheraccount:GUID) extends Event
case class WithdrawAccEvent(transactionid:GUID, committedtime:Date, account:GUID, amount:Int) extends Event
case class DepositAccEvent(transactionid:GUID, committedtime:Date, account:GUID, amount:Int) extends Event
case class RegisterAccountAccEvent(transactionid:GUID, committedtime:Date, account:GUID, username:String) extends Event
case class DeleteAccountAccEvent(transactionid:GUID, committedtime:Date, account:GUID) extends Event
case class NegativeBalanceAccEvent(transactionid:GUID, eventtype:String, committedtime:Date, account:GUID) extends Event

  //event-type in ErrorReportingAccEvent (defined as follow)
	//transfer Active = 1
	//transfer Passive = 2
	//withdraw  = 3
	//deposit = 4
	////NegativeBalance