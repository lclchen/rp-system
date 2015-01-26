package simpleExample

import CQRS.CommandHandler

trait CommandBus{  
}

/* ===========================================================
 *             Command Hander for Transaction Aggregate
 * ===========================================================
 */
class TransCommandHandler(repository:TransRepository) extends CommandHandler{
	var eventsstorage = List[Event]()
	def getEvents = eventsstorage
	def markEventsCommit():Unit = {eventsstorage = Nil}
  
	def receive: PartialFunction[Command, Unit]={
	  case c:TransferStartCommand => handle(c)
	  case c:TransferCommitCommand => handle(c)
	  case c:TransferCancelCommand => handle(c)
	  case c:TransferFailCommand => handle(c)
	  case c:WithdrawStartCommand => handle(c)
	  case c:WithdrawCommitCommand => handle(c)
	  case c:WithdrawCancelCommand => handle(c)
	  case c:WithdrawFailCommand => handle(c)
	  case c:DepositStartCommand => handle(c)
	  case c:DepositCommitCommand => handle(c)
	  case c:DepositCancelCommand => handle(c)
	  case c:DepositFailCommand => handle(c)
	  case c:RegisterAccountCommand => handle(c)
	  case c:DeleteAccountCommand => handle(c)
	  case _ => logger.warn("wrong type command")
	}
	
	def handle(c:TransferStartCommand):Unit={
		val trans = new TransactionAgg(c.commandid, "transfer", c.account)
		trans.setOtheraccount(c.passiveaccount)
		trans.setAmount(c.amount)
		repository.store(trans)
		trans.TransferStart()
		trans.getUncommittedChanges.foreach(ev => eventsstorage ::= ev)
		repository.save(trans,trans.getRevision)
	}
	
	def handle(c:TransferCommitCommand):Unit={
	  repository.getById(c.commandid) match{
	    case Some(trans)  => trans.TransferCommit();
	    	trans.getUncommittedChanges.foreach(ev => eventsstorage ::= ev);
	    	repository.remove(trans);
	    case _ => logger.warn("transaction not found")
	  }
	}
	
	def handle(c:TransferCancelCommand):Unit={
	  repository.getById(c.commandid) match{
	    case Some(trans)  => trans.TransferCancel();
	    	trans.getUncommittedChanges.foreach(ev => eventsstorage ::= ev);
	    	repository.remove(trans);
	    case _ => logger.warn("transaction not found")
	  }
	}
	
	def handle(c:TransferFailCommand):Unit={
	  repository.getById(c.commandid) match{
	    case Some(trans)  => trans.TransferFail();
	    	trans.getUncommittedChanges.foreach(ev => eventsstorage ::= ev);
	    	repository.remove(trans);
	    case _ => logger.warn("transaction not found")
	  }
	}
	
	//***
	def handle(c:WithdrawStartCommand):Unit={
		val trans = new TransactionAgg(c.commandid, "withdraw", c.account)
		trans.setAmount(c.amount)
		repository.store(trans)
		trans.WithdrawStart()
		trans.getUncommittedChanges.foreach(ev => eventsstorage ::= ev)
		repository.save(trans,trans.getRevision)
	}
	
	def handle(c:WithdrawCommitCommand):Unit={
	  repository.getById(c.commandid) match{
	    case Some(trans)  => trans.WithdrawCommit();
	    	trans.getUncommittedChanges.foreach(ev => eventsstorage ::= ev);
	    	repository.remove(trans);
	    case _ => logger.warn("transaction not found")
	  }
	}
  
	def handle(c:WithdrawCancelCommand):Unit={
	  repository.getById(c.commandid) match{
	    case Some(trans)  => trans.WithdrawCancel();
	    	trans.getUncommittedChanges.foreach(ev => eventsstorage ::= ev);
	    	repository.remove(trans);
	    case _ => logger.warn("transaction not found")
	  }
	}
	
	def handle(c:WithdrawFailCommand):Unit={
	  repository.getById(c.commandid) match{
	    case Some(trans)  => trans.WithdrawFail();
	    	trans.getUncommittedChanges.foreach(ev => eventsstorage ::= ev);
	    	repository.remove(trans);
	    case _ => logger.warn("transaction not found")
	  }
	}
	
	//**
	def handle(c:DepositStartCommand):Unit={
		val trans = new TransactionAgg(c.commandid, "deposit", c.account)
		trans.setAmount(c.amount)
		repository.store(trans)
		trans.DepositStart()
		trans.getUncommittedChanges.foreach(ev => eventsstorage ::= ev)
		repository.save(trans,trans.getRevision)
	}
	
	def handle(c:DepositCommitCommand):Unit={
	  repository.getById(c.commandid) match{
	    case Some(trans)  => trans.DepositCommit();
	    	trans.getUncommittedChanges.foreach(ev => eventsstorage ::= ev);
	    	repository.remove(trans);
	    case _ => logger.warn("transaction not found")
	  }
	}
	
