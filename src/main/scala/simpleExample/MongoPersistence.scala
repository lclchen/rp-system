package simpleExample

import com.mongodb.casbah.Imports._
import com.mongodb.WriteConcern
import java.util.Date
import scala.collection.mutable.ListBuffer
import com.novus.salat._
import CQRS.DomainEvent

case class TransactionStream(transactionid:Int, user:Int, events:ListBuffer[Event])

class MongoPersistence(mongo:MongoDB) {
	val EventCollection = mongo("events")
	val AccountSnapshotCollection = mongo("accounts")
	
	def ensureindex():Unit={
	    EventCollection.ensureIndex("transactionid")
	    AccountSnapshotCollection.ensureIndex("accountid")
	}
	
	def saveEvent(aggregateid:GUID, ev:Event, expectedVersion:Int):Unit={	
		val mongoobject = grater[CQRS.DomainEvent].asDBObject(ev)
		mongoobject.put("transactionid", mongoobject.get("transactionid").toString)
		mongoobject.put("account", mongoobject.get("account").toString)
		if (mongoobject.containsKey("otheraccount"))
		  mongoobject.put("otheraccount", mongoobject.get("otheraccount").toString)
		if (mongoobject.containsKey("accountpassive"))
		  mongoobject.put("accountpassive", mongoobject.get("accountpassive").toString)
		mongoobject.put("version", expectedVersion)
		EventCollection.insert(mongoobject, WriteConcern.SAFE)
	}
	
	def getEventsByID(aggregateid:GUID, aggname:String):List[Event]={
		val mongoobject = MongoDBObject(aggname -> aggregateid)
		val result = EventCollection.find(mongoobject)
		???
		Nil
	}
	
	def AddStreamtoMongo(stream:TransactionStream){
	    val mongoobject = grater[TransactionStream].asDBObject(stream)	
	    EventCollection.insert(mongoobject, WriteConcern.SAFE)
	}
	
	def addSnapShot(account:AccountAgg):Unit={
		val mongoobj = MongoDBObject("accountid" -> account.id.toString, "username" -> account.username, "balance"->account.balance, "revision"->account.getRevision(), "activated"->account.activated)
		AccountSnapshotCollection.insert(mongoobj, WriteConcern.SAFE)
	}
	
	def saveSnapShot(account:AccountAgg):Unit={
		val query = MongoDBObject("accountid" -> account.id.toString)
		val mongoobject = MongoDBObject("$set"->MongoDBObject("balance"->account.balance,"revision"->account.getRevision,"activated"->account.activated))
		AccountSnapshotCollection.update(query, mongoobject)
	}
	
	def getSnapshot(accountid:GUID):Option[AccountAgg]={
	    var agg:AccountAgg = null
		val queryDBObject = MongoDBObject("accountid" -> accountid.toString)
	    val result = AccountSnapshotCollection.findOne(queryDBObject)
	    result match{
	      case Some(e) => 	agg = new AccountAgg(getGUIDfromDB(e,"accountid"), getStringfromDB(e,"username"),getIntfromDB(e,"balance"))
	      					agg.setRevision(getIntfromDB(e,"revision"))
	      					agg.activated = getBooleanfromDB(e,"activated")
	      					return Some(agg)
	      case None => 		logger.warn("no found account: "+accountid)
	      					return None
	    }
	}
	
	
	//
	def getIntfromDB(dbobject:DBObject, key:String):Int={
		java.lang.Double.parseDouble(dbobject.get(key).toString).toInt
	}
	
	def getStringfromDB(dbobject:DBObject, key:String):String={
		dbobject.get(key).toString
	}
	
	def getBooleanfromDB(dbobject:DBObject, key:String):Boolean={
		java.lang.Boolean.parseBoolean(dbobject.get(key).toString)
	}
	
	def getGUIDfromDB(dbobject:DBObject, key:String):GUID={
		java.util.UUID.fromString(dbobject.get(key).toString)
	}
}