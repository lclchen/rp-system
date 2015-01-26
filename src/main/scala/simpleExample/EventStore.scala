package simpleExample

import java.util.HashMap
import scala.collection.mutable.ListBuffer

class EventStore(database:MongoPersistence){
	def saveEvents(aggregateid:GUID, events:Traversable[Event], expectedVersion: Int):Unit={
	  	var version:Int = expectedVersion
		events.foreach(ev => {version += 1; database.saveEvent(aggregateid,ev,version)})
	}
	
  	def getEventsForAggregate(aggregateid:GUID): List[Event]={
		var list:List[Event] = Nil;
		list :::= database.getEventsByID(aggregateid, "transactionid")
		list
	}
}