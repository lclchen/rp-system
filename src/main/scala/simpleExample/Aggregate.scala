package simpleExample

import scala.collection.mutable.HashMap
import CQRS.AggregateRoot
import CQRS.IRepository
import CQRS.DomainEvent
import java.util.Date
import java.util.UUID


/* ============================================
 *              Account Aggregate           
 * =============================================
 */
class AccountAgg(val id:GUID, var username:String = "", var balance:Int = 0) extends CQRS.AggregateRoot{
	private var revision:Int = 0
	var activated: Boolean = true
    
	private var changes = List[Event]()
	private def uncommittedChanges = changes
	override def getUncommittedChanges = uncommittedChanges.toIterable
    	
	//***
	
    def TransferMoney(transactionid:GUID, amount:Int, toWhom:GUID):Unit={
        if(amount<0)
            throw new Exception("tranferring money below 0")
        applyChange(TransferActiveAccEvent(transactionid, new Date(), id, amount, toWhom))
    }
    
    def GetTransferMoney(transactionid:GUID, amount:Int, fromWhom:GUID):Unit={
    	if(amount<0)
            throw new Exception("tranferring money below 0")
        applyChange(TransferPassiveAccEvent(transactionid, new Date(), id, amount, fromWhom))
    }
    
    def WithdrawMoney(transactionid:GUID, amount:Int):Unit={
        if(amount<0)
            throw new Exception("withdrawal money below 0")
        applyChange(WithdrawAccEvent(transactionid, new Date(), id, amount))

    }
    
    def DepositMoney(transactionid:GUID, amount:Int):Unit={
        if(amount<0)
            throw new Exception("deposit money below 0")
        applyChange(DepositAccEvent(transactionid, new Date(), id, amount))
    }
    
    def CreateAccount(transactionid:GUID, username:String):Unit={
    	applyChange(RegisterAccountAccEvent(transactionid, new Date(), id, username))
    }
    
    def DeleteAccount(transactionid:GUID):Unit={
    	applyChange(DeleteAccountAccEvent(transactionid, new Date(), id))
    }
    
    //***
    def MarkChangesAsCommitted():Unit={
    	changes = Nil
    }
    
    override def applyChange(e: DomainEvent, isNew: Boolean = true) = {
    	if (handle.isDefinedAt(e)) {
    		handle(e)
    	}
    	if (isNew) changes = changes :+ e
    }
    
    def handle: PartialFunction[Event, Unit]={
      case e:TransferActiveAccEvent => handle(e)
      case e:TransferPassiveAccEvent => handle(e)
      case e:WithdrawAccEvent => handle(e)
      case e:DepositAccEvent => handle(e)
      case e:RegisterAccountAccEvent => handle(e)
      case e:DeleteAccountAccEvent => handle(e)
      case _ => log.warn("warn event in account aggregate");
	}
    
    def handle(e:TransferActiveAccEvent):Unit={
    	balance -= e.amount
    	//revision += 1 change when event is committed
    	if(balance < 0)
    		changes ::= NegativeBalanceAccEvent(e.transactionid, "transfer Active", new Date(), id)
    }
    
    def handle(e:TransferPassiveAccEvent):Unit={
    	balance += e.amount
    	if(balance < 0)
    		changes ::= NegativeBalanceAccEvent(e.transactionid, "transfer Passtive", new Date(), id)
    }
    
    def handle(e:WithdrawAccEvent):Unit={
    	balance -= e.amount
    	if(balance < 0)
    		changes ::= NegativeBalanceAccEvent(e.transactionid, "withdraw", new Date(), id)
    }
    
    def handle(e:DepositAccEvent):Unit={
    	balance += e.amount
    	if(balance < 0)
    		changes ::= NegativeBalanceAccEvent(e.transactionid, "deposit", new Date(), id)
    }
    
    def handle(e:RegisterAccountAccEvent):Unit={
    	activated = true
    	username = e.username
    }
    
    def handle(e:DeleteAccountAccEvent):Unit={
    	activated = false
    }
	//***
	
	def setRevision(newrevision:Int):Unit={
	    revision = newrevision
	}
	
	override def getRevision():Int={
		revision
	}
}


/* ==============================================================
 *               Transaction Aggregate
 * ==============================================================
 */

class TransactionAgg(val id:GUID, val transactiontype:String, val account:GUID) extends CQRS.AggregateRoot{
	var otheraccount:GUID = null
	var state:String = ""
	var username:String =""
	var amount:Int = 0
	
	private var changes = List[Event]()
	private def uncommittedChanges = changes
	override def getUncommittedChanges = uncommittedChanges.toIterable
	var activated: Boolean = true
	private var revision:Int = 0
	
