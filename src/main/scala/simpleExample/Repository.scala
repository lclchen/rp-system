package simpleExample

import scala.collection.mutable.HashMap
import scala.reflect._
import CQRS.AggregateRoot
import java.util.Date
import java.util.UUID

trait IRepository[T <: AggregateRoot] {
  def save(aggregate: T, expectedVersion: Int):Unit
  def getById(id:GUID): Option[T]
}

/* =====================================================
 *                Account Repository
 * =====================================================
 */
class AccRepository(accstorage:AccStore, eventstorage:EventStore, mem:InMemoryPersistency[AccountAgg]) extends IRepository[AccountAgg]{
	def add(accountagg:AccountAgg, expectedVersion: Int):Unit={
		eventstorage.saveEvents(accountagg.id, accountagg.getUncommittedChanges, expectedVersion)
		accountagg.setRevision(accountagg.getUncommittedChanges.size+accountagg.getRevision)
	    accountagg.MarkChangesAsCommitted//event
		accstorage.addAccountAgg(accountagg)//account
	}
  
	def save(accountagg:AccountAgg, expectedVersion: Int):Unit={
	    eventstorage.saveEvents(accountagg.id, accountagg.getUncommittedChanges, expectedVersion)
	    accountagg.setRevision(accountagg.getUncommittedChanges.size+accountagg.getRevision)
	    accountagg.MarkChangesAsCommitted//event
		accstorage.saveAccountAgg(accountagg)//account
	}
	
	def getById(accid:GUID):Option[AccountAgg]={
	    accstorage.getAccountAgg(accid)
	}
}

/* =====================================================
 *               Transaction Repository
 * =====================================================
 */
class TransRepository(private val storage:EventStore, mem:InMemoryPersistency[TransactionAgg])extends IRepository[TransactionAgg]{	
	def store(trans:TransactionAgg):Unit={
	  	mem.save(trans.id, trans) 
	}
  
	def save(trans:TransactionAgg, expectedversion:Int):Unit={
	  	if(! mem.contain(trans.id))
	  		mem.save(trans.id, trans)
	  	storage.saveEvents(trans.id, trans.getUncommittedChanges, expectedversion)
	  	trans.setRevision(trans.getUncommittedChanges.size + trans.getRevision) //update aggregate's version
	  	trans.MarkChangesAsCommitted()
	}
	
	def remove(trans:TransactionAgg):Unit={
		save(trans,trans.getRevision)
		mem.remove(trans.id)
	}
	
	def getById(transid:GUID):Option[TransactionAgg]={
		mem.get(transid)
	}
}