	def handle(c:DepositCancelCommand):Unit={
	  repository.getById(c.commandid) match{
	    case Some(trans)  => trans.DepositCancel();
	    	trans.getUncommittedChanges.foreach(ev => eventsstorage ::= ev);
	    	repository.remove(trans);
	    case _ => logger.warn("transaction not found")
	  }
	}
	
	def handle(c:DepositFailCommand):Unit={
	  repository.getById(c.commandid) match{
	    case Some(trans)  => trans.DepositFail();
	    	trans.getUncommittedChanges.foreach(ev => eventsstorage ::= ev);
	    	repository.remove(trans);
	    case _ => logger.warn("transaction not found")
	  }
	}
	
	//**
	def handle(c:RegisterAccountCommand):Unit={
		val trans = new TransactionAgg(c.commandid, "registerAccount", c.account)
		trans.setUsername(c.username)
		repository.store(trans)
		trans.RegisterAccount()
		trans.getUncommittedChanges.foreach(ev => eventsstorage ::= ev)
		repository.remove(trans)
	}
	
	def handle(c:DeleteAccountCommand):Unit={
		val trans = new TransactionAgg(c.commandid, "deleteAccount", c.account)
		repository.store(trans)
		trans.DeleteAccount()
		trans.getUncommittedChanges.foreach(ev => eventsstorage ::= ev)
		repository.remove(trans)	  
	}
}

/* ===========================================================
 *             Command Hander for Account Aggregate
 * ===========================================================
 */

class AccCommandHandler(repository:AccRepository) extends CommandHandler{
	var eventsstorage = List[Event]()
	def getEvents = eventsstorage
	def markEventsCommit():Unit={eventsstorage = Nil}
  
	def receive: PartialFunction[Command, Unit]={
	  case c:TransferCommitCommand => handle(c)
	  case c:WithdrawCommitCommand => handle(c)
	  case c:DepositCommitCommand => handle(c)
	  case c:RegisterAccountCommand => handle(c)
	  case c:DeleteAccountCommand => handle(c)
	  case _ => logger.warn("wrong type command")
	}
	
	def handle(c:TransferCommitCommand):Unit={
		repository.getById(c.account) match{
		  case Some(accactive) =>{
			  repository.getById(c.passiveaccount) match{
			  	case Some(accpassive) =>{
			  		if((!accactive.activated)|(!accpassive.activated)){
			  			logger.warn("The account isn't activated"+accactive.id+accpassive.id)
			  			return
			  		}
			  		accactive.TransferMoney(c.commandid, c.amount, c.passiveaccount)
			  		accpassive.GetTransferMoney(c.commandid, c.amount, c.account)
			  		accactive.getUncommittedChanges.foreach(ev => eventsstorage ::= ev)
			  		accpassive.getUncommittedChanges.foreach(ev => eventsstorage ::= ev)
			  		repository.save(accactive, accactive.getRevision)
			  		repository.save(accpassive, accpassive.getRevision)
			  		}
			  	case _ => logger.warn("can't find this account")
			  }
		  }
		  case _ => logger.warn("can't find this account")
		}
	}
	
	def handle(c:WithdrawCommitCommand):Unit={
		repository.getById(c.account) match{
		  case Some(acc) =>{
		    if(!acc.activated){
		    	logger.warn("The account isn't activated"+acc.id)
			  	return
			}
		    acc.WithdrawMoney(c.commandid, c.amount)
		    acc.getUncommittedChanges.foreach(ev => eventsstorage ::= ev)
		    repository.save(acc, acc.getRevision)
		  }
		  case _ => logger.warn("can't find this account")
		}	
	}
	
	def handle(c:DepositCommitCommand):Unit={
		repository.getById(c.account) match{
		  case Some(acc) =>{
		    if(!acc.activated){
		    	logger.warn("The account isn't activated"+acc.id)
			  	return
			}
		    acc.DepositMoney(c.commandid, c.amount)
		    acc.getUncommittedChanges.foreach(ev => eventsstorage ::= ev)
		    repository.save(acc, acc.getRevision)
		  }
		  case _ => logger.warn("can't find this account")
		}
	}
	
	def handle(c:RegisterAccountCommand):Unit={
		val acc:AccountAgg = new AccountAgg(c.account, c.username)
		acc.CreateAccount(c.commandid, c.username)
		acc.getUncommittedChanges.foreach(ev => eventsstorage ::= ev)
		repository.add(acc, acc.getRevision)
	}
	
	def handle(c:DeleteAccountCommand):Unit={
		repository.getById(c.account) match{
		  case Some(acc) =>{
		    if(!acc.activated){
		    	logger.warn("The account isn't activated"+acc.id)
			  	return
			}
		    acc.DeleteAccount(c.commandid)
		    acc.getUncommittedChanges.foreach(ev => eventsstorage ::= ev)
		    repository.save(acc, acc.getRevision)
		  }
		  case _ => logger.warn("can't find this account")
		}
	}
}