	def setOtheraccount(other:GUID){otheraccount = other}
	def setState(newstate:String){state = newstate}
	def setUsername(name:String){username = name}
	def setAmount(newamount:Int){amount = newamount}
	
	//*****
	def TransferStart():Unit = {
		state = "start"
		if(amount < 0)
		  throw new Exception("transfer money below 0")
		applyChange(TransferStartTransEvent(id, state, new Date(), account, amount, otheraccount))
	}
	
	def TransferCommit():Unit = {	
		if(state != "start")
		  log.warn("maybe the transfer transaction hasn't started")
		state = "commit"
		applyChange(TransferCommitTransEvent(id, state, new Date(), account, amount, otheraccount))
	}
	
	def TransferCancel():Unit = {	
		if(state != "start")
		  log.warn("maybe the transfer transaction hasn't started")
		state = "cancel"
		applyChange(TransferCancelTransEvent(id, state, new Date(), account, amount, otheraccount))
	}
	
	def TransferFail():Unit = {	
		if(state != "start")
		  log.warn("maybe the transfer transaction hasn't started")
		state = "fail"
		applyChange(TransferFailTransEvent(id, state, new Date(), account, amount, otheraccount))
	}
	//***
	def WithdrawStart():Unit = {
		state = "start"
		if(amount < 0)
		  throw new Exception("withdraw money below 0")
		applyChange(WithdrawStartTransEvent(id, state, new Date(), account, amount))
	}
	
	def WithdrawCommit():Unit = {
		if(state != "start")
		  log.warn("maybe the withdraw transaction hasn't started")
		state = "commit"
		applyChange(WithdrawCommitTransEvent(id, state, new Date(), account, amount))
	}
	
	def WithdrawCancel():Unit = {
		if(state != "start")
		  log.warn("maybe the withdraw transaction hasn't started")
		state = "cancel"
		applyChange(WithdrawCancelTransEvent(id, state, new Date(), account, amount))
	}
	
	def WithdrawFail():Unit = {
		if(state != "start")
		  log.warn("maybe the withdraw transaction hasn't started")
		state = "fail"
		applyChange(WithdrawFailTransEvent(id, state, new Date(), account, amount))
	}
	//***
	def DepositStart():Unit = {
		state = "start"
		if(amount < 0)
		  throw new Exception("deposit money below 0")
		applyChange(DepositStartTransEvent(id, state, new Date(), account, amount))
	}
	
	def DepositCommit():Unit = {
		if(state != "start")
		  log.warn("maybe the deposit transaction hasn't started")
		state = "commit"
		applyChange(DepositCommitTransEvent(id, state, new Date(), account, amount))
	}
	
	def DepositCancel():Unit = {
		if(state != "start")
		  log.warn("maybe the deposit transaction hasn't started")
		state = "cancel"
		applyChange(DepositCancelTransEvent(id, state, new Date(), account, amount))
	}
	
	def DepositFail():Unit = {
		if(state != "start")
		  log.warn("maybe the deposit transaction hasn't started")
		state = "fail"
		applyChange(DepositFailTransEvent(id, state, new Date(), account, amount))
	}
	//***
	def RegisterAccount():Unit={
		state = "commit"
		applyChange(RegisterAccountTransEvent(id, new Date(), account, username))
	}
	
	def DeleteAccount():Unit={
		state = "commit"
		applyChange(DeleteAccountTransEvent(id, new Date(), account))
	}
	//*****

	override def applyChange(e: DomainEvent, isNew: Boolean = true) = {
		if (handle.isDefinedAt(e)) {
			handle(e)
		}
		if (isNew) changes = changes :+ e
	}
	
	def handle: PartialFunction[Event, Unit]={
//	  case e:TransferStartTransEvent => handle(e)
//	  case e:TransferCommitTransEvent => handle(e)
//	  case e:TransferCancelTransEvent => handle(e)
//	  case e:TransferFailTransEvent => handle(e)
//	  case e:WithdrawStartTransEvent => handle(e)
//	  case e:WithdrawCommitTransEvent => handle(e)
//	  case e:WithdrawCancelTransEvent => handle(e)
//	  case e:WithdrawFailTransEvent => handle(e)
//	  case e:DepositStartTransEvent => handle(e)
//	  case e:DepositCommitTransEvent => handle(e)
//	  case e:DepositCancelTransEvent => handle(e)
//	  case e:DepositFailTransEvent => handle(e)
//	  case e:RegisterAccountTransEvent => handle(e)
//	  case e:DeleteAccountTransEvent => handle(e)
	  case e:Event => ;
	  case _ => log.warn("Wrong type of event in Transaction Aggregate")
	}
	
	def MarkChangesAsCommitted():Unit = {changes = Nil}
	def setRevision(newrevision:Int):Unit = {revision = newrevision}
	override def getRevision():Int = {revision}
}

